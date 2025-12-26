package com.example.authserver;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Spring Authorization Server - Complete Patterns
 * 
 * Covers all OAuth 2.1 / OIDC patterns:
 * 1. Client Credentials Grant
 * 2. Refresh Token Grant
 * 3. Device Authorization Grant
 * 4. PKCE (Proof Key for Code Exchange)
 * 5. Token Introspection
 * 6. Token Revocation
 * 7. Client Authentication
 * 8. Consent Page (detailed)
 * 9. Authorization Server Settings
 * 
 * @author Spring Patterns
 */

@Data
class ClientConfig {
    private String clientId;
    private String clientSecret;
    private List<String> grantTypes;
    private List<String> scopes;
    private List<String> redirectUris;
}

@Data
class TokenInfo {
    private boolean active;
    private String scope;
    private String clientId;
    private String username;
    private Long exp;
}

/**
 * 1. Client Credentials Grant Pattern
 * Machine-to-machine authentication
 */
@Service
@Slf4j
class ClientCredentialsService {
    
    public RegisteredClient createClientCredentialsClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("service-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())
                .build();
    }
    
    public String getGrantInfo() {
        return """
                Client Credentials Grant
                ========================
                
                Use Case:
                - Machine-to-machine (M2M) communication
                - Service-to-service authentication
                - Backend services
                - API access without user context
                
                Flow:
                1. Client authenticates with credentials
                2. Server validates credentials
                3. Server issues access token
                4. Client uses token for API access
                
                Token Request:
                POST /oauth2/token
                Content-Type: application/x-www-form-urlencoded
                Authorization: Basic <client_id:client_secret>
                
                grant_type=client_credentials
                &scope=read write
                
                Response:
                {
                  "access_token": "...",
                  "token_type": "Bearer",
                  "expires_in": 1800,
                  "scope": "read write"
                }
                """;
    }
}

/**
 * 2. Refresh Token Grant Pattern
 */
@Service
@Slf4j
class RefreshTokenService {
    
    public RegisteredClient createClientWithRefreshToken() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("web-app")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://app.example.com/callback")
                .scope("openid")
                .scope("profile")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .reuseRefreshTokens(false) // Rotate refresh tokens
                        .build())
                .build();
    }
    
    public String getGrantInfo() {
        return """
                Refresh Token Grant
                ===================
                
                Use Case:
                - Obtain new access token without re-authentication
                - Long-lived sessions
                - Mobile/SPA applications
                
                Flow:
                1. Use refresh token to request new access token
                2. Server validates refresh token
                3. Server issues new access token (and optionally new refresh token)
                
                Token Request:
                POST /oauth2/token
                Content-Type: application/x-www-form-urlencoded
                
                grant_type=refresh_token
                &refresh_token=<refresh_token>
                &client_id=web-app
                &client_secret=secret
                
                Response:
                {
                  "access_token": "new_token",
                  "token_type": "Bearer",
                  "expires_in": 900,
                  "refresh_token": "new_refresh_token",
                  "scope": "openid profile"
                }
                
                Security:
                - Rotate refresh tokens (reuseRefreshTokens=false)
                - Limit refresh token lifetime
                - Detect token reuse
                - Revoke on suspicious activity
                """;
    }
}

/**
 * 3. Device Authorization Grant Pattern
 * For devices with limited input (smart TVs, IoT)
 */
@Service
@Slf4j
class DeviceAuthorizationService {
    
    public RegisteredClient createDeviceClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("device-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client
                .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:device_code"))
                .scope("device:read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .build();
    }
    
    public String getGrantInfo() {
        return """
                Device Authorization Grant (OAuth 2.0 Device Flow)
                ==================================================
                
                Use Case:
                - Smart TVs, streaming devices
                - IoT devices
                - CLI tools
                - Devices with limited input
                
                Flow:
                1. Device requests device code
                   POST /oauth2/device_authorization
                   client_id=device-client
                
                2. Server responds with:
                   {
                     "device_code": "...",
                     "user_code": "ABCD-1234",
                     "verification_uri": "https://example.com/device",
                     "expires_in": 1800
                   }
                
                3. Device shows user_code to user
                4. User visits verification_uri on another device
                5. User enters user_code and authorizes
                6. Device polls for token:
                   POST /oauth2/token
                   grant_type=urn:ietf:params:oauth:grant-type:device_code
                   &device_code=...
                   &client_id=device-client
                
                7. Once authorized, server returns access token
                
                Polling:
                - Device polls every 5 seconds
                - Returns authorization_pending until authorized
                - Returns slow_down if polling too fast
                """;
    }
}

/**
 * 4. PKCE Pattern (Proof Key for Code Exchange)
 * Enhanced security for public clients
 */
@Service
@Slf4j
class PKCEService {
    
