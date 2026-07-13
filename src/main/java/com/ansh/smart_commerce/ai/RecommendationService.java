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
 * RecommendationService — recommends accessories/complementary products.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final GeminiService geminiService;

    public RecommendationService(ProductService productService,
                                 ProductSearchService productSearchService,
                                 GeminiService geminiService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
        this.geminiService = geminiService;
    }

    public ChatResponse recommend(String userMessage, List<ChatMessage> history) {
        List<Product> allProducts = productService.getAllProducts();

        // Try to extract what they have/bought from the message
        String lower = userMessage.toLowerCase();
        String productHint = extractProductHint(lower);

        Optional<Product> baseProduct = productHint != null ?
                productSearchService.findByName(allProducts, productHint) :
                Optional.empty();

        List<Product> accessories;
        String aiMessage;

        if (baseProduct.isPresent()) {
            accessories = productSearchService.findAccessories(allProducts, baseProduct.get());
            String prompt = PromptBuilder.buildRecommendationMessage(userMessage, baseProduct.get(), accessories);
            List<Map<String, Object>> geminiHistory = convertHistory(history);
            aiMessage = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, prompt);
        } else {
            // Fallback: recommend top in-stock products
            accessories = allProducts.stream()
                    .filter(p -> p.getStock() > 0)
                    .limit(4)
                    .collect(java.util.stream.Collectors.toList());
            String prompt = "User request: " + userMessage + "\n\nRecommend our best-selling in-stock products for this user.";
            List<Map<String, Object>> geminiHistory = convertHistory(history);
            aiMessage = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, prompt);
        }

        List<ProductSuggestion> suggestions = accessories.stream()
                .map(p -> new ProductSuggestion(
                        p.getId(), p.getName(), p.getCost(),
                        p.getCategory(), p.getImageUrl(), p.getStock(),
                        "Recommended for you"
                ))
                .collect(java.util.stream.Collectors.toList());

        return ChatResponse.withProducts(aiMessage, suggestions,
                List.of("Tell me more", "Add to cart", "Show me laptops", "Show me phones"));
    }

    private String extractProductHint(String lower) {
        String[] patterns = {"i bought a ", "i have a ", "i own a ", "for my ", "after buying ", "with my "};
        for (String p : patterns) {
            int idx = lower.indexOf(p);
            if (idx >= 0) {
                String afterPattern = lower.substring(idx + p.length()).trim();
                // Take until period, comma, or end
                int end = afterPattern.length();
                for (char stop : new char[]{'.', ',', '?', '!'}) {
                    int s = afterPattern.indexOf(stop);
                    if (s > 0 && s < end) end = s;
                }
                return afterPattern.substring(0, end).trim();
            }
        }
        return null;
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
