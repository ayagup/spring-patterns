### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:48:23 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **API Security Patterns**
> - OAuth2 Pattern
> - OAuth2 Client Pattern
> - OAuth2 Resource Server Pattern
> - OAuth2 Authorization Server Pattern
> - JWT Authentication Pattern
> - API Key Pattern
> - Basic Authentication Pattern
> - Digest Authentication Pattern
> - Certificate-based Authentication Pattern
> - SAML Integration Pattern
> - OpenID Connect Pattern
> - Token Introspection Pattern
> - Token Refresh Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all these API security patterns. Let me generate the project structure with examples for each pattern.
> 
> ## Project Structure
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>api-security-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Starters -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-security</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-oauth2-client</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
>         </dependency>
>         
>         <!-- JWT -->
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-api</artifactId>
>             <version>0.12.3</version>
>         </dependency>
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-impl</artifactId>
>             <version>0.12.3</version>
>             <scope>runtime</scope>
>         </dependency>
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-jackson</artifactId>
>             <version>0.12.3</version>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <!-- SAML -->
>         <dependency>
>             <groupId>org.springframework.security</groupId>
>             <artifactId>spring-security-saml2-service-provider</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## Main Application
> 
> ```java src/main/java/com/example/security/ApiSecurityPatternsApplication.java
> package com.example.security;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class ApiSecurityPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(ApiSecurityPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 1. OAuth2 Pattern (General Configuration)
> 
> ```java src/main/java/com/example/security/config/OAuth2Config.java
> package com.example.security.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> @EnableWebSecurity
> public class OAuth2Config {
>     
>     @Bean
>     public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .authorizeHttpRequests(authorize -> authorize
>                 .requestMatchers("/oauth2/**", "/login/**", "/public/**").permitAll()
>                 .anyRequest().authenticated()
>             )
>             .oauth2Login(oauth2 -> oauth2
>                 .loginPage("/login")
>                 .defaultSuccessUrl("/oauth2/success")
>                 .failureUrl("/login?error=true")
>             )
>             .oauth2Client(oauth2 -> {});
>         
>         return http.build();
>     }
> }
> ```
> 
> ## 2. OAuth2 Client Pattern
> 
> ```java src/main/java/com/example/security/oauth2/client/OAuth2ClientController.java
> package com.example.security.oauth2.client;
> 
> import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
> import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.reactive.function.client.WebClient;
> 
> @RestController
> @RequestMapping("/api/oauth2-client")
> public class OAuth2ClientController {
>     
>     private final WebClient webClient;
>     
>     public OAuth2ClientController(WebClient.Builder webClientBuilder) {
>         this.webClient = webClientBuilder.build();
>     }
>     
>     @GetMapping("/user-info")
>     public String getUserInfo(
>             @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
>         
>         String accessToken = authorizedClient.getAccessToken().getTokenValue();
>         
>         return webClient
>             .get()
>             .uri("https://www.googleapis.com/oauth2/v3/userinfo")
>             .headers(headers -> headers.setBearerAuth(accessToken))
>             .retrieve()
>             .bodyToMono(String.class)
>             .block();
>     }
>     
>     @GetMapping("/token-info")
>     public OAuth2TokenInfo getTokenInfo(
>             @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
>         
>         return OAuth2TokenInfo.builder()
>             .tokenValue(authorizedClient.getAccessToken().getTokenValue())
>             .tokenType(authorizedClient.getAccessToken().getTokenType().getValue())
>             .expiresAt(authorizedClient.getAccessToken().getExpiresAt())
>             .scopes(authorizedClient.getAccessToken().getScopes())
>             .clientName(authorizedClient.getClientRegistration().getClientName())
>             .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/oauth2/client/OAuth2TokenInfo.java
> package com.example.security.oauth2.client;
> 
> import lombok.Builder;
> import lombok.Data;
> 
> import java.time.Instant;
> import java.util.Set;
> 
> @Data
> @Builder
> public class OAuth2TokenInfo {
>     private String tokenValue;
>     private String tokenType;
>     private Instant expiresAt;
>     private Set<String> scopes;
>     private String clientName;
> }
> ```
> 
> ## 3. OAuth2 Resource Server Pattern
> 
> ```java src/main/java/com/example/security/oauth2/resourceserver/ResourceServerConfig.java
> package com.example.security.oauth2.resourceserver;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.http.SessionCreationPolicy;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class ResourceServerConfig {
>     
>     @Bean
>     public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/resource/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .requestMatchers("/api/resource/public/**").permitAll()
>                 .requestMatchers("/api/resource/admin/**").hasAuthority("SCOPE_admin")
>                 .anyRequest().authenticated()
>             )
>             .oauth2ResourceServer(oauth2 -> oauth2
>                 .jwt(jwt -> jwt
>                     .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
>                 )
>             )
>             .sessionManagement(session -> session
>                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
>             );
>         
>         return http.build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/oauth2/resourceserver/CustomJwtAuthenticationConverter.java
> package com.example.security.oauth2.resourceserver;
> 
> import org.springframework.core.convert.converter.Converter;
> import org.springframework.security.authentication.AbstractAuthenticationToken;
> import org.springframework.security.core.GrantedAuthority;
> import org.springframework.security.core.authority.SimpleGrantedAuthority;
> import org.springframework.security.oauth2.jwt.Jwt;
> import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
> 
> import java.util.Collection;
> import java.util.List;
> import java.util.stream.Collectors;
> 
> public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
>     
>     @Override
>     public AbstractAuthenticationToken convert(Jwt jwt) {
>         Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
>         return new JwtAuthenticationToken(jwt, authorities);
>     }
>     
>     private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
>         List<String> scopes = jwt.getClaimAsStringList("scope");
>         if (scopes == null) {
>             return List.of();
>         }
>         
>         return scopes.stream()
>             .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
>             .collect(Collectors.toList());
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/oauth2/resourceserver/ResourceController.java
> package com.example.security.oauth2.resourceserver;
> 
> import org.springframework.security.core.annotation.AuthenticationPrincipal;
> import org.springframework.security.oauth2.jwt.Jwt;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/resource")
> public class ResourceController {
>     
>     @GetMapping("/public/info")
>     public Map<String, String> publicInfo() {
>         return Map.of("message", "This is public information");
>     }
>     
>     @GetMapping("/protected/data")
>     public Map<String, Object> protectedData(@AuthenticationPrincipal Jwt jwt) {
>         return Map.of(
>             "message", "Protected resource accessed",
>             "subject", jwt.getSubject(),
>             "claims", jwt.getClaims()
>         );
>     }
>     
>     @GetMapping("/admin/data")
>     public Map<String, String> adminData() {
>         return Map.of("message", "Admin-only data");
>     }
> }
> ```
> 
> ## 4. OAuth2 Authorization Server Pattern
> 
> ```java src/main/java/com/example/security/oauth2/authserver/AuthorizationServerConfig.java
> package com.example.security.oauth2.authserver;
> 
> import com.nimbusds.jose.jwk.JWKSet;
> import com.nimbusds.jose.jwk.RSAKey;
> import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
> import com.nimbusds.jose.jwk.source.JWKSource;
> import com.nimbusds.jose.proc.SecurityContext;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.core.annotation.Order;
> import org.springframework.security.config.Customizer;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.core.userdetails.User;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> import org.springframework.security.crypto.password.PasswordEncoder;
> import org.springframework.security.oauth2.core.AuthorizationGrantType;
> import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
> import org.springframework.security.oauth2.core.oidc.OidcScopes;
> import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
> import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
> import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
> import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
> import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
> import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
> import org.springframework.security.provisioning.InMemoryUserDetailsManager;
> import org.springframework.security.web.SecurityFilterChain;
> 
> import java.security.KeyPair;
> import java.security.KeyPairGenerator;
> import java.security.interfaces.RSAPrivateKey;
> import java.security.interfaces.RSAPublicKey;
> import java.util.UUID;
> 
> @Configuration
> public class AuthorizationServerConfig {
>     
>     @Bean
>     @Order(1)
>     public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
>         OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
>         return http.formLogin(Customizer.withDefaults()).build();
>     }
>     
>     @Bean
>     public RegisteredClientRepository registeredClientRepository() {
>         RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
>             .clientId("demo-client")
>             .clientSecret(passwordEncoder().encode("secret"))
>             .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
>             .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
>             .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
>             .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
>             .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
>             .redirectUri("http://localhost:8080/login/oauth2/code/demo-client")
>             .redirectUri("http://localhost:8080/authorized")
>             .scope(OidcScopes.OPENID)
>             .scope(OidcScopes.PROFILE)
>             .scope("read")
>             .scope("write")
>             .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
>             .build();
>         
>         return new InMemoryRegisteredClientRepository(registeredClient);
>     }
>     
>     @Bean
>     public JWKSource<SecurityContext> jwkSource() {
>         KeyPair keyPair = generateRsaKey();
>         RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
>         RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
>         
>         RSAKey rsaKey = new RSAKey.Builder(publicKey)
>             .privateKey(privateKey)
>             .keyID(UUID.randomUUID().toString())
>             .build();
>         
>         JWKSet jwkSet = new JWKSet(rsaKey);
>         return new ImmutableJWKSet<>(jwkSet);
>     }
>     
>     private static KeyPair generateRsaKey() {
>         try {
>             KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
>             keyPairGenerator.initialize(2048);
>             return keyPairGenerator.generateKeyPair();
>         } catch (Exception ex) {
>             throw new IllegalStateException(ex);
>         }
>     }
>     
>     @Bean
>     public AuthorizationServerSettings authorizationServerSettings() {
>         return AuthorizationServerSettings.builder().build();
>     }
>     
>     @Bean
>     public UserDetailsService userDetailsService() {
>         var user = User.withUsername("user")
>             .password(passwordEncoder().encode("password"))
>             .roles("USER")
>             .build();
>         
>         var admin = User.withUsername("admin")
>             .password(passwordEncoder().encode("admin"))
>             .roles("USER", "ADMIN")
>             .build();
>         
>         return new InMemoryUserDetailsManager(user, admin);
>     }
>     
>     @Bean
>     public PasswordEncoder passwordEncoder() {
>         return new BCryptPasswordEncoder();
>     }
> }
> ```
> 
> ## 5. JWT Authentication Pattern
> 
> ```java src/main/java/com/example/security/jwt/JwtConfig.java
> package com.example.security.jwt;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.http.SessionCreationPolicy;
> import org.springframework.security.web.SecurityFilterChain;
> import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
> 
> @Configuration
> public class JwtConfig {
>     
>     private final JwtAuthenticationFilter jwtAuthenticationFilter;
>     
>     public JwtConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
>         this.jwtAuthenticationFilter = jwtAuthenticationFilter;
>     }
>     
>     @Bean
>     public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/jwt/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .requestMatchers("/api/jwt/auth/**").permitAll()
>                 .anyRequest().authenticated()
>             )
>             .sessionManagement(session -> session
>                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
>             )
>             .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
>             .csrf(csrf -> csrf.disable());
>         
>         return http.build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/jwt/JwtTokenProvider.java
> package com.example.security.jwt;
> 
> import io.jsonwebtoken.*;
> import io.jsonwebtoken.security.Keys;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.security.core.Authentication;
> import org.springframework.security.core.GrantedAuthority;
> import org.springframework.stereotype.Component;
> 
> import javax.crypto.SecretKey;
> import java.util.Date;
> import java.util.stream.Collectors;
> 
> @Component
> public class JwtTokenProvider {
>     
>     private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
>     
>     @Value("${jwt.expiration:86400000}") // 24 hours
>     private long jwtExpirationMs;
>     
>     public String generateToken(Authentication authentication) {
>         String username = authentication.getName();
>         Date now = new Date();
>         Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
>         
>         String authorities = authentication.getAuthorities().stream()
>             .map(GrantedAuthority::getAuthority)
>             .collect(Collectors.joining(","));
>         
>         return Jwts.builder()
>             .setSubject(username)
>             .claim("authorities", authorities)
>             .setIssuedAt(now)
>             .setExpiration(expiryDate)
>             .signWith(jwtSecret)
>             .compact();
>     }
>     
>     public String getUsernameFromToken(String token) {
>         return Jwts.parserBuilder()
>             .setSigningKey(jwtSecret)
>             .build()
>             .parseClaimsJws(token)
>             .getBody()
>             .getSubject();
>     }
>     
>     public boolean validateToken(String token) {
>         try {
>             Jwts.parserBuilder()
>                 .setSigningKey(jwtSecret)
>                 .build()
>                 .parseClaimsJws(token);
>             return true;
>         } catch (JwtException | IllegalArgumentException e) {
>             return false;
>         }
>     }
>     
>     public Claims getClaimsFromToken(String token) {
>         return Jwts.parserBuilder()
>             .setSigningKey(jwtSecret)
>             .build()
>             .parseClaimsJws(token)
>             .getBody();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/jwt/JwtAuthenticationFilter.java
> package com.example.security.jwt;
> 
> import jakarta.servlet.FilterChain;
> import jakarta.servlet.ServletException;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> import org.springframework.security.core.context.SecurityContextHolder;
> import org.springframework.security.core.userdetails.UserDetails;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
> import org.springframework.stereotype.Component;
> import org.springframework.util.StringUtils;
> import org.springframework.web.filter.OncePerRequestFilter;
> 
> import java.io.IOException;
> 
> @Component
> public class JwtAuthenticationFilter extends OncePerRequestFilter {
>     
>     private final JwtTokenProvider tokenProvider;
>     private final UserDetailsService userDetailsService;
>     
>     public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
>         this.tokenProvider = tokenProvider;
>         this.userDetailsService = userDetailsService;
>     }
>     
>     @Override
>     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
>                                     FilterChain filterChain) throws ServletException, IOException {
>         try {
>             String jwt = getJwtFromRequest(request);
>             
>             if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
>                 String username = tokenProvider.getUsernameFromToken(jwt);
>                 UserDetails userDetails = userDetailsService.loadUserByUsername(username);
>                 
>                 UsernamePasswordAuthenticationToken authentication = 
>                     new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
>                 authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
>                 
>                 SecurityContextHolder.getContext().setAuthentication(authentication);
>             }
>         } catch (Exception ex) {
>             logger.error("Could not set user authentication in security context", ex);
>         }
>         
>         filterChain.doFilter(request, response);
>     }
>     
>     private String getJwtFromRequest(HttpServletRequest request) {
>         String bearerToken = request.getHeader("Authorization");
>         if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
>             return bearerToken.substring(7);
>         }
>         return null;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/jwt/JwtAuthController.java
> package com.example.security.jwt;
> 
> import lombok.Data;
> import org.springframework.http.ResponseEntity;
> import org.springframework.security.authentication.AuthenticationManager;
> import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> import org.springframework.security.core.Authentication;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/jwt/auth")
> public class JwtAuthController {
>     
>     private final AuthenticationManager authenticationManager;
>     private final JwtTokenProvider tokenProvider;
>     
>     public JwtAuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
>         this.authenticationManager = authenticationManager;
>         this.tokenProvider = tokenProvider;
>     }
>     
>     @PostMapping("/login")
>     public ResponseEntity<JwtAuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
>         Authentication authentication = authenticationManager.authenticate(
>             new UsernamePasswordAuthenticationToken(
>                 loginRequest.getUsername(),
>                 loginRequest.getPassword()
>             )
>         );
>         
>         String token = tokenProvider.generateToken(authentication);
>         
>         return ResponseEntity.ok(new JwtAuthResponse(token));
>     }
>     
>     @Data
>     static class LoginRequest {
>         private String username;
>         private String password;
>     }
>     
>     @Data
>     static class JwtAuthResponse {
>         private String accessToken;
>         private String tokenType = "Bearer";
>         
>         public JwtAuthResponse(String accessToken) {
>             this.accessToken = accessToken;
>         }
>     }
> }
> ```
> 
> ## 6. API Key Pattern
> 
> ```java src/main/java/com/example/security/apikey/ApiKeyConfig.java
> package com.example.security.apikey;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.web.SecurityFilterChain;
> import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
> 
> @Configuration
> public class ApiKeyConfig {
>     
>     private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
>     
>     public ApiKeyConfig(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
>         this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
>     }
>     
>     @Bean
>     public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/apikey/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .anyRequest().authenticated()
>             )
>             .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
>             .csrf(csrf -> csrf.disable());
>         
>         return http.build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/apikey/ApiKeyAuthenticationFilter.java
> package com.example.security.apikey;
> 
> import jakarta.servlet.FilterChain;
> import jakarta.servlet.ServletException;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.security.core.authority.SimpleGrantedAuthority;
> import org.springframework.security.core.context.SecurityContextHolder;
> import org.springframework.stereotype.Component;
> import org.springframework.web.filter.OncePerRequestFilter;
> 
> import java.io.IOException;
> import java.util.List;
> 
> @Component
> public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
>     
>     private static final String API_KEY_HEADER = "X-API-Key";
>     private final ApiKeyService apiKeyService;
>     
>     public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
>         this.apiKeyService = apiKeyService;
>     }
>     
>     @Override
>     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
>                                     FilterChain filterChain) throws ServletException, IOException {
>         String apiKey = request.getHeader(API_KEY_HEADER);
>         
>         if (apiKey != null && apiKeyService.isValidApiKey(apiKey)) {
>             String clientId = apiKeyService.getClientIdFromApiKey(apiKey);
>             List<String> roles = apiKeyService.getRolesForApiKey(apiKey);
>             
>             var authorities = roles.stream()
>                 .map(SimpleGrantedAuthority::new)
>                 .toList();
>             
>             ApiKeyAuthentication authentication = new ApiKeyAuthentication(clientId, authorities);
>             SecurityContextHolder.getContext().setAuthentication(authentication);
>         }
>         
>         filterChain.doFilter(request, response);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/apikey/ApiKeyAuthentication.java
> package com.example.security.apikey;
> 
> import org.springframework.security.authentication.AbstractAuthenticationToken;
> import org.springframework.security.core.GrantedAuthority;
> 
> import java.util.Collection;
> 
> public class ApiKeyAuthentication extends AbstractAuthenticationToken {
>     
>     private final String clientId;
>     
>     public ApiKeyAuthentication(String clientId, Collection<? extends GrantedAuthority> authorities) {
>         super(authorities);
>         this.clientId = clientId;
>         setAuthenticated(true);
>     }
>     
>     @Override
>     public Object getCredentials() {
>         return null;
>     }
>     
>     @Override
>     public Object getPrincipal() {
>         return clientId;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/apikey/ApiKeyService.java
> package com.example.security.apikey;
> 
> import org.springframework.stereotype.Service;
> 
> import java.util.List;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Service
> public class ApiKeyService {
>     
>     private final Map<String, ApiKeyInfo> apiKeys = new ConcurrentHashMap<>();
>     
>     public ApiKeyService() {
>         // Initialize with some demo API keys
>         apiKeys.put("demo-key-123", new ApiKeyInfo("client-1", List.of("ROLE_USER")));
>         apiKeys.put("admin-key-456", new ApiKeyInfo("client-2", List.of("ROLE_USER", "ROLE_ADMIN")));
>     }
>     
>     public boolean isValidApiKey(String apiKey) {
>         return apiKeys.containsKey(apiKey);
>     }
>     
>     public String getClientIdFromApiKey(String apiKey) {
>         ApiKeyInfo info = apiKeys.get(apiKey);
>         return info != null ? info.getClientId() : null;
>     }
>     
>     public List<String> getRolesForApiKey(String apiKey) {
>         ApiKeyInfo info = apiKeys.get(apiKey);
>         return info != null ? info.getRoles() : List.of();
>     }
>     
>     private static class ApiKeyInfo {
>         private final String clientId;
>         private final List<String> roles;
>         
>         public ApiKeyInfo(String clientId, List<String> roles) {
>             this.clientId = clientId;
>             this.roles = roles;
>         }
>         
>         public String getClientId() {
>             return clientId;
>         }
>         
>         public List<String> getRoles() {
>             return roles;
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/apikey/ApiKeyController.java
> package com.example.security.apikey;
> 
> import org.springframework.security.core.Authentication;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/apikey")
> public class ApiKeyController {
>     
>     @GetMapping("/protected")
>     public Map<String, Object> protectedResource(Authentication authentication) {
>         return Map.of(
>             "message", "Access granted via API Key",
>             "client", authentication.getPrincipal(),
>             "authorities", authentication.getAuthorities()
>         );
>     }
> }
> ```
> 
> ## 7. Basic Authentication Pattern
> 
> ```

#### _User_

> continue exactly where you left off

#### _Assistant_

> ```java src/main/java/com/example/security/basic/BasicAuthConfig.java
> package com.example.security.basic;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
> import org.springframework.security.core.userdetails.User;
> import org.springframework.security.core.userdetails.UserDetails;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
> import org.springframework.security.crypto.password.PasswordEncoder;
> import org.springframework.security.provisioning.InMemoryUserDetailsManager;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class BasicAuthConfig {
>     
>     @Bean
>     public SecurityFilterChain basicAuthSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/basic/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .requestMatchers("/api/basic/public/**").permitAll()
>                 .requestMatchers("/api/basic/admin/**").hasRole("ADMIN")
>                 .anyRequest().authenticated()
>             )
>             .httpBasic(basic -> basic
>                 .realmName("Basic Auth Realm")
>             )
>             .csrf(AbstractHttpConfigurer::disable);
>         
>         return http.build();
>     }
>     
>     @Bean
>     public UserDetailsService basicAuthUserDetailsService() {
>         UserDetails user = User.builder()
>             .username("basicuser")
>             .password(passwordEncoder().encode("password"))
>             .roles("USER")
>             .build();
>         
>         UserDetails admin = User.builder()
>             .username("basicadmin")
>             .password(passwordEncoder().encode("admin123"))
>             .roles("USER", "ADMIN")
>             .build();
>         
>         return new InMemoryUserDetailsManager(user, admin);
>     }
>     
>     @Bean
>     public PasswordEncoder passwordEncoder() {
>         return new BCryptPasswordEncoder();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/basic/BasicAuthController.java
> package com.example.security.basic;
> 
> import org.springframework.security.core.Authentication;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/basic")
> public class BasicAuthController {
>     
>     @GetMapping("/public/info")
>     public Map<String, String> publicInfo() {
>         return Map.of("message", "Public information - no auth required");
>     }
>     
>     @GetMapping("/user/profile")
>     public Map<String, Object> userProfile(Authentication authentication) {
>         return Map.of(
>             "message", "User profile",
>             "username", authentication.getName(),
>             "authorities", authentication.getAuthorities()
>         );
>     }
>     
>     @GetMapping("/admin/dashboard")
>     public Map<String, String> adminDashboard(Authentication authentication) {
>         return Map.of(
>             "message", "Admin dashboard",
>             "username", authentication.getName()
>         );
>     }
> }
> ```
> 
> ## 8. Digest Authentication Pattern
> 
> ```java src/main/java/com/example/security/digest/DigestAuthConfig.java
> package com.example.security.digest;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.core.userdetails.User;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.security.provisioning.InMemoryUserDetailsManager;
> import org.springframework.security.web.SecurityFilterChain;
> import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
> import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
> 
> @Configuration
> public class DigestAuthConfig {
>     
>     @Bean
>     public SecurityFilterChain digestAuthSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/digest/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .anyRequest().authenticated()
>             )
>             .addFilter(digestAuthenticationFilter())
>             .exceptionHandling(exception -> exception
>                 .authenticationEntryPoint(digestEntryPoint())
>             )
>             .csrf(csrf -> csrf.disable());
>         
>         return http.build();
>     }
>     
>     @Bean
>     public DigestAuthenticationFilter digestAuthenticationFilter() {
>         DigestAuthenticationFilter filter = new DigestAuthenticationFilter();
>         filter.setUserDetailsService(digestUserDetailsService());
>         filter.setAuthenticationEntryPoint(digestEntryPoint());
>         return filter;
>     }
>     
>     @Bean
>     public DigestAuthenticationEntryPoint digestEntryPoint() {
>         DigestAuthenticationEntryPoint entryPoint = new DigestAuthenticationEntryPoint();
>         entryPoint.setRealmName("Digest Auth Realm");
>         entryPoint.setKey("unique-digest-key-12345");
>         return entryPoint;
>     }
>     
>     @Bean
>     public UserDetailsService digestUserDetailsService() {
>         var user = User.withUsername("digestuser")
>             .password("digestpass")
>             .roles("USER")
>             .build();
>         
>         return new InMemoryUserDetailsManager(user);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/digest/DigestAuthController.java
> package com.example.security.digest;
> 
> import org.springframework.security.core.Authentication;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/digest")
> public class DigestAuthController {
>     
>     @GetMapping("/secure-data")
>     public Map<String, Object> secureData(Authentication authentication) {
>         return Map.of(
>             "message", "Secured with Digest Authentication",
>             "username", authentication.getName(),
>             "timestamp", System.currentTimeMillis()
>         );
>     }
> }
> ```
> 
> ## 9. Certificate-based Authentication Pattern
> 
> ```java src/main/java/com/example/security/certificate/CertificateAuthConfig.java
> package com.example.security.certificate;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.core.authority.AuthorityUtils;
> import org.springframework.security.core.userdetails.User;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class CertificateAuthConfig {
>     
>     @Bean
>     public SecurityFilterChain certificateAuthSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/certificate/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .anyRequest().authenticated()
>             )
>             .x509(x509 -> x509
>                 .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
>                 .userDetailsService(certificateUserDetailsService())
>             )
>             .csrf(csrf -> csrf.disable());
>         
>         return http.build();
>     }
>     
>     @Bean
>     public UserDetailsService certificateUserDetailsService() {
>         return username -> {
>             // In production, look up user details from database based on certificate CN
>             if (username.equals("client-cert")) {
>                 return User.withUsername(username)
>                     .password("")
>                     .authorities(AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_CLIENT"))
>                     .build();
>             }
>             throw new RuntimeException("Certificate not recognized: " + username);
>         };
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/certificate/CertificateAuthController.java
> package com.example.security.certificate;
> 
> import org.springframework.security.core.Authentication;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.security.Principal;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/certificate")
> public class CertificateAuthController {
>     
>     @GetMapping("/mtls-protected")
>     public Map<String, Object> mtlsProtected(Authentication authentication, Principal principal) {
>         return Map.of(
>             "message", "Mutual TLS authenticated",
>             "principal", principal.getName(),
>             "username", authentication.getName(),
>             "authorities", authentication.getAuthorities()
>         );
>     }
>     
>     @GetMapping("/client-info")
>     public Map<String, String> clientInfo(Principal principal) {
>         return Map.of(
>             "message", "Client certificate validated",
>             "clientCN", principal.getName()
>         );
>     }
> }
> ```
> 
> ## 10. SAML Integration Pattern
> 
> ```java src/main/java/com/example/security/saml/SamlConfig.java
> package com.example.security.saml;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
> import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
> import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
> import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class SamlConfig {
>     
>     @Bean
>     public SecurityFilterChain samlSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/saml/**", "/saml2/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .requestMatchers("/saml2/**").permitAll()
>                 .anyRequest().authenticated()
>             )
>             .saml2Login(saml2 -> saml2
>                 .defaultSuccessUrl("/api/saml/success")
>             )
>             .saml2Logout(saml2 -> saml2
>                 .logoutUrl("/api/saml/logout")
>             );
>         
>         return http.build();
>     }
>     
>     @Bean
>     public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
>         // Example configuration - in production, use actual IdP metadata URL
>         RelyingPartyRegistration registration = RelyingPartyRegistrations
>             .fromMetadataLocation("https://dev-example.okta.com/app/metadata")
>             .registrationId("okta")
>             .build();
>         
>         return new InMemoryRelyingPartyRegistrationRepository(registration);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/saml/SamlController.java
> package com.example.security.saml;
> 
> import org.springframework.security.core.Authentication;
> import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/saml")
> public class SamlController {
>     
>     @GetMapping("/success")
>     public Map<String, Object> loginSuccess(Authentication authentication) {
>         Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
>         
>         return Map.of(
>             "message", "SAML authentication successful",
>             "username", principal.getName(),
>             "attributes", principal.getAttributes(),
>             "sessionIndex", principal.getSessionIndexes()
>         );
>     }
>     
>     @GetMapping("/user-info")
>     public Map<String, Object> userInfo(Authentication authentication) {
>         Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
>         
>         return Map.of(
>             "name", principal.getName(),
>             "email", principal.getFirstAttribute("email"),
>             "attributes", principal.getAttributes()
>         );
>     }
> }
> ```
> 
> ## 11. OpenID Connect Pattern
> 
> ```java src/main/java/com/example/security/oidc/OidcConfig.java
> package com.example.security.oidc;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
> import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
> import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
> import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
> import org.springframework.security.oauth2.core.oidc.user.OidcUser;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class OidcConfig {
>     
>     @Bean
>     public SecurityFilterChain oidcSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/oidc/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .anyRequest().authenticated()
>             )
>             .oauth2Login(oauth2 -> oauth2
>                 .userInfoEndpoint(userInfo -> userInfo
>                     .oidcUserService(oidcUserService())
>                 )
>                 .defaultSuccessUrl("/api/oidc/success")
>             )
>             .oauth2Client(oauth2 -> {});
>         
>         return http.build();
>     }
>     
>     @Bean
>     public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
>         final OidcUserService delegate = new OidcUserService();
>         
>         return (userRequest) -> {
>             OidcUser oidcUser = delegate.loadUser(userRequest);
>             
>             // Custom user processing can be done here
>             // For example, sync with local database, map roles, etc.
>             
>             return new DefaultOidcUser(
>                 oidcUser.getAuthorities(),
>                 oidcUser.getIdToken(),
>                 oidcUser.getUserInfo()
>             );
>         };
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/oidc/OidcController.java
> package com.example.security.oidc;
> 
> import org.springframework.security.core.annotation.AuthenticationPrincipal;
> import org.springframework.security.oauth2.core.oidc.user.OidcUser;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/oidc")
> public class OidcController {
>     
>     @GetMapping("/success")
>     public Map<String, String> loginSuccess(@AuthenticationPrincipal OidcUser oidcUser) {
>         return Map.of(
>             "message", "OpenID Connect authentication successful",
>             "username", oidcUser.getName()
>         );
>     }
>     
>     @GetMapping("/userinfo")
>     public Map<String, Object> userInfo(@AuthenticationPrincipal OidcUser oidcUser) {
>         return Map.of(
>             "subject", oidcUser.getSubject(),
>             "email", oidcUser.getEmail(),
>             "name", oidcUser.getFullName(),
>             "claims", oidcUser.getClaims(),
>             "idToken", oidcUser.getIdToken().getTokenValue()
>         );
>     }
>     
>     @GetMapping("/id-token")
>     public Map<String, Object> idToken(@AuthenticationPrincipal OidcUser oidcUser) {
>         return Map.of(
>             "tokenValue", oidcUser.getIdToken().getTokenValue(),
>             "issuedAt", oidcUser.getIdToken().getIssuedAt(),
>             "expiresAt", oidcUser.getIdToken().getExpiresAt(),
>             "claims", oidcUser.getIdToken().getClaims()
>         );
>     }
> }
> ```
> 
> ## 12. Token Introspection Pattern
> 
> ```java src/main/java/com/example/security/introspection/TokenIntrospectionConfig.java
> package com.example.security.introspection;
> 
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
> import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class TokenIntrospectionConfig {
>     
>     @Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:http://localhost:9000/oauth2/introspect}")
>     private String introspectionUri;
>     
>     @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id:resource-server}")
>     private String clientId;
>     
>     @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret:secret}")
>     private String clientSecret;
>     
>     @Bean
>     public SecurityFilterChain introspectionSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/introspection/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .anyRequest().authenticated()
>             )
>             .oauth2ResourceServer(oauth2 -> oauth2
>                 .opaqueToken(opaque -> opaque
>                     .introspector(opaqueTokenIntrospector())
>                 )
>             );
>         
>         return http.build();
>     }
>     
>     @Bean
>     public OpaqueTokenIntrospector opaqueTokenIntrospector() {
>         return new SpringOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/introspection/CustomTokenIntrospector.java
> package com.example.security.introspection;
> 
> import org.springframework.security.core.GrantedAuthority;
> import org.springframework.security.core.authority.SimpleGrantedAuthority;
> import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
> import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
> import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
> import org.springframework.stereotype.Component;
> 
> import java.time.Instant;
> import java.util.Collection;
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> 
> @Component
> public class CustomTokenIntrospector implements OpaqueTokenIntrospector {
>     
>     @Override
>     public OAuth2AuthenticatedPrincipal introspect(String token) {
>         // In production, this would call an actual introspection endpoint
>         // For demo purposes, we're simulating the introspection
>         
>         Map<String, Object> attributes = new HashMap<>();
>         attributes.put("active", true);
>         attributes.put("sub", "user123");
>         attributes.put("scope", "read write");
>         attributes.put("client_id", "demo-client");
>         attributes.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());
>         attributes.put("iat", Instant.now().getEpochSecond());
>         
>         Collection<GrantedAuthority> authorities = List.of(
>             new SimpleGrantedAuthority("SCOPE_read"),
>             new SimpleGrantedAuthority("SCOPE_write")
>         );
>         
>         return new DefaultOAuth2AuthenticatedPrincipal(
>             attributes.get("sub").toString(),
>             attributes,
>             authorities
>         );
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/introspection/TokenIntrospectionController.java
> package com.example.security.introspection;
> 
> import org.springframework.security.core.Authentication;
> import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/introspection")
> public class TokenIntrospectionController {
>     
>     @GetMapping("/validate")
>     public Map<String, Object> validateToken(Authentication authentication) {
>         OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
>         
>         return Map.of(
>             "message", "Token is valid",
>             "principal", principal.getName(),
>             "attributes", principal.getAttributes(),
>             "authorities", authentication.getAuthorities()
>         );
>     }
>     
>     @GetMapping("/token-details")
>     public Map<String, Object> tokenDetails(Authentication authentication) {
>         OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
>         
>         return Map.of(
>             "active", true,
>             "subject", principal.getName(),
>             "clientId", principal.getAttribute("client_id"),
>             "scope", principal.getAttribute("scope"),
>             "expiresAt", principal.getAttribute("exp")
>         );
>     }
> }
> ```
> 
> ## 13. Token Refresh Pattern
> 
> ```java src/main/java/com/example/security/refresh/TokenRefreshConfig.java
> package com.example.security.refresh;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.web.SecurityFilterChain;
> 
> @Configuration
> public class TokenRefreshConfig {
>     
>     @Bean
>     public SecurityFilterChain refreshTokenSecurityFilterChain(HttpSecurity http) throws Exception {
>         http
>             .securityMatcher("/api/refresh/**")
>             .authorizeHttpRequests(authorize -> authorize
>                 .requestMatchers("/api/refresh/token").permitAll()
>                 .anyRequest().authenticated()
>             )
>             .csrf(csrf -> csrf.disable());
>         
>         return http.build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/refresh/RefreshTokenService.java
> package com.example.security.refresh;
> 
> import org.springframework.stereotype.Service;
> 
> import java.time.Instant;
> import java.util.Map;
> import java.util.UUID;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Service
> public class RefreshTokenService {
>     
>     private final Map<String, RefreshToken> refreshTokens = new ConcurrentHashMap<>();
>     private static final long REFRESH_TOKEN_VALIDITY = 30 * 24 * 60 * 60; // 30 days
>     
>     public RefreshToken createRefreshToken(String username) {
>         String token = UUID.randomUUID().toString();
>         Instant expiryDate = Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY);
>         
>         RefreshToken refreshToken = RefreshToken.builder()
>             .token(token)
>             .username(username)
>             .expiryDate(expiryDate)
>             .build();
>         
>         refreshTokens.put(token, refreshToken);
>         return refreshToken;
>     }
>     
>     public RefreshToken verifyExpiration(RefreshToken token) {
>         if (token.getExpiryDate().isBefore(Instant.now())) {
>             refreshTokens.remove(token.getToken());
>             throw new RuntimeException("Refresh token expired");
>         }
>         return token;
>     }
>     
>     public RefreshToken findByToken(String token) {
>         RefreshToken refreshToken = refreshTokens.get(token);
>         if (refreshToken == null) {
>             throw new RuntimeException("Refresh token not found");
>         }
>         return refreshToken;
>     }
>     
>     public void deleteByToken(String token) {
>         refreshTokens.remove(token);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/security/refresh/RefreshToken.java
> package com.example.security.refresh;
> 
> import lombok.Builder;
> import lombok.Data;
> 
> import java.time.Instant;
> 
> @Data
> @Builder
> public class RefreshToken {
>     private String token;
>     private String username;
>     private Instant expiryDate;
> }
> ```
> 
> ```java src/main/java/com/example/security/refresh/TokenRefreshController.java
> package com.example.security.refresh;
> 
> import com.example.security.jwt.JwtTokenProvider;
> import lombok.Data;
> import org.springframework.http.ResponseEntity;
> import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
> import org.springframework.security.core.Authentication;
> import org.springframework.security.core.userdetails.UserDetails;
> import org.springframework.security.core.userdetails.UserDetailsService;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/refresh")
> public class TokenRefreshController {
>     
>     private final RefreshTokenService refreshTokenService;
>     private final JwtTokenProvider jwtTokenProvider;
>     private final UserDetailsService userDetailsService;
>     
>     public TokenRefreshController(RefreshTokenService refreshTokenService,
>                                   JwtTokenProvider jwtTokenProvider,
>                                   UserDetailsService userDetailsService) {
>         this.refreshTokenService = refreshTokenService;
>         this.jwtTokenProvider = jwtTokenProvider;
>         this.userDetailsService = userDetailsService;
>     }
>     
>     @PostMapping("/token")
>     public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
>         String requestRefreshToken = request.getRefreshToken();
>         
>         RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
>         refreshTokenService.verifyExpiration(refreshToken);
>         
>         UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUsername());
>         Authentication authentication = new UsernamePasswordAuthenticationToken(
>             userDetails, null, userDetails.getAuthorities()
>         );
>         
>         String newAccessToken = jwtTokenProvider.generateToken(authentication);
>         
>         return ResponseEntity.ok(TokenRefreshResponse.builder()
>             .accessToken(newAccessToken)
>             .refreshToken(requestRefreshToken)
>             .tokenType("Bearer")
>             .build());
>     }
>     
>     @PostMapping("/logout")
>     public ResponseEntity<String> logout(@RequestBody TokenRefreshRequest request) {
>         refreshTokenService.deleteByToken(request.getRefreshToken());
>         return ResponseEntity.ok("Logout successful");
>     }
>     
>     @Data
>     static class TokenRefreshRequest {
>         private String refreshToken;
>     }
>     
>     @Data
>     @lombok.Builder
>     static class TokenRefreshResponse {
>         private String accessToken;
>         private String refreshToken;
>         private String tokenType;
>     }
> }
> ```
> 
> ## Application Properties
> 
> ```properties src/main/resources/application.properties
> # Server Configuration
> server.port=8080
> 
> # H2 Database
> spring.datasource.url=jdbc:h2:mem:securitydb
> spring.datasource.driverClassName=org.h2.Driver
> spring.datasource.username=sa
> spring.datasource.password=
> spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
> spring.h2.console.enabled=true
> 
> # JWT Configuration
> jwt.expiration=86400000
> 
> # OAuth2 Client Configuration (Example with Google)
> spring.security.oauth2.client.registration.google.client-id=your-client-id
> spring.security.oauth2.client.registration.google.client-secret=your-client-secret
> spring.security.oauth2.client.registration.google.scope=openid,profile,email
> spring.security.oauth2.client.provider.google.issuer-uri=https://accounts.google.com
> 
> # OAuth2 Resource Server Configuration
> spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000
> 
> # Token Introspection Configuration
> spring.security.oauth2.resourceserver.opaque-token.introspection-uri=http://localhost:9000/oauth2/introspect
> spring.security.oauth2.resourceserver.opaque-token.client-id=resource-server
> spring.security.oauth2.resourceserver.opaque-token.client-secret=secret
> 
> # SAML Configuration
> spring.security.saml2.relyingparty.registration.okta.assertingparty.metadata-uri=https://dev-example.okta.com/app/metadata
> 
> # Logging
> logging.level.org.springframework.security=DEBUG
> logging.level.com.example.security=DEBUG
> ```
> 
> ## README Documentation
> 
> ```markdown README.md
> # API Security Patterns - Spring Boot Demo
> 
> This project demonstrates 13 different API security patterns using Spring Boot and Spring Security.
> 
> ## Patterns Implemented
> 
> ### 1. OAuth2 Pattern
> General OAuth2 configuration with login support.
> 
> **Endpoints:**
> - `/oauth2/success` - OAuth2 login success
> 
> ### 2. OAuth2 Client Pattern
> Demonstrates OAuth2 client functionality for accessing protected resources.
> 
> **Endpoints:**
> - `GET /api/oauth2-client/user-info` - Fetch user info from OAuth2 provider
> - `GET /api/oauth2-client/token-info` - View token information
> 
> ### 3. OAuth2 Resource Server Pattern
> Resource server protecting APIs using JWT tokens.
> 
> **Endpoints:**
> - `GET /api/resource/public/info` - Public endpoint
> - `GET /api/resource/protected/data` - Protected endpoint (requires valid JWT)
> - `GET /api/resource/admin/data` - Admin endpoint (requires 'admin' scope)
> 
> ### 4. OAuth2 Authorization Server Pattern
> Full OAuth2 authorization server implementation.
> 
> **Endpoints:**
> - `/oauth2/authorize` - Authorization endpoint
> - `/oauth2/token` - Token endpoint
> - `/.well-known/oauth-authorization-server` - Server metadata
> 
> ### 5. JWT Authentication Pattern
> Custom JWT-based authentication.
> 
> **Endpoints:**
> - `POST /api/jwt/auth/login` - Login and receive JWT token
> 
> **Request Body:**
> ```json
> {
>   "username": "user",
>   "password": "password"
> }
> ```
> 
> ### 6. API Key Pattern
> Authentication using API keys in headers.
> 
> **Endpoints:**
> - `GET /api/apikey/protected` - Protected resource
> 
> **Headers:**
> ```
> X-API-Key: demo-key-123
> ```
> 
> **Demo API Keys:**
> - `demo-key-123` - User role
> - `admin-key-456` - Admin role
> 
> ### 7. Basic Authentication Pattern
> HTTP Basic Authentication.
> 
> **Endpoints:**
> - `GET /api/basic/public/info` - Public endpoint
> - `GET /api/basic/user/profile` - User profile (requires auth)
> - `GET /api/basic/admin/dashboard` - Admin dashboard (requires ADMIN role)
> 
> **Credentials:**
> - User: `basicuser` / `password`
> - Admin: `basicadmin` / `admin123`
> 
> ### 8. Digest Authentication Pattern
> HTTP Digest Authentication.
> 
> **Endpoints:**
> - `GET /api/digest/secure-data` - Secured with digest auth
> 
> **Credentials:**
> - User: `digestuser` / `digestpass`
> 
> ### 9. Certificate-based Authentication (mTLS)
> Mutual TLS authentication using client certificates.
> 
> **Endpoints:**
> - `GET /api/certificate/

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```markdown
> - `GET /api/certificate/mtls-protected` - Protected by client certificate
> - `GET /api/certificate/client-info` - View client certificate info
> 
> **Setup:**
> Requires client certificate with CN matching registered users.
> 
> ### 10. SAML Integration Pattern
> SAML 2.0 Service Provider integration.
> 
> **Endpoints:**
> - `/saml2/authenticate/{registrationId}` - Initiate SAML login
> - `GET /api/saml/success` - SAML login success
> - `GET /api/saml/user-info` - View SAML user attributes
> - `POST /api/saml/logout` - SAML logout
> 
> ### 11. OpenID Connect Pattern
> OpenID Connect authentication.
> 
> **Endpoints:**
> - `GET /api/oidc/success` - OIDC login success
> - `GET /api/oidc/userinfo` - User information from OIDC
> - `GET /api/oidc/id-token` - ID token details
> 
> ### 12. Token Introspection Pattern
> OAuth2 token introspection for opaque tokens.
> 
> **Endpoints:**
> - `GET /api/introspection/validate` - Validate opaque token
> - `GET /api/introspection/token-details` - Token details
> 
> **Headers:**
> ```
> Authorization: Bearer <opaque-token>
> ```
> 
> ### 13. Token Refresh Pattern
> JWT token refresh mechanism.
> 
> **Endpoints:**
> - `POST /api/refresh/token` - Refresh access token
> - `POST /api/refresh/logout` - Logout and invalidate refresh token
> 
> **Request Body:**
> ```json
> {
>   "refreshToken": "your-refresh-token-here"
> }
> ```
> 
> ## Running the Application
> 
> ### Prerequisites
> - Java 17+
> - Maven 3.6+
> 
> ### Build
> ```bash
> mvn clean install
> ```
> 
> ### Run
> ```bash
> mvn spring-boot:run
> ```
> 
> The application will start on `http://localhost:8080`
> 
> ## Testing the Patterns
> 
> ### 1. Testing JWT Authentication
> ```bash
> # Login
> curl -X POST http://localhost:8080/api/jwt/auth/login \
>   -H "Content-Type: application/json" \
>   -d '{"username":"user","password":"password"}'
> 
> # Use the returned token
> curl -X GET http://localhost:8080/api/resource/protected/data \
>   -H "Authorization: Bearer <your-jwt-token>"
> ```
> 
> ### 2. Testing Basic Authentication
> ```bash
> curl -X GET http://localhost:8080/api/basic/user/profile \
>   -u basicuser:password
> ```
> 
> ### 3. Testing API Key
> ```bash
> curl -X GET http://localhost:8080/api/apikey/protected \
>   -H "X-API-Key: demo-key-123"
> ```
> 
> ### 4. Testing Digest Authentication
> ```bash
> curl -X GET http://localhost:8080/api/digest/secure-data \
>   --digest -u digestuser:digestpass
> ```
> 
> ### 5. Testing Token Refresh
> ```bash
> # First login to get tokens
> curl -X POST http://localhost:8080/api/jwt/auth/login \
>   -H "Content-Type: application/json" \
>   -d '{"username":"user","password":"password"}'
> 
> # Use refresh token to get new access token
> curl -X POST http://localhost:8080/api/refresh/token \
>   -H "Content-Type: application/json" \
>   -d '{"refreshToken":"<your-refresh-token>"}'
> ```
> 
> ## Security Considerations
> 
> ### Production Recommendations
> 
> 1. **JWT Pattern**
>    - Use strong secret keys (minimum 256 bits)
>    - Store secrets in environment variables or secret management systems
>    - Implement token blacklisting for logout
>    - Use short-lived access tokens with refresh tokens
> 
> 2. **API Key Pattern**
>    - Store API keys hashed in database
>    - Implement rate limiting per API key
>    - Support key rotation
>    - Monitor and log API key usage
> 
> 3. **OAuth2/OIDC**
>    - Use HTTPS only
>    - Validate redirect URIs strictly
>    - Implement PKCE for public clients
>    - Use state parameter to prevent CSRF
> 
> 4. **Certificate Authentication**
>    - Use proper certificate validation
>    - Implement certificate revocation checking (CRL/OCSP)
>    - Store certificates securely
>    - Implement certificate rotation
> 
> 5. **SAML**
>    - Validate SAML assertions properly
>    - Use signed and encrypted assertions
>    - Implement replay attack prevention
>    - Validate audience restrictions
> 
> ## Configuration
> 
> ### Environment Variables
> ```bash
> export JWT_SECRET=your-secret-key
> export OAUTH2_CLIENT_ID=your-client-id
> export OAUTH2_CLIENT_SECRET=your-client-secret
> ```
> 
> ### SSL/TLS Configuration (for mTLS)
> ```properties
> server.ssl.enabled=true
> server.ssl.key-store=classpath:keystore.p12
> server.ssl.key-store-password=password
> server.ssl.key-store-type=PKCS12
> server.ssl.trust-store=classpath:truststore.p12
> server.ssl.trust-store-password=password
> server.ssl.client-auth=need
> ```
> 
> ## Project Structure
> ```
> src/main/java/com/example/security/
> ├── ApiSecurityPatternsApplication.java
> ├── config/
> │   └── OAuth2Config.java
> ├── oauth2/
> │   ├── client/
> │   │   ├── OAuth2ClientController.java
> │   │   └── OAuth2TokenInfo.java
> │   ├── resourceserver/
> │   │   ├── ResourceServerConfig.java
> │   │   ├── CustomJwtAuthenticationConverter.java
> │   │   └── ResourceController.java
> │   └── authserver/
> │       └── AuthorizationServerConfig.java
> ├── jwt/
> │   ├── JwtConfig.java
> │   ├── JwtTokenProvider.java
> │   ├── JwtAuthenticationFilter.java
> │   └── JwtAuthController.java
> ├── apikey/
> │   ├── ApiKeyConfig.java
> │   ├── ApiKeyAuthenticationFilter.java
> │   ├── ApiKeyAuthentication.java
> │   ├── ApiKeyService.java
> │   └── ApiKeyController.java
> ├── basic/
> │   ├── BasicAuthConfig.java
> │   └── BasicAuthController.java
> ├── digest/
> │   ├── DigestAuthConfig.java
> │   └── DigestAuthController.java
> ├── certificate/
> │   ├── CertificateAuthConfig.java
> │   └── CertificateAuthController.java
> ├── saml/
> │   ├── SamlConfig.java
> │   └── SamlController.java
> ├── oidc/
> │   ├── OidcConfig.java
> │   └── OidcController.java
> ├── introspection/
> │   ├── TokenIntrospectionConfig.java
> │   ├── CustomTokenIntrospector.java
> │   └── TokenIntrospectionController.java
> └── refresh/
>     ├── TokenRefreshConfig.java
>     ├── RefreshTokenService.java
>     ├── RefreshToken.java
>     └── TokenRefreshController.java
> ```
> 
> ## Common Issues and Solutions
> 
> ### Issue: Multiple SecurityFilterChain beans conflict
> **Solution:** Each pattern uses `securityMatcher()` to handle specific URL patterns, preventing conflicts.
> 
> ### Issue: JWT signature validation fails
> **Solution:** Ensure the same secret key is used for signing and validation. Check token expiration.
> 
> ### Issue: OAuth2 redirect URI mismatch
> **Solution:** Ensure registered redirect URIs match exactly with the callback URL.
> 
> ### Issue: CORS errors
> **Solution:** Configure CORS in SecurityFilterChain:
> ```java
> http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
> ```
> 
> ## Further Reading
> 
> - [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
> - [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
> - [JWT RFC 7519](https://tools.ietf.org/html/rfc7519)
> - [SAML 2.0 Specification](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)
> - [OpenID Connect Specification](https://openid.net/specs/openid-connect-core-1_0.html)
> 
> ## License
> MIT License
> ```
> 
> ## Additional Test Controller
> 
> ```java src/main/java/com/example/security/test/SecurityTestController.java
> package com.example.security.test;
> 
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/test")
> public class SecurityTestController {
>     
>     @GetMapping("/health")
>     public Map<String, String> health() {
>         return Map.of(
>             "status", "UP",
>             "message", "API Security Patterns Application is running"
>         );
>     }
>     
>     @GetMapping("/patterns")
>     public Map<String, Object> listPatterns() {
>         return Map.of(
>             "patterns", new String[]{
>                 "OAuth2 Pattern",
>                 "OAuth2 Client Pattern",
>                 "OAuth2 Resource Server Pattern",
>                 "OAuth2 Authorization Server Pattern",
>                 "JWT Authentication Pattern",
>                 "API Key Pattern",
>                 "Basic Authentication Pattern",
>                 "Digest Authentication Pattern",
>                 "Certificate-based Authentication Pattern",
>                 "SAML Integration Pattern",
>                 "OpenID Connect Pattern",
>                 "Token Introspection Pattern",
>                 "Token Refresh Pattern"
>             },
>             "totalPatterns", 13
>         );
>     }
> }
> ```
> 
> ## Complete Docker Configuration (Optional)
> 
> ```dockerfile Dockerfile
> FROM eclipse-temurin:17-jdk-alpine
> WORKDIR /app
> COPY target/*.jar app.jar
> EXPOSE 8080
> ENTRYPOINT ["java", "-jar", "app.jar"]
> ```
> 
> ```yaml docker-compose.yml
> version: '3.8'
> 
> services:
>   app:
>     build: .
>     ports:
>       - "8080:8080"
>     environment:
>       - SPRING_PROFILES_ACTIVE=docker
>       - JWT_SECRET=${JWT_SECRET}
>     networks:
>       - security-network
> 
> networks:
>   security-network:
>     driver: bridge
> ```
> 
> ---
> 
> ## Summary
> 
> This comprehensive Spring Boot application demonstrates **13 different API security patterns**:
> 
> ✅ **OAuth2 Pattern** - General OAuth2 configuration  
> ✅ **OAuth2 Client Pattern** - Client consuming OAuth2 protected resources  
> ✅ **OAuth2 Resource Server Pattern** - Protecting APIs with JWT  
> ✅ **OAuth2 Authorization Server Pattern** - Full authorization server  
> ✅ **JWT Authentication Pattern** - Custom JWT implementation  
> ✅ **API Key Pattern** - Header-based API key authentication  
> ✅ **Basic Authentication Pattern** - HTTP Basic Auth  
> ✅ **Digest Authentication Pattern** - HTTP Digest Auth  
> ✅ **Certificate-based Authentication** - Mutual TLS (mTLS)  
> ✅ **SAML Integration Pattern** - SAML 2.0 Service Provider  
> ✅ **OpenID Connect Pattern** - OIDC authentication  
> ✅ **Token Introspection Pattern** - OAuth2 token validation  
> ✅ **Token Refresh Pattern** - Refresh token implementation  
> 
> Each pattern is isolated using Spring Security's `securityMatcher()` to prevent conflicts and can be tested independently using the provided curl commands in the README.