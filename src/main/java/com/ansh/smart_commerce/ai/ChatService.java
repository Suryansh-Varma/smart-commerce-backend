package com.ansh.smart_commerce.ai;

import com.ansh.smart_commerce.dto.ai.ChatMessage;
import com.ansh.smart_commerce.dto.ai.ChatRequest;
import com.ansh.smart_commerce.dto.ai.ChatResponse;
import com.ansh.smart_commerce.dto.ai.ProductSuggestion;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.service.CouponService;
import com.ansh.smart_commerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ChatService — main AI orchestrator.
 * Routes messages by intent → fetches real data → calls Gemini → returns structured response.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ProductService productService;
    private final CouponService couponService;
    private final GeminiService geminiService;
    private final ProductSearchService productSearchService;
    private final ComparisonService comparisonService;
    private final RecommendationService recommendationService;
    private final SupportService supportService;

    public ChatService(ProductService productService,
                       CouponService couponService,
                       GeminiService geminiService,
                       ProductSearchService productSearchService,
                       ComparisonService comparisonService,
                       RecommendationService recommendationService,
                       SupportService supportService) {
        this.productService = productService;
        this.couponService = couponService;
        this.geminiService = geminiService;
        this.productSearchService = productSearchService;
        this.comparisonService = comparisonService;
        this.recommendationService = recommendationService;
        this.supportService = supportService;
    }

    /**
     * Authenticated chat — can access order history, coupons, user profile.
     */
    public ChatResponse chat(ChatRequest request, User user) {
        String message = request.getMessage();
        List<ChatMessage> history = limitHistory(request.getHistory());

        log.info("AI chat from user {}: intent classification for message '{}'", user.getId(), message);

        IntentClassifier.Intent intent = IntentClassifier.classify(message);
        log.info("Detected intent: {}", intent);

        return switch (intent) {
            case PRODUCT_SEARCH -> handleProductSearch(message, history);
            case PRODUCT_COMPARISON -> comparisonService.compare(message, history);
            case RECOMMENDATION -> recommendationService.recommend(message, history);
            case ORDER_SUPPORT -> supportService.handleSupport(message, user, history);
            case COUPON_QUERY -> handleCouponQuery(message, history);
            case GENERAL_FAQ -> handleFaq(message, history);
            case GENERAL -> handleGeneral(message, history);
        };
    }

    /**
     * Anonymous (public) chat — no order/coupon/profile access.
     * Only product search, comparisons, recommendations, and FAQ.
     */
    public ChatResponse chatPublic(ChatRequest request) {
        String message = request.getMessage();
        List<ChatMessage> history = limitHistory(request.getHistory());

        log.info("AI public chat: intent classification for message '{}'", message);

        IntentClassifier.Intent intent = IntentClassifier.classify(message);
        log.info("Detected intent (public): {}", intent);

        return switch (intent) {
            case PRODUCT_SEARCH -> handleProductSearch(message, history);
            case PRODUCT_COMPARISON -> comparisonService.compare(message, history);
            case RECOMMENDATION -> recommendationService.recommend(message, history);
            case ORDER_SUPPORT -> ChatResponse.textOnly(
                    "To track your order or get personalized support, please **sign in** first. " +
                    "I can then access your real order history and give you accurate updates.",
                    List.of("Sign in to track orders", "Browse products", "Return policy")
            );
            case COUPON_QUERY -> handleCouponQuery(message, history);
            case GENERAL_FAQ -> handleFaq(message, history);
            case GENERAL -> handleGeneral(message, history);
        };
    }

    // ──────────────────────────────────────────────
    // Private handlers
    // ──────────────────────────────────────────────

    private ChatResponse handleProductSearch(String message, List<ChatMessage> history) {
        List<Product> allProducts = productService.getAllProducts();
        List<Product> filtered = productSearchService.filterByIntent(allProducts, message);

        String prompt = PromptBuilder.buildProductSearchMessage(message, filtered);
        List<Map<String, Object>> geminiHistory = convertHistory(history);
        String aiResponse = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, prompt);

        List<ProductSuggestion> suggestions = filtered.stream()
                .map(p -> new ProductSuggestion(
                        p.getId(), p.getName(), p.getCost(),
                        p.getCategory(), p.getImageUrl(), p.getStock(),
                        "" // reason will be embedded in the AI text response
                ))
                .collect(java.util.stream.Collectors.toList());

        return ChatResponse.withProducts(aiResponse, suggestions,
                List.of("Add to cart", "Tell me more", "Compare products", "Show other options"));
    }

    private ChatResponse handleCouponQuery(String message, List<ChatMessage> history) {
        List<Coupon> coupons = List.of();
        try {
            coupons = couponService.getAllCoupons();
        } catch (Exception e) {
            log.warn("Could not fetch coupons for AI coupon query: {}", e.getMessage());
        }

        String prompt = PromptBuilder.buildCouponMessage(message, coupons);
        List<Map<String, Object>> geminiHistory = convertHistory(history);
        String aiResponse = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, prompt);

        return ChatResponse.textOnly(aiResponse,
                List.of("How do I use a coupon?", "Shop now", "Best deals"));
    }

    private ChatResponse handleFaq(String message, List<ChatMessage> history) {
        String prompt = PromptBuilder.buildFaqMessage(message);
        List<Map<String, Object>> geminiHistory = convertHistory(history);
        String aiResponse = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, prompt);

        return ChatResponse.textOnly(aiResponse,
                List.of("Track my order", "Browse products", "Available coupons"));
    }

    private ChatResponse handleGeneral(String message, List<ChatMessage> history) {
        // For general conversation, pass through to Gemini with system context
        List<Map<String, Object>> geminiHistory = convertHistory(history);
        String aiResponse = geminiService.generateContent(
                PromptBuilder.buildSystemPrompt(), geminiHistory,
                "User message: " + message + "\n\nRespond helpfully. If this is shopping-related, ask clarifying questions."
        );

        return ChatResponse.textOnly(aiResponse,
                List.of("Find a product", "Track my order", "Return policy", "Available coupons"));
    }

    /**
     * Limit history to last 8 turns to control Gemini token usage.
     */
    private List<ChatMessage> limitHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) return List.of();
        int size = history.size();
        if (size <= 8) return history;
        return history.subList(size - 8, size);
    }

    private List<Map<String, Object>> convertHistory(List<ChatMessage> history) {
        if (history == null) return List.of();
        return history.stream()
                .map(m -> Map.<String, Object>of(
                        "role", m.getRole(),
                        "parts", List.of(Map.of("text", m.getContent()))
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
