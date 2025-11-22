package com.example.session;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TOKEN AUTHENTICATION PATTERNS
 * ==============================
 * 
 * This file combines two related patterns:
 * 1. Token-based Session Pattern
 * 2. JWT (JSON Web Token) Session Pattern
 * 
 * TOKEN-BASED AUTHENTICATION
 * --------------------------
 * Purpose:
 * - Replace session cookies with tokens
 * - Enable stateless authentication
 * - Support multiple devices
 * - API authentication
 * 
 * Key Components:
 * - Token Generator: Creates unique tokens
 * - Token Store: Validates tokens
 * - Token Refresher: Renews tokens
 * 
 * JWT AUTHENTICATION
 * ------------------
 * Purpose:
 * - Self-contained authentication tokens
 * - No server-side storage needed
 * - Cryptographically signed
 * - Industry standard (RFC 7519)
 * 
 * JWT Structure:
 * - Header: Algorithm and type
 * - Payload: Claims (user data)
 * - Signature: Verification
 * 
 * Format: header.payload.signature
 */

// 1. SIMPLE TOKEN
class AuthToken {
    private final String token;
    private final String userId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final TokenType type;
    
    public AuthToken(String token, String userId, Instant issuedAt, Instant expiresAt, TokenType type) {
        this.token = token;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.type = type;
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    // Getters
    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public TokenType getType() { return type; }
    
    enum TokenType {
        ACCESS,  // Short-lived (15 min)
        REFRESH  // Long-lived (7 days)
    }
}

// 2. TOKEN GENERATOR
class TokenGenerator {
    
    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    public String generateSecureToken() {
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    public String generateHashedToken(String data, String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((data + secret).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hashed token", e);
        }
    }
}

// 3. TOKEN STORE (for non-JWT tokens)
class TokenStore {
    
    private final Map<String, AuthToken> tokens = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userTokens = new ConcurrentHashMap<>();
    
    public void saveToken(AuthToken token) {
        tokens.put(token.getToken(), token);
        userTokens.computeIfAbsent(token.getUserId(), k -> ConcurrentHashMap.newKeySet())
                  .add(token.getToken());
    }
    
    public AuthToken getToken(String token) {
        AuthToken authToken = tokens.get(token);
        if (authToken != null && authToken.isExpired()) {
            removeToken(token);
            return null;
        }
        return authToken;
    }
    
    public void removeToken(String token) {
        AuthToken authToken = tokens.remove(token);
        if (authToken != null) {
            Set<String> userTokenSet = userTokens.get(authToken.getUserId());
            if (userTokenSet != null) {
                userTokenSet.remove(token);
            }
        }
    }
    
    public void removeAllUserTokens(String userId) {
        Set<String> userTokenSet = userTokens.remove(userId);
        if (userTokenSet != null) {
            userTokenSet.forEach(tokens::remove);
        }
    }
    
    public Collection<AuthToken> getUserTokens(String userId) {
        Set<String> tokenSet = userTokens.get(userId);
        if (tokenSet == null) {
            return Collections.emptyList();
        }
        return tokenSet.stream()
                .map(tokens::get)
                .filter(Objects::nonNull)
                .filter(t -> !t.isExpired())
                .toList();
    }
    
    public void cleanupExpiredTokens() {
        tokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}

// 4. JWT TOKEN
class JwtToken {
    private final JwtHeader header;
    private final JwtPayload payload;
    private final String signature;
    
    public JwtToken(JwtHeader header, JwtPayload payload, String signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }
    
    public String encode() {
        String headerJson = header.toJson();
        String payloadJson = payload.toJson();
        
        String headerEncoded = base64UrlEncode(headerJson);
        String payloadEncoded = base64UrlEncode(payloadJson);
        
        return headerEncoded + "." + payloadEncoded + "." + signature;
    }
    
    public boolean isExpired() {
        return payload.getExp() != null && Instant.now().isAfter(payload.getExp());
    }
    
    private static String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }
    
    // Getters
    public JwtHeader getHeader() { return header; }
    public JwtPayload getPayload() { return payload; }
    public String getSignature() { return signature; }
}

// 5. JWT HEADER
class JwtHeader {
    private String alg = "HS256"; // Algorithm
    private String typ = "JWT";   // Type
    
    public String toJson() {
        return String.format("{\"alg\":\"%s\",\"typ\":\"%s\"}", alg, typ);
    }
    
    // Getters and setters
    public String getAlg() { return alg; }
    public void setAlg(String alg) { this.alg = alg; }
    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }
}

// 6. JWT PAYLOAD (Claims)
class JwtPayload {
    private String sub;  // Subject (user ID)
    private String iss;  // Issuer
    private Instant exp; // Expiration
    private Instant iat; // Issued at
    private String jti;  // JWT ID
    private final Map<String, Object> customClaims = new HashMap<>();
    
