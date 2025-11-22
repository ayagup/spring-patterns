# Spring Internationalization (i18n) Patterns

Comprehensive guide to Spring Framework Internationalization patterns with complete Java implementations.

## 📚 Table of Contents

1. [Message Source Pattern](#message-source-pattern)
2. [Locale Resolver Pattern](#locale-resolver-pattern)
3. [Locale Change Interceptor Pattern](#locale-change-interceptor-pattern)
4. [Resource Bundle Pattern](#resource-bundle-pattern)
5. [Message Format Pattern](#message-format-pattern)
6. [Timezone Handling Pattern](#timezone-handling-pattern)
7. [Currency Formatting Pattern](#currency-formatting-pattern)
8. [Date/Time Formatting Pattern](#datetime-formatting-pattern)

---

## Message Source Pattern

**Purpose**: Centralized message management for internationalization with support for multiple languages and resource reloading.

### Key Components

- **ResourceBundleMessageSource**: Standard Java ResourceBundle-based messages
- **ReloadableResourceBundleMessageSource**: Messages with automatic reload capability
- **StaticMessageSource**: Programmatic message definition for testing

### Example Usage

```java
// Configuration
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = 
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("classpath:i18n/messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setCacheSeconds(60);
    return messageSource;
}

// Usage
String greeting = messageSource.getMessage("greeting", 
    new Object[]{"John"}, Locale.ENGLISH);
// Output: "Hello, John!"
```

### Best Practices

- ✅ Use `ReloadableResourceBundleMessageSource` for development
- ✅ Set appropriate cache duration for production
- ✅ Always specify UTF-8 encoding
- ✅ Use hierarchical message sources for common/specific messages
- ❌ Don't use `StaticMessageSource` in production

---

## Locale Resolver Pattern

**Purpose**: Determine and manage user locale preferences across requests.

### Resolver Types

| Resolver | Storage | Persistence | Use Case |
|----------|---------|-------------|----------|
| AcceptHeaderLocaleResolver | HTTP Header | None | Auto-detection |
| CookieLocaleResolver | Cookie | Long-term | User preference |
| SessionLocaleResolver | HTTP Session | Session | Temporary |
| FixedLocaleResolver | Configuration | Permanent | Single language |

### Example Configuration

```java
@Bean
public LocaleResolver localeResolver() {
    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.US);
    resolver.setCookieName("user_locale");
    resolver.setCookieMaxAge(7 * 24 * 60 * 60); // 7 days
    return resolver;
}
```

### Common Patterns

**Browser Detection**:
```java
AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
resolver.setSupportedLocales(Arrays.asList(
    Locale.US, Locale.UK, new Locale("es"), Locale.FRENCH
));
```

**Session-based**:
```java
SessionLocaleResolver resolver = new SessionLocaleResolver();
resolver.setDefaultLocale(Locale.US);
resolver.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));
```

---

## Locale Change Interceptor Pattern

**Purpose**: Enable dynamic locale switching based on request parameters, headers, or paths.

### Configuration

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }
}
```

### URL Examples

```
http://example.com/products?lang=en
http://example.com/products?lang=es
http://example.com/products?lang=fr
```

### Advanced Usage

**Header-based switching**:
```java
// Custom implementation
public class HeaderLocaleChangeInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String locale = request.getHeader("X-Locale");
        if (locale != null) {
            localeResolver.setLocale(request, response, 
                Locale.forLanguageTag(locale));
        }
        return true;
    }
}
```

---

## Resource Bundle Pattern

**Purpose**: Organize and manage locale-specific resources in property files.

### File Structure

```
src/main/resources/
└── messages/
    ├── messages.properties (default)
    ├── messages_en.properties (English)
    ├── messages_es.properties (Spanish)
    ├── messages_fr.properties (French)
    └── messages_de.properties (German)
```

### Property File Example

**messages_en.properties**:
```properties
app.title=My Application
greeting=Hello, {0}!
error.notfound=Item not found
button.submit=Submit
```

**messages_es.properties**:
```properties
app.title=Mi Aplicación
greeting=¡Hola, {0}!
error.notfound=Artículo no encontrado
button.submit=Enviar
```

### Loading Resources

```java
ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
String greeting = bundle.getString("greeting");
```

---

## Message Format Pattern

**Purpose**: Format parameterized messages with proper pluralization and choice formatting.

### Basic Formatting

```java
String pattern = "Hello, {0}!";
MessageFormat formatter = new MessageFormat(pattern, Locale.US);
String result = formatter.format(new Object[]{"World"});
// Output: "Hello, World!"
```

### Pluralization (Choice Format)

```java
String pattern = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.";
MessageFormat formatter = new MessageFormat(pattern, Locale.US);

System.out.println(formatter.format(new Object[]{0}));  // "There are no files"
System.out.println(formatter.format(new Object[]{1}));  // "There is one file"
System.out.println(formatter.format(new Object[]{5}));  // "There are 5 files"
```

### Number Formatting

```java
String pattern = "Total: {0,number,currency}";
MessageFormat formatter = new MessageFormat(pattern, Locale.US);
String result = formatter.format(new Object[]{1234.56});
// Output: "Total: $1,234.56"
```

### Date Formatting

```java
String pattern = "Meeting on {0,date,long} at {0,time,short}";
MessageFormat formatter = new MessageFormat(pattern, Locale.US);
String result = formatter.format(new Object[]{new Date()});
// Output: "Meeting on November 10, 2025 at 2:30 PM"
```

---

## Timezone Handling Pattern

**Purpose**: Manage timezone conversions and display times in user-specific timezones.

### Basic Timezone Conversion

```java
ZonedDateTime nyTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
ZonedDateTime tokyoTime = nyTime.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
```

### UTC Normalization

```java
// Store in UTC
Instant utcTime = zonedDateTime.toInstant();

// Retrieve in user timezone
ZonedDateTime userTime = utcTime.atZone(userTimeZone);
```

### Meeting Scheduling Example

```java
public class MeetingScheduler {
    public Map<ZoneId, ZonedDateTime> scheduleMeeting(
            ZonedDateTime hostTime, 
            List<ZoneId> participantZones) {
        
        Map<ZoneId, ZonedDateTime> schedule = new HashMap<>();
        for (ZoneId zone : participantZones) {
            schedule.put(zone, hostTime.withZoneSameInstant(zone));
        }
        return schedule;
    }
}
```

### Best Practices

- ✅ Always store timestamps in UTC
- ✅ Convert to user timezone for display only
- ✅ Use `Instant` for database storage
- ✅ Handle daylight saving time transitions
- ❌ Don't store local times without timezone info

---

## Currency Formatting Pattern

**Purpose**: Format monetary values with locale-specific currency symbols and decimal places.

### Basic Currency Formatting

```java
NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
String formatted = formatter.format(1234.56);
// Output: "$1,234.56"

NumberFormat euroFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
String euroFormatted = euroFormatter.format(1234.56);
// Output: "1.234,56 €"
```

### Multi-Currency Support

```java
public class CurrencyFormatter {
    public String format(double amount, String currencyCode, Locale locale) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(Currency.getInstance(currencyCode));
        return formatter.format(amount);
    }
}

// Usage
String usd = formatter.format(100.00, "USD", Locale.US);  // "$100.00"
String eur = formatter.format(100.00, "EUR", Locale.GERMANY);  // "100,00 €"
String jpy = formatter.format(100.00, "JPY", Locale.JAPAN);  // "¥100"
```

### Currency Conversion

```java
public class CurrencyConverter {
    private Map<String, BigDecimal> rates;
    
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        BigDecimal fromRate = rates.get(from);
        BigDecimal toRate = rates.get(to);
        return amount.divide(fromRate).multiply(toRate);
    }
}
```

---

## Date/Time Formatting Pattern

**Purpose**: Format dates and times according to locale-specific conventions.

### Predefined Styles

```java
LocalDateTime now = LocalDateTime.now();

// Short: 11/10/25, 2:30 PM
DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    .withLocale(Locale.US).format(now);

// Medium: Nov 10, 2025, 2:30:00 PM
DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    .withLocale(Locale.US).format(now);

// Long: November 10, 2025, 2:30:00 PM EST
DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
    .withLocale(Locale.US).format(now);

// Full: Sunday, November 10, 2025, 2:30:00 PM Eastern Standard Time
DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
    .withLocale(Locale.US).format(now);
```

### Custom Patterns

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);
String formatted = formatter.format(LocalDateTime.now());
// Output: "2025-11-10 14:30:00"
```

### Common Patterns

| Pattern | Example | Description |
|---------|---------|-------------|
| `yyyy-MM-dd` | 2025-11-10 | ISO date |
| `MM/dd/yyyy` | 11/10/2025 | US date |
| `dd/MM/yyyy` | 10/11/2025 | European date |
| `MMMM d, yyyy` | November 10, 2025 | Long date |
| `HH:mm:ss` | 14:30:00 | 24-hour time |
| `hh:mm a` | 02:30 PM | 12-hour time |

### Localized Components

```java
// Get localized month names
for (Month month : Month.values()) {
    String name = month.getDisplayName(TextStyle.FULL, Locale.FRENCH);
    // janvier, février, mars...
}

// Get localized day names
for (DayOfWeek day : DayOfWeek.values()) {
    String name = day.getDisplayName(TextStyle.FULL, Locale.GERMAN);
    // Montag, Dienstag, Mittwoch...
}
```

---

## 🔧 Spring Configuration Example

### Complete i18n Configuration

```yaml
# application.yml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
    cache-duration: 3600
    fallback-to-system-locale: false
  web:
    locale: en_US
    locale-resolver: cookie
```

### Java Configuration

```java
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        resolver.setCookieName("locale");
        resolver.setCookieMaxAge(7 * 24 * 60 * 60);
        return resolver;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }
}
```

---

## 📦 Dependencies

### Maven

```xml
<dependencies>
    <!-- Spring Context -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.0.0</version>
    </dependency>
    
    <!-- Spring Web MVC -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>6.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```gradle
dependencies {
    implementation 'org.springframework:spring-context:6.0.0'
    implementation 'org.springframework:spring-webmvc:6.0.0'
}
```

---

## 🎯 Best Practices

### General

1. **UTF-8 Encoding**: Always use UTF-8 for property files
2. **Default Locale**: Set a sensible default locale
3. **Fallback**: Implement proper fallback mechanisms
4. **Caching**: Use appropriate cache durations

### Message Keys

```properties
# Good - Hierarchical and descriptive
user.validation.email.required=Email is required
user.validation.email.invalid=Email format is invalid
product.error.notfound=Product not found

# Bad - Flat and unclear
msg1=Email is required
error2=Not found
```

### Resource Organization

```
src/main/resources/
├── i18n/
│   ├── messages/           # Common messages
│   │   ├── messages.properties
│   │   ├── messages_en.properties
│   │   └── messages_es.properties
│   ├── validation/         # Validation messages
│   │   ├── validation.properties
│   │   └── validation_en.properties
│   └── errors/            # Error messages
│       ├── errors.properties
│       └── errors_en.properties
```

### Performance Tips

1. **Cache Messages**: Enable caching in production
2. **Lazy Loading**: Load messages on-demand
3. **Optimize Bundles**: Split large bundles into smaller ones
4. **Use Appropriate Resolvers**: Choose the right locale resolver

---

## 🔍 Testing i18n

### Unit Testing

```java
@Test
public void testMessageResolution() {
    MessageSource messageSource = new StaticMessageSource();
    ((StaticMessageSource) messageSource).addMessage(
        "greeting", Locale.ENGLISH, "Hello, {0}!"
    );
    
    String message = messageSource.getMessage("greeting", 
        new Object[]{"World"}, Locale.ENGLISH);
    
    assertEquals("Hello, World!", message);
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
public class LocaleIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testLocaleChange() throws Exception {
        mockMvc.perform(get("/").param("lang", "es"))
            .andExpect(status().isOk())
            .andExpect(cookie().value("locale", "es"));
    }
}
```

---

## 🌍 Supported Locales

### Common Locales

| Locale Code | Language | Country |
|-------------|----------|---------|
| en_US | English | United States |
| en_GB | English | United Kingdom |
| es_ES | Spanish | Spain |
| fr_FR | French | France |
| de_DE | German | Germany |
| ja_JP | Japanese | Japan |
| zh_CN | Chinese | China |
| pt_BR | Portuguese | Brazil |
| it_IT | Italian | Italy |
| ru_RU | Russian | Russia |

---

## 📖 Additional Resources

- [Spring Framework Documentation - Internationalization](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/localeresolver.html)
- [Java Internationalization Guide](https://docs.oracle.com/javase/tutorial/i18n/)
- [Unicode CLDR](https://cldr.unicode.org/)
- [ISO 639 Language Codes](https://www.loc.gov/standards/iso639-2/php/code_list.php)
- [ISO 3166 Country Codes](https://www.iso.org/iso-3166-country-codes.html)

---

## 🎓 Pattern Summary

| Pattern | Primary Use | Complexity | Production Ready |
|---------|-------------|------------|------------------|
| Message Source | Message management | Medium | ✅ Yes |
| Locale Resolver | User locale detection | Low | ✅ Yes |
| Locale Change Interceptor | Dynamic locale switching | Low | ✅ Yes |
| Resource Bundle | Resource organization | Low | ✅ Yes |
| Message Format | Parameterized messages | Medium | ✅ Yes |
| Timezone Handling | Time zone conversion | High | ✅ Yes |
| Currency Formatting | Money display | Medium | ✅ Yes |
| Date/Time Formatting | Date display | Medium | ✅ Yes |

---

## 📝 License

These patterns are provided as educational examples for Spring Framework internationalization.

---

## 🤝 Contributing

Contributions are welcome! Please ensure all examples follow Spring best practices and include proper documentation.

---

**Happy Internationalizing! 🌐**
