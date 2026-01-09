package com.example.crawler.service;

import com.example.crawler.model.ContactData;
import com.example.crawler.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final ContactRepository contactRepository;

    @Qualifier("crawlerExecutor")
    private final ExecutorService executorService;

    private final Set<String> visitedLinks = ConcurrentHashMap.newKeySet();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d[\\d -]{8,12}\\d");

    public void startCrawling(String startUrl) {
        log.info("Запуск краулера с: {}", startUrl);
        CompletableFuture.runAsync(() -> parsePage(startUrl, startUrl), executorService);
    }

    private void parsePage(String url, String baseDomain) {
        if (visitedLinks.contains(url)) return;
        visitedLinks.add(url);

        try {
            Thread.sleep(200);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get();

            extractContactsFromText(doc.text(), url);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.attr("abs:href");

                if (nextUrl.startsWith(baseDomain) && !visitedLinks.contains(nextUrl)) {
                    executorService.submit(() -> parsePage(nextUrl, baseDomain));
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при обработке {}: {}", url, e.getMessage());
        }
    }

    private void extractContactsFromText(String text, String url) {
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
    }

    private void saveContact(String type, String value, String url) {
        synchronized (this) {
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
    }

    public List<ContactData> getAllContacts() {
        return contactRepository.findAll();
    }
}