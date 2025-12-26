package com.example.reactive.testing;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Reactive Test Context Pattern - Spring Context Support for Reactive Testing
 * 
 * This pattern demonstrates how to integrate reactive testing with Spring's
 * test context framework. It shows how to test reactive components within
 * a Spring application context, including reactive repositories, services,
 * and WebFlux controllers.
 * 
 * Key Concepts:
 * - Spring Test Context: Load Spring application context for tests
 * - Reactive Components: Test reactive beans and services
 * - Context Configuration: Configure test context for reactive testing
 * - Dependency Injection: Inject reactive beans into tests
 * - Test Slices: Use specialized test annotations for reactive components
 * 
 * Spring Reactive Test Annotations:
 * - @SpringBootTest: Full application context
 * - @WebFluxTest: WebFlux controller slice testing
 * - @DataR2dbcTest: R2DBC repository testing
 * - @DataMongoTest: Reactive MongoDB testing (with useDefaultFilters=false)
 * - @ExtendWith(SpringExtension.class): JUnit 5 integration
 * 
 * Testing Components:
 * - Reactive Repositories: Test R2DBC, MongoDB reactive repositories
 * - Reactive Services: Test business logic with reactive streams
 * - WebFlux Controllers: Test HTTP endpoints with WebTestClient
 * - Reactive Validators: Test validation logic
 * - Event Handlers: Test reactive event processing
 * 
 * Use Cases:
 * - Integration testing reactive applications
 * - Testing reactive database access
 * - Testing WebFlux REST APIs
 * - Testing reactive service layer
 * - Testing reactive event-driven systems
 * 
 * Best Practices:
 * - Use appropriate test slice annotations
 * - Mock external reactive dependencies
 * - Use StepVerifier for assertion
 * - Test both success and error scenarios
 * - Verify proper resource cleanup
 * - Use test containers for database tests
 */
public class ReactiveTestContextPattern {

    // Domain model
    static class User {
        private Long id;
        private String username;
        private String email;
        