    public RegisteredClient createPKCEClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("mobile-app")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("com.example.app://callback")
                .scope("openid")
                .scope("profile")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true) // Require PKCE
                        .build())
                .build();
    }
    
    public String getGrantInfo() {
        return """
                PKCE (Proof Key for Code Exchange)
                ===================================
                
                Use Case:
                - Mobile applications
                - Single Page Applications (SPAs)
                - Public clients (no client secret)
                - Prevent authorization code interception
                
                Flow:
                1. Client generates code_verifier (random string)
                2. Client creates code_challenge = SHA256(code_verifier)
                3. Authorization request includes:
                   /oauth2/authorize?
                     client_id=mobile-app
                     &response_type=code
                     &redirect_uri=com.example.app://callback
                     &code_challenge=...
                     &code_challenge_method=S256
                
                4. User authorizes, server returns code
                5. Token request includes code_verifier:
                   POST /oauth2/token
                   grant_type=authorization_code
                   &code=...
                   &client_id=mobile-app
                   &redirect_uri=com.example.app://callback
                   &code_verifier=<original_verifier>
                
                6. Server verifies: SHA256(code_verifier) == code_challenge
                7. If match, returns access token
                
                Security Benefits:
                - Prevents authorization code interception
                - No client secret required
                - Mitigates CSRF attacks
                - Required for public clients (OAuth 2.1)
                """;
    }
}

/**
 * 5. Token Introspection Pattern
 * Validate and get token information
 */
@Service
@Slf4j
class TokenIntrospectionService {
    
    public String getIntrospectionInfo() {
        return """
                Token Introspection
                ===================
                
                Use Case:
                - Validate access tokens
                - Get token metadata
                - Check token status
                - Resource server validation
                
                Request:
                POST /oauth2/introspect
                Authorization: Basic <client_credentials>
                Content-Type: application/x-www-form-urlencoded
                
                token=<access_token>
                &token_type_hint=access_token
                
                Response (Active Token):
                {
                  "active": true,
                  "scope": "read write",
                  "client_id": "web-app",
                  "username": "user@example.com",
                  "token_type": "Bearer",
                  "exp": 1234567890,
                  "iat": 1234567000,
                  "sub": "user-123",
                  "aud": ["api.example.com"]
                }
                
                Response (Inactive Token):
                {
                  "active": false
                }
                
                Use Cases:
                - Resource servers validate tokens
                - API gateways check permissions
                - Logging and auditing
                - Token debugging
                """;
    }
}

/**
 * 6. Token Revocation Pattern
 * Revoke access/refresh tokens
 */
@Service
@Slf4j
class TokenRevocationService {
    
    public String getRevocationInfo() {
        return """
                Token Revocation
                ================
                
                Use Case:
                - User logout
                - Compromised tokens
                - Session termination
                - Security incidents
                
                Request:
                POST /oauth2/revoke
                Authorization: Basic <client_credentials>
                Content-Type: application/x-www-form-urlencoded
                
                token=<token>
                &token_type_hint=access_token
                
                Response:
                HTTP 200 OK
                (No response body)
                
                Notes:
                - Revoking refresh token revokes all associated access tokens
                - Revoking access token only revokes that specific token
                - Client must be authorized to revoke the token
                - Idempotent operation (can revoke multiple times)
                
                Best Practices:
                - Implement token blacklist/cache
                - Revoke all tokens on password change
                - Monitor revocation patterns
                - Log revocation events
                """;
    }
}

/**
 * 7. Client Authentication Pattern
 * Various methods to authenticate clients
 */
@Service
@Slf4j
class ClientAuthenticationService {
    
    public String getAuthenticationInfo() {
        return """
                Client Authentication Methods
                =============================
                
                1. CLIENT_SECRET_BASIC
                   Authorization: Basic base64(client_id:client_secret)
                   - Most common
                   - HTTP Basic authentication
                
                2. CLIENT_SECRET_POST
                   POST body: client_id=...&client_secret=...
                   - Client credentials in form data
                
                3. CLIENT_SECRET_JWT
                   - Client creates JWT signed with shared secret
                   - JWT in client_assertion parameter
                   - More secure than plain secret
                
                4. PRIVATE_KEY_JWT
                   - Client creates JWT signed with private key
                   - Server verifies with public key
                   - Most secure method
                   - No shared secrets
                
                5. NONE
                   - Public clients (mobile apps, SPAs)
                   - No authentication
                   - MUST use PKCE
                
                Configuration:
                RegisteredClient.builder()
                    .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(
                        ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                    .build();
                """;
    }
}

/**
 * 8. Consent Page Pattern (Detailed)
 * User consent for authorization
 */
@Service
@Slf4j
class ConsentPageService {
    
    public String getConsentInfo() {
        return """
                Consent Page Pattern
                ====================
                
                Purpose:
                - Show user what permissions app requests
                - Allow user to approve/deny
                - Record user consent
                - Support scope selection
                
                Flow:
                1. User authenticates
                2. Server checks if consent required
                3. If required, redirect to consent page
                4. Show:
                   - Client name and description
                   - Requested scopes
                   - Previous consents
                   - Approve/Deny buttons
                5. User approves/denies
                6. Server records consent
                7. Continue authorization flow
                
                Configuration:
                ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .build();
                
                Consent Page Content:
                - Client application name
                - Client description/purpose
                - Requested permissions (scopes)
                - Data access explanation
                - Duration of access
                - Ability to revoke later
                
                Best Practices:
                - Clear, plain language
                - Granular scope selection
                - Remember consent (per client/scope)
                - Allow consent revocation
                - Audit consent decisions
                """;
    }
}

