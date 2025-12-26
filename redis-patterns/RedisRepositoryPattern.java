package com.example.redis.repository;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis Repository Pattern
 * 
 * Demonstrates the use of Spring Data Redis Repository for object mapping.
 * Redis Repository provides:
 * - CRUD operations through repository interface
 * - Automatic object-to-hash mapping
 * - Secondary indexes for non-primary-key lookups
 * - TTL (Time-To-Live) support
 * - Query method derivation
 * - Custom query methods
 * 
 * Use cases:
 * - Domain object caching
 * - Session management
 * - User profiles storage
 * - Shopping cart management
 * - Temporary data storage with expiration
 */

@Configuration
@EnableRedisRepositories
class RedisRepositoryConfig {
}

@RedisHash("sessions")
record UserSession(
    @Id String id,
    String userId,
    @Indexed String username,
    String ipAddress,
    @Indexed String deviceId,
    LocalDateTime loginTime,
    LocalDateTime lastAccessTime,
    boolean active,
    @TimeToLive(unit = TimeUnit.MINUTES) Long ttl
) {}

@RedisHash("carts")
record ShoppingCart(
    @Id String id,
    @Indexed String userId,
    List<CartItem> items,
    double totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @TimeToLive(unit = TimeUnit.HOURS) Long ttl
) {}

record CartItem(String productId, String name, int quantity, double price) {}

@Repository
interface UserSessionRepository extends CrudRepository<UserSession, String> {
    
    // Query method derivation based on @Indexed fields
    List<UserSession> findByUsername(String username);
    
    List<UserSession> findByDeviceId(String deviceId);
    
    List<UserSession> findByUserId(String userId);
    
    List<UserSession> findByActive(boolean active);
    
    List<UserSession> findByUsernameAndActive(String username, boolean active);
    
    long countByUsername(String username);
    
    boolean existsByDeviceId(String deviceId);
    
    void deleteByUsername(String username);
}

@Repository
interface ShoppingCartRepository extends CrudRepository<ShoppingCart, String> {
    
    Optional<ShoppingCart> findByUserId(String userId);
    
    List<ShoppingCart> findByTotalAmountGreaterThan(double amount);
    
    long countByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    void deleteByUserId(String userId);
}

@Service
class UserSessionService {
    
    private final UserSessionRepository sessionRepository;
    
