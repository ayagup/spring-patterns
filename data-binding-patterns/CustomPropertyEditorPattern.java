package com.example.databinding;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom Property Editor Pattern
 * 
 * Demonstrates creating and using custom PropertyEditors for data binding.
 * PropertyEditors convert between String and Object representations.
 * 
 * Features:
 * - Custom PropertyEditor implementation
 * - Built-in PropertyEditors
 * - PropertyEditorRegistrar
 * - @InitBinder registration
 * - Type conversion
 * 
 * Note: PropertyEditors are legacy but still widely used.
 * Consider using Converter/Formatter for new code.
 */
@SpringBootApplication
public class CustomPropertyEditorPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomPropertyEditorPattern.class, args);
    }

    /**
     * Custom PropertyEditor for Phone number
     */
    public static class PhoneNumberEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (text == null || text.isEmpty()) {
                setValue(null);
                return;
            }
            
            // Remove formatting characters
            String cleaned = text.replaceAll("[^0-9]", "");
            
            if (cleaned.length() != 10) {
                throw new IllegalArgumentException("Phone number must be 10 digits");
            }
            
            // Create PhoneNumber object
            PhoneNumber phone = new PhoneNumber();
            phone.setAreaCode(cleaned.substring(0, 3));
            phone.setPrefix(cleaned.substring(3, 6));
            phone.setNumber(cleaned.substring(6, 10));
            
            setValue(phone);
        }

        @Override
        public String getAsText() {
            PhoneNumber phone = (PhoneNumber) getValue();
            if (phone == null) {
                return "";
            }
            return String.format("(%s) %s-%s", 
                phone.getAreaCode(), phone.getPrefix(), phone.getNumber());
        }
    }

    /**
     * Custom PropertyEditor for Email
     */
    public static class EmailEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (text == null || text.isEmpty()) {
                setValue(null);
                return;
            }
            
            text = text.trim().toLowerCase();
            
            if (!text.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            Email email = new Email(text);
            setValue(email);
        }

        @Override
        public String getAsText() {
            Email email = (Email) getValue();
            return email != null ? email.getAddress() : "";
        }
    }

    /**
     * Custom PropertyEditor for SSN (Social Security Number)
     */
    public static class SSNEditor extends PropertyEditorSupport {

        private boolean mask = true;

        public SSNEditor(boolean mask) {
            this.mask = mask;
        }

        @Override
        public void setAsText(String text) {
            if (text == null || text.isEmpty()) {
                setValue(null);
                return;
            }
            
            String cleaned = text.replaceAll("[^0-9]", "");
            
            if (cleaned.length() != 9) {
                throw new IllegalArgumentException("SSN must be 9 digits");
            }
            
            setValue(new SSN(cleaned));
        }

        @Override
        public String getAsText() {
            SSN ssn = (SSN) getValue();
            if (ssn == null) {
                return "";
            }
            
            String value = ssn.getValue();
            if (mask) {
                return "XXX-XX-" + value.substring(5);
            } else {
                return value.substring(0, 3) + "-" + 
                       value.substring(3, 5) + "-" + 
                       value.substring(5);
            }
        }
    }

    /**
     * PropertyEditorRegistrar for bulk registration
     */
    public static class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

        @Override
        public void registerCustomEditors(PropertyEditorRegistry registry) {
            registry.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
            registry.registerCustomEditor(Email.class, new EmailEditor());
            registry.registerCustomEditor(SSN.class, new SSNEditor(true));
            
            // Built-in editors
            registry.registerCustomEditor(String.class, new StringTrimmerEditor(true));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            registry.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        }
    }

    /**
     * Controller with @InitBinder
     */
    @Controller
    public static class PropertyEditorController {

        @InitBinder
        public void initBinder(WebDataBinder binder) {
            // Register custom editors
            binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
            binder.registerCustomEditor(Email.class, new EmailEditor());
            binder.registerCustomEditor(SSN.class, new SSNEditor(true));
            
            // String trimming (empty strings to null)
            binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
            
            // Date formatting
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        }

        @GetMapping("/propertyeditor/form")
        public String showForm(Model model) {
            model.addAttribute("contact", new Contact());
            return "propertyeditor/form";
        }

        @PostMapping("/propertyeditor/submit")
        public String submitForm(@ModelAttribute Contact contact, Model model) {
            // Phone, Email, SSN are automatically converted by PropertyEditors
            model.addAttribute("contact", contact);
            return "propertyeditor/result";
        }
    }

    /**
     * Contact model
     */
    public static class Contact {
        private String name;
        private PhoneNumber phone;
        private Email email;
        private SSN ssn;
        private Date birthDate;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public PhoneNumber getPhone() { return phone; }
        public void setPhone(PhoneNumber phone) { this.phone = phone; }
        public Email getEmail() { return email; }
        public void setEmail(Email email) { this.email = email; }
        public SSN getSsn() { return ssn; }
        public void setSsn(SSN ssn) { this.ssn = ssn; }
        public Date getBirthDate() { return birthDate; }
        public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    }

    /**
     * PhoneNumber value object
     */
    public static class PhoneNumber {
        private String areaCode;
        private String prefix;
        private String number;

        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }

        @Override
        public String toString() {
            return "(" + areaCode + ") " + prefix + "-" + number;
        }
    }

    /**
     * Email value object
     */
    public static class Email {
        private String address;

        public Email(String address) {
            this.address = address;
        }

        public String getAddress() { return address; }

        @Override
        public String toString() {
            return address;
        }
    }

    /**
     * SSN value object
     */
    public static class SSN {
        private String value;

        public SSN(String value) {
            this.value = value;
        }

        public String getValue() { return value; }

        @Override
        public String toString() {
            return value.substring(0, 3) + "-" + 
                   value.substring(3, 5) + "-" + 
                   value.substring(5);
        }
    }
}

