package com.example.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadConfig {

    @Bean(name = "crawlerExecutor")
    public ExecutorService crawlerExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}