    public UserSessionService(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    public UserSession createSession(String userId, String username, String ipAddress, 
                                     String deviceId, long ttlMinutes) {
        UserSession session = new UserSession(
            null, // Auto-generated ID
            userId,
            username,
            ipAddress,
            deviceId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true,
            ttlMinutes
        );
        return sessionRepository.save(session);
    }
    
    public Optional<UserSession> getSession(String sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    public List<UserSession> getSessionsByUsername(String username) {
        return sessionRepository.findByUsername(username);
    }
    
    public List<UserSession> getActiveSessionsByUsername(String username) {
        return sessionRepository.findByUsernameAndActive(username, true);
    }
    
    public long countSessionsByUsername(String username) {
        return sessionRepository.countByUsername(username);
    }
    
    public UserSession updateLastAccess(String sessionId) {
        Optional<UserSession> optSession = sessionRepository.findById(sessionId);
        if (optSession.isPresent()) {
            UserSession session = optSession.get();
            UserSession updated = new UserSession(
                session.id(),
                session.userId(),
                session.username(),
                session.ipAddress(),
                session.deviceId(),
                session.loginTime(),
                LocalDateTime.now(),
                session.active(),
                session.ttl()
            );
            return sessionRepository.save(updated);
        }
        return null;
    }
    
    public void invalidateSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
    
    public void invalidateUserSessions(String username) {
        sessionRepository.deleteByUsername(username);
    }
    
    public List<UserSession> getAllActiveSessions() {
        return sessionRepository.findByActive(true);
    }
}

@Service
class ShoppingCartService {
    
    private final ShoppingCartRepository cartRepository;
    
    public ShoppingCartService(ShoppingCartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    public ShoppingCart createCart(String userId, long ttlHours) {
        ShoppingCart cart = new ShoppingCart(
            null, // Auto-generated ID
            userId,
            List.of(),
            0.0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            ttlHours
        );
        return cartRepository.save(cart);
    }
    
    public Optional<ShoppingCart> getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId);
    }
    
    public ShoppingCart addItem(String userId, CartItem item) {
        Optional<ShoppingCart> optCart = cartRepository.findByUserId(userId);
        ShoppingCart cart;
        
        if (optCart.isPresent()) {
            cart = optCart.get();
            List<CartItem> items = new java.util.ArrayList<>(cart.items());
            items.add(item);
            double newTotal = cart.totalAmount() + (item.quantity() * item.price());
            
            cart = new ShoppingCart(
                cart.id(),
                cart.userId(),
                items,
                newTotal,
                cart.createdAt(),
                LocalDateTime.now(),
                cart.ttl()
            );
        } else {
            cart = new ShoppingCart(
                null,
                userId,
                List.of(item),
                item.quantity() * item.price(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                24L // Default 24 hours
            );
        }
        
        return cartRepository.save(cart);
    }
    
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    public List<ShoppingCart> getHighValueCarts(double minAmount) {
        return cartRepository.findByTotalAmountGreaterThan(minAmount);
    }
    
    public boolean cartExists(String userId) {
        return cartRepository.existsByUserId(userId);
    }
}

@RestController
@RequestMapping("/api/redis/repository")
class RedisRepositoryController {
    
    private final UserSessionService sessionService;
    private final ShoppingCartService cartService;
    
    public RedisRepositoryController(UserSessionService sessionService, 
                                    ShoppingCartService cartService) {
        this.sessionService = sessionService;
        this.cartService = cartService;
    }
    
    @PostMapping("/sessions")
    public UserSession createSession(@RequestParam String userId,
                                     @RequestParam String username,
                                     @RequestParam String ipAddress,
                                     @RequestParam String deviceId,
                                     @RequestParam(defaultValue = "30") long ttlMinutes) {
        return sessionService.createSession(userId, username, ipAddress, deviceId, ttlMinutes);
    }
    
    @GetMapping("/sessions/{sessionId}")
    public UserSession getSession(@PathVariable String sessionId) {
        return sessionService.getSession(sessionId).orElse(null);
    }
    
    @GetMapping("/sessions/user/{username}")
    public List<UserSession> getUserSessions(@PathVariable String username) {
        return sessionService.getSessionsByUsername(username);
    }
    
    @GetMapping("/sessions/user/{username}/active")
    public List<UserSession> getActiveSessions(@PathVariable String username) {
        return sessionService.getActiveSessionsByUsername(username);
    }
    
    @GetMapping("/sessions/user/{username}/count")
    public long countSessions(@PathVariable String username) {
        return sessionService.countSessionsByUsername(username);
    }
    
    @PutMapping("/sessions/{sessionId}/access")
    public UserSession updateLastAccess(@PathVariable String sessionId) {
        return sessionService.updateLastAccess(sessionId);
    }
    
    @DeleteMapping("/sessions/{sessionId}")
    public String invalidateSession(@PathVariable String sessionId) {
        sessionService.invalidateSession(sessionId);
        return "Session invalidated";
    }
    
    @DeleteMapping("/sessions/user/{username}")
    public String invalidateUserSessions(@PathVariable String username) {
        sessionService.invalidateUserSessions(username);
        return "All user sessions invalidated";
    }
    
    @GetMapping("/sessions/active")
    public List<UserSession> getAllActiveSessions() {
        return sessionService.getAllActiveSessions();
    }
    
    @PostMapping("/carts")
    public ShoppingCart createCart(@RequestParam String userId,
                                   @RequestParam(defaultValue = "24") long ttlHours) {
        return cartService.createCart(userId, ttlHours);
    }
    
    @GetMapping("/carts/user/{userId}")
    public ShoppingCart getCart(@PathVariable String userId) {
        return cartService.getCartByUserId(userId).orElse(null);
    }
    
    @PostMapping("/carts/{userId}/items")
    public ShoppingCart addItem(@PathVariable String userId, @RequestBody CartItem item) {
        return cartService.addItem(userId, item);
    }
    
    @DeleteMapping("/carts/{userId}")
    public String clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return "Cart cleared";
    }
    
    @GetMapping("/carts/high-value")
    public List<ShoppingCart> getHighValueCarts(@RequestParam double minAmount) {
        return cartService.getHighValueCarts(minAmount);
    }
    
    @GetMapping("/carts/user/{userId}/exists")
    public boolean cartExists(@PathVariable String userId) {
        return cartService.cartExists(userId);
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Repository Pattern
                =======================
                Features:
                - CRUD operations via CrudRepository
                - Object-to-hash mapping with @RedisHash
                - Secondary indexes with @Indexed
                - TTL support with @TimeToLive
                - Query method derivation
                - Custom query methods
                
                Examples:
                - UserSession: Session management with TTL
                - ShoppingCart: Shopping cart with expiration
                
                Indexed fields enable fast lookups:
                - findByUsername, findByDeviceId
                - existsByUserId, countByUsername
                """;
    }
}
