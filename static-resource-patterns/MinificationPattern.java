package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minification Pattern
 * 
 * Demonstrates minification of static resources (CSS, JS) to reduce file size.
 * Minification removes whitespace, comments, and shortens variable names.
 * 
 * Note: Actual minification typically done at build time using tools like:
 * - UglifyJS, Terser (JavaScript)
 * - CSSNano, CleanCSS (CSS)
 * - HTMLMinifier (HTML)
 */
@SpringBootApplication
public class MinificationPattern {

    public static void main(String[] args) {
        SpringApplication.run(MinificationPattern.class, args);
    }

    /**
     * Example of un-minified vs minified JavaScript
     */
    @RestController
    static class MinificationExampleController {

        /**
         * Original JavaScript (unmminified)
         */
        @GetMapping("/js/original.js")
        public String originalJavaScript() {
            return "/**\n" +
                   " * User authentication module\n" +
                   " * @author John Doe\n" +
                   " */\n" +
                   "function authenticateUser(username, password) {\n" +
                   "    // Check if username is valid\n" +
                   "    if (!username || username.length === 0) {\n" +
                   "        console.error('Username is required');\n" +
                   "        return false;\n" +
                   "    }\n" +
                   "    \n" +
                   "    // Check if password is valid\n" +
                   "    if (!password || password.length < 8) {\n" +
                   "        console.error('Password must be at least 8 characters');\n" +
                   "        return false;\n" +
                   "    }\n" +
                   "    \n" +
                   "    // Authenticate user\n" +
                   "    return performAuthentication(username, password);\n" +
                   "}\n";
        }

        /**
         * Minified JavaScript
         * - Comments removed\n         * - Whitespace removed
         * - Variable names could be shortened (advanced minification)
         */
        @GetMapping("/js/minified.js")
        public String minifiedJavaScript() {
            return "function authenticateUser(e,r){return e&&0!==e.length?(r&&r.length>=8||(console.error('Password must be at least 8 characters'),!1),performAuthentication(e,r)):(console.error('Username is required'),!1)}";
        }

        /**
         * Original CSS (unminified)
         */
        @GetMapping("/css/original.css")
        public String originalCSS() {
            return "/* Main stylesheet */\n" +
                   "body {\n" +
                   "    margin: 0;\n" +
                   "    padding: 0;\n" +
                   "    font-family: Arial, sans-serif;\n" +
                   "    background-color: #ffffff;\n" +
                   "}\n" +
                   "\n" +
                   ".container {\n" +
                   "    max-width: 1200px;\n" +
                   "    margin: 0 auto;\n" +
                   "    padding: 20px;\n" +
                   "}\n" +
                   "\n" +
                   "/* Button styles */\n" +
                   ".button {\n" +
                   "    display: inline-block;\n" +
                   "    padding: 10px 20px;\n" +
                   "    background-color: #007bff;\n" +
                   "    color: white;\n" +
                   "    border: none;\n" +
                   "    border-radius: 4px;\n" +
                   "    cursor: pointer;\n" +
                   "}\n" +
                   "\n" +
                   ".button:hover {\n" +
                   "    background-color: #0056b3;\n" +
                   "}\n";
        }

        /**
         * Minified CSS
         */
        @GetMapping("/css/minified.css")
        public String minifiedCSS() {
            return "body{margin:0;padding:0;font-family:Arial,sans-serif;background-color:#fff}.container{max-width:1200px;margin:0 auto;padding:20px}.button{display:inline-block;padding:10px 20px;background-color:#007bff;color:#fff;border:none;border-radius:4px;cursor:pointer}.button:hover{background-color:#0056b3}";
        }

        /**
         * File size comparison
         */
        @GetMapping("/comparison")
        public String sizeComparison() {
            String original = originalJavaScript();
            String minified = minifiedJavaScript();
            
            double reduction = ((original.length() - minified.length()) / (double) original.length()) * 100;
            
            return String.format(
                "Size Comparison:\n" +
                "Original: %d bytes\n" +
                "Minified: %d bytes\n" +
                "Reduction: %.2f%%\n",
                original.length(),
                minified.length(),
                reduction
            );
        }
    }

    /**
     * Build-time minification configuration example
     * (This would typically be in build.gradle or pom.xml)
     */
    static class BuildTimeMinificationExample {

        /**
         * Maven example (pom.xml):
         * 
         * <plugin>
         *     <groupId>com.github.buckelieg</groupId>
         *     <artifactId>minify-maven-plugin</artifactId>
         *     <version>1.7.6</version>
         *     <executions>
         *         <execution>
         *             <id>default-minify</id>
         *             <phase>process-resources</phase>
         *             <goals>
         *                 <goal>minify</goal>
         *             </goals>
         *             <configuration>
         *                 <cssSourceDir>src/main/resources/static/css</cssSourceDir>
         *                 <cssTargetDir>target/classes/static/css</cssTargetDir>
         *                 <jsSourceDir>src/main/resources/static/js</jsSourceDir>
         *                 <jsTargetDir>target/classes/static/js</jsTargetDir>
         *                 <suffix>.min</suffix>
         *             </configuration>
         *         </execution>
         *     </executions>
         * </plugin>
         */
        
        /**
         * Gradle example (build.gradle):
         * 
         * plugins {
         *     id 'com.github.eirslett.node' version '1.5.1'
         * }
         * 
         * task minifyJs(type: Exec) {
         *     commandLine 'npm', 'run', 'minify'
         * }
         * 
         * processResources.dependsOn minifyJs
         */
        
        /**
         * NPM scripts (package.json):
         * 
         * {
         *   "scripts": {
         *     "minify:js": "terser src/main/resources/static/js/*.js -o target/classes/static/js/app.min.js",
         *     "minify:css": "cleancss -o target/classes/static/css/app.min.css src/main/resources/static/css/*.css",
         *     "minify": "npm run minify:js && npm run minify:css"
         *   },
         *   "devDependencies": {
         *     "terser": "^5.0.0",
         *     "clean-css-cli": "^5.0.0"
         *   }
         * }
         */
    }

    /**
     * Minification benefits
     */
    static class MinificationBenefits {
        /**
         * 1. Reduced file size (30-90% reduction typical)
         * 2. Faster download times
         * 3. Reduced bandwidth usage
         * 4. Improved page load performance
         * 5. Better SEO (faster loading pages rank higher)
         * 6. Lower hosting costs (less bandwidth)
         * 
         * Best practices:
         * - Always keep original (unminified) source files
         * - Use source maps for debugging minified code
         * - Minify as part of build process, not runtime
         * - Combine with gzip compression for maximum reduction
         * - Use versioning/cache busting with minified files
         */
    }
}