    public void addCustomClaim(String key, Object value) {
        customClaims.put(key, value);
    }
    
    public Object getCustomClaim(String key) {
        return customClaims.get(key);
    }
    
    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        if (sub != null) json.append("\"sub\":\"").append(sub).append("\",");
        if (iss != null) json.append("\"iss\":\"").append(iss).append("\",");
        if (exp != null) json.append("\"exp\":").append(exp.getEpochSecond()).append(",");
        if (iat != null) json.append("\"iat\":").append(iat.getEpochSecond()).append(",");
        if (jti != null) json.append("\"jti\":\"").append(jti).append("\",");
        
        customClaims.forEach((key, value) -> {
            if (value instanceof String) {
                json.append("\"").append(key).append("\":\"").append(value).append("\",");
            } else {
                json.append("\"").append(key).append("\":").append(value).append(",");
            }
        });
        
        if (json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
    
    // Getters and setters
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getIss() { return iss; }
    public void setIss(String iss) { this.iss = iss; }
    public Instant getExp() { return exp; }
    public void setExp(Instant exp) { this.exp = exp; }
    public Instant getIat() { return iat; }
    public void setIat(Instant iat) { this.iat = iat; }
    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }
}

// 7. JWT GENERATOR
class JwtGenerator {
    
    private final String secret;
    private final String issuer;
    
    public JwtGenerator(String secret, String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }
    
    public JwtToken generateToken(String userId, String username, List<String> roles, long validitySeconds) {
        JwtHeader header = new JwtHeader();
        
        JwtPayload payload = new JwtPayload();
        payload.setSub(userId);
        payload.setIss(issuer);
        payload.setIat(Instant.now());
        payload.setExp(Instant.now().plusSeconds(validitySeconds));
        payload.setJti(UUID.randomUUID().toString());
        payload.addCustomClaim("username", username);
        payload.addCustomClaim("roles", roles);
        
        String signature = sign(header, payload);
        
        return new JwtToken(header, payload, signature);
    }
    
    public boolean validateToken(JwtToken token) {
        if (token.isExpired()) {
            return false;
        }
        
        String expectedSignature = sign(token.getHeader(), token.getPayload());
        return expectedSignature.equals(token.getSignature());
    }
    
    private String sign(JwtHeader header, JwtPayload payload) {
        try {
            String headerJson = header.toJson();
            String payloadJson = payload.toJson();
            
            String headerEncoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            
            String data = headerEncoded + "." + payloadEncoded;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }
}

// 8. TOKEN REFRESH SERVICE
class TokenRefreshService {
    
    private final JwtGenerator jwtGenerator;
    private final TokenStore tokenStore;
    
    public TokenRefreshService(JwtGenerator jwtGenerator, TokenStore tokenStore) {
        this.jwtGenerator = jwtGenerator;
        this.tokenStore = tokenStore;
    }
    
    public TokenPair createTokenPair(String userId, String username, List<String> roles) {
        // Access token: short-lived (15 minutes)
        JwtToken accessToken = jwtGenerator.generateToken(userId, username, roles, 900);
        
        // Refresh token: long-lived (7 days)
        String refreshTokenStr = UUID.randomUUID().toString();
        AuthToken refreshToken = new AuthToken(
            refreshTokenStr,
            userId,
            Instant.now(),
            Instant.now().plusSeconds(604800),
            AuthToken.TokenType.REFRESH
        );
        tokenStore.saveToken(refreshToken);
        
        return new TokenPair(accessToken, refreshToken);
    }
    
