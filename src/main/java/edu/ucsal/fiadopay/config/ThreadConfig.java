package edu.ucsal.fiadopay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class ThreadConfig {

    @Bean(name = "webhookExecutor")
    public ExecutorService webhookExecutor() {

        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("fiadopay-webhook-worker");
            thread.setDaemon(true);
            return thread;
        };

        return Executors.newFixedThreadPool(4, factory);
    }
}