/**
 * 9. Authorization Server Settings Pattern
 * Global server configuration
 */
@Service
@Slf4j
class AuthorizationServerSettingsService {
    
    public String getSettingsInfo() {
        return """
                Authorization Server Settings
                ==============================
                
                Core Settings:
                1. Issuer URL
                   - Base URL of authorization server
                   - Used in tokens (iss claim)
                   - Example: https://auth.example.com
                
                2. Endpoints
                   - Authorization: /oauth2/authorize
                   - Token: /oauth2/token
                   - JWK Set: /oauth2/jwks
                   - Introspection: /oauth2/introspect
                   - Revocation: /oauth2/revoke
                   - Device Authorization: /oauth2/device_authorization
                   - User Info: /userinfo
                
                3. Token Settings (Global)
                   - Access token TTL (default: 5 min)
                   - Refresh token TTL (default: 60 min)
                   - Authorization code TTL (default: 5 min)
                   - Device code TTL (default: 5 min)
                
                4. Security Settings
                   - Token format (JWT, opaque)
                   - Signing algorithm (RS256, etc.)
                   - Key rotation policy
                   - CORS configuration
                
                Configuration:
                @Bean
                AuthorizationServerSettings settings() {
                    return AuthorizationServerSettings.builder()
                        .issuer("https://auth.example.com")
                        .authorizationEndpoint("/oauth2/authorize")
                        .tokenEndpoint("/oauth2/token")
                        .jwkSetEndpoint("/oauth2/jwks")
                        .tokenIntrospectionEndpoint("/oauth2/introspect")
                        .tokenRevocationEndpoint("/oauth2/revoke")
                        .build();
                }
                
                Discovery:
                GET /.well-known/oauth-authorization-server
                Returns server metadata (RFC 8414)
                """;
    }
}

/**
 * REST Controller for all Authorization Server patterns
 */
@RestController
@RequestMapping("/oauth2/patterns")
@Slf4j
class AuthorizationServerPatternsController {
    
    private final ClientCredentialsService clientCredentialsService;
    private final RefreshTokenService refreshTokenService;
    private final DeviceAuthorizationService deviceService;
    private final PKCEService pkceService;
    private final TokenIntrospectionService introspectionService;
    private final TokenRevocationService revocationService;
    private final ClientAuthenticationService clientAuthService;
    private final ConsentPageService consentService;
    private final AuthorizationServerSettingsService settingsService;
    
    public AuthorizationServerPatternsController(
            ClientCredentialsService clientCredentialsService,
            RefreshTokenService refreshTokenService,
            DeviceAuthorizationService deviceService,
            PKCEService pkceService,
            TokenIntrospectionService introspectionService,
            TokenRevocationService revocationService,
            ClientAuthenticationService clientAuthService,
            ConsentPageService consentService,
            AuthorizationServerSettingsService settingsService) {
        this.clientCredentialsService = clientCredentialsService;
        this.refreshTokenService = refreshTokenService;
        this.deviceService = deviceService;
        this.pkceService = pkceService;
        this.introspectionService = introspectionService;
        this.revocationService = revocationService;
        this.clientAuthService = clientAuthService;
        this.consentService = consentService;
        this.settingsService = settingsService;
    }
    
    @GetMapping("/client-credentials")
    public String getClientCredentialsInfo() {
        return clientCredentialsService.getGrantInfo();
    }
    
    @GetMapping("/refresh-token")
    public String getRefreshTokenInfo() {
        return refreshTokenService.getGrantInfo();
    }
    
    @GetMapping("/device-authorization")
    public String getDeviceAuthorizationInfo() {
        return deviceService.getGrantInfo();
    }
    
    @GetMapping("/pkce")
    public String getPKCEInfo() {
        return pkceService.getGrantInfo();
    }
    
    @GetMapping("/introspection")
    public String getIntrospectionInfo() {
        return introspectionService.getIntrospectionInfo();
    }
    
    @GetMapping("/revocation")
    public String getRevocationInfo() {
        return revocationService.getRevocationInfo();
    }
    
    @GetMapping("/client-authentication")
    public String getClientAuthenticationInfo() {
        return clientAuthService.getAuthenticationInfo();
    }
    
    @GetMapping("/consent")
    public String getConsentInfo() {
        return consentService.getConsentInfo();
    }
    
    @GetMapping("/settings")
    public String getSettingsInfo() {
        return settingsService.getSettingsInfo();
    }
}

@SpringBootApplication
public class AuthorizationServerCompletePatterns {
    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerCompletePatterns.class, args);
    }
}
