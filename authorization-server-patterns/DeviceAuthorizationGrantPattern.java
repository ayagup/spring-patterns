package com.example.authorizationserver.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Device Authorization Grant Pattern (RFC 8628)
 * 
 * Demonstrates OAuth 2.0 Device Authorization Grant flow for devices with
 * limited input capabilities (smart TVs, IoT devices, CLI tools).
 * 
 * Key Components:
 * - Device code generation and user code generation
 * - User verification URL (short code display)
 * - Device polling for authorization status
 * - User authorization flow on secondary device
 * - Token issuance after user approval
 * 
 * Flow:
 * 1. Device requests device code and user code
 * 2. Device displays user code and verification URL to user
 * 3. User visits verification URL on another device (phone/computer)
 * 4. User enters user code and authorizes the device
 * 5. Device polls token endpoint with device code
 * 6. Server issues access token after user approval
 * 
 * Use Cases:
 * - Smart TV applications
 * - IoT devices without keyboard
 * - CLI tools and terminal applications
 * - Gaming consoles
 * - Set-top boxes
 * 
 * Security Considerations:
 * - Short user code lifetime (15 minutes typical)
 * - Rate limiting on device code endpoint
 * - Implement slow_down response for aggressive polling
 * - User code should be short and easy to type
 * - Device code should be cryptographically random
 */
@SpringBootApplication
public class DeviceAuthorizationGrantPattern {

    public static void main(String[] args) {
        SpringApplication.run(DeviceAuthorizationGrantPattern.class, args);
    }

    /**
     * OAuth2 Authorization Server Configuration
     */
    @Configuration
    @EnableWebSecurity
    public static class AuthorizationServerConfig {

        @Bean
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
            OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
            return http.formLogin(Customizer.withDefaults()).build();
        }

        @Bean
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/**").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .build();
        }

        @Bean
        public RegisteredClientRepository registeredClientRepository() {
            // Smart TV client
            RegisteredClient tvClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("smart-tv-client")
                .clientSecret("{noop}tv-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:device_code"))
                .scope("tv.stream")
                .scope("tv.control")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(4))
                    .refreshTokenTimeToLive(Duration.ofDays(90))
                    .reuseRefreshTokens(true)
                    .build())
                .build();

            // IoT device client
            RegisteredClient iotClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("iot-device-client")
                .clientSecret("{noop}iot-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:device_code"))
                .scope("device.read")
                .scope("device.control")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .build())
                .build();

            // CLI tool client
            RegisteredClient cliClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("cli-tool-client")
                .clientSecret("{noop}cli-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:device_code"))
                .scope("api.read")
                .scope("api.write")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(2))
                    .build())
                .build();

            return new InMemoryRegisteredClientRepository(tvClient, iotClient, cliClient);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
            return new InMemoryUserDetailsManager(user);
        }
    }

    /**
     * Service to manage device authorization flow
     */
    @Service
    public static class DeviceAuthorizationService {

        private static final String USER_CODE_CHARS = "BCDFGHJKLMNPQRSTVWXZ"; // Avoid ambiguous characters
        private static final int USER_CODE_LENGTH = 8;
        private static final int DEVICE_CODE_INTERVAL = 5; // seconds
        private static final Duration USER_CODE_LIFETIME = Duration.ofMinutes(15);
        private static final Duration DEVICE_CODE_LIFETIME = Duration.ofMinutes(15);

        private final RegisteredClientRepository clientRepository;
        private final SecureRandom secureRandom = new SecureRandom();
        private final Map<String, DeviceAuthorizationRequest> deviceCodeStore = new HashMap<>();
        private final Map<String, String> userCodeToDeviceCode = new HashMap<>();
        private final Map<String, DevicePollingInfo> pollingInfo = new HashMap<>();

        public DeviceAuthorizationService(RegisteredClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        /**
         * Initiate device authorization flow
         */
        public DeviceAuthorizationResponse initiateDeviceAuthorization(String clientId, Set<String> scopes) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null) {
                throw new IllegalArgumentException("Invalid client");
            }

            String deviceCode = generateDeviceCode();
            String userCode = generateUserCode();
            Instant now = Instant.now();

