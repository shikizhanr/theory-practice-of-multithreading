package src.main.java.com.example.crawler.sheduler;

import com.example.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTask {

    private final CrawlerService crawlerService;

    // Запускаем через 10 секунд после старта, а потом раз в час
    @Scheduled(initialDelay = 10000, fixedRate = 3600000)
    public void runScheduledCrawling() {
        log.info("Автоматический запуск краулера...");
        // В качестве примера берем сайт МГУ (там обычно много контактов на страницах)
        // Либо можно взять любой тестовый сайт.
        crawlerService.startCrawling("https://www.msu.ru/info/"); 
    }
}