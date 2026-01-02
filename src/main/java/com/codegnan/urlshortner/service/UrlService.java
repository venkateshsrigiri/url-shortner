package com.codegnan.urlshortner.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import com.codegnan.urlshortner.exception.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegnan.urlshortner.dto.ShortenRequest;
import com.codegnan.urlshortner.dto.ShortenResponse;
import com.codegnan.urlshortner.exception.BadRequestException;

import com.codegnan.urlshortner.exception.UrlExpiredException;
import com.codegnan.urlshortner.model.UrlMapping;
import com.codegnan.urlshortner.repository.UrlRepository;
import com.codegnan.urlshortner.util.UrlValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository repository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.default-expiry-minutes:10080}")
    private long defaultExpiryMinutes;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        String originalUrl = request == null ? null : request.getUrl();

        if (!UrlValidator.isValidHttpUrl(originalUrl)) {
            throw new BadRequestException("Invalid URL. Only http/https URLs are allowed.");
        }

        long expiryMinutes = request.getExpiryMinutes() == null
                ? defaultExpiryMinutes
                : request.getExpiryMinutes();

        if (expiryMinutes <= 0) {
            throw new BadRequestException("expiryMinutes must be a positive number.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt = now.plusMinutes(expiryMinutes);

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl.trim())
                .createdAt(now)
                .expiryAt(expiryAt)
                .clickCount(0)
                .shortCode("temp")
                .build();

        repository.save(mapping);

        String shortCode = encodeBase62(mapping.getId());
        mapping.setShortCode(shortCode);
        repository.save(mapping);

        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/u/" + shortCode)
                .shortCode(shortCode)
                .originalUrl(mapping.getOriginalUrl())
                .createdAt(mapping.getCreatedAt())
                .expiryAt(mapping.getExpiryAt())
                .build();
    }

    @Transactional(readOnly = true)
    public UrlMapping getValidMappingOrThrow(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("Short URL not found"));

        if (mapping.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("This short URL has expired");
        }

        return mapping;
    }

    @Transactional
    public void incrementClicks(Long id) {
        repository.incrementClickCount(id);
    }

    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String encodeBase62(long value) {
        if (value <= 0) return "a";

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}
