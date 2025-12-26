package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PrintStream;
import java.util.Map;

/**
 * Banner Pattern
 * 
 * Demonstrates customizing Spring Boot application banner,
 * including custom banners, banner modes, and banner customization.
 * 
 * Key Concepts:
 * - Custom banner
 * - Banner mode
 * - Banner properties
 * - Resource banner
 * - Programmatic banner
 * 
 * Use Cases:
 * - Application branding
 * - Version display
 * - Environment info
 * - Startup customization
 * - Company branding
 */
@SpringBootApplication
public class BannerPattern {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BannerPattern.class);
        
        // Set banner mode
        app.setBannerMode(Banner.Mode.CONSOLE);
        
        // Set custom banner
        app.setBanner(new CustomBanner());
        
        app.run(args);
    }
}

/**
 * Custom banner implementation
 */
class CustomBanner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println("╔═══════════════════════════════════════╗");
        out.println("║   SPRING BOOT BANNER PATTERN DEMO     ║");
        out.println("║                                       ║");
        out.println("║   Application: " + environment.getProperty("spring.application.name", "MyApp") + "           ║");
        out.println("║   Version: 1.0.0                      ║");
        out.println("║   Spring Boot: " + getSpringBootVersion() + "              ║");
        out.println("║                                       ║");
        out.println("╚═══════════════════════════════════════╝");
    }

    private String getSpringBootVersion() {
        return org.springframework.boot.SpringBootVersion.getVersion();
    }
}

/**
 * Controller providing banner information
 */
@Controller
class BannerController {

    @GetMapping("/banner/info")
    @ResponseBody
    public Map<String, Object> getBannerInfo() {
        return Map.of(
                "bannerMode", "CONSOLE",
                "customBanner", true,
                "springBootVersion", org.springframework.boot.SpringBootVersion.getVersion(),
                "bannerType", "Custom Implementation"
        );
    }

    @GetMapping("/banner/ascii")
    @ResponseBody
    public Map<String, String> getAsciiBanner() {
        return Map.of(
                "banner", 
                "╔═══════════════════════════════════════╗\n" +
                "║   SPRING BOOT BANNER PATTERN DEMO     ║\n" +
                "║                                       ║\n" +
                "║   Application: MyApp                  ║\n" +
                "║   Version: 1.0.0                      ║\n" +
                "╚═══════════════════════════════════════╝"
        );
    }
}

/**
 * Documentation:
 * 
 * Banner Modes:
 * - Banner.Mode.OFF - Disable banner
 * - Banner.Mode.CONSOLE - Print to console
 * - Banner.Mode.LOG - Print to log
 * 
 * application.properties:
 * spring.banner.location=classpath:banner.txt
 * spring.banner.charset=UTF-8
 * spring.banner.image.location=classpath:banner.gif
 * spring.banner.image.width=76
 * spring.banner.image.height=
 * spring.banner.image.margin=2
 * spring.banner.image.invert=false
 * 
 * Disable Banner:
 * spring.main.banner-mode=off
 * 
 * Or programmatically:
 * SpringApplication app = new SpringApplication(MyApp.class);
 * app.setBannerMode(Banner.Mode.OFF);
 * 
 * Custom banner.txt (src/main/resources):
 * ╔═══════════════════════════════════════╗
 * ║          MY APPLICATION               ║
 * ║   Version: ${application.version}     ║
 * ║   Spring Boot: ${spring-boot.version} ║
 * ╚═══════════════════════════════════════╝
 * 
 * Available Variables in banner.txt:
 * ${application.version} - From MANIFEST.MF
 * ${application.formatted-version} - Formatted version
 * ${spring-boot.version} - Spring Boot version
 * ${spring-boot.formatted-version} - Formatted Spring Boot version
 * ${application.title} - Application title
 * ${AnsiColor.NAME} - ANSI colors
 * ${AnsiBackground.NAME} - ANSI backgrounds
 * ${AnsiStyle.NAME} - ANSI styles
 * 
 * ANSI Colors:
 * ${AnsiColor.BLACK}
 * ${AnsiColor.RED}
 * ${AnsiColor.GREEN}
 * ${AnsiColor.YELLOW}
 * ${AnsiColor.BLUE}
 * ${AnsiColor.MAGENTA}
 * ${AnsiColor.CYAN}
 * ${AnsiColor.WHITE}
 * ${AnsiColor.BRIGHT_BLACK}
 * etc.
 * 
 * Custom Banner Implementation:
 * public class MyBanner implements Banner {
 *     @Override
 *     public void printBanner(Environment env, 
 *                            Class<?> sourceClass, 
 *                            PrintStream out) {
 *         out.println("My Custom Banner");
 *         out.println("Version: " + env.getProperty("app.version"));
 *     }
 * }
 * 
 * SpringApplication app = new SpringApplication(MyApp.class);
 * app.setBanner(new MyBanner());
 * 
 * Resource Banner:
 * app.setBanner(new ResourceBanner(
 *     new ClassPathResource("my-banner.txt")));
 * 
 * ASCII Art Generators:
 * - http://patorjk.com/software/taag/
 * - https://www.ascii-art-generator.org/
 * - http://www.network-science.de/ascii/
 * 
 * Example with Colors:
 * ${AnsiColor.GREEN} ███████╗██████╗ ██████╗ ██╗███╗   ██╗ ██████╗ 
 * ${AnsiColor.GREEN} ██╔════╝██╔══██╗██╔══██╗██║████╗  ██║██╔════╝ 
 * ${AnsiColor.GREEN} ███████╗██████╔╝██████╔╝██║██╔██╗ ██║██║  ███╗
 * ${AnsiColor.GREEN} ╚════██║██╔═══╝ ██╔══██╗██║██║╚██╗██║██║   ██║
 * ${AnsiColor.GREEN} ███████║██║     ██║  ██║██║██║ ╚████║╚██████╔╝
 * ${AnsiColor.GREEN} ╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝ ╚═════╝ 
 * ${AnsiColor.DEFAULT}
 * 
 * Best Practices:
 * - Keep banner concise
 * - Show relevant info (version, env)
 * - Use OFF mode in production
 * - Test banner rendering
 * - Consider terminal width
 * - Use meaningful branding
 */
