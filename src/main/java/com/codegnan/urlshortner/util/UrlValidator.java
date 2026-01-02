package com.codegnan.urlshortner.util;

import java.net.URI;

public final class UrlValidator {

    private UrlValidator() {}

    public static boolean isValidHttpUrl(String input) {
        if (input == null) return false;

        String url = input.trim();
        if (url.isEmpty()) return false;

        // Quick length sanity
        if (url.length() > 2048) return false;

        try {
            URI uri = URI.create(url);

            String scheme = uri.getScheme();
            if (scheme == null) return false;

            // Only allow http/https
            if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) return false;

            // Must have host (blocks things like "https:/foo" or "http://")
            if (uri.getHost() == null || uri.getHost().isBlank()) return false;

            // Basic checks passed
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
