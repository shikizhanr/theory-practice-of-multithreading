package com.example.crawler.controller;

import com.example.crawler.model.ContactData;
import com.example.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    @GetMapping("/run")
    public String start(@RequestParam String url) {
        crawlerService.startCrawling(url); 
        return "Краулер запущен по адресу: " + url;
    }

    @GetMapping("/answer")
    public List<ContactData> getContacts(
            @RequestParam(required = false, defaultValue = "EMAIL") String typeFilter,
            @RequestParam(required = false, defaultValue = "false") boolean sortNewest
    ) {
        List<ContactData> all = crawlerService.getAllContacts();

        return all.parallelStream()
                .filter(c -> c.getType().equalsIgnoreCase(typeFilter))
                .sorted(sortNewest 
                        ? Comparator.comparing(ContactData::getFoundAt).reversed() 
                        : Comparator.comparing(ContactData::getFoundAt))
                .collect(Collectors.toList());
    }
}