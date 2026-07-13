package com.ansh.smart_commerce.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * GeminiService — the only class that holds and uses the Gemini API key.
 * Calls the Gemini REST API via RestTemplate.
 * The API key is NEVER returned in any response or logged in full.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private static final String DEFAULT_GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent";

    private final Environment environment;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiService(Environment environment) {
        this.environment = environment;
    }

    /**
     * Calls Gemini with a system instruction, conversation history, and the latest user message.
     *
     * @param systemPrompt  The system-level instruction (company context, policies, data).
     * @param history       Previous turns: list of {role, content} maps alternating user/model.
     * @param userMessage   The current user message.
     * @return              Gemini's text response, or a safe fallback on error.
     */
    @SuppressWarnings("unchecked")
    public String generateContent(String systemPrompt, List<Map<String, Object>> history, String userMessage) {

        String apiKey = resolveApiKey();
        String apiUrl = resolveApiUrl();

        if (apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Set gemini.api.key in application.properties " +
                     "or the GEMINI_API_KEY environment variable.");
            return "I'm sorry, the AI service is not configured yet. Please contact the administrator.";
        }

        try {
            List<Map<String, Object>> contents = new java.util.ArrayList<>(history);
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            ));

            Map<String, Object> generationConfig = Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 1500,
                    "topP", 0.9
            );

            Map<String, Object> systemInstruction = Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            );

            Map<String, Object> requestBody = Map.of(
                    "systemInstruction", systemInstruction,
                    "contents", contents,
                    "generationConfig", generationConfig
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Prefer sending the key via header rather than query string — works
            // identically for AIza and AQ-style keys and keeps it out of access logs/URLs.
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }

            log.warn("Gemini returned empty or unexpected response: {}", response.getStatusCode());
            return "I couldn't generate a response right now. Please try again.";

        } catch (HttpClientErrorException e) {
            // Log the actual error body from Google — this tells you WHY the key/request failed
            // (e.g. "API key not valid", "API key expired", quota, restricted API, etc.)
            log.error("Gemini API rejected the request ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "I'm experiencing some technical difficulties. Please try again in a moment.";
        } catch (Exception e) {
            log.error("Error calling Gemini API: {} — {}", e.getClass().getSimpleName(), e.getMessage());
            return "I'm experiencing some technical difficulties. Please try again in a moment.";
        }
    }

    // ──────────────────────────────────────────────
    // Safe config resolution helpers
    // ──────────────────────────────────────────────

    /**
     * Resolve Gemini API key.
     * Priority:
     *  1. Spring Environment property "gemini.api.key" (application.properties)
     *  2. GEMINI_API_KEY system environment variable (useful for prod/containers)
     *  3. Empty string (feature disabled)
     *
     * Deliberately does NOT validate or mutate the key's shape. Google does not
     * guarantee a stable key format — some accounts now issue "AQ."-prefixed keys
     * instead of the traditional "AIzaSy" ones — so any prefix/length check here
     * would just be a future bug waiting to happen. The Gemini API itself is the
     * only reliable source of truth on whether a key is valid.
     */
    private String resolveApiKey() {
        String key = "";

        try {
            String propKey = environment.getProperty("gemini.api.key", "");
            if (propKey != null && !propKey.isBlank()) {
                key = propKey;
            }
        } catch (Exception e) {
            log.warn("Could not resolve gemini.api.key from Spring Environment: {}", e.getMessage());
        }

        if (key.isBlank()) {
            String envKey = System.getenv("GEMINI_API_KEY");
            if (envKey != null && !envKey.isBlank()) {
                key = envKey;
            }
        }

        // Strip whitespace and accidental surrounding quotes — a common copy/paste
        // artifact from .env files or properties files. This is NOT format validation.
        key = key.trim();
        if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length() - 1).trim();
        }

        return key;
    }

    private String resolveApiUrl() {
        try {
            String url = environment.getProperty("gemini.api.url", DEFAULT_GEMINI_URL);
            return (url != null && !url.isBlank()) ? url.trim() : DEFAULT_GEMINI_URL;
        } catch (Exception e) {
            return DEFAULT_GEMINI_URL;
        }
    }
}