package com.example.crawler.repository;

import com.example.crawler.model.ContactData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactData, Long> {
    boolean existsByValue(String value);
}