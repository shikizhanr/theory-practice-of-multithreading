package com.example.crawler.sсheduler;

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
    
    @Scheduled(initialDelay = 10000, fixedRate = 3600000)
    public void runScheduledCrawling() {
        log.info("Автоматический запуск краулера...");
        crawlerService.startCrawling("https://www.msu.ru/info/"); 
    }
}