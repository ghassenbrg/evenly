package io.evenly.core.mock.service;

import io.evenly.core.features.auth.UserService;
import io.evenly.core.features.auth.dto.User;
import io.evenly.core.mock.data.MockDataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Mock implementation of UserService.
 * Provides realistic mock data that can be easily swapped with real persistence.
 * Only active when running with the "mock" profile.
 */
@Alternative
@ApplicationScoped
@jakarta.annotation.Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class UserServiceMock implements UserService {
    
    @Inject
    private MockDataProvider mockDataProvider;
    
    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(mockDataProvider.getUsers().get(userId));
    }
    
    @Override
    public User getOrCreate(String userId, String email, String username) {
        Map<String, User> users = mockDataProvider.getUsers();
        User user = users.get(userId);
        if (user == null) {
            user = new User();
            user.setId(userId);
            user.setEmail(email);
            user.setUsername(username);
            user.setDisplayName(username);
            user.setPreferredCurrency("USD");
            user.setLocale("en-US");
            user.setTimezone("America/New_York");
            user.setCreatedAt(OffsetDateTime.now());
            users.put(userId, user);
        } else {
            // Update existing user with provided information if different
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
            }
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
            }
        }
        return user;
    }
    
    /**
     * Create a new user (for registration).
     */
    public User create(String userId, String email, String username, String displayName, String preferredCurrency) {
        Map<String, User> users = mockDataProvider.getUsers();
        if (users.containsKey(userId)) {
            throw new RuntimeException("User already exists");
        }
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(displayName != null ? displayName : username);
        user.setPreferredCurrency(preferredCurrency != null ? preferredCurrency : "USD");
        user.setLocale("en-US");
        user.setTimezone("America/New_York");
        user.setCreatedAt(OffsetDateTime.now());
        users.put(userId, user);
        return user;
    }
}
