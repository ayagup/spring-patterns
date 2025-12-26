package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie Pattern
 * 
 * Demonstrates how to read and write HTTP cookies in Spring MVC.
 * @CookieValue annotation binds cookie values to method parameters.
 */
@SpringBootApplication
public class CookiePattern {

    public static void main(String[] args) {
        SpringApplication.run(CookiePattern.class, args);
    }

    @RestController
    @RequestMapping("/api/cookies")
    static class CookieController {

        /**
         * Read simple cookie
         */
        @GetMapping("/read")
        public String readCookie(@CookieValue(name = "username", defaultValue = "Guest") String username) {
            return "Username from cookie: " + username;
        }

        /**
         * Read multiple cookies
         */
        @GetMapping("/read-all")
        public String readAllCookies(
                @CookieValue(name = "sessionId", required = false) String sessionId,
                @CookieValue(name = "userId", required = false) String userId,
                @CookieValue(name = "theme", defaultValue = "light") String theme) {
            return String.format("SessionId: %s, UserId: %s, Theme: %s",
                    sessionId != null ? sessionId : "N/A",
                    userId != null ? userId : "N/A",
                    theme);
        }

        /**
         * Set simple cookie
         */
        @GetMapping("/set")
        public String setCookie(HttpServletResponse response) {
            Cookie cookie = new Cookie("username", "JohnDoe");
            cookie.setMaxAge(3600); // 1 hour
            cookie.setPath("/");
            response.addCookie(cookie);
            return "Cookie set successfully";
        }

        /**
         * Set multiple cookies
         */
        @GetMapping("/set-multiple")
        public String setMultipleCookies(HttpServletResponse response) {
            // Session cookie
            Cookie sessionCookie = new Cookie("sessionId", "sess_" + System.currentTimeMillis());
            sessionCookie.setMaxAge(1800); // 30 minutes
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);

            // Preference cookie
            Cookie themeCookie = new Cookie("theme", "dark");
            themeCookie.setMaxAge(86400 * 30); // 30 days
            themeCookie.setPath("/");

            response.addCookie(sessionCookie);
            response.addCookie(themeCookie);

            return "Multiple cookies set";
        }

        /**
         * Secure cookie (HTTPS only)
         */
        @GetMapping("/set-secure")
        public String setSecureCookie(HttpServletResponse response) {
            Cookie secureCookie = new Cookie("authToken", "token_12345");
            secureCookie.setMaxAge(3600);
            secureCookie.setPath("/");
            secureCookie.setSecure(true); // HTTPS only
            secureCookie.setHttpOnly(true); // Not accessible via JavaScript
            response.addCookie(secureCookie);
            return "Secure cookie set";
        }

        /**
         * Delete/Remove cookie
         */
        @GetMapping("/delete")
        public String deleteCookie(HttpServletResponse response) {
            Cookie cookie = new Cookie("username", null);
            cookie.setMaxAge(0); // Delete immediately
            cookie.setPath("/");
            response.addCookie(cookie);
            return "Cookie deleted";
        }

        /**
         * Update cookie value
         */
        @GetMapping("/update")
        public String updateCookie(
                @CookieValue(name = "username", defaultValue = "Guest") String oldValue,
                @RequestParam String newValue,
                HttpServletResponse response) {
            Cookie cookie = new Cookie("username", newValue);
            cookie.setMaxAge(3600);
            cookie.setPath("/");
            response.addCookie(cookie);
            return String.format("Cookie updated from '%s' to '%s'", oldValue, newValue);
        }

        /**
         * Session cookie (no expiry)
         */
        @GetMapping("/session-cookie")
        public String setSessionCookie(HttpServletResponse response) {
            Cookie sessionCookie = new Cookie("tempSession", "temp_" + System.currentTimeMillis());
            // No setMaxAge - cookie expires when browser closes
            sessionCookie.setPath("/");
            response.addCookie(sessionCookie);
            return "Session cookie set (expires on browser close)";
        }

        /**
         * Cookie with domain and path
         */
        @GetMapping("/scoped")
        public String setScopedCookie(HttpServletResponse response) {
            Cookie cookie = new Cookie("scopedCookie", "scoped_value");
            cookie.setMaxAge(3600);
            cookie.setPath("/api"); // Only available under /api path
            cookie.setDomain("localhost"); // Specify domain
            response.addCookie(cookie);
            return "Scoped cookie set";
        }

        /**
         * SameSite cookie attribute (Spring 5.x / Spring Boot 2.6+)
         */
        @GetMapping("/samesite")
        public ResponseEntity<String> setSameSiteCookie(HttpServletResponse response) {
            // Note: SameSite attribute requires manual header setting in older Spring versions
            response.addHeader("Set-Cookie", "sameSiteCookie=value; Path=/; Max-Age=3600; SameSite=Strict; Secure; HttpOnly");
            return ResponseEntity.ok("SameSite cookie set");
        }

        /**
         * Read cookie and perform action
         */
        @GetMapping("/preferences")
        public String getUserPreferences(
                @CookieValue(name = "theme", defaultValue = "light") String theme,
                @CookieValue(name = "language", defaultValue = "en") String language,
                @CookieValue(name = "fontSize", defaultValue = "14") int fontSize) {
            return String.format("User Preferences - Theme: %s, Language: %s, Font Size: %dpx",
                    theme, language, fontSize);
        }
    }

    /**
     * Shopping cart cookie example
     */
    @RestController
    @RequestMapping("/api/cart")
    static class ShoppingCartController {

        @PostMapping("/add")
        public String addToCart(
                @RequestParam String productId,
                @CookieValue(name = "cart", required = false) String cart,
                HttpServletResponse response) {
            String newCart = cart != null ? cart + "," + productId : productId;
            Cookie cartCookie = new Cookie("cart", newCart);
            cartCookie.setMaxAge(86400 * 7); // 7 days
            cartCookie.setPath("/");
            response.addCookie(cartCookie);
            return "Product added to cart";
        }

        @GetMapping("/view")
        public String viewCart(@CookieValue(name = "cart", defaultValue = "") String cart) {
            return cart.isEmpty() ? "Cart is empty" : "Cart items: " + cart;
        }

        @DeleteMapping("/clear")
        public String clearCart(HttpServletResponse response) {
            Cookie cartCookie = new Cookie("cart", null);
            cartCookie.setMaxAge(0);
            cartCookie.setPath("/");
            response.addCookie(cartCookie);
            return "Cart cleared";
        }
    }
}
