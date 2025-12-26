package com.example.authserver.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING AUTHORIZATION SERVER - AUTHORIZATION CODE GRANT PATTERN 💡
 * =====================================================================
 * 
 * OAuth2 Authorization Code Grant is the most secure flow for server-side
 * applications, using authorization code exchange for access tokens.
 * 
 * 🎯 KEY FEATURES:
 * - Authorization code exchange
 * - PKCE (Proof Key for Code Exchange)
 * - Refresh token support
 * - Client authentication
 * - Consent page
 * - Token revocation
 * 
 * 📦 AUTHORIZATION SERVER CONFIG:
 * ===============================
 * 
 * @Configuration
 * class AuthorizationServerConfig {
 *     @Bean
 *     fun authorizationServerSettings(): AuthorizationServerSettings {
 *         return AuthorizationServerSettings.builder()
 *             .issuer("https://auth.example.com")
 *             .authorizationEndpoint("/oauth2/authorize")
 *             .tokenEndpoint("/oauth2/token")
 *             .build()
 *     }
 *     
 *     @Bean
 *     fun registeredClientRepository(): RegisteredClientRepository {
 *         val client = RegisteredClient.withId(UUID.randomUUID().toString())
 *             .clientId("client-app")
 *             .clientSecret("{noop}secret")
 *             .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
 *             .redirectUri("http://localhost:8080/callback")
 *             .scope("read")
 *             .scope("write")
 *             .build()
 *         
 *         return InMemoryRegisteredClientRepository(client)
 *     }
 * }
 * 
 * 🔧 AUTHORIZATION FLOW:
 * ======================
 * 
 * 1. Client redirects to /oauth2/authorize
 *    GET /oauth2/authorize?
 *        response_type=code&
 *        client_id=client-app&
 *        redirect_uri=http://localhost:8080/callback&
 *        scope=read write&
 *        state=xyz
 * 
 * 2. User authenticates and grants consent
 * 
 * 3. Redirects back with authorization code
 *    http://localhost:8080/callback?code=ABC123&state=xyz
 * 
 * 4. Client exchanges code for tokens
 *    POST /oauth2/token
 *    grant_type=authorization_code&
 *    code=ABC123&
 *    redirect_uri=http://localhost:8080/callback&
 *    client_id=client-app&
 *    client_secret=secret
 * 
 * 5. Returns access token and refresh token
 *    {
 *      "access_token": "...",
 *      "token_type": "Bearer",
 *      "expires_in": 3600,
 *      "refresh_token": "...",
 *      "scope": "read write"
 *    }
 * 
 * 💡 PKCE SUPPORT:
 * ================
 * 
 * 1. Generate code verifier and challenge
 *    code_verifier = random(43-128 chars)
 *    code_challenge = BASE64URL(SHA256(code_verifier))
 * 
 * 2. Authorization request with PKCE
 *    GET /oauth2/authorize?
 *        ...&
 *        code_challenge=CHALLENGE&
 *        code_challenge_method=S256
 * 
 * 3. Token request with code verifier
 *    POST /oauth2/token
 *    ...&
 *    code_verifier=VERIFIER
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class AuthorizationCodeGrantPattern {
    public static void main(String[] args) {
        SpringApplication.run(AuthorizationCodeGrantPattern.class, args);
    }
}

@Service
class AuthorizationCodeGrantService {
    public Map<String, Object> getGrantInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("grant_type", "authorization_code");
        info.put("flow", "Authorization Code Grant");
        info.put("security", "Most secure OAuth2 flow");
        info.put("features", Arrays.asList(
            "Authorization code exchange",
            "PKCE support",
            "Refresh tokens",
            "Client authentication",
            "User consent"
        ));
        info.put("use_cases", Arrays.asList(
            "Server-side web apps",
            "Single Page Apps (SPA) with PKCE",
            "Mobile apps with PKCE",
            "Confidential clients"
        ));
        return info;
    }
    
    public Map<String, String> getFlowSteps() {
        Map<String, String> steps = new LinkedHashMap<>();
        steps.put("1. Authorization Request", "Client redirects to /oauth2/authorize");
        steps.put("2. User Authentication", "User logs in and grants consent");
        steps.put("3. Authorization Code", "Redirects back with code parameter");
        steps.put("4. Token Exchange", "POST to /oauth2/token with code");
        steps.put("5. Access Token", "Returns access_token and refresh_token");
        steps.put("6. API Access", "Use access_token in Authorization header");
        steps.put("7. Token Refresh", "Use refresh_token to get new access_token");
        return steps;
    }
    
    public Map<String, String> getPkceInfo() {
        Map<String, String> pkce = new LinkedHashMap<>();
        pkce.put("Purpose", "Prevents authorization code interception");
        pkce.put("Code Verifier", "Random string (43-128 characters)");
        pkce.put("Code Challenge", "BASE64URL(SHA256(code_verifier))");
        pkce.put("Challenge Method", "S256 (SHA-256) or plain");
        pkce.put("When to Use", "SPAs, mobile apps, public clients");
        pkce.put("Security", "Protects against PKCE downgrade attacks");
        return pkce;
    }
}

@RestController
@RequestMapping("/api/auth-server/authorization-code")
class AuthorizationCodeGrantController {
    private final AuthorizationCodeGrantService service;
    
    public AuthorizationCodeGrantController(AuthorizationCodeGrantService service) {
        this.service = service;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return service.getGrantInfo();
    }
    
    @GetMapping("/flow")
    public Map<String, String> getFlow() {
        return service.getFlowSteps();
    }
    
    @GetMapping("/pkce")
    public Map<String, String> getPkce() {
        return service.getPkceInfo();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ AUTHORIZATION REQUEST:
 * curl "http://localhost:9000/oauth2/authorize?\
 *   response_type=code&\
 *   client_id=client-app&\
 *   redirect_uri=http://localhost:8080/callback&\
 *   scope=read write&\
 *   state=xyz"
 * 
 * 2️⃣ TOKEN EXCHANGE:
 * curl -X POST http://localhost:9000/oauth2/token \
 *   -H "Content-Type: application/x-www-form-urlencoded" \
 *   -d "grant_type=authorization_code" \
 *   -d "code=ABC123" \
 *   -d "redirect_uri=http://localhost:8080/callback" \
 *   -d "client_id=client-app" \
 *   -d "client_secret=secret"
 * 
 * 3️⃣ REFRESH TOKEN:
 * curl -X POST http://localhost:9000/oauth2/token \
 *   -H "Content-Type: application/x-www-form-urlencoded" \
 *   -d "grant_type=refresh_token" \
 *   -d "refresh_token=REFRESH_TOKEN" \
 *   -d "client_id=client-app" \
 *   -d "client_secret=secret"
 */
