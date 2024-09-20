package com.bradwick.tibia_genius.auth;

import com.bradwick.tibia_genius.email.EmailService;
import com.bradwick.tibia_genius.email.EmailTemplateName;
import com.bradwick.tibia_genius.role.RoleRepository;
import com.bradwick.tibia_genius.security.JwtService;
import com.bradwick.tibia_genius.user.Token;
import com.bradwick.tibia_genius.user.TokenRepository;
import com.bradwick.tibia_genius.user.User;
import com.bradwick.tibia_genius.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${application.security.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                // todo -> better exception handling
                .orElseThrow(() -> new RuntimeException("User role not found"));
        var user = User.builder()
                .email(request.getEmail())
                .username(null)
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var authManager = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = ((User) authManager.getPrincipal());
        claims.put("email", user.getEmail());
        var jwt = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder().jwt(jwt).build();
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var activationToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                activationToken,
                "Account Activation"
        );
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedActivationToken = generateActivationToken();
        var token = Token.builder()
                .token(generatedActivationToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generatedActivationToken;
    }

    private String generateActivationToken() {
        String characters = "0123456789";
        StringBuilder token = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            token.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }
        return token.toString();
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                // todo exception to be defined
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token expired. A new token has been sent.");
        }
        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}