/*
 * PropertyEditor Methods:
 * 
 * setAsText(String)      - Convert String to Object
 * getAsText()            - Convert Object to String
 * setValue(Object)       - Set the current value
 * getValue()             - Get the current value
 * 
 * 
 * Built-in PropertyEditors:
 * 
 * CustomDateEditor       - String ↔ Date
 * CustomNumberEditor     - String ↔ Number
 * StringTrimmerEditor    - Trim/empty to null
 * URLEditor              - String ↔ URL
 * FileEditor             - String ↔ File
 * LocaleEditor           - String ↔ Locale
 * PatternEditor          - String ↔ Pattern (regex)
 * PropertiesEditor       - String ↔ Properties
 * 
 * 
 * Form Example:
 * 
 * <form method="post" action="/propertyeditor/submit">
 *     <input type="text" name="name" value="John Doe" />
 *     <input type="text" name="phone" value="(555) 123-4567" />
 *     <input type="text" name="email" value="john@example.com" />
 *     <input type="text" name="ssn" value="123-45-6789" />
 *     <input type="date" name="birthDate" value="1990-01-15" />
 *     <button type="submit">Submit</button>
 * </form>
 * 
 * PropertyEditors automatically convert:
 * - "(555) 123-4567" → PhoneNumber object
 * - "john@example.com" → Email object
 * - "123-45-6789" → SSN object
 * - "1990-01-15" → Date object
 * 
 * 
 * Global Registration:
 * 
 * @Configuration
 * public class WebConfig {
 *     
 *     @Bean
 *     public CustomEditorConfigurer customEditorConfigurer() {
 *         CustomEditorConfigurer configurer = new CustomEditorConfigurer();
 *         Map<Class<?>, Class<? extends PropertyEditor>> editors = new HashMap<>();
 *         editors.put(PhoneNumber.class, PhoneNumberEditor.class);
 *         editors.put(Email.class, EmailEditor.class);
 *         configurer.setCustomEditors(editors);
 *         return configurer;
 *     }
 * }
 * 
 * 
 * PropertyEditor vs Converter:
 * 
 * PropertyEditor:
 * - Legacy (JavaBeans spec)
 * - String ↔ Object only
 * - Not thread-safe (stateful)
 * - Used by Spring MVC binding
 * - Registered per request (@InitBinder)
 * 
 * Converter:
 * - Modern (Spring 3.0+)
 * - Any type → Any type
 * - Thread-safe (stateless)
 * - Global registration
 * - Better performance
 * 
 * Recommendation: Use Converter/Formatter for new code
 * 
 * 
 * Best Practices:
 * 
 * 1. Keep PropertyEditors stateless when possible
 * 2. Handle null/empty input gracefully
 * 3. Validate input in setAsText()
 * 4. Provide clear error messages
 * 5. Use @InitBinder for controller-specific editors
 * 6. Use PropertyEditorRegistrar for reusable config
 * 7. Consider Converter/Formatter for new code
 * 8. Document expected input formats
 */
