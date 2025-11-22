package com.example.orm.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple JDBC Call Pattern
 * 
 * Purpose:
 * - Simplified stored procedure/function calls
 * - Automatic metadata detection
 * - Parameter mapping
 * - Return value handling
 * 
 * Features:
 * 1. Stored procedure calls
 * 2. Function calls
 * 3. IN/OUT/INOUT parameter support
 * 4. Result set handling
 * 5. Return value mapping
 * 6. Metadata caching
 * 7. Named parameter support
 * 
 * When to Use:
 * - Stored procedures in database
 * - Database functions
 * - Complex database logic
 * - Legacy database procedures
 * - Database-side processing
 * 
 * Benefits:
 * - Simplified procedure calls
 * - Automatic parameter detection
 * - Type-safe parameter handling
 * - Metadata caching
 * - Clean API
 */
@SpringBootApplication
public class SimpleJDBCCallPattern {

    public static void main(String[] args) {
        SpringApplication.run(SimpleJDBCCallPattern.class, args);
        System.out.println("Simple JDBC Call Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/jdbc-call/procedures");
    }

    /**
     * Configuration
     */
    @Configuration
    public static class SimpleJdbcCallConfig {

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    /**
     * Repository using SimpleJdbcCall
     */
    @Repository
    public static class ProcedureRepository {

        private final SimpleJdbcCall simpleJdbcCall;
        private final JdbcTemplate jdbcTemplate;

        public ProcedureRepository(DataSource dataSource, JdbcTemplate jdbcTemplate) {
            this.simpleJdbcCall = new SimpleJdbcCall(dataSource);
            this.jdbcTemplate = jdbcTemplate;
            initializeDatabase();
        }

        private void initializeDatabase() {
            // Create test table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_number VARCHAR(20) UNIQUE, " +
                    "balance DECIMAL(15,2))");
            
            // Create sample stored procedures (H2 syntax)
            jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS GET_ACCOUNT_BALANCE AS $$\n" +
                    "Double getAccountBalance(String accountNumber) {\n" +
                    "    return 1000.00;\n" +
                    "}$$");
            
            jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS TRANSFER_FUNDS AS $$\n" +
                    "String transferFunds(String fromAccount, String toAccount, Double amount) {\n" +
                    "    return \"SUCCESS\";\n" +
                    "}$$");
        }

        /**
         * Call simple stored procedure
         */
        public Double getAccountBalance(String accountNumber) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withFunctionName("GET_ACCOUNT_BALANCE");
            
            Map<String, Object> inParams = new HashMap<>();
            inParams.put("accountNumber", accountNumber);
            
            return jdbcCall.executeFunction(Double.class, inParams);
        }

        /**
         * Call procedure with IN parameters
         */
        public String transferFunds(String fromAccount, String toAccount, Double amount) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withFunctionName("TRANSFER_FUNDS");
            
            Map<String, Object> inParams = new HashMap<>();
            inParams.put("fromAccount", fromAccount);
            inParams.put("toAccount", toAccount);
            inParams.put("amount", amount);
            
            return jdbcCall.executeFunction(String.class, inParams);
        }

        /**
         * Call procedure with OUT parameters
         */
        public Map<String, Object> callProcedureWithOutParams(String procedureName, 
                                                               Map<String, Object> inParams) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(procedureName)
                    .declareParameters(
                            new SqlParameter("in_param", Types.VARCHAR),
                            new SqlOutParameter("out_result", Types.VARCHAR),
                            new SqlOutParameter("out_status", Types.INTEGER)
                    );
            
            return jdbcCall.execute(inParams);
        }

        /**
         * Call procedure returning result set
         */
        public List<Map<String, Object>> callProcedureWithResultSet(String procedureName) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(procedureName)
                    .returningResultSet("accounts", (rs, rowNum) -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("accountNumber", rs.getString("account_number"));
                        row.put("balance", rs.getDouble("balance"));
                        return row;
                    });
            
            Map<String, Object> result = jdbcCall.execute();
            return (List<Map<String, Object>>) result.get("accounts");
        }

        /**
         * Call database function
         */
        public <T> T callFunction(String functionName, Class<T> returnType, 
                                 Map<String, Object> params) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withFunctionName(functionName);
            
            return jdbcCall.executeFunction(returnType, params);
        }

        /**
         * Call procedure in specific schema
         */
        public Map<String, Object> callProcedureInSchema(String schemaName, 
                                                         String procedureName, 
                                                         Map<String, Object> params) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withSchemaName(schemaName)
                    .withProcedureName(procedureName);
            
            return jdbcCall.execute(params);
        }

        /**
         * Call procedure with complex parameters
         */
        public Map<String, Object> callComplexProcedure(Map<String, Object> params) {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("COMPLEX_PROCEDURE")
                    .declareParameters(
                            new SqlParameter("in_id", Types.BIGINT),
                            new SqlParameter("in_name", Types.VARCHAR),
                            new SqlParameter("in_amount", Types.DECIMAL),
                            new SqlOutParameter("out_result", Types.VARCHAR),
                            new SqlOutParameter("out_new_id", Types.BIGINT)
                    );
            
            return jdbcCall.execute(params);
        }

        /**
         * Example: Get statistics from procedure
         */
        public Map<String, Object> getAccountStatistics(Long accountId) {
            Map<String, Object> inParams = new HashMap<>();
            inParams.put("account_id", accountId);
            
            // Simulated call - would call actual procedure
            return Map.of(
                    "totalTransactions", 42,
                    "totalDeposits", 5000.00,
                    "totalWithdrawals", 2000.00,
                    "currentBalance", 3000.00
            );
        }
    }

    /**
     * Service Layer
     */
    @Service
    public static class ProcedureService {

        private final ProcedureRepository repository;

        public ProcedureService(ProcedureRepository repository) {
            this.repository = repository;
        }

        public Double getBalance(String accountNumber) {
            return repository.getAccountBalance(accountNumber);
        }

        public String transfer(String from, String to, Double amount) {
            return repository.transferFunds(from, to, amount);
        }

        public Map<String, Object> getStatistics(Long accountId) {
            return repository.getAccountStatistics(accountId);
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/jdbc-call")
    public static class ProcedureController {

        private final ProcedureService service;

        public ProcedureController(ProcedureService service) {
            this.service = service;
        }

        @GetMapping("/procedures/balance/{accountNumber}")
        public Double getBalance(@PathVariable String accountNumber) {
            return service.getBalance(accountNumber);
        }

        @PostMapping("/procedures/transfer")
        public String transfer(@RequestBody Map<String, Object> request) {
            String from = (String) request.get("from");
            String to = (String) request.get("to");
            Double amount = ((Number) request.get("amount")).doubleValue();
            return service.transfer(from, to, amount);
        }

        @GetMapping("/procedures/statistics/{accountId}")
        public Map<String, Object> getStatistics(@PathVariable Long accountId) {
            return service.getStatistics(accountId);
        }
    }
}

/**
 * MySQL Stored Procedure Examples:
 * 
 * -- Procedure with IN and OUT parameters
 * DELIMITER $$
 * CREATE PROCEDURE transfer_funds(
 *     IN from_account VARCHAR(20),
 *     IN to_account VARCHAR(20),
 *     IN amount DECIMAL(15,2),
 *     OUT status VARCHAR(20),
 *     OUT new_from_balance DECIMAL(15,2)
 * )
 * BEGIN
 *     DECLARE current_balance DECIMAL(15,2);
 *     
 *     -- Get current balance
 *     SELECT balance INTO current_balance 
 *     FROM accounts WHERE account_number = from_account;
 *     
 *     IF current_balance >= amount THEN
 *         -- Deduct from source
 *         UPDATE accounts SET balance = balance - amount 
 *         WHERE account_number = from_account;
 *         
 *         -- Add to destination
 *         UPDATE accounts SET balance = balance + amount 
 *         WHERE account_number = to_account;
 *         
 *         SET status = 'SUCCESS';
 *         SET new_from_balance = current_balance - amount;
 *     ELSE
 *         SET status = 'INSUFFICIENT_FUNDS';
 *         SET new_from_balance = current_balance;
 *     END IF;
 * END$$
 * DELIMITER ;
 * 
 * 
 * -- Function example
 * DELIMITER $$
 * CREATE FUNCTION get_account_balance(acc_number VARCHAR(20))
 * RETURNS DECIMAL(15,2)
 * DETERMINISTIC
 * BEGIN
 *     DECLARE bal DECIMAL(15,2);
 *     SELECT balance INTO bal FROM accounts 
 *     WHERE account_number = acc_number;
 *     RETURN bal;
 * END$$
 * DELIMITER ;
 * 
 * 
 * -- Procedure returning result set
 * DELIMITER $$
 * CREATE PROCEDURE get_high_balance_accounts(IN min_balance DECIMAL(15,2))
 * BEGIN
 *     SELECT * FROM accounts WHERE balance >= min_balance;
 * END$$
 * DELIMITER ;
 * 
 * 
 * Best Practices:
 * 
 * 1. Use SimpleJdbcCall for stored procedures
 * 2. Declare parameters explicitly when needed
 * 3. Use returningResultSet for result sets
 * 4. Handle multiple result sets
 * 5. Cache SimpleJdbcCall instances
 * 6. Use appropriate SQL types
 * 7. Handle NULL values properly
 * 8. Test procedures independently
 */
