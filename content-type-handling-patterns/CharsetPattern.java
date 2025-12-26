package com.example.contenttypehandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Charset Pattern
 * 
 * Demonstrates character encoding handling in Spring MVC for HTTP requests and responses.
 * This pattern shows how to configure, negotiate, and handle different character sets.
 * 
 * Key Concepts:
 * - Character encoding configuration
 * - Content-Type with charset parameter
 * - Message converter charset settings
 * - Accept-Charset header handling
 * - UTF-8, ISO-8859-1, and other encodings
 * 
 * Use Cases:
 * - Internationalization (i18n)
 * - Multi-language applications
 * - Legacy system integration
 * - Special character handling
 * - Encoding negotiation
 */
@SpringBootApplication
public class CharsetPattern {

    public static void main(String[] args) {
        SpringApplication.run(CharsetPattern.class, args);
    }
}

/**
 * Configuration for charset handling
 */
@Configuration
class CharsetConfig implements WebMvcConfigurer {

    /**
     * Configure message converters with charset support
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // UTF-8 String converter
        StringHttpMessageConverter utf8StringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        utf8StringConverter.setWriteAcceptCharset(false); // Don't write Accept-Charset header
        converters.add(utf8StringConverter);

        // ISO-8859-1 String converter for legacy systems
        StringHttpMessageConverter isoStringConverter = new StringHttpMessageConverter(StandardCharsets.ISO_8859_1);
        converters.add(isoStringConverter);

        // JSON converter with UTF-8
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        mediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));
        jsonConverter.setSupportedMediaTypes(mediaTypes);
        converters.add(jsonConverter);
    }

    /**
     * Bean for UTF-8 String converter
     */
    @Bean
    public StringHttpMessageConverter utf8StringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    /**
     * Bean for ISO-8859-1 String converter
     */
    @Bean
    public StringHttpMessageConverter isoStringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.ISO_8859_1);
    }
}

/**
 * Controller demonstrating charset handling
 */
@RestController
@RequestMapping("/api/charset")
class CharsetController {

    /**
     * Returns UTF-8 encoded JSON
     * Content-Type: application/json;charset=UTF-8
     */
    @GetMapping(value = "/utf8", produces = "application/json;charset=UTF-8")
    public Map<String, String> getUtf8Content() {
        return Map.of(
                "message", "Hello with UTF-8: こんにちは, 你好, مرحبا",
                "encoding", "UTF-8"
        );
    }

    /**
     * Returns ISO-8859-1 encoded text
     * Content-Type: text/plain;charset=ISO-8859-1
     */
    @GetMapping(value = "/iso", produces = "text/plain;charset=ISO-8859-1")
    public String getIsoContent() {
        return "Hello with ISO-8859-1: Héllo, Café";
    }

    /**
     * Returns UTF-16 encoded text
     * Content-Type: text/plain;charset=UTF-16
     */
    @GetMapping(value = "/utf16", produces = "text/plain;charset=UTF-16")
    public String getUtf16Content() {
        return "Hello with UTF-16: 你好世界";
    }

    /**
     * Accepts and returns content with explicit charset
     */
    @PostMapping(
            value = "/echo",
            consumes = "text/plain;charset=UTF-8",
            produces = "text/plain;charset=UTF-8"
    )
    public String echoUtf8(@RequestBody String content) {
        return "Echo (UTF-8): " + content;
    }

    /**
     * Returns content with charset specified in header
     */
    @GetMapping("/with-header")
    public ResponseEntity<String> getContentWithCharsetHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .headers(headers)
                .body("Content with UTF-8 charset in header");
    }
}

/**
 * Controller for multi-language content
 */
@RestController
@RequestMapping("/api/i18n")
class InternationalContentController {

    /**
     * Returns content in different languages with appropriate charset
     */
    @GetMapping(value = "/greeting/{lang}", produces = "application/json;charset=UTF-8")
    public Map<String, String> getGreeting(@PathVariable String lang) {
        return switch (lang.toLowerCase()) {
            case "en" -> Map.of("language", "English", "greeting", "Hello, World!");
            case "ja" -> Map.of("language", "Japanese", "greeting", "こんにちは、世界！");
            case "zh" -> Map.of("language", "Chinese", "greeting", "你好，世界！");
            case "ar" -> Map.of("language", "Arabic", "greeting", "مرحبا بالعالم!");
            case "ru" -> Map.of("language", "Russian", "greeting", "Привет, мир!");
            case "ko" -> Map.of("language", "Korean", "greeting", "안녕하세요, 세계!");
            case "hi" -> Map.of("language", "Hindi", "greeting", "नमस्ते दुनिया!");
            default -> Map.of("language", "Unknown", "greeting", "Hello!");
        };
    }

    /**
     * Returns unicode characters
     */
    @GetMapping(value = "/unicode", produces = "text/plain;charset=UTF-8")
    public String getUnicodeContent() {
        return "Unicode symbols: ★ ♠ ♣ ♥ ♦ ☺ ☻ ✓ ✗ € £ ¥ © ® ™";
    }

    /**
     * Returns emoji content (requires UTF-8)
     */
    @GetMapping(value = "/emoji", produces = "text/plain;charset=UTF-8")
    public String getEmojiContent() {
        return "Emojis: 😀 😃 😄 😁 🎉 🎊 👍 ❤️ 🌟 ⭐";
    }
}

/**
 * Controller demonstrating charset negotiation
 */
@RestController
@RequestMapping("/api/negotiate")
class CharsetNegotiationController {

