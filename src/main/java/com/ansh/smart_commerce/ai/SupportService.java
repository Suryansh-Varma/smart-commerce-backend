package com.ansh.smart_commerce.ai;

import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.dto.ai.ChatMessage;
import com.ansh.smart_commerce.dto.ai.ChatResponse;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.service.CouponService;
import com.ansh.smart_commerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * SupportService — handles authenticated customer support queries.
 * Uses real order history and coupon data from existing services.
 */
@Service
public class SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportService.class);

    private final OrderService orderService;
    private final CouponService couponService;
    private final GeminiService geminiService;

    public SupportService(OrderService orderService,
                          CouponService couponService,
                          GeminiService geminiService) {
        this.orderService = orderService;
        this.couponService = couponService;
        this.geminiService = geminiService;
    }

    /**
     * Handle a support query for an authenticated user.
     * Fetches real order and coupon data to give accurate answers.
     */
    public ChatResponse handleSupport(String userMessage, User user, List<ChatMessage> history) {
        log.info("AI support query from user {}: {}", user.getId(), userMessage);

        // Fetch user's order history (existing service, no logic change)
        List<OrderResponse> orders = List.of();
        try {
            orders = orderService.getOrderHistory(user.getId());
        } catch (Exception e) {
            log.warn("Could not fetch orders for user {} in AI support: {}", user.getId(), e.getMessage());
        }

        // Fetch all active coupons
        List<Coupon> coupons = List.of();
        try {
            coupons = couponService.getAllCoupons();
        } catch (Exception e) {
            log.warn("Could not fetch coupons in AI support: {}", e.getMessage());
        }

        String supportPrompt = PromptBuilder.buildSupportMessage(userMessage, user, orders, coupons);
        List<Map<String, Object>> geminiHistory = convertHistory(history);
        String aiResponse = geminiService.generateContent(PromptBuilder.buildSystemPrompt(), geminiHistory, supportPrompt);

        // If user asked about their latest order, attach the order card
        boolean isOrderQuery = containsOrderQuery(userMessage);
        if (isOrderQuery && !orders.isEmpty()) {
            OrderResponse latestOrder = orders.get(0);
            return ChatResponse.withOrder(aiResponse, latestOrder,
                    List.of("View order details", "Cancel order", "Download invoice", "Track shipment"));
        }

        return ChatResponse.textOnly(aiResponse,
                List.of("Where is my order?", "Available coupons", "Return policy", "Cancel my order"));
    }

    private boolean containsOrderQuery(String message) {
        String lower = message.toLowerCase();
        return lower.contains("order") || lower.contains("track") ||
               lower.contains("delivery") || lower.contains("shipped") ||
               lower.contains("where is") || lower.contains("status");
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
