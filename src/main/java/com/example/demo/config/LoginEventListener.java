package com.example.demo.config;

import com.example.demo.model.LoginHistory;
import com.example.demo.repository.LoginHistoryRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LoginEventListener {

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginEventListener(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        LoginHistory history = new LoginHistory();
        history.setUsername(username);
        history.setLoginTime(LocalDateTime.now());
        history.setIpAddress("127.0.0.1"); // Default local address
        history.setStatus("SUCCESS");

        loginHistoryRepository.save(history);
    }
}
