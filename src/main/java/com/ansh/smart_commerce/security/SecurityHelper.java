package com.ansh.smart_commerce.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.exception.UserNotFoundException;
import com.ansh.smart_commerce.repository.UserRepository;

@Component
public class SecurityHelper {

    private final UserRepository userRepository;

    public SecurityHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        String email = authentication.getName();
        if ("root".equalsIgnoreCase(email) || "root@techheaven.com".equalsIgnoreCase(email) || "root@teachheaven.com".equalsIgnoreCase(email)) {
            User rootUser = new User();
            rootUser.setId(9999L);
            rootUser.setName("Root Administrator");
            rootUser.setEmail(email.contains("@") ? email.toLowerCase() : "root@techheaven.com");
            return rootUser;
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(0L)); // 0L fallback since we look up by email
    }
}
