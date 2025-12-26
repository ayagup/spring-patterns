package com.example.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Authentication Pattern
 * 
 * Demonstrates:
 * - Custom JWT token generation
 * - JWT token validation and parsing
 * - Stateless authentication with JWT
 * - Token-based security filter
 * - Claims extraction and validation
 * - Token refresh mechanism
 * 
 * JWT Structure:
 * - Header: Algorithm and token type
 * - Payload: Claims (user data, expiration, etc.)
 * - Signature: Verification signature
 * 
 * Use Cases:
 * - Stateless REST API authentication
 * - Mobile app authentication
 * - Microservices authentication
 * - Single sign-on (SSO)
 * 
 * Dependencies:
 * - jjwt-api, jjwt-impl, jjwt-jackson
 * - spring-boot-starter-security
 */

@SpringBootApplication
public class JWTAuthenticationPattern {
    public static void main(String[] args) {
        SpringApplication.run(JWTAuthenticationPattern.class, args);
    }
}

@Configuration
class JWTSecurityConfig {
    
    private final JWTAuthenticationFilter jwtAuthFilter;
    
    public JWTSecurityConfig(JWTAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();
        
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin"))
            .roles("USER", "ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user, admin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

@Component
class JWTTokenProvider {
    
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final long jwtExpirationMs = 86400000; // 24 hours
    private final long refreshExpirationMs = 604800000; // 7 days
    
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
            .setSubject(username)
            .claim("authorities", getAuthorities(authentication))
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(jwtSecret)
            .compact();
    }
    
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);
        
        return Jwts.builder()
            .setSubject(username)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(jwtSecret)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return claims.getSubject();
    }
    
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");
        
        if (authorities == null) {
            return Collections.emptyList();
        }
        
        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public TokenInfo getTokenInfo(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return new TokenInfo(
            claims.getSubject(),
            claims.get("type", String.class),
            claims.getIssuedAt(),
            claims.getExpiration(),
            claims.get("authorities")
        );
    }
    
    private List<String> getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
    }
}

@Component
class JWTAuthenticationFilter extends OncePerRequestFilter {
    
    private final JWTTokenProvider tokenProvider;
    
    public JWTAuthenticationFilter(JWTTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                List<GrantedAuthority> authorities = tokenProvider.getAuthoritiesFromToken(token);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}

@RestController
@RequestMapping("/api/auth")
class JWTAuthenticationController {
    
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider tokenProvider;
    
    public JWTAuthenticationController(AuthenticationManager authenticationManager,
                                      JWTTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());
            
            return ResponseEntity.ok(new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                86400L
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new AuthResponse(null, null, null, 0L));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            if (tokenProvider.validateToken(request.getRefreshToken())) {
                String username = tokenProvider.getUsernameFromToken(request.getRefreshToken());
                
                // Create simple authentication for token generation
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList()
                );
                
                String newAccessToken = tokenProvider.generateToken(auth);
                String newRefreshToken = tokenProvider.generateRefreshToken(username);
                
                return ResponseEntity.ok(new AuthResponse(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    86400L
                ));
            } else {
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest request) {
        
        boolean isValid = tokenProvider.validateToken(request.getToken());
        
        if (isValid) {
            TokenInfo info = tokenProvider.getTokenInfo(request.getToken());
            return ResponseEntity.ok(new TokenValidationResponse(true, info));
        } else {
            return ResponseEntity.ok(new TokenValidationResponse(false, null));
        }
    }
}

@RestController
@RequestMapping("/api/protected")
class ProtectedResourceController {
    
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        Map<String, Object> info = new HashMap<>();
        info.put("username", authentication.getName());
        info.put("authorities", authentication.getAuthorities());
        info.put("authenticated", authentication.isAuthenticated());
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, String>> getAdminDashboard() {
        return ResponseEntity.ok(Map.of(
            "message", "Admin dashboard",
            "access", "granted"
        ));
    }
}

class LoginRequest {
    private String username;
    private String password;
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class RefreshTokenRequest {
    private String refreshToken;
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}

class TokenValidationRequest {
    private String token;
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    
    public AuthResponse(String accessToken, String refreshToken, 
                       String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
    
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
}

class TokenValidationResponse {
    private boolean valid;
    private TokenInfo tokenInfo;
    
    public TokenValidationResponse(boolean valid, TokenInfo tokenInfo) {
        this.valid = valid;
        this.tokenInfo = tokenInfo;
    }
    
    public boolean isValid() { return valid; }
    public TokenInfo getTokenInfo() { return tokenInfo; }
}

class TokenInfo {
    private String subject;
    private String type;
    private Date issuedAt;
    private Date expiresAt;
    private Object authorities;
    
    public TokenInfo(String subject, String type, Date issuedAt, 
                    Date expiresAt, Object authorities) {
        this.subject = subject;
        this.type = type;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.authorities = authorities;
    }
    
    public String getSubject() { return subject; }
    public String getType() { return type; }
    public Date getIssuedAt() { return issuedAt; }
    public Date getExpiresAt() { return expiresAt; }
    public Object getAuthorities() { return authorities; }
}
