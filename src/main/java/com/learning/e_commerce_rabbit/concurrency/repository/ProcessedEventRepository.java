package com.learning.e_commerce_rabbit.concurrency.repository;

import com.learning.e_commerce_rabbit.concurrency.event.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, String> {
}

