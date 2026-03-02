package com.example.crawler.service;

import com.example.crawler.model.ContactData;
import com.example.crawler.repository.ContactRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CrawlerService {

    private final ContactRepository contactRepository;
    private final ExecutorService executorService;
    private final Set<String> visitedLinks = ConcurrentHashMap.newKeySet();

    private final Timer parsingTimer;
    private final Counter processedPagesCounter;
    private final Tracer tracer; // Добавили Tracer

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d[\\d -]{8,12}\\d");

    public CrawlerService(ContactRepository contactRepository,
                          @Qualifier("crawlerExecutor") ExecutorService executorService,
                          MeterRegistry meterRegistry,
                          Tracer tracer) { // Внедрили Tracer
        this.contactRepository = contactRepository;
        this.executorService = executorService;
        this.parsingTimer = meterRegistry.timer("crawler.parsing.time", "type", "html");
        this.processedPagesCounter = meterRegistry.counter("crawler.pages.processed");
        this.tracer = tracer;
    }

    public void startCrawling(String startUrl) {
        log.info("Запуск краулера с: {}", startUrl);
        CompletableFuture.runAsync(() -> parsePage(startUrl, startUrl), executorService);
    }

    private void parsePage(String url, String baseDomain) {
        if (!visitedLinks.add(url)) return;

        // Создаем родительский Span для всей страницы
        Span pageSpan = tracer.nextSpan().name("process-page").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(pageSpan)) {
            pageSpan.tag("url", url);
            log.info("Начало обработки страницы: {}", url);

            parsingTimer.record(() -> {
                try {
                    Thread.sleep(200);

                    // Дочерний Span для замера времени HTTP запроса Jsoup
                    Span httpSpan = tracer.nextSpan().name("http-fetch").start();
                    Document doc;
                    try (Tracer.SpanInScope httpWs = tracer.withSpan(httpSpan)) {
                        doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .timeout(10000)
                                .get();
                    } finally {
                        httpSpan.end();
                    }

                    extractContactsFromText(doc.text(), url);
                    processedPagesCounter.increment();

                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.attr("abs:href");
                        if (nextUrl.startsWith(baseDomain) && !visitedLinks.contains(nextUrl)) {
                            executorService.submit(() -> parsePage(nextUrl, baseDomain));
                        }
                    }
                } catch (Exception e) {
                    log.error("Ошибка при обработке {}: {}", url, e.getMessage());
                    pageSpan.error(e);
                }
            });
        } finally {
            pageSpan.end();
        }
    }

    private void extractContactsFromText(String text, String url) {
        // Дочерний Span для замера времени регулярных выражений
        Span regexSpan = tracer.nextSpan().name("extract-contacts-regex").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(regexSpan)) {

            Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
            while (emailMatcher.find()) {
                saveContact("EMAIL", emailMatcher.group(), url);
            }

            if (text.contains("Контакты") || text.contains("Contact") || text.contains("Phone")) {
                Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
                while (phoneMatcher.find()) {
                    if (phoneMatcher.group().length() > 9) {
                        saveContact("PHONE", phoneMatcher.group(), url);
                    }
                }
            }
        } finally {
            regexSpan.end();
        }
    }

    @Transactional
    public void saveContact(String type, String value, String url) {
        if (!contactRepository.existsByValue(value)) {
            ContactData contact = ContactData.builder()
                    .type(type)
                    .value(value)
                    .sourceUrl(url)
                    .foundAt(LocalDateTime.now())
                    .build();
            contactRepository.save(contact);
            log.info("Найдено: {} -> {}", type, value);
        }
    }

    public List<ContactData> getAllContacts() {
        return contactRepository.findAll();
    }
}