package com.ansh.smart_commerce.ai;

import com.ansh.smart_commerce.dto.ai.ChatMessage;
import com.ansh.smart_commerce.dto.ai.ChatResponse;
import com.ansh.smart_commerce.dto.ai.ProductSuggestion;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ComparisonService — handles "compare X vs Y" requests.
 */
@Service
public class ComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final GeminiService geminiService;

    public ComparisonService(ProductService productService,
                             ProductSearchService productSearchService,
                             GeminiService geminiService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
        this.geminiService = geminiService;
    }

    public ChatResponse compare(String userMessage, List<ChatMessage> history) {
        List<Product> allProducts = productService.getAllProducts();

        // Try to extract two product names from the message
        // Pattern: "compare X vs Y", "X versus Y", "difference between X and Y"
        String lower = userMessage.toLowerCase();
        String nameA = null;
        String nameB = null;

        // Extract around "vs", "versus", "and"
        String[] splitPatterns = {" vs ", " versus ", " or "};
        String actionWords = "compare |difference between |which is better |";

        String cleaned = lower;
        for (String prefix : new String[]{"compare ", "difference between ", "which is better "}) {
            cleaned = cleaned.replace(prefix, "");
        }

        for (String pattern : splitPatterns) {
            if (cleaned.contains(pattern)) {
                String[] parts = cleaned.split(pattern, 2);
                nameA = parts[0].trim();
                nameB = parts[1].trim().replaceAll("[?]$", "").trim();
                break;
            }
        }

        if (nameA == null || nameB == null) {
            return ChatResponse.textOnly(
                    "To compare products, please mention two specific product names. For example: **\"Compare iPhone 16 vs Samsung S25\"**",
                    List.of("Compare two laptops", "Compare headphones", "Which phone is better?")
            );
        }

        Optional<Product> optA = productSearchService.findByName(allProducts, nameA);
        Optional<Product> optB = productSearchService.findByName(allProducts, nameB);

        if (optA.isEmpty() && optB.isEmpty()) {
            return ChatResponse.textOnly(
                    "I couldn't find either **" + nameA + "** or **" + nameB + "** in our catalogue. Please check the product names and try again.",
                    List.of("Browse all products", "Search for a laptop", "What phones do you have?")
            );
        }

        if (optA.isEmpty()) {
            return ChatResponse.textOnly(
                    "I couldn't find **" + nameA + "** in our catalogue. Did you mean something else?",
                    List.of("Browse all products")
            );
        }

        if (optB.isEmpty()) {
            return ChatResponse.textOnly(
                    "I couldn't find **" + nameB + "** in our catalogue. Did you mean something else?",
                    List.of("Browse all products")
            );
        }

        Product productA = optA.get();
        Product productB = optB.get();

        log.info("Comparing products: {} vs {}", productA.getName(), productB.getName());

        String comparisonPrompt = PromptBuilder.buildComparisonMessage(userMessage, productA, productB);
        List<Map<String, Object>> geminiHistory = convertHistory(history);
        String aiResponse = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, comparisonPrompt);

        // Include both products as suggestions so user can view/add them
        List<ProductSuggestion> suggestions = List.of(
                new ProductSuggestion(productA.getId(), productA.getName(), productA.getCost(),
                        productA.getCategory(), productA.getImageUrl(), productA.getStock(), "Product A in comparison"),
                new ProductSuggestion(productB.getId(), productB.getName(), productB.getCost(),
                        productB.getCategory(), productB.getImageUrl(), productB.getStock(), "Product B in comparison")
        );

        return ChatResponse.withProducts(aiResponse, suggestions,
                List.of("Add to cart", "View full details", "Compare other products"));
    }

    private List<Map<String, Object>> convertHistory(List<ChatMessage> history) {
        if (history == null) return List.of();
        return history.stream()
                .limit(8)
                .map(m -> Map.<String, Object>of(
                        "role", m.getRole(),
                        "parts", List.of(Map.of("text", m.getContent()))
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
