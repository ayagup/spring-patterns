package com.example.security.oauth2client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

/**
 * OAuth2 Client Pattern
 * 
 * Demonstrates:
 * - OAuth2 client configuration
 * - Authorized client management
 * - Token exchange and refresh
 * - REST API calls with OAuth2 tokens
 * - Client credentials flow
 * - Authorization code flow
 * 
 * Use Cases:
 * - Service-to-service communication
 * - API integration with OAuth2 providers
 * - Token management and caching
 * - Automatic token refresh
 * 
 * Dependencies:
 * - spring-boot-starter-oauth2-client
 * - spring-boot-starter-webflux (for WebClient)
 */

@SpringBootApplication
public class OAuth2ClientPattern {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2ClientPattern.class, args);
    }
}

@Configuration
class OAuth2ClientConfig {
    
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {
        
        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .password()
                .build();
        
        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
            new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository);
        
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        
        return authorizedClientManager;
    }
    
    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        
        oauth2Client.setDefaultOAuth2AuthorizedClient(true);
        
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .build();
    }
}

@RestController
@RequestMapping("/api/oauth2-client")
class OAuth2ClientController {
    
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final WebClient webClient;
    private final OAuth2AuthorizedClientService authorizedClientService;
    
    public OAuth2ClientController(
            OAuth2AuthorizedClientManager authorizedClientManager,
            WebClient webClient,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientManager = authorizedClientManager;
        this.webClient = webClient;
        this.authorizedClientService = authorizedClientService;
    }
    
    @GetMapping("/call-api/{registrationId}")
    public ResponseEntity<ApiCallResult> callProtectedApi(
            @PathVariable String registrationId,
            @RequestParam String apiUrl) {
        
        try {
            String response = webClient
                .get()
                .uri(apiUrl)
                .attributes(attrs -> attrs.put(
                    "oauth2_authorized_client_registration_id", registrationId))
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(10));
            
            return ResponseEntity.ok(new ApiCallResult(
                true,
                registrationId,
                apiUrl,
                response,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiCallResult(
                false,
                registrationId,
                apiUrl,
                null,
                e.getMessage()
            ));
        }
    }
    
    @GetMapping("/token/{registrationId}")
    public ResponseEntity<ClientTokenInfo> getClientToken(
            @PathVariable String registrationId,
            @RequestParam String principalName) {
        
        OAuth2AuthorizedClient authorizedClient =
            authorizedClientService.loadAuthorizedClient(registrationId, principalName);
        
        if (authorizedClient == null) {
            return ResponseEntity.notFound().build();
        }
        
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        
        ClientTokenInfo tokenInfo = new ClientTokenInfo(
            registrationId,
            principalName,
            accessToken.getTokenValue().substring(0, 20) + "...",
            accessToken.getTokenType().getValue(),
            accessToken.getScopes(),
            accessToken.getIssuedAt(),
            accessToken.getExpiresAt(),
            authorizedClient.getRefreshToken() != null
        );
        
        return ResponseEntity.ok(tokenInfo);
    }
    
    @PostMapping("/refresh/{registrationId}")
    public ResponseEntity<RefreshResult> refreshToken(
            @PathVariable String registrationId,
            @RequestParam String principalName) {
        
        OAuth2AuthorizedClient authorizedClient =
            authorizedClientService.loadAuthorizedClient(registrationId, principalName);
        
        if (authorizedClient == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (authorizedClient.getRefreshToken() == null) {
            return ResponseEntity.ok(new RefreshResult(
                false,
                "No refresh token available",
                null
            ));
        }
        
        try {
            // Trigger token refresh by creating authorization request
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(principalName)
                .build();
            
            OAuth2AuthorizedClient refreshedClient =
                authorizedClientManager.authorize(authorizeRequest);
            
            if (refreshedClient != null) {
                return ResponseEntity.ok(new RefreshResult(
                    true,
                    "Token refreshed successfully",
                    refreshedClient.getAccessToken().getExpiresAt()
                ));
            } else {
                return ResponseEntity.ok(new RefreshResult(
                    false,
                    "Failed to refresh token",
                    null
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new RefreshResult(
                false,
                e.getMessage(),
                null
            ));
        }
    }
    
    @GetMapping("/clients")
    public ResponseEntity<List<ClientSummary>> getAuthorizedClients(
            @RequestParam String principalName) {
        
        List<ClientSummary> clients = new ArrayList<>();
        
        // Note: In production, track client registration IDs
        String[] registrationIds = {"google", "github", "facebook"};
        
        for (String registrationId : registrationIds) {
            OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(registrationId, principalName);
            
            if (client != null) {
                clients.add(new ClientSummary(
                    registrationId,
                    client.getClientRegistration().getClientName(),
                    client.getAccessToken().getExpiresAt(),
                    client.getRefreshToken() != null
                ));
            }
        }
        
        return ResponseEntity.ok(clients);
    }
}

class ApiCallResult {
    private boolean success;
    private String clientId;
    private String apiUrl;
    private String response;
    private String error;
    
    public ApiCallResult(boolean success, String clientId, String apiUrl,
                        String response, String error) {
        this.success = success;
        this.clientId = clientId;
        this.apiUrl = apiUrl;
        this.response = response;
        this.error = error;
    }
    
    public boolean isSuccess() { return success; }
    public String getClientId() { return clientId; }
    public String getApiUrl() { return apiUrl; }
    public String getResponse() { return response; }
    public String getError() { return error; }
}

class ClientTokenInfo {
    private String registrationId;
    private String principalName;
    private String accessToken;
    private String tokenType;
    private Set<String> scopes;
    private Object issuedAt;
    private Object expiresAt;
    private boolean hasRefreshToken;
    
    public ClientTokenInfo(String registrationId, String principalName, String accessToken,
                          String tokenType, Set<String> scopes, Object issuedAt,
                          Object expiresAt, boolean hasRefreshToken) {
        this.registrationId = registrationId;
        this.principalName = principalName;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.scopes = scopes;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.hasRefreshToken = hasRefreshToken;
    }
    
    public String getRegistrationId() { return registrationId; }
    public String getPrincipalName() { return principalName; }
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Set<String> getScopes() { return scopes; }
    public Object getIssuedAt() { return issuedAt; }
    public Object getExpiresAt() { return expiresAt; }
    public boolean isHasRefreshToken() { return hasRefreshToken; }
}

class RefreshResult {
    private boolean success;
    private String message;
    private Object newExpiresAt;
    
    public RefreshResult(boolean success, String message, Object newExpiresAt) {
        this.success = success;
        this.message = message;
        this.newExpiresAt = newExpiresAt;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getNewExpiresAt() { return newExpiresAt; }
}

class ClientSummary {
    private String registrationId;
    private String clientName;
    private Object expiresAt;
    private boolean hasRefreshToken;
    
    public ClientSummary(String registrationId, String clientName,
                        Object expiresAt, boolean hasRefreshToken) {
        this.registrationId = registrationId;
        this.clientName = clientName;
        this.expiresAt = expiresAt;
        this.hasRefreshToken = hasRefreshToken;
    }
    
    public String getRegistrationId() { return registrationId; }
    public String getClientName() { return clientName; }
    public Object getExpiresAt() { return expiresAt; }
    public boolean isHasRefreshToken() { return hasRefreshToken; }
}
