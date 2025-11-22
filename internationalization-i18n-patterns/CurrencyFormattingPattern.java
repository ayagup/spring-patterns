package com.spring.patterns.internationalization;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

/**
 * Currency Formatting Pattern
 * 
 * Demonstrates currency formatting for internationalization:
 * - Locale-specific currency formatting
 * - Currency conversion
 * - Multiple currency support
 * - Custom currency symbols
 * - Rounding strategies
 * 
 * Use Cases:
 * 1. E-commerce pricing display
 * 2. Financial reporting
 * 3. Multi-currency applications
 * 4. Invoice generation
 * 5. Payment processing
 */

/**
 * Currency Formatter
 */
class CurrencyFormatter {
    
    private final Locale locale;
    private final Currency currency;
    
    public CurrencyFormatter(Locale locale) {
        this.locale = locale;
        this.currency = Currency.getInstance(locale);
    }
    
    public CurrencyFormatter(Locale locale, Currency currency) {
        this.locale = locale;
        this.currency = currency;
    }
    
    /**
     * Format amount in default currency
     */
    public String format(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(currency);
        return formatter.format(amount);
    }
    
    /**
     * Format BigDecimal amount
     */
    public String format(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(currency);
        return formatter.format(amount);
    }
    
    /**
     * Format with custom decimal places
     */
    public String formatWithPrecision(double amount, int decimalPlaces) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(currency);
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setMaximumFractionDigits(decimalPlaces);
        return formatter.format(amount);
    }
    
    /**
     * Format without currency symbol
     */
    public String formatWithoutSymbol(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        return formatter.format(amount);
    }
    
    /**
     * Get currency symbol
     */
    public String getCurrencySymbol() {
        return currency.getSymbol(locale);
    }
    
    /**
     * Get currency code
     */
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }
}

/**
 * Multi-Currency Formatter
 */
class MultiCurrencyFormatter {
    
    private final Map<String, CurrencyFormatter> formatters = new HashMap<>();
    
    /**
     * Register currency formatter
     */
    public void registerFormatter(String currencyCode, Locale locale) {
        Currency currency = Currency.getInstance(currencyCode);
        formatters.put(currencyCode, new CurrencyFormatter(locale, currency));
    }
    
    /**
     * Format amount in specific currency
     */
    public String format(double amount, String currencyCode) {
        CurrencyFormatter formatter = formatters.get(currencyCode);
        if (formatter == null) {
            throw new IllegalArgumentException("Currency not registered: " + currencyCode);
        }
        return formatter.format(amount);
    }
    
    /**
     * Get all supported currencies
     */
    public Set<String> getSupportedCurrencies() {
        return formatters.keySet();
    }
}

/**
 * Currency Converter
 */
class CurrencyConverter {
    
    private final Map<String, BigDecimal> exchangeRates;
    private final String baseCurrency;
    
    public CurrencyConverter(String baseCurrency) {
        this.baseCurrency = baseCurrency;
        this.exchangeRates = new HashMap<>();
        initializeRates();
    }
    
    private void initializeRates() {
        // Sample exchange rates (USD as base)
        exchangeRates.put("USD", BigDecimal.ONE);
        exchangeRates.put("EUR", new BigDecimal("0.92"));
        exchangeRates.put("GBP", new BigDecimal("0.79"));
        exchangeRates.put("JPY", new BigDecimal("149.50"));
        exchangeRates.put("CNY", new BigDecimal("7.24"));
        exchangeRates.put("INR", new BigDecimal("83.12"));
        exchangeRates.put("AUD", new BigDecimal("1.52"));
        exchangeRates.put("CAD", new BigDecimal("1.36"));
    }
    
    /**
     * Convert amount between currencies
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        BigDecimal fromRate = exchangeRates.get(fromCurrency);
        BigDecimal toRate = exchangeRates.get(toCurrency);
        
        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Currency not supported");
        }
        
        // Convert to base currency, then to target currency
        BigDecimal baseAmount = amount.divide(fromRate, 6, RoundingMode.HALF_UP);
        return baseAmount.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Set exchange rate
     */
    public void setExchangeRate(String currency, BigDecimal rate) {
        exchangeRates.put(currency, rate);
    }
    
    /**
     * Get exchange rate
     */
    public BigDecimal getExchangeRate(String currency) {
        return exchangeRates.get(currency);
    }
}

/**
 * Money class for type-safe currency handling
 */
class Money {
    
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money(double amount, String currencyCode) {
        this.amount = BigDecimal.valueOf(amount);
        this.currency = Currency.getInstance(currencyCode);
    }
    
