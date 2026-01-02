package com.codegnan.urlshortner.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.codegnan.urlshortner.dto.ShortenRequest;
import com.codegnan.urlshortner.dto.ShortenResponse;
import com.codegnan.urlshortner.model.UrlMapping;
import com.codegnan.urlshortner.service.UrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService service;

    @PostMapping("/api/shorten")
    public ShortenResponse shorten(@RequestBody ShortenRequest request) {
        return service.shorten(request);
    }

    @GetMapping("/u/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        UrlMapping mapping = service.getValidMappingOrThrow(code);
        service.incrementClicks(mapping.getId());

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(mapping.getOriginalUrl()))
                .build();
    }

    @GetMapping("/api/stats/{code}")
    public Map<String, Object> stats(@PathVariable String code) {
        UrlMapping m = service.getValidMappingOrThrow(code);
        return Map.of(
                "shortCode", m.getShortCode(),
                "originalUrl", m.getOriginalUrl(),
                "createdAt", m.getCreatedAt(),
                "expiryAt", m.getExpiryAt(),
                "clickCount", m.getClickCount()
        );
    }
}
