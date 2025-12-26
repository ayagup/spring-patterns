package com.example.databinding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Form Backing Object Pattern
 * 
 * Demonstrates Spring's form backing object (command object) pattern for:
 * - Binding form data to Java objects
 * - Two-way data binding (display and submit)
 * - Form validation
 * - Error handling and display
 * - Multi-field forms
 * 
 * Key Features:
 * - Command object as form model
 * - Automatic property binding
 * - Validation support
 * - Error message display
 * - Form pre-population
 * 
 * Use Cases:
 * - User registration forms
 * - Profile editing
 * - Search forms
 * - Survey/questionnaire forms
 * - Multi-step wizards
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class FormBackingObjectPattern {

    public static void main(String[] args) {
        SpringApplication.run(FormBackingObjectPattern.class, args);
    }

    /**
     * Simple form with command object
     */
    @Controller
    @RequestMapping("/registration")
    public static class RegistrationController {

        /**
         * Display registration form
         * Form backing object is added to model
         */
        @GetMapping
        public String showForm(Model model) {
            model.addAttribute("registrationForm", new RegistrationForm());
            return "registration/form";
        }

        /**
         * Process form submission
         * Spring automatically binds form fields to RegistrationForm
         */
        @PostMapping
        public String submitForm(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                BindingResult bindingResult,
                                Model model) {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                // Return to form with error messages
                return "registration/form";
            }

            // Process successful registration
            System.out.println("Registration successful for: " + form.getUsername());
            
            model.addAttribute("message", "Registration successful!");
            return "registration/success";
        }

        /**
         * Pre-populate form for editing
         */
        @GetMapping("/edit/{id}")
        public String editForm(@PathVariable Long id, Model model) {
            // In real app, fetch from database
            RegistrationForm form = new RegistrationForm();
            form.setUsername("john_doe");
            form.setEmail("john@example.com");
            form.setFirstName("John");
            form.setLastName("Doe");
            form.setBirthDate(LocalDate.of(1990, 1, 15));
            form.setCountry("USA");
            form.setAcceptTerms(true);
            
            model.addAttribute("registrationForm", form);
            return "registration/form";
        }
    }

    /**
     * Complex form with nested objects
     */
    @Controller
    @RequestMapping("/profile")
    public static class ProfileController {

        @GetMapping("/create")
        public String showProfileForm(Model model) {
            ProfileForm form = new ProfileForm();
            // Initialize nested objects
            form.setPersonalInfo(new PersonalInfo());
            form.setContactInfo(new ContactInfo());
            form.setPreferences(new Preferences());
            
            model.addAttribute("profileForm", form);
            return "profile/form";
        }

        @PostMapping("/create")
        public String submitProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
            if (bindingResult.hasErrors()) {
                return "profile/form";
            }

            // Process profile creation
            System.out.println("Creating profile for: " + form.getPersonalInfo().getFirstName());
            
            redirectAttributes.addFlashAttribute("success", "Profile created successfully!");
            return "redirect:/profile/view";
        }

        @GetMapping("/view")
        public String viewProfile() {
            return "profile/view";
        }
    }

    /**
     * Search form with dynamic criteria
     */
    @Controller
    @RequestMapping("/search")
    public static class SearchController {

        @GetMapping
        public String showSearchForm(Model model) {
            model.addAttribute("searchForm", new SearchForm());
            return "search/form";
        }

        @GetMapping("/results")
        public String searchUsers(@ModelAttribute("searchForm") SearchForm form, Model model) {
            // Perform search based on criteria
            List<UserResult> results = performSearch(form);
            
            model.addAttribute("results", results);
            model.addAttribute("searchForm", form); // Keep search criteria displayed
            
            return "search/results";
        }

        private List<UserResult> performSearch(SearchForm form) {
            List<UserResult> results = new ArrayList<>();
            
            System.out.println("Searching with criteria:");
            System.out.println("  Keyword: " + form.getKeyword());
            System.out.println("  Country: " + form.getCountry());
            System.out.println("  Min Age: " + form.getMinAge());
            System.out.println("  Max Age: " + form.getMaxAge());
            
            // Mock search results
            UserResult result = new UserResult();
            result.setUsername("john_doe");
            result.setEmail("john@example.com");
            result.setCountry("USA");
            results.add(result);
            
            return results;
        }

        /**
         * Advanced search with multiple select options
         */
        @GetMapping("/advanced")
        public String showAdvancedSearch(Model model) {
            AdvancedSearchForm form = new AdvancedSearchForm();
            model.addAttribute("advancedSearchForm", form);
            
            // Populate dropdown/checkbox options
            model.addAttribute("countries", getCountries());
            model.addAttribute("categories", getCategories());
            model.addAttribute("tags", getTags());
            
            return "search/advanced";
        }

        @PostMapping("/advanced")
        public String performAdvancedSearch(@ModelAttribute("advancedSearchForm") AdvancedSearchForm form,
                                           Model model) {
            System.out.println("Advanced search:");
            System.out.println("  Selected countries: " + form.getSelectedCountries());
            System.out.println("  Selected categories: " + form.getSelectedCategories());
            System.out.println("  Selected tags: " + form.getSelectedTags());
            
            model.addAttribute("results", new ArrayList<UserResult>());
            return "search/results";
        }

        private List<String> getCountries() {
            List<String> countries = new ArrayList<>();
            countries.add("USA");
            countries.add("Canada");
            countries.add("UK");
            countries.add("Australia");
            return countries;
        }

        private List<String> getCategories() {
            List<String> categories = new ArrayList<>();
            categories.add("Technology");
            categories.add("Business");
            categories.add("Education");
            return categories;
        }

        private List<String> getTags() {
            List<String> tags = new ArrayList<>();
            tags.add("Java");
            tags.add("Spring");
            tags.add("Microservices");
            return tags;
        }
    }

    /**
     * Survey/Questionnaire form
     */
    @Controller
    @RequestMapping("/survey")
    public static class SurveyController {

        @GetMapping
        public String showSurvey(Model model) {
            SurveyForm form = new SurveyForm();
            model.addAttribute("surveyForm", form);
            return "survey/form";
        }

        @PostMapping
        public String submitSurvey(@Valid @ModelAttribute("surveyForm") SurveyForm form,
                                  BindingResult bindingResult,
                                  Model model) {
            if (bindingResult.hasErrors()) {
                return "survey/form";
            }

            // Process survey responses
            System.out.println("Survey submitted:");
            System.out.println("  Overall satisfaction: " + form.getOverallSatisfaction());
            System.out.println("  Features: " + form.getUsefulFeatures());
            System.out.println("  Comments: " + form.getComments());
            
            model.addAttribute("message", "Thank you for your feedback!");
            return "survey/thanks";
        }
    }

    /**
     * Dynamic form with add/remove fields
     */
    @Controller
    @RequestMapping("/contacts")
    public static class ContactListController {

        @GetMapping("/add")
        public String showContactForm(Model model) {
            ContactListForm form = new ContactListForm();
            // Initialize with one empty contact
            form.getContacts().add(new Contact());
            
            model.addAttribute("contactListForm", form);
            return "contacts/form";
        }

        @PostMapping("/add")
        public String submitContacts(@Valid @ModelAttribute("contactListForm") ContactListForm form,
                                    BindingResult bindingResult,
                                    Model model) {
            if (bindingResult.hasErrors()) {
                return "contacts/form";
            }

            // Process contacts
            System.out.println("Saving " + form.getContacts().size() + " contacts");
            for (Contact contact : form.getContacts()) {
                System.out.println("  - " + contact.getName() + ": " + contact.getEmail());
            }
            
            model.addAttribute("message", "Contacts saved successfully!");
            return "contacts/success";
        }
    }

    // Form Backing Objects (Command Objects)

    public static class RegistrationForm {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "Password confirmation is required")
        private String confirmPassword;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Past(message = "Birth date must be in the past")
        private LocalDate birthDate;

        @NotBlank(message = "Country is required")
        private String country;

        @AssertTrue(message = "You must accept the terms and conditions")
        private Boolean acceptTerms;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public Boolean getAcceptTerms() { return acceptTerms; }
        public void setAcceptTerms(Boolean acceptTerms) { this.acceptTerms = acceptTerms; }
    }

    public static class ProfileForm {
        @Valid
        private PersonalInfo personalInfo;

        @Valid
        private ContactInfo contactInfo;

        private Preferences preferences;

        public PersonalInfo getPersonalInfo() { return personalInfo; }
        public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }

        public ContactInfo getContactInfo() { return contactInfo; }
        public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }

        public Preferences getPreferences() { return preferences; }
        public void setPreferences(Preferences preferences) { this.preferences = preferences; }
    }

    public static class PersonalInfo {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private LocalDate birthDate;
        private String gender;
        private String bio;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
    }

    public static class ContactInfo {
        @Email(message = "Email must be valid")
        private String email;

        private String phone;
        private String address;
        private String city;
        private String country;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class Preferences {
        private Boolean emailNotifications;
        private Boolean smsNotifications;
        private String theme;
        private String language;

        public Boolean getEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }

        public Boolean getSmsNotifications() { return smsNotifications; }
        public void setSmsNotifications(Boolean smsNotifications) { this.smsNotifications = smsNotifications; }

        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class SearchForm {
        private String keyword;
        private String country;
        private Integer minAge;
        private Integer maxAge;
        private String sortBy = "username";
        private String sortOrder = "asc";

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public Integer getMinAge() { return minAge; }
        public void setMinAge(Integer minAge) { this.minAge = minAge; }

        public Integer getMaxAge() { return maxAge; }
        public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }

        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }

        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class AdvancedSearchForm {
        private String keyword;
        private List<String> selectedCountries = new ArrayList<>();
        private List<String> selectedCategories = new ArrayList<>();
        private List<String> selectedTags = new ArrayList<>();
        private LocalDate startDate;
        private LocalDate endDate;

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public List<String> getSelectedCountries() { return selectedCountries; }
        public void setSelectedCountries(List<String> selectedCountries) { this.selectedCountries = selectedCountries; }

        public List<String> getSelectedCategories() { return selectedCategories; }
        public void setSelectedCategories(List<String> selectedCategories) { this.selectedCategories = selectedCategories; }

        public List<String> getSelectedTags() { return selectedTags; }
        public void setSelectedTags(List<String> selectedTags) { this.selectedTags = selectedTags; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    public static class SurveyForm {
        @NotNull(message = "Overall satisfaction rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        private Integer overallSatisfaction;

        private List<String> usefulFeatures = new ArrayList<>();

        @Size(max = 1000, message = "Comments must not exceed 1000 characters")
        private String comments;

        private Boolean recommendToOthers;

        public Integer getOverallSatisfaction() { return overallSatisfaction; }
        public void setOverallSatisfaction(Integer overallSatisfaction) { this.overallSatisfaction = overallSatisfaction; }

        public List<String> getUsefulFeatures() { return usefulFeatures; }
        public void setUsefulFeatures(List<String> usefulFeatures) { this.usefulFeatures = usefulFeatures; }

        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }

        public Boolean getRecommendToOthers() { return recommendToOthers; }
        public void setRecommendToOthers(Boolean recommendToOthers) { this.recommendToOthers = recommendToOthers; }
    }

    public static class ContactListForm {
        @Valid
        private List<Contact> contacts = new ArrayList<>();

        public List<Contact> getContacts() { return contacts; }
        public void setContacts(List<Contact> contacts) { this.contacts = contacts; }
    }

    public static class Contact {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Email must be valid")
        private String email;

        private String phone;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class UserResult {
        private String username;
        private String email;
        private String country;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Form Backing Object Pattern:
 * 
 * 1. Command Object:
 *    - Java class that represents form data
 *    - Properties correspond to form fields
 *    - Automatically populated by Spring MVC
 *    - Can include validation annotations
 * 
 * 2. Two-way Binding:
 *    - Display: Object properties → form fields
 *    - Submit: Form fields → object properties
 *    - Spring handles conversion automatically
 * 
 * 3. Validation:
 *    - JSR-303 annotations on form object
 *    - @Valid triggers validation
 *    - BindingResult captures errors
 *    - Errors displayed in view
 * 
 * 4. Nested Objects:
 *    - Form objects can contain other objects
 *    - Use @Valid on nested properties
 *    - Field names use dot notation (personalInfo.firstName)
 * 
 * 5. Collections:
 *    - Form can contain lists/arrays
 *    - Field names use brackets (contacts[0].name)
 *    - Dynamic add/remove supported
 * 
 * Best Practices:
 * - Create dedicated form classes (DTOs)
 * - Don't use domain entities as form objects
 * - Include all necessary validation
 * - Provide meaningful error messages
 * - Initialize nested objects to avoid null
 * - Use @ModelAttribute for naming consistency
 * 
 * View Integration (Thymeleaf example):
 * - th:object="${formName}"
 * - th:field="*{propertyName}"
 * - th:errors="*{propertyName}"
 * 
 * Common Use Cases:
 * - User registration/login
 * - Profile editing
 * - Search forms
 * - Surveys/questionnaires
 * - Multi-step wizards
 * - Contact forms
 */
