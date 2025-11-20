package edu.ucsal.fiadopay.service;

import edu.ucsal.fiadopay.exception.TooManyAttemptsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VelocityRateLimiter {

    private final Map<String, Deque<Instant>> attemptsPerKey = new ConcurrentHashMap<>();

    public void check(String key, int maxAttempts, int windowSeconds) {
        Instant now = Instant.now();

        Deque<Instant> deque = attemptsPerKey
                .computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (deque) {
            Instant limit = now.minusSeconds(windowSeconds);

            // remove tentativas fora da janela
            while (!deque.isEmpty() && deque.peekFirst().isBefore(limit)) {
                deque.pollFirst();
            }

            if (deque.size() >= maxAttempts) {
                throw new TooManyAttemptsException(
                        "Muitas tentativas para a chave '" + key +
                                "' (máx: " + maxAttempts + " em " + windowSeconds + "s)"
                );
            }

            // registra tentativa atual
            deque.addLast(now);
        }
    }
}
