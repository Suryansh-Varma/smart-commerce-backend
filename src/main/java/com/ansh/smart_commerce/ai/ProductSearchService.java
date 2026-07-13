package com.ansh.smart_commerce.ai;

import com.ansh.smart_commerce.entity.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ProductSearchService — filters existing product catalogue based on user intent.
 * No Gemini call. Works purely with the Product list from ProductService.
 */
@Service
public class ProductSearchService {

    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(?:under|below|less than|upto|up to|within)\\s*[₹rs.]*\\s*([0-9][0-9,]*)\\s*(?:k|thousand|lakh)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final String[] CATEGORY_KEYWORDS = {
        "laptop", "phone", "mobile", "headphone", "headset", "earphone",
        "earbud", "monitor", "keyboard", "mouse", "tablet", "camera",
        "smartwatch", "watch", "speaker", "tv", "television", "charger",
        "cable", "router", "gaming", "printer", "hard disk", "ssd", "ram",
        "processor", "gpu", "motherboard", "cooling", "case", "power supply",
        "webcam", "microphone", "drone", "vr", "graphics"
    };

    /**
     * Filter the full product list to those matching the user's message intent.
     * Returns at most 5 results.
     */
    public List<Product> filterByIntent(List<Product> allProducts, String message) {
        String lower = message.toLowerCase();
        Optional<Double> budget = extractBudget(lower);
        String categoryHint = extractCategory(lower);

        List<Product> filtered = new ArrayList<>();

        for (Product p : allProducts) {
            boolean nameMatch = categoryHint != null &&
                    (p.getName().toLowerCase().contains(categoryHint) ||
                     (p.getCategory() != null && p.getCategory().toLowerCase().contains(categoryHint)));

            boolean budgetMatch = budget.isEmpty() || p.getCost() <= budget.get();

            if (nameMatch && budgetMatch) {
                filtered.add(p);
            }
        }

        // If category match gave nothing, fall back to budget-only match
        if (filtered.isEmpty() && budget.isPresent()) {
            for (Product p : allProducts) {
                if (p.getCost() <= budget.get()) {
                    filtered.add(p);
                }
            }
        }

        // If still empty, return top products overall (not more than 5)
        if (filtered.isEmpty()) {
            filtered.addAll(allProducts.stream().limit(5).toList());
        }

        // Limit to max 5 results to avoid overwhelming Gemini context
        return filtered.stream().limit(5).toList();
    }

    /**
     * Find a product by partial name match (used for comparisons and recommendations).
     */
    public Optional<Product> findByName(List<Product> allProducts, String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String lower = name.toLowerCase().trim();

        return allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(lower))
                .findFirst();
    }

    /**
     * Find accessory/complementary products for a base product.
     * Simple logic: different category, in stock, not the base product.
     */
    public List<Product> findAccessories(List<Product> allProducts, Product baseProduct) {
        String baseCategory = baseProduct.getCategory() != null ?
                baseProduct.getCategory().toLowerCase() : "";

        // Find products from a different category
        return allProducts.stream()
                .filter(p -> p.getId() != baseProduct.getId())
                .filter(p -> p.getStock() > 0)
                .filter(p -> {
                    String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
                    return !cat.equals(baseCategory);
                })
                .limit(4)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Extract budget from natural language.
     * Handles: "under ₹90000", "below 50k", "less than 1.5 lakh", etc.
     */
    public Optional<Double> extractBudget(String message) {
        Matcher m = PRICE_PATTERN.matcher(message);
        if (m.find()) {
            try {
                String numStr = m.group(1).replace(",", "");
                double value = Double.parseDouble(numStr);

                // Handle "k" / "thousand"
                String suffix = message.substring(m.end()).trim().toLowerCase();
                if (suffix.startsWith("k") || suffix.startsWith("thousand")) {
                    value *= 1000;
                } else if (suffix.startsWith("lakh")) {
                    value *= 100000;
                } else if (value < 1000) {
                    // Raw "90" likely means 90,000 in context of electronics
                    value *= 1000;
                }

                return Optional.of(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return Optional.empty();
    }

    /**
     * Extract a product category from the user's message.
     */
    public String extractCategory(String message) {
        String lower = message.toLowerCase();
        for (String kw : CATEGORY_KEYWORDS) {
            if (lower.contains(kw)) return kw;
        }
        return null;
    }
}