            DeviceAuthorizationRequest request = new DeviceAuthorizationRequest(
                deviceCode,
                userCode,
                clientId,
                scopes,
                now,
                now.plus(DEVICE_CODE_LIFETIME),
                DeviceAuthorizationStatus.PENDING
            );

            deviceCodeStore.put(deviceCode, request);
            userCodeToDeviceCode.put(userCode, deviceCode);
            pollingInfo.put(deviceCode, new DevicePollingInfo(now, 0));

            return new DeviceAuthorizationResponse(
                deviceCode,
                userCode,
                "http://localhost:8080/device",
                "http://localhost:8080/device?user_code=" + userCode,
                DEVICE_CODE_LIFETIME.toSeconds(),
                DEVICE_CODE_INTERVAL
            );
        }

        /**
         * Verify user code (user enters code on secondary device)
         */
        public UserCodeVerificationResult verifyUserCode(String userCode) {
            String deviceCode = userCodeToDeviceCode.get(userCode);
            if (deviceCode == null) {
                return new UserCodeVerificationResult(false, "Invalid user code", null);
            }

            DeviceAuthorizationRequest request = deviceCodeStore.get(deviceCode);
            if (request == null) {
                return new UserCodeVerificationResult(false, "User code expired", null);
            }

            if (Instant.now().isAfter(request.expiresAt)) {
                cleanupExpiredCode(deviceCode, userCode);
                return new UserCodeVerificationResult(false, "User code expired", null);
            }

            if (request.status == DeviceAuthorizationStatus.APPROVED) {
                return new UserCodeVerificationResult(false, "User code already used", null);
            }

            return new UserCodeVerificationResult(
                true,
                "Valid user code",
                new UserCodeInfo(request.clientId, request.scopes, request.createdAt)
            );
        }

        /**
         * Approve device authorization (user approves on secondary device)
         */
        public void approveDeviceAuthorization(String userCode, String userId) {
            String deviceCode = userCodeToDeviceCode.get(userCode);
            if (deviceCode == null) {
                throw new IllegalArgumentException("Invalid user code");
            }

            DeviceAuthorizationRequest request = deviceCodeStore.get(deviceCode);
            if (request == null) {
                throw new IllegalArgumentException("User code expired");
            }

            request.status = DeviceAuthorizationStatus.APPROVED;
            request.userId = userId;
            request.approvedAt = Instant.now();
        }

        /**
         * Deny device authorization
         */
        public void denyDeviceAuthorization(String userCode) {
            String deviceCode = userCodeToDeviceCode.get(userCode);
            if (deviceCode == null) {
                throw new IllegalArgumentException("Invalid user code");
            }

            DeviceAuthorizationRequest request = deviceCodeStore.get(deviceCode);
            if (request != null) {
                request.status = DeviceAuthorizationStatus.DENIED;
            }
        }

        /**
         * Poll for device authorization status (device polls this endpoint)
         */
        public DeviceTokenResponse pollDeviceAuthorization(String clientId, String deviceCode) {
            DeviceAuthorizationRequest request = deviceCodeStore.get(deviceCode);
            if (request == null) {
                throw new DeviceAuthorizationException("expired_token", "The device code has expired");
            }

            if (!request.clientId.equals(clientId)) {
                throw new DeviceAuthorizationException("invalid_client", "Client mismatch");
            }

            if (Instant.now().isAfter(request.expiresAt)) {
                cleanupExpiredCode(deviceCode, request.userCode);
                throw new DeviceAuthorizationException("expired_token", "The device code has expired");
            }

            // Check polling rate
            DevicePollingInfo pollInfo = pollingInfo.get(deviceCode);
            if (pollInfo != null) {
                Duration timeSinceLastPoll = Duration.between(pollInfo.lastPollTime, Instant.now());
                if (timeSinceLastPoll.toSeconds() < DEVICE_CODE_INTERVAL) {
                    pollInfo.pollCount++;
                    if (pollInfo.pollCount > 10) {
                        throw new DeviceAuthorizationException("slow_down", 
                            "Polling too frequently. Wait " + (DEVICE_CODE_INTERVAL * 2) + " seconds");
                    }
                    throw new DeviceAuthorizationException("authorization_pending", 
                        "User has not yet authorized the device");
                }
                pollInfo.lastPollTime = Instant.now();
                pollInfo.pollCount = 0;
            }

            switch (request.status) {
                case PENDING:
                    throw new DeviceAuthorizationException("authorization_pending", 
                        "User has not yet authorized the device");
                
                case DENIED:
                    cleanupExpiredCode(deviceCode, request.userCode);
                    throw new DeviceAuthorizationException("access_denied", 
                        "User denied the authorization request");
                
                case APPROVED:
                    // Issue tokens
                    String accessToken = UUID.randomUUID().toString();
                    Instant now = Instant.now();
                    Duration tokenLifetime = Duration.ofHours(1);
                    
                    cleanupExpiredCode(deviceCode, request.userCode);
                    
                    return new DeviceTokenResponse(
                        accessToken,
                        "Bearer",
                        tokenLifetime.toSeconds(),
                        request.scopes
                    );
                
                default:
                    throw new DeviceAuthorizationException("invalid_request", "Invalid authorization state");
            }
        }

