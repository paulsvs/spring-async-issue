package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired StateMachinePublisherService publisherService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        publisherService.publishEvent("test");
    }

    @Configuration
    @EnableAsync
    public static class AsyncConfig implements AsyncConfigurer {
        // the idea is to handle error with this bean -> save error in DB
        @Autowired ErrorHandler errorHandler; // fixme: causes transaction not to exist
    }

    @Service
    public static class ErrorHandler {
        @Autowired DbService dbService;
    }

    @Component
    public static class AsyncNotifier {

        @Autowired DbService dbService;

        @EventListener(CustomEvent.class)
        public void asyncCall() {
            // time consuming http call
            dbService.accessDb(); // save result in db
        }

    }

    @Repository
    public static class DbService {
        private static final Logger LOG = LoggerFactory.getLogger(DbService.class);

        @Autowired JdbcTemplate jdbcTemplate;

        @Transactional
        public void accessDb() {
            TransactionAspectSupport.currentTransactionStatus(); // is there a transaction ??
            int updated = jdbcTemplate.update("insert into test_table(test_column) values (10)");
            LOG.info("insert changed: {}", updated);
        }
    }

    @Component
    public static class StateMachinePublisherService implements ApplicationEventPublisherAware {

        private ApplicationEventPublisher publisher;

        @Override
        public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            this.publisher = applicationEventPublisher;
        }

        public void publishEvent(Object data) {
            publisher.publishEvent(new CustomEvent(this, data));
        }

    }

    public static class CustomEvent extends ApplicationEvent {

        public Object data;

        public CustomEvent(Object source, Object data) {
            super(source);
            this.data = data;
        }

    }

}
