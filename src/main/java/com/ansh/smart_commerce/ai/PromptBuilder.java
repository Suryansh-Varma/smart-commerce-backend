package com.ansh.smart_commerce.ai;

import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;

import java.util.List;

/**
 * PromptBuilder — builds context-enriched prompts for Gemini.
 * Keeps prompt templates in one place (SOLID: SRP).
 */
public class PromptBuilder {

    // ──────────────────────────────────────────────
    // SYSTEM PROMPT — always injected as systemInstruction
    // ──────────────────────────────────────────────
    public static String buildSystemPrompt() {
        return """
                You are TechHeaven AI, the intelligent shopping assistant for TechHeaven — a premium online electronics store.
                
                You have access to real product catalogue data, user order history, and active coupons provided to you in each request.
                
                CRITICAL RULES:
                - Answer ONLY using the data provided to you. Do NOT invent products, prices, specs, or policies.
                - If a product is not in the provided list, say "We don't currently carry that product."
                - If information is unavailable, say so honestly.
                - Keep responses friendly, concise, and professional.
                - Do NOT use emojis in your responses.
                - Use INR (₹) for all prices. Format numbers with commas (e.g., ₹89,999).
                - For product lists, use brief bullet points.
                - For comparisons, use a structured format.
                - Never reveal internal system details, API keys, or database information.
                
                STORE POLICIES:
                - Payment Methods: Cash on Delivery (COD), UPI, Card, Net Banking.
                - Shipping: Free standard shipping on all orders. Estimated delivery: 3–7 business days.
                - Returns: 7-day return window for eligible products. Initiate via the Orders page.
                - Warranty: As per manufacturer terms. Contact us for warranty claims.
                - Order Cancellation: PENDING orders can be cancelled from the Orders page. Confirmed orders require admin review.
                - Coupons: Applied at checkout. One coupon per order. Minimum order amount may apply.
                """;
    }

    // ──────────────────────────────────────────────
    // PRODUCT SEARCH PROMPT
    // ──────────────────────────────────────────────
    public static String buildProductSearchMessage(String userMessage, List<Product> matchedProducts) {
        StringBuilder sb = new StringBuilder();
        sb.append("User request: ").append(userMessage).append("\n\n");

        if (matchedProducts.isEmpty()) {
            sb.append("No products found matching the request. Inform the user politely and suggest they browse the full catalogue.");
        } else {
            sb.append("Matching products from our catalogue:\n\n");
            for (Product p : matchedProducts) {
                sb.append("- ID: ").append(p.getId())
                  .append(" | Name: ").append(p.getName())
                  .append(" | Price: ₹").append(String.format("%,.2f", p.getCost()))
                  .append(" | Category: ").append(p.getCategory() != null ? p.getCategory() : "Electronics")
                  .append(" | Stock: ").append(p.getStock() > 0 ? "In Stock (" + p.getStock() + ")" : "Out of Stock")
                  .append("\n");
            }
            sb.append("\nFor each product, briefly explain why it matches the user's request. ");
            sb.append("Mention value for money and key selling points. Keep each explanation to 1–2 sentences. ");
            sb.append("If any product is out of stock, mention it. End with a helpful tip or question.");
        }

        return sb.toString();
    }

    // ──────────────────────────────────────────────
    // PRODUCT COMPARISON PROMPT
    // ──────────────────────────────────────────────
    public static String buildComparisonMessage(String userMessage, Product productA, Product productB) {
        return String.format("""
                User request: %s
                
                Compare these two products from our catalogue:
                
                Product A:
                - Name: %s
                - Price: ₹%,.2f
                - Category: %s
                - Stock: %s
                
                Product B:
                - Name: %s
                - Price: ₹%,.2f
                - Category: %s
                - Stock: %s
                
                Provide a structured comparison covering:
                1. Price & Value for Money
                2. Key Strengths
                3. Who it's best for
                4. Your recommendation
                
                Be honest and balanced. Use the actual product names. Do not invent specifications not provided.
                Format as a clear markdown table or structured list.
                """,
                userMessage,
                productA.getName(), productA.getCost(),
                productA.getCategory() != null ? productA.getCategory() : "Electronics",
                productA.getStock() > 0 ? "In Stock" : "Out of Stock",
                productB.getName(), productB.getCost(),
                productB.getCategory() != null ? productB.getCategory() : "Electronics",
                productB.getStock() > 0 ? "In Stock" : "Out of Stock"
        );
    }