        /**
         * Get device authorization statistics
         */
        public DeviceAuthorizationStatistics getStatistics() {
            long pending = deviceCodeStore.values().stream()
                .filter(req -> req.status == DeviceAuthorizationStatus.PENDING)
                .count();
            
            long approved = deviceCodeStore.values().stream()
                .filter(req -> req.status == DeviceAuthorizationStatus.APPROVED)
                .count();
            
            long denied = deviceCodeStore.values().stream()
                .filter(req -> req.status == DeviceAuthorizationStatus.DENIED)
                .count();

            return new DeviceAuthorizationStatistics(
                deviceCodeStore.size(),
                pending,
                approved,
                denied
            );
        }

        private String generateDeviceCode() {
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        private String generateUserCode() {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < USER_CODE_LENGTH; i++) {
                if (i == USER_CODE_LENGTH / 2) {
                    code.append('-'); // Add separator in middle
                }
                int index = secureRandom.nextInt(USER_CODE_CHARS.length());
                code.append(USER_CODE_CHARS.charAt(index));
            }
            return code.toString();
        }

        private void cleanupExpiredCode(String deviceCode, String userCode) {
            deviceCodeStore.remove(deviceCode);
            userCodeToDeviceCode.remove(userCode);
            pollingInfo.remove(deviceCode);
        }
    }

    /**
     * REST Controller for Device Authorization operations
     */
    @RestController
    @RequestMapping("/api/device-authorization")
    public static class DeviceAuthorizationController {

        private final DeviceAuthorizationService deviceAuthService;

        public DeviceAuthorizationController(DeviceAuthorizationService deviceAuthService) {
            this.deviceAuthService = deviceAuthService;
        }

        /**
         * Initiate device authorization (device endpoint)
         */
        @PostMapping("/authorize")
        public DeviceAuthorizationResponse initiateAuthorization(@RequestBody DeviceAuthRequest request) {
            return deviceAuthService.initiateDeviceAuthorization(request.clientId, request.scopes);
        }

        /**
         * Verify user code (user endpoint on secondary device)
         */
        @PostMapping("/verify")
        public UserCodeVerificationResult verifyCode(@RequestBody VerifyCodeRequest request) {
            return deviceAuthService.verifyUserCode(request.userCode);
        }

        /**
         * Approve device authorization (user endpoint)
         */
        @PostMapping("/approve")
        public ApprovalResponse approveDevice(@RequestBody ApprovalRequest request) {
            deviceAuthService.approveDeviceAuthorization(request.userCode, request.userId);
            return new ApprovalResponse(true, "Device authorized successfully");
        }

        /**
         * Deny device authorization (user endpoint)
         */
        @PostMapping("/deny")
        public ApprovalResponse denyDevice(@RequestBody DenyRequest request) {
            deviceAuthService.denyDeviceAuthorization(request.userCode);
            return new ApprovalResponse(true, "Device authorization denied");
        }

        /**
         * Poll for token (device endpoint)
         */
        @PostMapping("/token")
        public DeviceTokenResponse pollToken(@RequestBody DeviceTokenRequest request) {
            return deviceAuthService.pollDeviceAuthorization(request.clientId, request.deviceCode);
        }

        /**
         * Get device authorization statistics
         */
        @GetMapping("/statistics")
        public DeviceAuthorizationStatistics getStatistics() {
            return deviceAuthService.getStatistics();
        }

        /**
         * Get pattern information
         */
        @GetMapping("/info")
        public PatternInfo getInfo() {
            return new PatternInfo(
                "Device Authorization Grant Pattern (RFC 8628)",
                "OAuth 2.0 flow for input-constrained devices like Smart TVs and IoT devices",
                List.of(
                    "POST /api/device-authorization/authorize - Initiate device authorization",
                    "POST /api/device-authorization/verify - Verify user code",
                    "POST /api/device-authorization/approve - Approve device",
                    "POST /api/device-authorization/deny - Deny device",
                    "POST /api/device-authorization/token - Poll for token",
                    "GET /api/device-authorization/statistics - Get statistics",
                    "GET /api/device-authorization/info - Get pattern information"
                ),
                Map.of(
                    "grant_type", "urn:ietf:params:oauth:grant-type:device_code",
                    "use_cases", List.of("Smart TVs", "IoT devices", "CLI tools", "Gaming consoles"),
                    "polling_interval", "5 seconds",
                    "code_lifetime", "15 minutes"
                )
            );
        }

        @ExceptionHandler(DeviceAuthorizationException.class)
        public DeviceErrorResponse handleDeviceAuthError(DeviceAuthorizationException ex) {
            return new DeviceErrorResponse(ex.error, ex.errorDescription);
        }
    }

    // DTOs
    public record DeviceAuthRequest(String clientId, Set<String> scopes) {}
    public record VerifyCodeRequest(String userCode) {}
    public record ApprovalRequest(String userCode, String userId) {}
    public record DenyRequest(String userCode) {}
    public record DeviceTokenRequest(String clientId, String deviceCode) {}
    public record ApprovalResponse(boolean success, String message) {}
    public record DeviceErrorResponse(String error, String errorDescription) {}
    public record PatternInfo(String name, String description, List<String> endpoints, Map<String, Object> details) {}

    // Domain Objects
    public record DeviceAuthorizationResponse(String deviceCode, String userCode, String verificationUri,
                                             String verificationUriComplete, long expiresIn, int interval) {}

    public record DeviceTokenResponse(String accessToken, String tokenType, long expiresIn, Set<String> scopes) {}

    public record UserCodeVerificationResult(boolean valid, String message, UserCodeInfo info) {}

    public record UserCodeInfo(String clientId, Set<String> scopes, Instant requestTime) {}

    public enum DeviceAuthorizationStatus {
        PENDING, APPROVED, DENIED
    }

    public static class DeviceAuthorizationRequest {
        public final String deviceCode;
        public final String userCode;
        public final String clientId;
        public final Set<String> scopes;
        public final Instant createdAt;
        public final Instant expiresAt;
        public DeviceAuthorizationStatus status;
        public String userId;
        public Instant approvedAt;

        public DeviceAuthorizationRequest(String deviceCode, String userCode, String clientId, 
                                        Set<String> scopes, Instant createdAt, Instant expiresAt,
                                        DeviceAuthorizationStatus status) {
            this.deviceCode = deviceCode;
            this.userCode = userCode;
            this.clientId = clientId;
            this.scopes = scopes;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.status = status;
        }
    }

    public static class DevicePollingInfo {
        public Instant lastPollTime;
        public int pollCount;

        public DevicePollingInfo(Instant lastPollTime, int pollCount) {
            this.lastPollTime = lastPollTime;
            this.pollCount = pollCount;
        }
    }

    public static class DeviceAuthorizationException extends RuntimeException {
        public final String error;
        public final String errorDescription;

        public DeviceAuthorizationException(String error, String errorDescription) {
            super(errorDescription);
            this.error = error;
            this.errorDescription = errorDescription;
        }
    }

    public record DeviceAuthorizationStatistics(long totalRequests, long pending, long approved, long denied) {}
}