    /**
     * Add money (same currency only)
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtract money
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    /**
     * Multiply by factor
     */
    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    /**
     * Divide by factor
     */
    public Money divide(BigDecimal divisor) {
        return new Money(
            this.amount.divide(divisor, currency.getDefaultFractionDigits(), RoundingMode.HALF_UP),
            this.currency
        );
    }
    
    /**
     * Format money in specific locale
     */
    public String format(Locale locale) {
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        return formatter.format(amount);
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    @Override
    public String toString() {
        return amount + " " + currency.getCurrencyCode();
    }
}

/**
 * Price Formatter for e-commerce
 */
class PriceFormatter {
    
    private final Locale locale;
    private final Currency currency;
    
    public PriceFormatter(Locale locale, Currency currency) {
        this.locale = locale;
        this.currency = currency;
    }
    
    /**
     * Format product price
     */
    public String formatPrice(BigDecimal price) {
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        return formatter.format(price);
    }
    
    /**
     * Format discount price
     */
    public String formatDiscount(BigDecimal originalPrice, double discountPercent) {
        BigDecimal discount = originalPrice.multiply(BigDecimal.valueOf(discountPercent));
        BigDecimal finalPrice = originalPrice.subtract(discount);
        
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        return String.format("Was: %s, Now: %s (Save %s%%)", 
            formatter.format(originalPrice),
            formatter.format(finalPrice),
            (int)(discountPercent * 100));
    }
    
    /**
     * Format price range
     */
    public String formatRange(BigDecimal minPrice, BigDecimal maxPrice) {
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        return formatter.format(minPrice) + " - " + formatter.format(maxPrice);
    }
    
    /**
     * Format with tax
     */
    public String formatWithTax(BigDecimal price, double taxRate) {
        BigDecimal tax = price.multiply(BigDecimal.valueOf(taxRate));
        BigDecimal total = price.add(tax);
        
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        return String.format("Subtotal: %s + Tax: %s = Total: %s",
            formatter.format(price),
            formatter.format(tax),
            formatter.format(total));
    }
}

/**
 * Currency Information Provider
 */
class CurrencyInformationProvider {
    
    /**
     * Get all available currencies
     */
    public Set<Currency> getAllCurrencies() {
        return Currency.getAvailableCurrencies();
    }
    
    /**
     * Get currency info
     */
    public CurrencyInfo getCurrencyInfo(Currency currency, Locale locale) {
        return new CurrencyInfo(
            currency.getCurrencyCode(),
            currency.getDisplayName(locale),
            currency.getSymbol(locale),
            currency.getDefaultFractionDigits(),
            currency.getNumericCode()
        );
    }
    
    /**
     * Get common currencies
     */
    public List<Currency> getCommonCurrencies() {
        return Arrays.asList(
            Currency.getInstance("USD"),
            Currency.getInstance("EUR"),
            Currency.getInstance("GBP"),
            Currency.getInstance("JPY"),
            Currency.getInstance("CNY"),
            Currency.getInstance("CHF"),
            Currency.getInstance("AUD"),
            Currency.getInstance("CAD"),
            Currency.getInstance("INR")
        );
    }
}

record CurrencyInfo(
    String code,
    String displayName,
    String symbol,
    int defaultFractionDigits,
    int numericCode
) {}

/**
 * Invoice Formatter
 */
class InvoiceFormatter {
    
    private final Locale locale;
    private final Currency currency;
    
    public InvoiceFormatter(Locale locale, Currency currency) {
        this.locale = locale;
        this.currency = currency;
    }
    
    /**
     * Format invoice line item
     */
    public String formatLineItem(String description, int quantity, BigDecimal unitPrice) {
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        
        return String.format("%-30s  %3d x %s = %s",
            description,
            quantity,
            formatter.format(unitPrice),
            formatter.format(total));
    }
    
    /**
     * Format invoice total
     */
    public String formatTotal(List<Money> lineItems) {
        BigDecimal total = lineItems.stream()
            .map(Money::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        CurrencyFormatter formatter = new CurrencyFormatter(locale, currency);
        return "Total: " + formatter.format(total);
    }
}

/**
 * Demonstration class
 */
public class CurrencyFormattingPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Currency Formatting Pattern Demo ===\n");
        
        // 1. Basic Currency Formatting
        demonstrateBasicFormatting();
        
        // 2. Multi-Currency Formatting
        demonstrateMultiCurrencyFormatting();
        
        // 3. Currency Conversion
        demonstrateCurrencyConversion();
        
        // 4. Money Type Demo
        demonstrateMoneyType();
        
        // 5. Price Formatting
        demonstratePriceFormatting();
        
        // 6. Currency Information
        demonstrateCurrencyInformation();
        