        public User(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Reactive repository interface
    interface UserRepository {
        Mono<User> findById(Long id);
        Flux<User> findAll();
        Mono<User> save(User user);
        Mono<Void> deleteById(Long id);
        Flux<User> findByUsername(String username);
    }

    // Reactive repository implementation (simulated)
    static class UserRepositoryImpl implements UserRepository {
        
        @Override
        public Mono<User> findById(Long id) {
            return Mono.just(new User(id, "user-" + id, "user" + id + "@example.com"));
        }
        
        @Override
        public Flux<User> findAll() {
            return Flux.just(
                new User(1L, "alice", "alice@example.com"),
                new User(2L, "bob", "bob@example.com"),
                new User(3L, "charlie", "charlie@example.com")
            );
        }
        
        @Override
        public Mono<User> save(User user) {
            if (user.getId() == null) {
                user.setId(System.currentTimeMillis());
            }
            return Mono.just(user);
        }
        
        @Override
        public Mono<Void> deleteById(Long id) {
            return Mono.empty();
        }
        
        @Override
        public Flux<User> findByUsername(String username) {
            return findAll()
                .filter(user -> user.getUsername().equals(username));
        }
    }

    // Reactive service
    static class UserService {
        
        private final UserRepository repository;
        
        public UserService(UserRepository repository) {
            this.repository = repository;
        }
        
        public Mono<User> getUser(Long id) {
            return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
        }
        
        public Flux<User> getAllUsers() {
            return repository.findAll();
        }
        
        public Mono<User> createUser(String username, String email) {
            User user = new User(null, username, email);
            return repository.save(user);
        }
        
        public Mono<User> updateUser(Long id, String username, String email) {
            return repository.findById(id)
                .flatMap(user -> {
                    user.setUsername(username);
                    user.setEmail(email);
                    return repository.save(user);
                });
        }
        
        public Mono<Void> deleteUser(Long id) {
            return repository.deleteById(id);
        }
        
        public Flux<User> searchByUsername(String username) {
            return repository.findByUsername(username);
        }
    }

    // Test examples (would use @SpringBootTest in real application)
    static class ReactiveServiceTests {
        
        // Simulated dependency injection
        private final UserRepository repository = new UserRepositoryImpl();
        private final UserService service = new UserService(repository);
        
        // Test getting a user
        public void testGetUser() {
            Mono<User> result = service.getUser(1L);
            
            StepVerifier.create(result)
                .expectNextMatches(user -> 
                    user.getId().equals(1L) && 
                    user.getUsername().equals("user-1"))
                .verifyComplete();
        }
        
        // Test user not found scenario
        public void testGetUserNotFound() {
            // In real test, mock repository to return empty
            Mono<User> result = Mono.empty()
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
            
            StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
        }
        
        // Test getting all users
        public void testGetAllUsers() {
            Flux<User> result = service.getAllUsers();
            
            StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
        }
        
        // Test creating user
        public void testCreateUser() {
            Mono<User> result = service.createUser("newuser", "new@example.com");
            
            StepVerifier.create(result)
                .expectNextMatches(user -> 
                    user.getUsername().equals("newuser") &&
                    user.getEmail().equals("new@example.com") &&
                    user.getId() != null)
                .verifyComplete();
        }
        
        // Test updating user
        public void testUpdateUser() {
            Mono<User> result = service.updateUser(1L, "updated", "updated@example.com");
            
            StepVerifier.create(result)
                .expectNextMatches(user -> 
                    user.getUsername().equals("updated") &&
                    user.getEmail().equals("updated@example.com"))
                .verifyComplete();
        }
        
        // Test deleting user
        public void testDeleteUser() {
            Mono<Void> result = service.deleteUser(1L);
            
            StepVerifier.create(result)
                .verifyComplete();
        }
        
        // Test searching by username
        public void testSearchByUsername() {
            Flux<User> result = service.searchByUsername("alice");
            
            StepVerifier.create(result)
                .expectNextMatches(user -> user.getUsername().equals("alice"))
                .verifyComplete();
        }
    }

    // Reactive controller (simulated)
    static class UserController {
        
        private final UserService service;
        
        public UserController(UserService service) {
            this.service = service;
        }
        
        public Mono<User> getUser(Long id) {
            return service.getUser(id);
        }
        
        public Flux<User> getAllUsers() {
            return service.getAllUsers();
        }
        
        public Mono<User> createUser(User user) {
            return service.createUser(user.getUsername(), user.getEmail());
        }
        
        public Mono<User> updateUser(Long id, User user) {
            return service.updateUser(id, user.getUsername(), user.getEmail());
        }
        
        public Mono<Void> deleteUser(Long id) {
            return service.deleteUser(id);
        }
    }

    // Controller tests (would use @WebFluxTest in real application)
    static class UserControllerTests {
        
        private final UserRepository repository = new UserRepositoryImpl();
        private final UserService service = new UserService(repository);
        private final UserController controller = new UserController(service);
        
        // Test GET endpoint
        public void testGetUserEndpoint() {
            Mono<User> result = controller.getUser(1L);
            
            StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(1L))
                .verifyComplete();
        }
        
        // Test GET all endpoint
        public void testGetAllUsersEndpoint() {
            Flux<User> result = controller.getAllUsers();
            
            StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
        }
        
        // Test POST endpoint
        public void testCreateUserEndpoint() {
            User newUser = new User(null, "testuser", "test@example.com");
            Mono<User> result = controller.createUser(newUser);
            
            StepVerifier.create(result)
                .expectNextMatches(user -> 
                    user.getUsername().equals("testuser") &&
                    user.getId() != null)
                .verifyComplete();
        }
        
        // Test PUT endpoint
        public void testUpdateUserEndpoint() {
            User updatedUser = new User(1L, "updated", "updated@example.com");
            Mono<User> result = controller.updateUser(1L, updatedUser);
            
            StepVerifier.create(result)
                .expectNextMatches(user -> user.getUsername().equals("updated"))
                .verifyComplete();
        }
        
        // Test DELETE endpoint
        public void testDeleteUserEndpoint() {
            Mono<Void> result = controller.deleteUser(1L);
            
            StepVerifier.create(result)
                .verifyComplete();
        }
    }

    public static void main(String[] args) {
        System.out.println("Reactive Test Context Pattern - Spring Context Support");
        System.out.println("=======================================================");
        
        System.out.println("\nRunning reactive service tests...");
        ReactiveServiceTests serviceTests = new ReactiveServiceTests();
        
        try {
            serviceTests.testGetUser();
            System.out.println("✓ Get user test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Get user test failed: " + e.getMessage());
        }
        
        try {
            serviceTests.testGetAllUsers();
            System.out.println("✓ Get all users test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Get all users test failed: " + e.getMessage());
        }
        
        try {
            serviceTests.testCreateUser();
            System.out.println("✓ Create user test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Create user test failed: " + e.getMessage());
        }
        
        System.out.println("\nRunning reactive controller tests...");
        UserControllerTests controllerTests = new UserControllerTests();
        
        try {
            controllerTests.testGetUserEndpoint();
            System.out.println("✓ GET endpoint test passed");
        } catch (AssertionError e) {
            System.out.println("✗ GET endpoint test failed: " + e.getMessage());
        }
        
        try {
            controllerTests.testCreateUserEndpoint();
            System.out.println("✓ POST endpoint test passed");
        } catch (AssertionError e) {
            System.out.println("✗ POST endpoint test failed: " + e.getMessage());
        }
        
        System.out.println("\nReactive test context tests completed!");
        System.out.println("\nKey Annotations:");
        System.out.println("- @SpringBootTest: Full context testing");
        System.out.println("- @WebFluxTest: WebFlux slice testing");
        System.out.println("- @DataR2dbcTest: R2DBC repository testing");
        System.out.println("- @ExtendWith(SpringExtension.class): JUnit 5 integration");
        System.out.println("\nTesting Components:");
        System.out.println("- Reactive repositories");
        System.out.println("- Reactive services");
        System.out.println("- WebFlux controllers");
        System.out.println("- Reactive validators");
    }
}
