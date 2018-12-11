package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired AsyncService asyncService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        asyncService.asyncCall();
    }

    @Configuration
    @EnableAsync
    public static class AsyncConfig implements AsyncConfigurer {
        @Lazy // <- this is the missing part
        @Autowired
        ErrorHandler errorHandler;

        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            return (throwable, method, objects) -> errorHandler.writeError(throwable);
        }
    }

    @Service
    public static class ErrorHandler {
        private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);

        @Autowired DbService dbService;

        public void writeError(Throwable throwable) {
            dbService.writeError(throwable.getMessage());
            LOG.error("Error handler received error: {}", throwable.getMessage());
        }
    }

    @Component
    public static class AsyncService {

        @Autowired DbService dbService;

        @Async
        public void asyncCall() {
            dbService.accessDb(); // save result in db
            throw new RuntimeException("exception thrown");
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

        @Transactional
        public void writeError(String message) {
            TransactionAspectSupport.currentTransactionStatus(); // is there a transaction ??
            int updated = jdbcTemplate.update("insert into test_table(test_column) values (?)", message);
            LOG.info("error changed: {}", updated);
        }
    }

}
