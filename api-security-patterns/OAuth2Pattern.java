package com.example.security.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Pattern
 * 
 * Demonstrates:
 * - OAuth2 authentication and authorization
 * - OAuth2 login integration
 * - Client registration configuration
 * - Access token management
 * - OAuth2 user principal handling
 * - Multiple OAuth2 providers
 * 
 * OAuth2 Flow:
 * 1. User initiates login
 * 2. Redirect to OAuth2 provider
 * 3. User authenticates with provider
 * 4. Provider redirects back with authorization code
 * 5. Exchange code for access token
 * 6. Access protected resources
 * 
 * Dependencies:
 * - spring-boot-starter-oauth2-client
 * - spring-boot-starter-security
 */

@SpringBootApplication
public class OAuth2Pattern {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2Pattern.class, args);
    }
}

@Configuration
@EnableWebSecurity
class OAuth2SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login**", "/error**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }
    
    /**
     * Configure OAuth2 client registrations
     * In production, use application.yml configuration
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            googleClientRegistration(),
            githubClientRegistration(),
            facebookClientRegistration()
        );
    }
    
    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
            .clientId("google-client-id")
            .clientSecret("google-client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("sub")
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .clientName("Google")
            .build();
    }
    
    private ClientRegistration githubClientRegistration() {
        return ClientRegistration.withRegistrationId("github")
            .clientId("github-client-id")
            .clientSecret("github-client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("read:user", "user:email")
            .authorizationUri("https://github.com/login/oauth/authorize")
            .tokenUri("https://github.com/login/oauth/access_token")
            .userInfoUri("https://api.github.com/user")
            .userNameAttributeName("id")
            .clientName("GitHub")
            .build();
    }
    
    private ClientRegistration facebookClientRegistration() {
        return ClientRegistration.withRegistrationId("facebook")
            .clientId("facebook-client-id")
            .clientSecret("facebook-client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("public_profile", "email")
            .authorizationUri("https://www.facebook.com/v12.0/dialog/oauth")
            .tokenUri("https://graph.facebook.com/v12.0/oauth/access_token")
            .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture")
            .userNameAttributeName("id")
            .clientName("Facebook")
            .build();
    }
}

@RestController
@RequestMapping("/api/oauth2")
class OAuth2Controller {
    
    @GetMapping("/user")
    public ResponseEntity<OAuth2UserInfo> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        OAuth2UserInfo userInfo = new OAuth2UserInfo(
            principal.getAttribute("name"),
            principal.getAttribute("email"),
            principal.getAttribute("picture"),
            principal.getAttributes()
        );
        
        return ResponseEntity.ok(userInfo);
    }
    
    @GetMapping("/user-details")
    public ResponseEntity<UserDetails> getUserDetails(
            @AuthenticationPrincipal OAuth2User principal,
            Authentication authentication) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        UserDetails details = new UserDetails(
            principal.getName(),
            authentication.getAuthorities().toString(),
            authentication.isAuthenticated(),
            authentication.getPrincipal().getClass().getSimpleName(),
            principal.getAttributes()
        );
        
        return ResponseEntity.ok(details);
    }
    
    @GetMapping("/token-info")
    public ResponseEntity<TokenInfo> getTokenInfo(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        
        if (authorizedClient == null) {
            return ResponseEntity.status(401).build();
        }
        
        TokenInfo tokenInfo = new TokenInfo(
            authorizedClient.getClientRegistration().getRegistrationId(),
            authorizedClient.getClientRegistration().getClientName(),
            authorizedClient.getAccessToken().getTokenValue(),
            authorizedClient.getAccessToken().getTokenType().getValue(),
            authorizedClient.getAccessToken().getScopes(),
            authorizedClient.getAccessToken().getIssuedAt(),
            authorizedClient.getAccessToken().getExpiresAt(),
            authorizedClient.getRefreshToken() != null ? 
                authorizedClient.getRefreshToken().getTokenValue() : null
        );
        
        return ResponseEntity.ok(tokenInfo);
    }
    
    @GetMapping("/client-registrations")
    public ResponseEntity<Map<String, ClientInfo>> getClientRegistrations(
            ClientRegistrationRepository clientRegistrationRepository) {
        
        Map<String, ClientInfo> clients = new HashMap<>();
        
        // Note: InMemoryClientRegistrationRepository doesn't expose iteration
        // In production, you'd track registered client IDs
        String[] clientIds = {"google", "github", "facebook"};
        
        for (String clientId : clientIds) {
            try {
                ClientRegistration registration = 
                    clientRegistrationRepository.findByRegistrationId(clientId);
                
                if (registration != null) {
                    clients.put(clientId, new ClientInfo(
                        registration.getRegistrationId(),
                        registration.getClientName(),
                        registration.getAuthorizationGrantType().getValue(),
                        registration.getScopes(),
                        registration.getRedirectUri()
                    ));
                }
            } catch (Exception e) {
                // Client not found
            }
        }
        
        return ResponseEntity.ok(clients);
    }
}

class OAuth2UserInfo {
    private String name;
    private String email;
    private String picture;
    private Map<String, Object> attributes;
    
    public OAuth2UserInfo(String name, String email, String picture, 
                         Map<String, Object> attributes) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.attributes = attributes;
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPicture() { return picture; }
    public Map<String, Object> getAttributes() { return attributes; }
}

class UserDetails {
    private String username;
    private String authorities;
    private boolean authenticated;
    private String principalType;
    private Map<String, Object> attributes;
    
    public UserDetails(String username, String authorities, boolean authenticated,
                      String principalType, Map<String, Object> attributes) {
        this.username = username;
        this.authorities = authorities;
        this.authenticated = authenticated;
        this.principalType = principalType;
        this.attributes = attributes;
    }
    
    public String getUsername() { return username; }
    public String getAuthorities() { return authorities; }
    public boolean isAuthenticated() { return authenticated; }
    public String getPrincipalType() { return principalType; }
    public Map<String, Object> getAttributes() { return attributes; }
}

class TokenInfo {
    private String registrationId;
    private String clientName;
    private String accessToken;
    private String tokenType;
    private Object scopes;
    private Object issuedAt;
    private Object expiresAt;
    private String refreshToken;
    
    public TokenInfo(String registrationId, String clientName, String accessToken,
                    String tokenType, Object scopes, Object issuedAt, 
                    Object expiresAt, String refreshToken) {
        this.registrationId = registrationId;
        this.clientName = clientName;
        this.accessToken = maskToken(accessToken);
        this.tokenType = tokenType;
        this.scopes = scopes;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.refreshToken = maskToken(refreshToken);
    }
    
    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 10) + "...";
    }
    
    public String getRegistrationId() { return registrationId; }
    public String getClientName() { return clientName; }
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Object getScopes() { return scopes; }
    public Object getIssuedAt() { return issuedAt; }
    public Object getExpiresAt() { return expiresAt; }
    public String getRefreshToken() { return refreshToken; }
}

class ClientInfo {
    private String registrationId;
    private String clientName;
    private String grantType;
    private Object scopes;
    private String redirectUri;
    
    public ClientInfo(String registrationId, String clientName, String grantType,
                     Object scopes, String redirectUri) {
        this.registrationId = registrationId;
        this.clientName = clientName;
        this.grantType = grantType;
        this.scopes = scopes;
        this.redirectUri = redirectUri;
    }
    
    public String getRegistrationId() { return registrationId; }
    public String getClientName() { return clientName; }
    public String getGrantType() { return grantType; }
    public Object getScopes() { return scopes; }
    public String getRedirectUri() { return redirectUri; }
}
