package com.novelassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NovelAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelAssistantApplication.class, args);
    }
} 