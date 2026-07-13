package com.ansh.smart_commerce.dto.ai;

import java.util.List;

import com.ansh.smart_commerce.dto.OrderResponse;

public class ChatResponse {

    /**
     * Response types:
     * TEXT        — plain/markdown text response
     * PRODUCTS    — text + product cards
     * ORDER       — text + order status card
     * COMPARISON  — text comparison table
     */
    public enum ResponseType {
        TEXT, PRODUCTS, ORDER, COMPARISON
    }

    private String message;
    private ResponseType responseType;
    private List<ProductSuggestion> products;
    private OrderResponse orderInfo;
    private List<String> suggestedQuestions;

    public ChatResponse() {
    }

    // Convenience factory methods

    public static ChatResponse textOnly(String message, List<String> suggestions) {
        ChatResponse r = new ChatResponse();
        r.message = message;
        r.responseType = ResponseType.TEXT;
        r.suggestedQuestions = suggestions;
        return r;
    }

    public static ChatResponse withProducts(String message, List<ProductSuggestion> products, List<String> suggestions) {
        ChatResponse r = new ChatResponse();
        r.message = message;
        r.responseType = ResponseType.PRODUCTS;
        r.products = products;
        r.suggestedQuestions = suggestions;
        return r;
    }

    public static ChatResponse withOrder(String message, OrderResponse order, List<String> suggestions) {
        ChatResponse r = new ChatResponse();
        r.message = message;
        r.responseType = ResponseType.ORDER;
        r.orderInfo = order;
        r.suggestedQuestions = suggestions;
        return r;
    }

    public static ChatResponse comparison(String message, List<String> suggestions) {
        ChatResponse r = new ChatResponse();
        r.message = message;
        r.responseType = ResponseType.COMPARISON;
        r.suggestedQuestions = suggestions;
        return r;
    }

    // Getters and setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public List<ProductSuggestion> getProducts() {
        return products;
    }

    public void setProducts(List<ProductSuggestion> products) {
        this.products = products;
    }

    public OrderResponse getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderResponse orderInfo) {
        this.orderInfo = orderInfo;
    }

    public List<String> getSuggestedQuestions() {
        return suggestedQuestions;
    }

    public void setSuggestedQuestions(List<String> suggestedQuestions) {
        this.suggestedQuestions = suggestedQuestions;
    }
}
