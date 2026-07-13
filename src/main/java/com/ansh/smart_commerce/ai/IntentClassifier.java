package com.ansh.smart_commerce.ai;

/**
 * IntentClassifier — keyword-based intent detection.
 * No API call needed; fast and deterministic.
 */
public class IntentClassifier {

    public enum Intent {
        PRODUCT_SEARCH,
        PRODUCT_COMPARISON,
        RECOMMENDATION,
        ORDER_SUPPORT,
        COUPON_QUERY,
        GENERAL_FAQ,
        GENERAL
    }

    private static final String[] PRODUCT_SEARCH_KEYWORDS = {
        "suggest", "recommend", "find", "looking for", "need a", "need an",
        "best ", "good for", "under ₹", "under rs", "below ₹", "below rs",
        "cheap", "affordable", "budget", "gaming laptop", "gaming phone",
        "buy", "purchase", "show me", "what laptop", "what phone", "which phone",
        "which laptop", "which headphone", "which tv", "wireless", "monitor"
    };

    private static final String[] COMPARISON_KEYWORDS = {
        "compare", " vs ", " vs.", "versus", "difference between",
        "which is better", "which one", "better than"
    };

    private static final String[] RECOMMENDATION_KEYWORDS = {
        "accessories", "goes with", "also buy", "pair with", "what else",
        "along with", "compatible with", "for my laptop", "for my phone",
        "after buying", "i bought", "i have a"
    };

    private static final String[] ORDER_SUPPORT_KEYWORDS = {
        "my order", "where is", "track", "cancel my", "order status",
        "delivery", "shipped", "when will", "return", "refund", "my latest order",
        "order id", "order number", "not received"
    };

    private static final String[] COUPON_KEYWORDS = {
        "coupon", "discount", "promo", "offer", "deal", "voucher",
        "coupon code", "promotion", "any offers", "available coupons"
    };

    private static final String[] FAQ_KEYWORDS = {
        "return policy", "refund", "shipping policy", "payment method",
        "how long", "warranty", "support", "contact", "policy",
        "how does", "how do", "can i cancel", "cancellation"
    };

    /**
     * Classify the user's message into a detected intent.
     */
    public static Intent classify(String message) {
        if (message == null || message.isBlank()) {
            return Intent.GENERAL;
        }

        String lower = message.toLowerCase();

        if (containsAny(lower, COMPARISON_KEYWORDS)) return Intent.PRODUCT_COMPARISON;
        if (containsAny(lower, ORDER_SUPPORT_KEYWORDS)) return Intent.ORDER_SUPPORT;
        if (containsAny(lower, COUPON_KEYWORDS)) return Intent.COUPON_QUERY;
        if (containsAny(lower, RECOMMENDATION_KEYWORDS)) return Intent.RECOMMENDATION;
        if (containsAny(lower, PRODUCT_SEARCH_KEYWORDS)) return Intent.PRODUCT_SEARCH;
        if (containsAny(lower, FAQ_KEYWORDS)) return Intent.GENERAL_FAQ;

        return Intent.GENERAL;
    }

    private static boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
