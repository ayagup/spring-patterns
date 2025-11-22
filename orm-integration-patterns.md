### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:53:19 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **ORM Integration Patterns**
> - JPA Integration Pattern
> - Hibernate Integration Pattern
> - MyBatis Integration Pattern
> - JDBC Template Pattern
> - Named Parameter JDBC Template Pattern
> - Simple JDBC Insert Pattern
> - Simple JDBC Call Pattern
> - Entity Manager Pattern
> - Entity Manager Factory Pattern
> - Persistence Unit Pattern
> - Persistence Context Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all these ORM integration patterns.
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
>     <artifactId>orm-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>         <mybatis.version>3.0.3</mybatis.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Starters -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <!-- Database -->
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <!-- MyBatis -->
>         <dependency>
>             <groupId>org.mybatis.spring.boot</groupId>
>             <artifactId>mybatis-spring-boot-starter</artifactId>
>             <version>${mybatis.version}</version>
>         </dependency>
>         
>         <!-- Lombok -->
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         
>         <!-- Hibernate (already included in spring-boot-starter-data-jpa) -->
>         <dependency>
>             <groupId>org.hibernate.orm</groupId>
>             <artifactId>hibernate-core</artifactId>
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
> ```properties src/main/resources/application.properties
> # Database Configuration
> spring.datasource.url=jdbc:h2:mem:ormpatterns
> spring.datasource.driverClassName=org.h2.Driver
> spring.datasource.username=sa
> spring.datasource.password=
> 
> # JPA Configuration
> spring.jpa.hibernate.ddl-auto=create-drop
> spring.jpa.show-sql=true
> spring.jpa.properties.hibernate.format_sql=true
> spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
> 
> # H2 Console
> spring.h2.console.enabled=true
> spring.h2.console.path=/h2-console
> 
> # MyBatis Configuration
> mybatis.mapper-locations=classpath:mappers/*.xml
> mybatis.type-aliases-package=com.example.ormpatterns.entity
> mybatis.configuration.map-underscore-to-camel-case=true
> 
> # Logging
> logging.level.com.example.ormpatterns=DEBUG
> logging.level.org.hibernate.SQL=DEBUG
> logging.level.org.mybatis=DEBUG
> ```
> 
> ## Domain Entity
> 
> ```java src/main/java/com/example/ormpatterns/entity/User.java
> package com.example.ormpatterns.entity;
> 
> import jakarta.persistence.*;
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> @Entity
> @Table(name = "users")
> @Data
> @NoArgsConstructor
> @AllArgsConstructor
> public class User {
>     
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>     
>     @Column(nullable = false)
>     private String username;
>     
>     @Column(nullable = false)
>     private String email;
>     
>     private Integer age;
>     
>     public User(String username, String email, Integer age) {
>         this.username = username;
>         this.email = email;
>         this.age = age;
>     }
> }
> ```
> 
> ## Pattern 1: JPA Integration Pattern
> 
> ```java src/main/java/com/example/ormpatterns/repository/UserJpaRepository.java
> package com.example.ormpatterns.repository;
> 
> import com.example.ormpatterns.entity.User;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.data.jpa.repository.Query;
> import org.springframework.data.repository.query.Param;
> import org.springframework.stereotype.Repository;
> 
> import java.util.List;
> import java.util.Optional;
> 
> /**
>  * JPA Integration Pattern
>  * Uses Spring Data JPA repository abstraction
>  */
> @Repository
> public interface UserJpaRepository extends JpaRepository<User, Long> {
>     
>     // Query derivation from method name
>     Optional<User> findByUsername(String username);
>     
>     List<User> findByAgeGreaterThan(Integer age);
>     
>     // Custom JPQL query
>     @Query("SELECT u FROM User u WHERE u.email LIKE %:domain%")
>     List<User> findByEmailDomain(@Param("domain") String domain);
>     
>     // Native SQL query
>     @Query(value = "SELECT * FROM users WHERE age BETWEEN :minAge AND :maxAge", 
>            nativeQuery = true)
>     List<User> findByAgeBetween(@Param("minAge") Integer minAge, 
>                                 @Param("maxAge") Integer maxAge);
> }
> ```
> 
> ```java src/main/java/com/example/ormpatterns/service/JpaIntegrationService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import com.example.ormpatterns.repository.UserJpaRepository;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.List;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class JpaIntegrationService {
>     
>     private final UserJpaRepository userJpaRepository;
>     
>     @Transactional
>     public User createUser(String username, String email, Integer age) {
>         log.info("JPA Pattern: Creating user with username: {}", username);
>         User user = new User(username, email, age);
>         return userJpaRepository.save(user);
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findUsersByAgeGreaterThan(Integer age) {
>         log.info("JPA Pattern: Finding users with age > {}", age);
>         return userJpaRepository.findByAgeGreaterThan(age);
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findUsersByEmailDomain(String domain) {
>         log.info("JPA Pattern: Finding users by email domain: {}", domain);
>         return userJpaRepository.findByEmailDomain(domain);
>     }
> }
> ```
> 
> ## Pattern 2: Hibernate Integration Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/HibernateIntegrationService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.hibernate.Session;
> import org.hibernate.SessionFactory;
> import org.hibernate.query.Query;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import jakarta.persistence.EntityManagerFactory;
> import java.util.List;
> 
> /**
>  * Hibernate Integration Pattern
>  * Direct use of Hibernate Session API
>  */
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class HibernateIntegrationService {
>     
>     private final EntityManagerFactory entityManagerFactory;
>     
>     private SessionFactory getSessionFactory() {
>         return entityManagerFactory.unwrap(SessionFactory.class);
>     }
>     
>     @Transactional
>     public User saveUser(User user) {
>         log.info("Hibernate Pattern: Saving user with Hibernate Session");
>         Session session = getSessionFactory().getCurrentSession();
>         session.persist(user);
>         return user;
>     }
>     
>     @Transactional(readOnly = true)
>     public User getUserById(Long id) {
>         log.info("Hibernate Pattern: Getting user by ID: {}", id);
>         Session session = getSessionFactory().getCurrentSession();
>         return session.get(User.class, id);
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> getAllUsers() {
>         log.info("Hibernate Pattern: Getting all users using HQL");
>         Session session = getSessionFactory().getCurrentSession();
>         Query<User> query = session.createQuery("FROM User", User.class);
>         return query.list();
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> getUsersByCriteria(String username) {
>         log.info("Hibernate Pattern: Using Criteria API");
>         Session session = getSessionFactory().getCurrentSession();
>         
>         return session.createQuery(
>             "FROM User u WHERE u.username = :username", User.class)
>             .setParameter("username", username)
>             .list();
>     }
>     
>     @Transactional
>     public void updateUser(Long id, String newEmail) {
>         log.info("Hibernate Pattern: Updating user email");
>         Session session = getSessionFactory().getCurrentSession();
>         User user = session.get(User.class, id);
>         if (user != null) {
>             user.setEmail(newEmail);
>             session.merge(user);
>         }
>     }
> }
> ```
> 
> ## Pattern 3: MyBatis Integration Pattern
> 
> ```java src/main/java/com/example/ormpatterns/mapper/UserMapper.java
> package com.example.ormpatterns.mapper;
> 
> import com.example.ormpatterns.entity.User;
> import org.apache.ibatis.annotations.*;
> 
> import java.util.List;
> 
> /**
>  * MyBatis Integration Pattern
>  * SQL mapping using annotations and XML
>  */
> @Mapper
> public interface UserMapper {
>     
>     @Select("SELECT * FROM users WHERE id = #{id}")
>     User findById(Long id);
>     
>     @Select("SELECT * FROM users")
>     List<User> findAll();
>     
>     @Insert("INSERT INTO users (username, email, age) VALUES (#{username}, #{email}, #{age})")
>     @Options(useGeneratedKeys = true, keyProperty = "id")
>     int insert(User user);
>     
>     @Update("UPDATE users SET username = #{username}, email = #{email}, age = #{age} WHERE id = #{id}")
>     int update(User user);
>     
>     @Delete("DELETE FROM users WHERE id = #{id}")
>     int delete(Long id);
>     
>     // Complex query defined in XML mapper
>     List<User> findByComplexCriteria(@Param("minAge") Integer minAge, 
>                                      @Param("maxAge") Integer maxAge,
>                                      @Param("emailPattern") String emailPattern);
> }
> ```
> 
> ```xml src/main/resources/mappers/UserMapper.xml
> <?xml version="1.0" encoding="UTF-8" ?>
> <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
>         "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
> 
> <mapper namespace="com.example.ormpatterns.mapper.UserMapper">
>     
>     <resultMap id="UserResultMap" type="com.example.ormpatterns.entity.User">
>         <id property="id" column="id"/>
>         <result property="username" column="username"/>
>         <result property="email" column="email"/>
>         <result property="age" column="age"/>
>     </resultMap>
>     
>     <select id="findByComplexCriteria" resultMap="UserResultMap">
>         SELECT * FROM users
>         <where>
>             <if test="minAge != null">
>                 AND age &gt;= #{minAge}
>             </if>
>             <if test="maxAge != null">
>                 AND age &lt;= #{maxAge}
>             </if>
>             <if test="emailPattern != null">
>                 AND email LIKE CONCAT('%', #{emailPattern}, '%')
>             </if>
>         </where>
>     </select>
>     
> </mapper>
> ```
> 
> ```java src/main/java/com/example/ormpatterns/service/MyBatisIntegrationService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import com.example.ormpatterns.mapper.UserMapper;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.List;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class MyBatisIntegrationService {
>     
>     private final UserMapper userMapper;
>     
>     @Transactional
>     public User createUser(String username, String email, Integer age) {
>         log.info("MyBatis Pattern: Creating user");
>         User user = new User(username, email, age);
>         userMapper.insert(user);
>         return user;
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findByComplexCriteria(Integer minAge, Integer maxAge, String emailPattern) {
>         log.info("MyBatis Pattern: Finding users by complex criteria");
>         return userMapper.findByComplexCriteria(minAge, maxAge, emailPattern);
>     }
> }
> ```
> 
> ## Pattern 4: JDBC Template Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/JdbcTemplateService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.jdbc.core.JdbcTemplate;
> import org.springframework.jdbc.core.RowMapper;
> import org.springframework.jdbc.support.GeneratedKeyHolder;
> import org.springframework.jdbc.support.KeyHolder;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.sql.PreparedStatement;
> import java.sql.Statement;
> import java.util.List;
> 
> /**
>  * JDBC Template Pattern
>  * Direct SQL execution using JdbcTemplate
>  */
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class JdbcTemplateService {
>     
>     private final JdbcTemplate jdbcTemplate;
>     
>     private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
>         User user = new User();
>         user.setId(rs.getLong("id"));
>         user.setUsername(rs.getString("username"));
>         user.setEmail(rs.getString("email"));
>         user.setAge(rs.getInt("age"));
>         return user;
>     };
>     
>     @Transactional
>     public User createUser(String username, String email, Integer age) {
>         log.info("JdbcTemplate Pattern: Creating user");
>         String sql = "INSERT INTO users (username, email, age) VALUES (?, ?, ?)";
>         
>         KeyHolder keyHolder = new GeneratedKeyHolder();
>         
>         jdbcTemplate.update(connection -> {
>             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
>             ps.setString(1, username);
>             ps.setString(2, email);
>             ps.setInt(3, age);
>             return ps;
>         }, keyHolder);
>         
>         Long id = keyHolder.getKey().longValue();
>         return new User(id, username, email, age);
>     }
>     
>     @Transactional(readOnly = true)
>     public User findById(Long id) {
>         log.info("JdbcTemplate Pattern: Finding user by ID");
>         String sql = "SELECT * FROM users WHERE id = ?";
>         return jdbcTemplate.queryForObject(sql, userRowMapper, id);
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findAll() {
>         log.info("JdbcTemplate Pattern: Finding all users");
>         String sql = "SELECT * FROM users";
>         return jdbcTemplate.query(sql, userRowMapper);
>     }
>     
>     @Transactional
>     public int updateUser(Long id, String email) {
>         log.info("JdbcTemplate Pattern: Updating user");
>         String sql = "UPDATE users SET email = ? WHERE id = ?";
>         return jdbcTemplate.update(sql, email, id);
>     }
>     
>     @Transactional
>     public int deleteUser(Long id) {
>         log.info("JdbcTemplate Pattern: Deleting user");
>         String sql = "DELETE FROM users WHERE id = ?";
>         return jdbcTemplate.update(sql, id);
>     }
>     
>     @Transactional(readOnly = true)
>     public Integer countUsers() {
>         log.info("JdbcTemplate Pattern: Counting users");
>         String sql = "SELECT COUNT(*) FROM users";
>         return jdbcTemplate.queryForObject(sql, Integer.class);
>     }
> }
> ```
> 
> ## Pattern 5: Named Parameter JDBC Template Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/NamedParameterJdbcTemplateService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.jdbc.core.BeanPropertyRowMapper;
> import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
> import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
> import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
> import org.springframework.jdbc.core.namedparam.SqlParameterSource;
> import org.springframework.jdbc.support.GeneratedKeyHolder;
> import org.springframework.jdbc.support.KeyHolder;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> 
> /**
>  * Named Parameter JDBC Template Pattern
>  * Uses named parameters instead of positional parameters
>  */
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class NamedParameterJdbcTemplateService {
>     
>     private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
>     
>     @Transactional
>     public User createUser(String username, String email, Integer age) {
>         log.info("NamedParameterJdbcTemplate Pattern: Creating user");
>         
>         String sql = "INSERT INTO users (username, email, age) VALUES (:username, :email, :age)";
>         
>         MapSqlParameterSource params = new MapSqlParameterSource()
>             .addValue("username", username)
>             .addValue("email", email)
>             .addValue("age", age);
>         
>         KeyHolder keyHolder = new GeneratedKeyHolder();
>         namedParameterJdbcTemplate.update(sql, params, keyHolder);
>         
>         Long id = keyHolder.getKey().longValue();
>         return new User(id, username, email, age);
>     }
>     
>     @Transactional
>     public User createUserWithBean(User user) {
>         log.info("NamedParameterJdbcTemplate Pattern: Creating user with bean");
>         
>         String sql = "INSERT INTO users (username, email, age) VALUES (:username, :email, :age)";
>         
>         SqlParameterSource params = new BeanPropertySqlParameterSource(user);
>         KeyHolder keyHolder = new GeneratedKeyHolder();
>         
>         namedParameterJdbcTemplate.update(sql, params, keyHolder);
>         user.setId(keyHolder.getKey().longValue());
>         return user;
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findByAgeRange(Integer minAge, Integer maxAge) {
>         log.info("NamedParameterJdbcTemplate Pattern: Finding users by age range");
>         
>         String sql = "SELECT * FROM users WHERE age BETWEEN :minAge AND :maxAge";
>         
>         Map<String, Object> params = new HashMap<>();
>         params.put("minAge", minAge);
>         params.put("maxAge", maxAge);
>         
>         return namedParameterJdbcTemplate.query(sql, params, 
>             new BeanPropertyRowMapper<>(User.class));
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findByUsernames(List<String> usernames) {
>         log.info("NamedParameterJdbcTemplate Pattern: Finding users by username list");
>         
>         String sql = "SELECT * FROM users WHERE username IN (:usernames)";
>         
>         MapSqlParameterSource params = new MapSqlParameterSource()
>             .addValue("usernames", usernames);
>         
>         return namedParameterJdbcTemplate.query(sql, params, 
>             new BeanPropertyRowMapper<>(User.class));
>     }
>     
>     @Transactional
>     public int updateUserEmail(Long id, String email) {
>         log.info("NamedParameterJdbcTemplate Pattern: Updating user email");
>         
>         String sql = "UPDATE users SET email = :email WHERE id = :id";
>         
>         MapSqlParameterSource params = new MapSqlParameterSource()
>             .addValue("id", id)
>             .addValue("email", email);
>         
>         return namedParameterJdbcTemplate.update(sql, params);
>     }
> }
> ```
> 
> ## Pattern 6: Simple JDBC Insert Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/SimpleJdbcInsertService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Simple JDBC Insert Pattern
>  * Simplified insert operations with automatic key generation
>  */
> @Service
> @Slf4j
> public class SimpleJdbcInsertService {
>     
>     private final SimpleJdbcInsert simpleJdbcInsert;
>     
>     public SimpleJdbcInsertService(DataSource dataSource) {
>         this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
>             .withTableName("users")
>             .usingGeneratedKeyColumns("id");
>     }
>     
>     @Transactional
>     public User insertUser(String username, String email, Integer age) {
>         log.info("SimpleJdbcInsert Pattern: Inserting user with map");
>         
>         Map<String, Object> parameters = new HashMap<>();
>         parameters.put("username", username);
>         parameters.put("email", email);
>         parameters.put("age", age);
>         
>         Number key = simpleJdbcInsert.executeAndReturnKey(parameters);
>         return new User(key.longValue(), username, email, age);
>     }
>     
>     @Transactional
>     public User insertUserWithColumnMapping(User user) {
>         log.info("SimpleJdbcInsert Pattern: Inserting user with column mapping");
>         
>         // Configure column names if they differ from property names
>         SimpleJdbcInsert insert = new SimpleJdbcInsert(simpleJdbcInsert.getJdbcTemplate())
>             .withTableName("users")
>             .usingGeneratedKeyColumns("id")
>             .usingColumns("username", "email", "age");
>         
>         Map<String, Object> parameters = new HashMap<>();
>         parameters.put("username", user.getUsername());
>         parameters.put("email", user.getEmail());
>         parameters.put("age", user.getAge());
>         
>         Number key = insert.executeAndReturnKey(parameters);
>         user.setId(key.longValue());
>         return user;
>     }
>     
>     @Transactional
>     public int[] batchInsert(List<User> users) {
>         log.info("SimpleJdbcInsert Pattern: Batch inserting {} users", users.size());
>         
>         @SuppressWarnings("unchecked")
>         Map<String, Object>[] batch = users.stream()
>             .map(user -> {
>                 Map<String, Object> params = new HashMap<>();
>                 params.put("username", user.getUsername());
>                 params.put("email", user.getEmail());
>                 params.put("age", user.getAge());
>                 return params;
>             })
>             .toArray(Map[]::new);
>         
>         return simpleJdbcInsert.executeBatch(batch);
>     }
> }
> ```
> 
> ## Pattern 7: Simple JDBC Call Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/SimpleJdbcCallService.java
> package com.example.ormpatterns.service;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.jdbc.core.SqlOutParameter;
> import org.springframework.jdbc.core.SqlParameter;
> import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
> import org.springframework.jdbc.core.simple.SimpleJdbcCall;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import javax.sql.DataSource;
> import java.sql.Types;
> import java.util.Map;
> 
> /**
>  * Simple JDBC Call Pattern
>  * Calling stored procedures and functions
>  */
> @Service
> @Slf4j
> public class SimpleJdbcCallService {
>     
>     private final SimpleJdbcCall getUserCountCall;
>     private final SimpleJdbcCall updateUserEmailCall;
>     
>     public SimpleJdbcCallService(DataSource dataSource) {
>         // Example stored procedure configuration
>         // Note: H2 syntax for creating procedures
>         this.getUserCountCall = new SimpleJdbcCall(dataSource)
>             .withFunctionName("GET_USER_COUNT");
>         
>         this.updateUserEmailCall = new SimpleJdbcCall(dataSource)
>             .withProcedureName("UPDATE_USER_EMAIL")
>             .declareParameters(
>                 new SqlParameter("p_user_id", Types.BIGINT),
>                 new SqlParameter("p_email", Types.VARCHAR),
>                 new SqlOutParameter("p_result", Types.INTEGER)
>             );
>     }
>     
>     @Transactional(readOnly = true)
>     public Integer getUserCount() {
>         log.info("SimpleJdbcCall Pattern: Calling function to get user count");
>         
>         try {
>             return getUserCountCall.executeFunction(Integer.class);
>         } catch (Exception e) {
>             log.warn("Function GET_USER_COUNT not available (expected in demo): {}", e.getMessage());
>             return 0;
>         }
>     }
>     
>     @Transactional
>     public Map<String, Object> updateUserEmailWithProcedure(Long userId, String email) {
>         log.info("SimpleJdbcCall Pattern: Calling stored procedure to update email");
>         
>         MapSqlParameterSource params = new MapSqlParameterSource()
>             .addValue("p_user_id", userId)
>             .addValue("p_email", email);
>         
>         try {
>             return updateUserEmailCall.execute(params);
>         } catch (Exception e) {
>             log.warn("Procedure UPDATE_USER_EMAIL not available (expected in demo): {}", e.getMessage());
>             return Map.of("p_result", 0);
>         }
>     }
>     
>     @Transactional(readOnly = true)
>     public Map<String, Object> callCustomProcedure(String procedureName, Map<String, Object> params) {
>         log.info("SimpleJdbcCall Pattern: Calling custom procedure: {}", procedureName);
>         
>         SimpleJdbcCall call = new SimpleJdbcCall(getUserCountCall.getJdbcTemplate())
>             .withProcedureName(procedureName);
>         
>         try {
>             return call.execute(params);
>         } catch (Exception e) {
>             log.warn("Procedure {} not available: {}", procedureName, e.getMessage());
>             return Map.of();
>         }
>     }
> }
> ```
> 
> ## Pattern 8: Entity Manager Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/EntityManagerService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.PersistenceContext;
> import jakarta.persistence.TypedQuery;
> import jakarta.persistence.criteria.CriteriaBuilder;
> import jakarta.persistence.criteria.CriteriaQuery;
> import jakarta.persistence.criteria.Root;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.List;
> 
> /**
>  * Entity Manager Pattern
>  * Direct use of JPA EntityManager
>  */
> @Service
> @Slf4j
> public class EntityManagerService {
>     
>     @PersistenceContext
>     private EntityManager entityManager;
>     
>     @Transactional
>     public User persistUser(String username, String email, Integer age) {
>         log.info("EntityManager Pattern: Persisting user");
>         User user = new User(username, email, age);
>         entityManager.persist(user);
>         return user;
>     }
>     
>     @Transactional
>     public User mergeUser(User user) {
>         log.info("EntityManager Pattern: Merging user");
>         return entityManager.merge(user);
>     }
>     
>     @Transactional(readOnly = true)
>     public User findUser(Long id) {
>         log.info("EntityManager Pattern: Finding user by ID");
>         return entityManager.find(User.class, id);
>     }
>     
>     @Transactional(readOnly = true)
>     public User getReferenceUser(Long id) {
>         log.info("EntityManager Pattern: Getting user reference (lazy)");
>         return entityManager.getReference(User.class, id);
>     }
>     
>     @Transactional
>     public void removeUser(Long id) {
>         log.info("EntityManager Pattern: Removing user");
>         User user = entityManager.find(User.class, id);
>         if (user != null) {
>             entityManager.remove(user);
>         }
>     }
>     
>     @Transactional
>     public void detachUser(User user) {
>         log.info("EntityManager Pattern: Detaching user from persistence context");
>         entityManager.detach(user);
>     }
>     
>     @Transactional
>     public void flush() {
>         log.info("EntityManager Pattern: Flushing persistence context");
>         entityManager.flush();
>     }
>     
>     @Transactional
>     public void clear() {
>         log.info("EntityManager Pattern: Clearing persistence context");
>         entityManager.clear();
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findUsersWithJPQL(String email) {
>         log.info("EntityManager Pattern: Finding users with JPQL");

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/ormpatterns/service/EntityManagerService.java
>     @Transactional(readOnly = true)
>     public List<User> findUsersWithJPQL(String email) {
>         log.info("EntityManager Pattern: Finding users with JPQL");
>         TypedQuery<User> query = entityManager.createQuery(
>             "SELECT u FROM User u WHERE u.email LIKE :email", User.class);
>         query.setParameter("email", "%" + email + "%");
>         return query.getResultList();
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findUsersWithCriteria(Integer minAge) {
>         log.info("EntityManager Pattern: Finding users with Criteria API");
>         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
>         CriteriaQuery<User> cq = cb.createQuery(User.class);
>         Root<User> root = cq.from(User.class);
>         
>         cq.select(root).where(cb.greaterThan(root.get("age"), minAge));
>         
>         return entityManager.createQuery(cq).getResultList();
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> findUsersWithNamedQuery(String username) {
>         log.info("EntityManager Pattern: Finding users with named query");
>         TypedQuery<User> query = entityManager.createNamedQuery("User.findByUsername", User.class);
>         query.setParameter("username", username);
>         return query.getResultList();
>     }
>     
>     @Transactional
>     public int updateUsersAgeByEmail(String email, Integer newAge) {
>         log.info("EntityManager Pattern: Bulk update using JPQL");
>         return entityManager.createQuery(
>             "UPDATE User u SET u.age = :age WHERE u.email = :email")
>             .setParameter("age", newAge)
>             .setParameter("email", email)
>             .executeUpdate();
>     }
> }
> ```
> 
> ## Pattern 9: Entity Manager Factory Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/EntityManagerFactoryService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.EntityManagerFactory;
> import jakarta.persistence.EntityTransaction;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.util.List;
> 
> /**
>  * Entity Manager Factory Pattern
>  * Manual EntityManager lifecycle management
>  */
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class EntityManagerFactoryService {
>     
>     private final EntityManagerFactory entityManagerFactory;
>     
>     public User createUserManually(String username, String email, Integer age) {
>         log.info("EntityManagerFactory Pattern: Creating user with manual transaction");
>         
>         EntityManager em = entityManagerFactory.createEntityManager();
>         EntityTransaction tx = null;
>         User user = null;
>         
>         try {
>             tx = em.getTransaction();
>             tx.begin();
>             
>             user = new User(username, email, age);
>             em.persist(user);
>             
>             tx.commit();
>             log.info("User created successfully with ID: {}", user.getId());
>         } catch (Exception e) {
>             if (tx != null && tx.isActive()) {
>                 tx.rollback();
>             }
>             log.error("Error creating user", e);
>             throw e;
>         } finally {
>             em.close();
>         }
>         
>         return user;
>     }
>     
>     public User findUserManually(Long id) {
>         log.info("EntityManagerFactory Pattern: Finding user manually");
>         
>         EntityManager em = entityManagerFactory.createEntityManager();
>         try {
>             return em.find(User.class, id);
>         } finally {
>             em.close();
>         }
>     }
>     
>     public List<User> executeQueryManually(String jpql) {
>         log.info("EntityManagerFactory Pattern: Executing query manually");
>         
>         EntityManager em = entityManagerFactory.createEntityManager();
>         try {
>             return em.createQuery(jpql, User.class).getResultList();
>         } finally {
>             em.close();
>         }
>     }
>     
>     public void performBatchOperation(List<User> users) {
>         log.info("EntityManagerFactory Pattern: Performing batch operation");
>         
>         EntityManager em = entityManagerFactory.createEntityManager();
>         EntityTransaction tx = null;
>         
>         try {
>             tx = em.getTransaction();
>             tx.begin();
>             
>             int batchSize = 20;
>             for (int i = 0; i < users.size(); i++) {
>                 em.persist(users.get(i));
>                 
>                 if (i > 0 && i % batchSize == 0) {
>                     em.flush();
>                     em.clear();
>                 }
>             }
>             
>             tx.commit();
>         } catch (Exception e) {
>             if (tx != null && tx.isActive()) {
>                 tx.rollback();
>             }
>             log.error("Error in batch operation", e);
>             throw e;
>         } finally {
>             em.close();
>         }
>     }
>     
>     public boolean isEntityManagerFactoryOpen() {
>         return entityManagerFactory.isOpen();
>     }
>     
>     public void demonstrateEntityManagerProperties() {
>         log.info("EntityManagerFactory Pattern: Demonstrating properties");
>         
>         EntityManager em = entityManagerFactory.createEntityManager();
>         try {
>             log.info("EntityManager is open: {}", em.isOpen());
>             log.info("EntityManager properties: {}", em.getProperties());
>         } finally {
>             em.close();
>         }
>     }
> }
> ```
> 
> ## Pattern 10: Persistence Unit Pattern
> 
> ```java src/main/java/com/example/ormpatterns/config/PersistenceUnitConfig.java
> package com.example.ormpatterns.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
> import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
> 
> import javax.sql.DataSource;
> import java.util.Properties;
> 
> /**
>  * Persistence Unit Pattern
>  * Configuration of JPA persistence unit
>  */
> @Configuration
> public class PersistenceUnitConfig {
>     
>     @Bean
>     public LocalContainerEntityManagerFactoryBean customEntityManagerFactory(DataSource dataSource) {
>         LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
>         
>         // Set datasource
>         em.setDataSource(dataSource);
>         
>         // Set packages to scan for entities
>         em.setPackagesToScan("com.example.ormpatterns.entity");
>         
>         // Set JPA vendor adapter
>         HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
>         vendorAdapter.setShowSql(true);
>         vendorAdapter.setGenerateDdl(true);
>         em.setJpaVendorAdapter(vendorAdapter);
>         
>         // Set JPA properties
>         em.setJpaProperties(additionalProperties());
>         
>         // Set persistence unit name
>         em.setPersistenceUnitName("customPersistenceUnit");
>         
>         return em;
>     }
>     
>     private Properties additionalProperties() {
>         Properties properties = new Properties();
>         
>         // Hibernate specific properties
>         properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
>         properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
>         properties.setProperty("hibernate.show_sql", "true");
>         properties.setProperty("hibernate.format_sql", "true");
>         properties.setProperty("hibernate.use_sql_comments", "true");
>         
>         // Performance settings
>         properties.setProperty("hibernate.jdbc.batch_size", "20");
>         properties.setProperty("hibernate.order_inserts", "true");
>         properties.setProperty("hibernate.order_updates", "true");
>         properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
>         
>         // Cache settings
>         properties.setProperty("hibernate.cache.use_second_level_cache", "false");
>         properties.setProperty("hibernate.cache.use_query_cache", "false");
>         
>         // Statistics
>         properties.setProperty("hibernate.generate_statistics", "true");
>         
>         return properties;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/ormpatterns/service/PersistenceUnitService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.PersistenceContext;
> import jakarta.persistence.PersistenceUnit;
> import jakarta.persistence.EntityManagerFactory;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.List;
> 
> /**
>  * Persistence Unit Pattern
>  * Using @PersistenceUnit to inject EntityManagerFactory
>  */
> @Service
> @Slf4j
> public class PersistenceUnitService {
>     
>     @PersistenceUnit(unitName = "customPersistenceUnit")
>     private EntityManagerFactory customEntityManagerFactory;
>     
>     @PersistenceContext
>     private EntityManager defaultEntityManager;
>     
>     @Transactional
>     public User createWithCustomUnit(String username, String email, Integer age) {
>         log.info("PersistenceUnit Pattern: Creating user with custom persistence unit");
>         
>         EntityManager em = customEntityManagerFactory.createEntityManager();
>         try {
>             em.getTransaction().begin();
>             
>             User user = new User(username, email, age);
>             em.persist(user);
>             
>             em.getTransaction().commit();
>             return user;
>         } catch (Exception e) {
>             if (em.getTransaction().isActive()) {
>                 em.getTransaction().rollback();
>             }
>             throw e;
>         } finally {
>             em.close();
>         }
>     }
>     
>     @Transactional(readOnly = true)
>     public List<User> getAllFromCustomUnit() {
>         log.info("PersistenceUnit Pattern: Getting all users from custom persistence unit");
>         
>         EntityManager em = customEntityManagerFactory.createEntityManager();
>         try {
>             return em.createQuery("SELECT u FROM User u", User.class).getResultList();
>         } finally {
>             em.close();
>         }
>     }
>     
>     public void demonstratePersistenceUnitInfo() {
>         log.info("PersistenceUnit Pattern: Persistence unit is open: {}", 
>             customEntityManagerFactory.isOpen());
>         
>         EntityManager em = customEntityManagerFactory.createEntityManager();
>         try {
>             log.info("Persistence unit properties: {}", em.getProperties());
>         } finally {
>             em.close();
>         }
>     }
> }
> ```
> 
> ## Pattern 11: Persistence Context Pattern
> 
> ```java src/main/java/com/example/ormpatterns/service/PersistenceContextService.java
> package com.example.ormpatterns.service;
> 
> import com.example.ormpatterns.entity.User;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.PersistenceContext;
> import jakarta.persistence.PersistenceContextType;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Propagation;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.List;
> 
> /**
>  * Persistence Context Pattern
>  * Managing JPA Persistence Context lifecycle and scopes
>  */
> @Service
> @Slf4j
> public class PersistenceContextService {
>     
>     // Transaction-scoped persistence context (default)
>     @PersistenceContext
>     private EntityManager transactionScopedEM;
>     
>     // Extended persistence context
>     @PersistenceContext(type = PersistenceContextType.EXTENDED)
>     private EntityManager extendedEM;
>     
>     @Transactional
>     public User createAndManageUser(String username, String email, Integer age) {
>         log.info("PersistenceContext Pattern: Creating user in transaction-scoped context");
>         
>         User user = new User(username, email, age);
>         transactionScopedEM.persist(user);
>         
>         // User is now in MANAGED state
>         log.info("User is managed: {}", transactionScopedEM.contains(user));
>         
>         return user;
>     }
>     
>     @Transactional
>     public void demonstrateManagedState() {
>         log.info("PersistenceContext Pattern: Demonstrating managed state");
>         
>         User user = new User("managed", "managed@test.com", 25);
>         
>         // NEW state
>         log.info("Before persist - Contains: {}", transactionScopedEM.contains(user));
>         
>         transactionScopedEM.persist(user);
>         
>         // MANAGED state
>         log.info("After persist - Contains: {}", transactionScopedEM.contains(user));
>         
>         // Changes are automatically tracked
>         user.setEmail("updated@test.com");
>         
>         // No need to call merge, changes will be flushed automatically
>         transactionScopedEM.flush();
>     }
>     
>     @Transactional
>     public void demonstrateDetachedState() {
>         log.info("PersistenceContext Pattern: Demonstrating detached state");
>         
>         User user = new User("detached", "detached@test.com", 30);
>         transactionScopedEM.persist(user);
>         transactionScopedEM.flush();
>         
>         // MANAGED state
>         log.info("Before detach - Contains: {}", transactionScopedEM.contains(user));
>         
>         transactionScopedEM.detach(user);
>         
>         // DETACHED state
>         log.info("After detach - Contains: {}", transactionScopedEM.contains(user));
>         
>         // Changes won't be tracked
>         user.setEmail("nottracked@test.com");
>         
>         // Need to merge to make it managed again
>         User managedUser = transactionScopedEM.merge(user);
>         log.info("After merge - Contains: {}", transactionScopedEM.contains(managedUser));
>     }
>     
>     @Transactional(propagation = Propagation.REQUIRES_NEW)
>     public void demonstratePersistenceContextBoundary() {
>         log.info("PersistenceContext Pattern: New transaction creates new persistence context");
>         
>         User user = new User("boundary", "boundary@test.com", 35);
>         transactionScopedEM.persist(user);
>         
>         log.info("User persisted in new transaction");
>     }
>     
>     @Transactional
>     public void demonstrateFirstLevelCache() {
>         log.info("PersistenceContext Pattern: Demonstrating first-level cache");
>         
>         User user = new User("cache", "cache@test.com", 40);
>         transactionScopedEM.persist(user);
>         transactionScopedEM.flush();
>         
>         Long userId = user.getId();
>         
>         // First find - hits database
>         User found1 = transactionScopedEM.find(User.class, userId);
>         log.info("First find - User: {}", found1);
>         
>         // Second find - uses cache (no database hit)
>         User found2 = transactionScopedEM.find(User.class, userId);
>         log.info("Second find - User: {}", found2);
>         
>         // Same instance from cache
>         log.info("Same instance: {}", found1 == found2);
>     }
>     
>     @Transactional
>     public void demonstrateCascading() {
>         log.info("PersistenceContext Pattern: Demonstrating cascade operations");
>         
>         User user = new User("cascade", "cascade@test.com", 45);
>         transactionScopedEM.persist(user);
>         
>         // Cascade persist would propagate to related entities
>         // (if relationships were defined with cascade=PERSIST)
>     }
>     
>     @Transactional
>     public List<User> demonstrateFlushMode() {
>         log.info("PersistenceContext Pattern: Demonstrating flush modes");
>         
>         User user = new User("flush", "flush@test.com", 50);
>         transactionScopedEM.persist(user);
>         
>         // AUTO flush mode: entity manager decides when to flush
>         log.info("Flush mode: {}", transactionScopedEM.getFlushMode());
>         
>         // Query will trigger auto-flush before execution
>         List<User> users = transactionScopedEM
>             .createQuery("SELECT u FROM User u WHERE u.username = 'flush'", User.class)
>             .getResultList();
>         
>         return users;
>     }
>     
>     @Transactional
>     public void demonstrateClear() {
>         log.info("PersistenceContext Pattern: Demonstrating clear operation");
>         
>         User user1 = new User("clear1", "clear1@test.com", 55);
>         User user2 = new User("clear2", "clear2@test.com", 60);
>         
>         transactionScopedEM.persist(user1);
>         transactionScopedEM.persist(user2);
>         
>         log.info("Users managed: {}, {}", 
>             transactionScopedEM.contains(user1),
>             transactionScopedEM.contains(user2));
>         
>         // Clear persistence context
>         transactionScopedEM.clear();
>         
>         log.info("After clear - Users managed: {}, {}", 
>             transactionScopedEM.contains(user1),
>             transactionScopedEM.contains(user2));
>     }
>     
>     public void demonstrateExtendedContext() {
>         log.info("PersistenceContext Pattern: Demonstrating extended persistence context");
>         
>         // Extended context survives transaction boundaries
>         // Typically used in stateful session beans (EJB)
>         User user = new User("extended", "extended@test.com", 65);
>         extendedEM.persist(user);
>         
>         log.info("User persisted in extended context");
>         // User remains managed even after transaction ends
>     }
> }
> ```
> 
> ## Main Application
> 
> ```java src/main/java/com/example/ormpatterns/OrmPatternsApplication.java
> package com.example.ormpatterns;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class OrmPatternsApplication {
>     
>     public static void main(String[] args) {
>         SpringApplication.run(OrmPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## REST Controller to Test All Patterns
> 
> ```java src/main/java/com/example/ormpatterns/controller/OrmPatternsController.java
> package com.example.ormpatterns.controller;
> 
> import com.example.ormpatterns.entity.User;
> import com.example.ormpatterns.service.*;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/orm-patterns")
> @RequiredArgsConstructor
> @Slf4j
> public class OrmPatternsController {
>     
>     private final JpaIntegrationService jpaService;
>     private final HibernateIntegrationService hibernateService;
>     private final MyBatisIntegrationService myBatisService;
>     private final JdbcTemplateService jdbcTemplateService;
>     private final NamedParameterJdbcTemplateService namedParamService;
>     private final SimpleJdbcInsertService simpleJdbcInsertService;
>     private final SimpleJdbcCallService simpleJdbcCallService;
>     private final EntityManagerService entityManagerService;
>     private final EntityManagerFactoryService emFactoryService;
>     private final PersistenceUnitService persistenceUnitService;
>     private final PersistenceContextService persistenceContextService;
>     
>     @PostMapping("/jpa/create")
>     public ResponseEntity<User> createWithJpa(@RequestBody UserRequest request) {
>         log.info("Testing JPA Integration Pattern");
>         User user = jpaService.createUser(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @GetMapping("/jpa/age-greater-than/{age}")
>     public ResponseEntity<List<User>> findByAgeJpa(@PathVariable Integer age) {
>         return ResponseEntity.ok(jpaService.findUsersByAgeGreaterThan(age));
>     }
>     
>     @PostMapping("/hibernate/create")
>     public ResponseEntity<User> createWithHibernate(@RequestBody UserRequest request) {
>         log.info("Testing Hibernate Integration Pattern");
>         User user = new User(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(hibernateService.saveUser(user));
>     }
>     
>     @GetMapping("/hibernate/all")
>     public ResponseEntity<List<User>> getAllWithHibernate() {
>         return ResponseEntity.ok(hibernateService.getAllUsers());
>     }
>     
>     @PostMapping("/mybatis/create")
>     public ResponseEntity<User> createWithMyBatis(@RequestBody UserRequest request) {
>         log.info("Testing MyBatis Integration Pattern");
>         User user = myBatisService.createUser(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @GetMapping("/mybatis/criteria")
>     public ResponseEntity<List<User>> findByMyBatisCriteria(
>             @RequestParam(required = false) Integer minAge,
>             @RequestParam(required = false) Integer maxAge,
>             @RequestParam(required = false) String emailPattern) {
>         return ResponseEntity.ok(myBatisService.findByComplexCriteria(minAge, maxAge, emailPattern));
>     }
>     
>     @PostMapping("/jdbc-template/create")
>     public ResponseEntity<User> createWithJdbcTemplate(@RequestBody UserRequest request) {
>         log.info("Testing JDBC Template Pattern");
>         User user = jdbcTemplateService.createUser(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @GetMapping("/jdbc-template/all")
>     public ResponseEntity<List<User>> getAllWithJdbcTemplate() {
>         return ResponseEntity.ok(jdbcTemplateService.findAll());
>     }
>     
>     @PostMapping("/named-param-jdbc/create")
>     public ResponseEntity<User> createWithNamedParam(@RequestBody UserRequest request) {
>         log.info("Testing Named Parameter JDBC Template Pattern");
>         User user = namedParamService.createUser(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @GetMapping("/named-param-jdbc/age-range")
>     public ResponseEntity<List<User>> findByAgeRange(
>             @RequestParam Integer minAge, 
>             @RequestParam Integer maxAge) {
>         return ResponseEntity.ok(namedParamService.findByAgeRange(minAge, maxAge));
>     }
>     
>     @PostMapping("/simple-jdbc-insert/create")
>     public ResponseEntity<User> createWithSimpleJdbcInsert(@RequestBody UserRequest request) {
>         log.info("Testing Simple JDBC Insert Pattern");
>         User user = simpleJdbcInsertService.insertUser(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @GetMapping("/simple-jdbc-call/count")
>     public ResponseEntity<Integer> getUserCountWithCall() {
>         log.info("Testing Simple JDBC Call Pattern");
>         return ResponseEntity.ok(simpleJdbcCallService.getUserCount());
>     }
>     
>     @PostMapping("/entity-manager/create")
>     public ResponseEntity<User> createWithEntityManager(@RequestBody UserRequest request) {
>         log.info("Testing Entity Manager Pattern");
>         User user = entityManagerService.persistUser(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @GetMapping("/entity-manager/criteria/{minAge}")
>     public ResponseEntity<List<User>> findWithCriteria(@PathVariable Integer minAge) {
>         return ResponseEntity.ok(entityManagerService.findUsersWithCriteria(minAge));
>     }
>     
>     @PostMapping("/em-factory/create")
>     public ResponseEntity<User> createWithEmFactory(@RequestBody UserRequest request) {
>         log.info("Testing Entity Manager Factory Pattern");
>         User user = emFactoryService.createUserManually(request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @PostMapping("/persistence-unit/create")
>     public ResponseEntity<User> createWithPersistenceUnit(@RequestBody UserRequest request) {
>         log.info("Testing Persistence Unit Pattern");
>         User user = persistenceUnitService.createWithCustomUnit(
>             request.getUsername(), request.getEmail(), request.getAge());
>         return ResponseEntity.ok(user);
>     }
>     
>     @PostMapping("/persistence-context/demo")
>     public ResponseEntity<Map<String, String>> demonstratePersistenceContext(@RequestBody UserRequest request) {
>         log.info("Testing Persistence Context Pattern");
>         
>         persistenceContextService.createAndManageUser(
>             request.getUsername(), request.getEmail(), request.getAge());
>         persistenceContextService.demonstrateManagedState();
>         persistenceContextService.demonstrateDetachedState();
>         persistenceContextService.demonstrateFirstLevelCache();
>         
>         Map<String, String> result = new HashMap<>();
>         result.put("message", "Persistence Context demonstrations completed. Check logs for details.");
>         return ResponseEntity.ok(result);
>     }
>     
>     @GetMapping("/demo-all")
>     public ResponseEntity<Map<String, String>> demonstrateAllPatterns() {
>         log.info("Demonstrating all ORM patterns");
>         
>         Map<String, String> results = new HashMap<>();
>         
>         try {
>             // 1. JPA
>             jpaService.createUser("jpa_user", "jpa@test.com", 25);
>             results.put("JPA Integration", "Success");
>             
>             // 2. Hibernate
>             hibernateService.saveUser(new User("hibernate_user", "hibernate@test.com", 30));
>             results.put("Hibernate Integration", "Success");
>             
>             // 3. MyBatis
>             myBatisService.createUser("mybatis_user", "mybatis@test.com", 35);
>             results.put("MyBatis Integration", "Success");
>             
>             // 4. JDBC Template
>             jdbcTemplateService.createUser("jdbc_user", "jdbc@test.com", 40);
>             results.put("JDBC Template", "Success");
>             
>             // 5. Named Parameter JDBC
>             namedParamService.createUser("named_user", "named@test.com", 45);
>             results.put("Named Parameter JDBC Template", "Success");
>             
>             // 6. Simple JDBC Insert
>             simpleJdbcInsertService.insertUser("simple_user", "simple@test.com", 50);
>             results.put("Simple JDBC Insert", "Success");
>             
>             // 7. Simple JDBC Call
>             simpleJdbcCallService.getUserCount();
>             results.put("Simple JDBC Call", "Success");
>             
>             // 8. Entity Manager
>             entityManagerService.persistUser("em_user", "em@test.com", 55);
>             results.put("Entity Manager", "Success");
>             
>             // 9. Entity Manager Factory
>             emFactoryService.createUserManually("emf_user", "emf@test.com", 60);
>             results.put("Entity Manager Factory", "Success");
>             
>             // 10. Persistence Unit
>             persistenceUnitService.createWithCustomUnit("pu_user", "pu@test.com", 65);
>             results.put("Persistence Unit", "Success");
>             
>             // 11. Persistence Context
>             persistenceContextService.createAndManageUser("pc_user", "pc@test.com", 70);
>             results.put("Persistence Context", "Success");
>             
>         } catch (Exception e) {
>             results.put("Error", e.getMessage());
>         }
>         
>         return ResponseEntity.ok(results);
>     }
>     
>     // DTO for requests
>     @lombok.Data
>     public static class UserRequest {
>         private String username;
>         private String email;
>         private Integer age;
>     }
> }
> ```
> 
> ## SQL Scripts for Stored Procedures (H2 Compatible)
> 
> ```sql src/main/resources/schema.sql
> -- Example stored procedures for SimpleJdbcCall pattern
> -- Note: H2 syntax
> 
> CREATE ALIAS IF NOT EXISTS GET_USER_COUNT AS $$
> int getUserCount() throws SQLException {
>     Connection conn = DriverManager.getConnection("jdbc:default:connection");
>     Statement stmt = conn.createStatement();
>     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
>     rs.next();
>     return rs.getInt(1);
> }
> $$;
> 
> CREATE ALIAS IF NOT EXISTS UPDATE_USER_EMAIL AS $$
> int updateUserEmail(Long userId, String email) throws SQLException {
>     Connection conn = DriverManager.getConnection("jdbc:default:connection");
>     PreparedStatement stmt = conn.prepareStatement("UPDATE users SET email = ? WHERE id = ?");
>     stmt.setString(1, email);
>     stmt.setLong(2, userId);
>     return stmt.executeUpdate();
> }
> $$;
> ```
> 
> ## Summary
> 
> This comprehensive Spring Boot application demonstrates all 11 ORM integration patterns:
> 
> 1. **JPA Integration** - Spring Data JPA repositories with query derivation
> 2. **Hibernate Integration** - Direct Hibernate Session API usage
> 3. **MyBatis Integration** - SQL mapping with annotations and XML
> 4. **JDBC Template** - Direct SQL execution with JdbcTemplate
> 5. **Named Parameter JDBC Template** - Named parameters instead of positional
> 6. **Simple JDBC Insert** - Simplified insert operations
> 7. **Simple JDBC Call** - Stored procedure/function calls
> 8. **Entity Manager** - JPA EntityManager for persistence operations
> 9. **Entity Manager Factory** - Manual EntityManager lifecycle management
> 10. **Persistence Unit** - Custom persistence unit configuration
> 11. **Persistence Context** - Managing JPA persistence context lifecycle
> 
> Each pattern is isolated in its own service and can be tested via the REST controller endpoints!