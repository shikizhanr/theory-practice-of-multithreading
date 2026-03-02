package com.example.crawler;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }

    // Принудительно заставляем Spring Boot отправлять трейсы по HTTP в Jaeger
    @Bean
    public OtlpHttpSpanExporter otlpHttpSpanExporter() {
        System.out.println("=== ОТПРАВКА ТРЕЙСОВ В JAEGER АКТИВИРОВАНА ===");
        return OtlpHttpSpanExporter.builder()
                .setEndpoint("http://localhost:4318/v1/traces")
                .build();
    }
}