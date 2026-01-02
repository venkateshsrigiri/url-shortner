package com.codegnan.urlshortner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {
    private String url;
    private Long expiryMinutes;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getExpiryMinutes() {
        return expiryMinutes;
    }

    public void setExpiryMinutes(Long expiryMinutes) {
        this.expiryMinutes = expiryMinutes;
    }
}