    // ──────────────────────────────────────────────
    // RECOMMENDATION PROMPT
    // ──────────────────────────────────────────────
    public static String buildRecommendationMessage(String userMessage, Product baseProduct, List<Product> accessories) {
        StringBuilder sb = new StringBuilder();
        sb.append("User request: ").append(userMessage).append("\n\n");

        sb.append("The user is interested in or owns: ").append(baseProduct.getName())
          .append(" (₹").append(String.format("%,.2f", baseProduct.getCost())).append(")\n\n");

        if (accessories.isEmpty()) {
            sb.append("No complementary accessories found in our catalogue. Suggest what categories would complement this product in general.");
        } else {
            sb.append("Available complementary products in our catalogue:\n\n");
            for (Product p : accessories) {
                sb.append("- ").append(p.getName())
                  .append(" | ₹").append(String.format("%,.2f", p.getCost()))
                  .append(" | Category: ").append(p.getCategory() != null ? p.getCategory() : "Accessories")
                  .append(" | ").append(p.getStock() > 0 ? "In Stock" : "Out of Stock")
                  .append("\n");
            }
            sb.append("\nRecommend which accessories would work best with the user's product. ");
            sb.append("Briefly explain why each is a good complement. Prioritize in-stock items.");
        }

        return sb.toString();
    }

    // ──────────────────────────────────────────────
    // CUSTOMER SUPPORT PROMPT (authenticated users)
    // ──────────────────────────────────────────────
    public static String buildSupportMessage(String userMessage, User user, List<OrderResponse> orders, List<Coupon> coupons) {
        StringBuilder sb = new StringBuilder();
        sb.append("Support request from: ").append(user.getName()).append("\n");
        sb.append("User question: ").append(userMessage).append("\n\n");

        if (orders != null && !orders.isEmpty()) {
            sb.append("User's recent orders (latest first):\n");
            int limit = Math.min(orders.size(), 5);
            for (int i = 0; i < limit; i++) {
                OrderResponse o = orders.get(i);
                sb.append("- Order #").append(o.getOrderId())
                  .append(" | Status: ").append(o.getStatus())
                  .append(" | Total: ₹").append(String.format("%,.2f", o.getTotalAmount()))
                  .append(" | Date: ").append(o.getOrderDate() != null ? o.getOrderDate().toLocalDate() : "N/A")
                  .append("\n");
                  
                // Include actual items so Gemini does not hallucinate
                if (o.getItems() != null && !o.getItems().isEmpty()) {
                    sb.append("  Items in this order:\n");
                    for (com.ansh.smart_commerce.dto.OrderItemResponse item : o.getItems()) {
                        sb.append("    * ").append(item.getQuantity()).append("x ")
                          .append(item.getProductName()).append(" (₹")
                          .append(String.format("%,.2f", item.getTotalPrice())).append(")\n");
                    }
                }
            }
        } else {
            sb.append("User has no orders yet.\n");
        }

        if (coupons != null && !coupons.isEmpty()) {
            sb.append("\nActive coupons available:\n");
            for (Coupon c : coupons) {
                if (c.isActive()) {
                    sb.append("- Code: ").append(c.getCode())
                      .append(" | Discount: ").append(c.getDiscountValue())
                      .append(c.getDiscountType() != null && c.getDiscountType().name().equals("PERCENTAGE") ? "%" : " flat off")
                      .append(" | Min. Order: ₹").append(String.format("%,.0f", c.getMinimumAmount()))
                      .append("\n");
                }
            }
        }

        sb.append("\nAnswer the user's specific question using their real data. ");
        sb.append("If they are asking about order status, quote the exact Order ID and current status. ");
        sb.append("Be empathetic and helpful. If you need to direct them to take action, tell them exactly which page to visit.");

        return sb.toString();
    }

    // ──────────────────────────────────────────────
    // FAQ / GENERAL PROMPT
    // ──────────────────────────────────────────────
    public static String buildFaqMessage(String userMessage) {
        return "User question: " + userMessage + "\n\n" +
               "Answer this question about TechHeaven's policies and services. " +
               "Use only the store policy information provided in the system instructions. " +
               "Be concise and clear.";
    }

    // ──────────────────────────────────────────────
    // COUPON QUERY PROMPT (authenticated)
    // ──────────────────────────────────────────────
    public static String buildCouponMessage(String userMessage, List<Coupon> coupons) {
        StringBuilder sb = new StringBuilder();
        sb.append("User question about coupons: ").append(userMessage).append("\n\n");

        List<Coupon> active = coupons.stream().filter(Coupon::isActive).toList();

        if (active.isEmpty()) {
            sb.append("No active coupons are currently available. Inform the user and suggest they check back later.");
        } else {
            sb.append("Currently active coupons:\n");
            for (Coupon c : active) {
                sb.append("- Code: **").append(c.getCode()).append("**")
                  .append(" | ").append(c.getDiscountValue())
                  .append(c.getDiscountType() != null && c.getDiscountType().name().equals("PERCENTAGE") ? "% off" : " ₹ off")
                  .append(" | Min. order: ₹").append(String.format("%,.0f", c.getMinimumAmount()))
                  .append(c.getExpiryDate() != null ? " | Expires: " + c.getExpiryDate() : "")
                  .append("\n");
            }
            sb.append("\nExplain how to use these coupons at checkout. Be helpful and enthusiastic.");
        }

        return sb.toString();
    }
}