        // 7. Invoice Formatting
        demonstrateInvoiceFormatting();
    }
    
    private static void demonstrateBasicFormatting() {
        System.out.println("1. Basic Currency Formatting:");
        
        double amount = 1234.56;
        
        Locale[] locales = {
            Locale.US, Locale.UK, Locale.GERMANY, Locale.FRANCE, Locale.JAPAN
        };
        
        for (Locale locale : locales) {
            CurrencyFormatter formatter = new CurrencyFormatter(locale);
            System.out.println(locale.getDisplayCountry() + ": " + formatter.format(amount));
        }
        System.out.println();
    }
    
    private static void demonstrateMultiCurrencyFormatting() {
        System.out.println("2. Multi-Currency Formatting:");
        
        MultiCurrencyFormatter formatter = new MultiCurrencyFormatter();
        formatter.registerFormatter("USD", Locale.US);
        formatter.registerFormatter("EUR", Locale.GERMANY);
        formatter.registerFormatter("GBP", Locale.UK);
        formatter.registerFormatter("JPY", Locale.JAPAN);
        
        double amount = 100.00;
        
        for (String currency : formatter.getSupportedCurrencies()) {
            System.out.println(currency + ": " + formatter.format(amount, currency));
        }
        System.out.println();
    }
    
    private static void demonstrateCurrencyConversion() {
        System.out.println("3. Currency Conversion:");
        
        CurrencyConverter converter = new CurrencyConverter("USD");
        BigDecimal amount = new BigDecimal("100.00");
        
        String[] currencies = {"EUR", "GBP", "JPY", "CNY", "INR"};
        
        for (String currency : currencies) {
            BigDecimal converted = converter.convert(amount, "USD", currency);
            System.out.println("100 USD = " + converted + " " + currency);
        }
        System.out.println();
    }
    
    private static void demonstrateMoneyType() {
        System.out.println("4. Money Type Demo:");
        
        Money usd1 = new Money(100.00, "USD");
        Money usd2 = new Money(50.00, "USD");
        
        System.out.println("Amount 1: " + usd1.format(Locale.US));
        System.out.println("Amount 2: " + usd2.format(Locale.US));
        System.out.println("Sum: " + usd1.add(usd2).format(Locale.US));
        System.out.println("Difference: " + usd1.subtract(usd2).format(Locale.US));
        System.out.println("Doubled: " + usd1.multiply(new BigDecimal("2")).format(Locale.US));
        System.out.println("Halved: " + usd1.divide(new BigDecimal("2")).format(Locale.US));
        System.out.println();
    }
    
    private static void demonstratePriceFormatting() {
        System.out.println("5. Price Formatting:");
        
        PriceFormatter formatter = new PriceFormatter(Locale.US, Currency.getInstance("USD"));
        
        BigDecimal price = new BigDecimal("99.99");
        System.out.println("Product Price: " + formatter.formatPrice(price));
        System.out.println("With 20% discount: " + formatter.formatDiscount(price, 0.20));
        System.out.println("Price range: " + 
            formatter.formatRange(new BigDecimal("49.99"), new BigDecimal("199.99")));
        System.out.println(formatter.formatWithTax(price, 0.08)); // 8% tax
        System.out.println();
    }
    
    private static void demonstrateCurrencyInformation() {
        System.out.println("6. Currency Information:");
        
        CurrencyInformationProvider provider = new CurrencyInformationProvider();
        List<Currency> common = provider.getCommonCurrencies();
        
        for (Currency currency : common.subList(0, 5)) {
            CurrencyInfo info = provider.getCurrencyInfo(currency, Locale.US);
            System.out.printf("%s (%s): Symbol: %s, Digits: %d%n",
                info.code(), info.displayName(), info.symbol(), info.defaultFractionDigits());
        }
        System.out.println();
    }
    
    private static void demonstrateInvoiceFormatting() {
        System.out.println("7. Invoice Formatting:");
        
        InvoiceFormatter formatter = new InvoiceFormatter(Locale.US, Currency.getInstance("USD"));
        
        System.out.println("INVOICE");
        System.out.println("----------------------------------------");
        System.out.println(formatter.formatLineItem("Product A", 2, new BigDecimal("29.99")));
        System.out.println(formatter.formatLineItem("Product B", 1, new BigDecimal("49.99")));
        System.out.println(formatter.formatLineItem("Service Fee", 1, new BigDecimal("10.00")));
        System.out.println("----------------------------------------");
        
        List<Money> items = Arrays.asList(
            new Money(59.98, "USD"),
            new Money(49.99, "USD"),
            new Money(10.00, "USD")
        );
        
        System.out.println(formatter.formatTotal(items));
    }
}
