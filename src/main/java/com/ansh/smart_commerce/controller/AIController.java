package com.ansh.smart_commerce.controller;

import com.ansh.smart_commerce.ai.ChatService;
import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.ai.ChatRequest;
import com.ansh.smart_commerce.dto.ai.ChatResponse;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.security.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AIController — REST endpoints for the TechHeaven AI assistant.
 *
 * POST /ai/chat         — authenticated chat (can access user orders, coupons)
 * POST /ai/chat/public  — anonymous chat (products, FAQs, comparisons only)
 *
 * Does NOT modify any existing controller or service.
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final ChatService chatService;
    private final SecurityHelper securityHelper;

    public AIController(ChatService chatService, SecurityHelper securityHelper) {
        this.chatService = chatService;
        this.securityHelper = securityHelper;
    }

    /**
     * Authenticated AI chat endpoint.
     * The user's JWT must be in the Authorization header.
     * Can answer personalised questions about orders, coupons, and profile.
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        log.info("AI chat request received");

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ChatResponse>error("Message cannot be empty", null));
        }

        User user = securityHelper.getCurrentUser();
        ChatResponse response = chatService.chat(request, user);

        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

}