    public JwtToken refreshAccessToken(String refreshTokenStr, String username, List<String> roles) {
        AuthToken refreshToken = tokenStore.getToken(refreshTokenStr);
        
        if (refreshToken == null || refreshToken.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        
        // Generate new access token
        return jwtGenerator.generateToken(refreshToken.getUserId(), username, roles, 900);
    }
    
    static class TokenPair {
        private final JwtToken accessToken;
        private final AuthToken refreshToken;
        
        public TokenPair(JwtToken accessToken, AuthToken refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
        
        public JwtToken getAccessToken() { return accessToken; }
        public AuthToken getRefreshToken() { return refreshToken; }
    }
}

/**
 * DEMONSTRATION
 */
public class TokenAuthenticationPatterns {
    
    public static void main(String[] args) {
        System.out.println("=== TOKEN AUTHENTICATION PATTERNS ===\n");
        
        // 1. Simple Token Authentication
        System.out.println("1. SIMPLE TOKEN AUTHENTICATION:");
        TokenGenerator tokenGen = new TokenGenerator();
        TokenStore tokenStore = new TokenStore();
        
        String simpleToken = tokenGen.generateSecureToken();
        AuthToken authToken = new AuthToken(
            simpleToken,
            "user-123",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            AuthToken.TokenType.ACCESS
        );
        tokenStore.saveToken(authToken);
        
        System.out.println("   Token: " + simpleToken.substring(0, 20) + "...");
        System.out.println("   User ID: " + authToken.getUserId());
        System.out.println("   Expires: " + authToken.getExpiresAt());
        System.out.println();
        
        // 2. JWT Token Generation
        System.out.println("2. JWT TOKEN GENERATION:");
        String secret = "my-secret-key-change-in-production";
        JwtGenerator jwtGen = new JwtGenerator(secret, "my-app");
        
        JwtToken jwt = jwtGen.generateToken(
            "user-456",
            "alice",
            Arrays.asList("USER", "ADMIN"),
            3600
        );
        
        String jwtString = jwt.encode();
        System.out.println("   JWT: " + jwtString.substring(0, 50) + "...");
        System.out.println("   Subject: " + jwt.getPayload().getSub());
        System.out.println("   Username: " + jwt.getPayload().getCustomClaim("username"));
        System.out.println("   Roles: " + jwt.getPayload().getCustomClaim("roles"));
        System.out.println("   Expires: " + jwt.getPayload().getExp());
        System.out.println();
        
        // 3. JWT Validation
        System.out.println("3. JWT VALIDATION:");
        boolean isValid = jwtGen.validateToken(jwt);
        System.out.println("   Valid: " + isValid);
        System.out.println("   Expired: " + jwt.isExpired());
        System.out.println();
        
        // 4. Token Refresh Flow
        System.out.println("4. TOKEN REFRESH FLOW:");
        TokenRefreshService refreshService = new TokenRefreshService(jwtGen, tokenStore);
        
        TokenRefreshService.TokenPair tokenPair = refreshService.createTokenPair(
            "user-789",
            "bob",
            Arrays.asList("USER")
        );
        
        System.out.println("   Access Token (short-lived):");
        System.out.println("      Expires: " + tokenPair.getAccessToken().getPayload().getExp());
        System.out.println("   Refresh Token (long-lived):");
        System.out.println("      Expires: " + tokenPair.getRefreshToken().getExpiresAt());
        
        // Simulate token refresh
        JwtToken newAccessToken = refreshService.refreshAccessToken(
            tokenPair.getRefreshToken().getToken(),
            "bob",
            Arrays.asList("USER")
        );
        System.out.println("   New Access Token:");
        System.out.println("      Expires: " + newAccessToken.getPayload().getExp());
        System.out.println();
        
        // 5. Multiple Device Support
        System.out.println("5. MULTIPLE DEVICE SUPPORT:");
        String userId = "user-multi";
        
        // Login from web browser
        AuthToken webToken = new AuthToken(
            tokenGen.generateSecureToken(), userId,
            Instant.now(), Instant.now().plusSeconds(3600),
            AuthToken.TokenType.ACCESS
        );
        tokenStore.saveToken(webToken);
        
        // Login from mobile app
        AuthToken mobileToken = new AuthToken(
            tokenGen.generateSecureToken(), userId,
            Instant.now(), Instant.now().plusSeconds(3600),
            AuthToken.TokenType.ACCESS
        );
        tokenStore.saveToken(mobileToken);
        
        Collection<AuthToken> userTokens = tokenStore.getUserTokens(userId);
        System.out.println("   User has " + userTokens.size() + " active tokens");
        System.out.println("   (Supports multiple concurrent sessions)");
        System.out.println();
        
        System.out.println("JWT Structure:");
        System.out.println("   header.payload.signature");
        System.out.println();
        System.out.println("   Header: {\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        System.out.println("   Payload: {\"sub\":\"user-id\",\"exp\":1234567890,...}");
        System.out.println("   Signature: HMACSHA256(base64(header) + '.' + base64(payload), secret)");
        System.out.println();
        
        System.out.println("Security Best Practices:");
        System.out.println("   ✓ Use HTTPS only");
        System.out.println("   ✓ Short expiration for access tokens");
        System.out.println("   ✓ Implement token refresh");
        System.out.println("   ✓ Store tokens securely (httpOnly cookies)");
        System.out.println("   ✓ Use strong signing keys");
        System.out.println("   ✓ Validate token signature");
        System.out.println("   ✓ Check token expiration");
        System.out.println("   ✓ Consider token blacklisting for logout");
        System.out.println();
        
        System.out.println("Use Cases:");
        System.out.println("   • Single Page Applications");
        System.out.println("   • Mobile applications");
        System.out.println("   • RESTful APIs");
        System.out.println("   • Microservices authentication");
        System.out.println("   • Third-party API access");
    }
}