    /**
     * Supports multiple charsets
     * Client can specify Accept-Charset header
     */
    @GetMapping(value = "/text", produces = {
            "text/plain;charset=UTF-8",
            "text/plain;charset=ISO-8859-1",
            "text/plain;charset=US-ASCII"
    })
    public String getTextWithNegotiation(@RequestHeader(value = "Accept-Charset", defaultValue = "UTF-8") String acceptCharset) {
        return "Text with negotiated charset: " + acceptCharset;
    }

    /**
     * Returns charset information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCharsetInfo(
            @RequestHeader(value = "Accept-Charset", required = false) String acceptCharset) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        
        Map<String, Object> info = Map.of(
                "defaultCharset", Charset.defaultCharset().name(),
                "systemCharset", System.getProperty("file.encoding"),
                "acceptCharset", acceptCharset != null ? acceptCharset : "Not specified",
                "supportedCharsets", List.of("UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII")
        );
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(info);
    }
}

/**
 * Controller demonstrating charset conversion
 */
@RestController
@RequestMapping("/api/convert")
class CharsetConversionController {

    /**
     * Convert between charsets
     */
    @PostMapping(value = "/charset")
    public ResponseEntity<String> convertCharset(
            @RequestBody String content,
            @RequestParam(defaultValue = "UTF-8") String sourceCharset,
            @RequestParam(defaultValue = "ISO-8859-1") String targetCharset) {
        
        try {
            // Convert string to bytes in source charset, then create string in target charset
            byte[] bytes = content.getBytes(sourceCharset);
            String converted = new String(bytes, targetCharset);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", Charset.forName(targetCharset)));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body("Converted from " + sourceCharset + " to " + targetCharset + ": " + converted);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error converting charset: " + e.getMessage());
        }
    }

    /**
     * Encode string to different charsets
     */
    @GetMapping("/encode/{text}")
    public Map<String, String> encodeText(@PathVariable String text) {
        return Map.of(
                "original", text,
                "utf8", new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                "iso88591", new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1),
                "utf16", new String(text.getBytes(StandardCharsets.UTF_16), StandardCharsets.UTF_16),
                "ascii", new String(text.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII)
        );
    }
}

/**
 * Controller for handling legacy systems with specific charsets
 */
@RestController
@RequestMapping("/api/legacy")
class LegacyCharsetController {

    /**
     * Endpoint for legacy system using ISO-8859-1
     */
    @PostMapping(
            value = "/import",
            consumes = "text/plain;charset=ISO-8859-1",
            produces = "application/json;charset=UTF-8"
    )
    public Map<String, String> importFromLegacy(@RequestBody String legacyData) {
        // Convert from ISO-8859-1 to UTF-8
        String utf8Data = new String(legacyData.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return Map.of(
                "status", "imported",
                "originalEncoding", "ISO-8859-1",
                "currentEncoding", "UTF-8",
                "data", utf8Data
        );
    }

    /**
     * Endpoint for exporting to legacy system
     */
    @GetMapping(value = "/export", produces = "text/plain;charset=ISO-8859-1")
    public String exportToLegacy() {
        return "Data for legacy system";
    }
}

/**
 * Documentation:
 * 
 * Common Character Sets:
 * - UTF-8: Universal, supports all characters, 1-4 bytes per character
 * - UTF-16: 2-4 bytes per character, used by Java internally
 * - ISO-8859-1 (Latin-1): Single byte, Western European characters
 * - US-ASCII: 7-bit, basic English characters only
 * - Windows-1252: Extension of ISO-8859-1
 * 
 * Content-Type Header with Charset:
 * - Format: Content-Type: media/type;charset=encoding
 * - Example: Content-Type: application/json;charset=UTF-8
 * - Example: Content-Type: text/html;charset=ISO-8859-1
 * 
 * Accept-Charset Header:
 * - Client specifies acceptable charsets
 * - Format: Accept-Charset: UTF-8, ISO-8859-1;q=0.8
 * - Quality values (q) indicate preference
 * - Server should honor if possible
 * 
 * Spring Boot Defaults:
 * - Default charset: UTF-8
 * - Can be changed via spring.http.encoding properties
 * - spring.http.encoding.charset=UTF-8
 * - spring.http.encoding.enabled=true
 * - spring.http.encoding.force=true
 * 
 * Message Converter Configuration:
 * - StringHttpMessageConverter handles text
 * - MappingJackson2HttpMessageConverter for JSON
 * - Each converter can have specific charset
 * - Converters tried in order
 * 
 * Best Practices:
 * - Always use UTF-8 for new applications
 * - Explicitly specify charset in Content-Type
 * - Handle charset conversion carefully
 * - Test with multi-byte characters
 * - Document charset requirements
 * - Validate input encoding
 * - Set writeAcceptCharset to false to avoid Accept-Charset response header
 * 
 * Common Issues:
 * - Mojibake (garbled characters) from wrong encoding
 * - Data loss when converting from larger to smaller charset
 * - BOM (Byte Order Mark) handling
 * - Normalization of unicode characters
 * - Encoding mismatches between layers
 * 
 * Internationalization Tips:
 * - UTF-8 supports all languages
 * - Store data in UTF-8
 * - Convert only at boundaries
 * - Use ResourceBundle for messages
 * - Consider locale-specific formatting
 * - Handle right-to-left languages
 * - Test with various language inputs
 * 
 * Performance Considerations:
 * - UTF-8 is variable length
 * - Conversion has overhead
 * - Cache converted strings if repeated
 * - Consider compression for large text
 */
