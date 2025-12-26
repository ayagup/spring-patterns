package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource Bundling Pattern
 * 
 * Demonstrates bundling multiple resource files into a single file
 * to reduce HTTP requests and improve page load performance.
 * 
 * Bundling combines multiple CSS or JS files into one file.
 */
@SpringBootApplication
public class ResourceBundlingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResourceBundlingPattern.class, args);
    }

    /**
     * Example of separate vs bundled resources
     */
    @RestController
    static class BundlingExampleController {

        /**
         * Separate CSS files (3 HTTP requests)
         */
        @GetMapping("/separate/index.html")
        public String separateResources() {
            return "<!DOCTYPE html>\n" +
                   "<html>\n" +
                   "<head>\n" +
                   "    <link rel=\"stylesheet\" href=\"/css/reset.css\">\n" +
                   "    <link rel=\"stylesheet\" href=\"/css/layout.css\">\n" +
                   "    <link rel=\"stylesheet\" href=\"/css/theme.css\">\n" +
                   "    <script src=\"/js/jquery.js\"></script>\n" +
                   "    <script src=\"/js/bootstrap.js\"></script>\n" +
                   "    <script src=\"/js/app.js\"></script>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <h1>Page with 6 separate resource files</h1>\n" +
                   "</body>\n" +
                   "</html>";
        }

        /**
         * Bundled resources (2 HTTP requests)
         */
        @GetMapping("/bundled/index.html")
        public String bundledResources() {
            return "<!DOCTYPE html>\n" +
                   "<html>\n" +
                   "<head>\n" +
                   "    <link rel=\"stylesheet\" href=\"/css/bundle.min.css\">\n" +
                   "    <script src=\"/js/bundle.min.js\"></script>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <h1>Page with 2 bundled resource files</h1>\n" +
                   "</body>\n" +
                   "</html>";
        }

        /**
         * Individual CSS files
         */
        @GetMapping("/css/reset.css")
        public String resetCSS() {
            return "* { margin: 0; padding: 0; box-sizing: border-box; }";
        }

        @GetMapping("/css/layout.css")
        public String layoutCSS() {
            return ".container { max-width: 1200px; margin: 0 auto; }";
        }

        @GetMapping("/css/theme.css")
        public String themeCSS() {
            return "body { font-family: Arial; color: #333; }";
        }

        /**
         * Bundled CSS (all three combined and minified)
         */
        @GetMapping("/css/bundle.min.css")
        public String bundledCSS() {
            return "*{margin:0;padding:0;box-sizing:border-box}" +
                   ".container{max-width:1200px;margin:0 auto}" +
                   "body{font-family:Arial;color:#333}";
        }

        /**
         * Individual JavaScript files
         */
        @GetMapping("/js/jquery.js")
        public String jqueryJS() {
            return "// jQuery library code here (simulated)";
        }

        @GetMapping("/js/bootstrap.js")
        public String bootstrapJS() {
            return "// Bootstrap library code here (simulated)";
        }

        @GetMapping("/js/app.js")
        public String appJS() {
            return "// Application code here (simulated)";
        }

        /**
         * Bundled JavaScript (all three combined and minified)
         */
        @GetMapping("/js/bundle.min.js")
        public String bundledJS() {
            return "/*jQuery*/(function(){})();" +
                   "/*Bootstrap*/(function(){})();" +
                   "/*App*/(function(){})();";
        }

        /**
         * Performance comparison
         */
        @GetMapping("/performance")
        public String performanceComparison() {
            return "Performance Comparison:\n\n" +
                   "Separate Files:\n" +
                   "- 3 CSS files: 3 HTTP requests\n" +
                   "- 3 JS files: 3 HTTP requests\n" +
                   "- Total: 6 HTTP requests\n" +
                   "- Each request has overhead (DNS, TCP, TLS handshake)\n\n" +
                   "Bundled Files:\n" +
                   "- 1 CSS bundle: 1 HTTP request\n" +
                   "- 1 JS bundle: 1 HTTP request\n" +
                   "- Total: 2 HTTP requests\n" +
                   "- Reduced connection overhead\n" +
                   "- Faster page load (typically 30-70% improvement)\n\n" +
                   "Note: With HTTP/2, bundling is less critical due to multiplexing,\n" +
                   "but still beneficial for reducing parser blocking.";
        }
    }

    /**
     * Build-time bundling configuration examples
     */
    static class BuildTimeBundlingExample {

        /**
         * Webpack configuration (webpack.config.js):
         * 
         * module.exports = {
         *   entry: {
         *     bundle: ['./src/app.js', './src/utils.js', './src/components.js']
         *   },
         *   output: {
         *     filename: '[name].min.js',
         *     path: path.resolve(__dirname, 'dist')
         *   },
         *   optimization: {
         *     minimize: true,
         *     minimizer: [new TerserPlugin()]
         *   }
         * };
         */

        /**
         * Gulp configuration (gulpfile.js):
         * 
         * const gulp = require('gulp');
         * const concat = require('gulp-concat');
         * const uglify = require('gulp-uglify');
         * const cleanCSS = require('gulp-clean-css');
         * 
         * gulp.task('bundle-js', () => {
         *   return gulp.src(['src/js/jquery.js', 'src/js/bootstrap.js', 'src/js/app.js'])
         *     .pipe(concat('bundle.js'))
         *     .pipe(uglify())
         *     .pipe(gulp.dest('dist/js'));
         * });
         * 
         * gulp.task('bundle-css', () => {
         *   return gulp.src(['src/css/reset.css', 'src/css/layout.css', 'src/css/theme.css'])
         *     .pipe(concat('bundle.css'))
         *     .pipe(cleanCSS())
         *     .pipe(gulp.dest('dist/css'));
         * });
         */

        /**
         * Maven plugin (pom.xml):
         * 
         * <plugin>
         *     <groupId>com.github.warmuuh</groupId>
         *     <artifactId>libsass-maven-plugin</artifactId>
         *     <version>0.2.26</version>
         *     <executions>
         *         <execution>
         *             <phase>generate-resources</phase>
         *             <goals>
         *                 <goal>compile</goal>
         *             </goals>
         *         </execution>
         *     </executions>
         *     <configuration>
         *         <inputPath>${basedir}/src/main/sass/</inputPath>
         *         <outputPath>${basedir}/target/classes/static/css/</outputPath>
         *         <outputStyle>compressed</outputStyle>
         *     </configuration>
         * </plugin>
         */
    }

    /**
     * Bundling strategies
     */
    static class BundlingStrategies {
        /**
         * 1. Single Bundle:
         *    - All CSS/JS in one file
         *    - Simplest approach
         *    - May include unused code
         * 
         * 2. Page-Specific Bundles:
         *    - home-bundle.js, admin-bundle.js, etc.
         *    - Only load what's needed per page
         *    - Better for large applications
         * 
         * 3. Vendor + App Bundles:
         *    - vendor-bundle.js (libraries, rarely changes)
         *    - app-bundle.js (application code, changes often)
         *    - Better caching strategy
         * 
         * 4. Critical + Deferred:
         *    - critical.css (above-the-fold styles, inline)
         *    - main-bundle.css (rest of styles, async load)
         *    - Improves perceived performance
         * 
         * 5. Route-Based Code Splitting:
         *    - Used in SPAs (React, Vue, Angular)
         *    - Load bundles on-demand per route
         *    - Reduces initial load time
         */
    }

    /**
     * Bundling best practices
     */
    static class BundlingBestPractices {
        /**
         * 1. Bundle at build time, not runtime
         * 2. Combine bundling with minification
         * 3. Use source maps for debugging
         * 4. Version/fingerprint bundle files
         * 5. Enable gzip/brotli compression
         * 6. Consider HTTP/2 multiplexing
         * 7. Split vendor and app code
         * 8. Use lazy loading for large bundles
         * 9. Monitor bundle sizes (< 200KB ideal)
         * 10. Remove unused code (tree shaking)
         */
    }
}
