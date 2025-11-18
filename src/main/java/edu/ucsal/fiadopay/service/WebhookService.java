package edu.ucsal.fiadopay.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final ExecutorService webhookExecutor;

    public void sendWebhookAsync(Runnable func) {
        try {
            webhookExecutor.submit(() -> {
                func.run();
            });
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }
}

