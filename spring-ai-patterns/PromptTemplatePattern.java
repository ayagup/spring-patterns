package com.example.ai.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING AI - PROMPT TEMPLATE PATTERN 💡
 * ==========================================
 * 
 * Prompt templates enable structured, reusable prompts for LLM interactions
 * with variable substitution, formatting, and prompt engineering best practices.
 * 
 * 🎯 KEY FEATURES:
 * - Template-based prompts
 * - Variable substitution
 * - Prompt composition
 * - System/user/assistant roles
 * - Few-shot examples
 * - Chain-of-thought prompting
 * 
 * 📦 PROMPT TEMPLATE (Spring AI):
 * ===============================
 * 
 * @Service
 * class ChatService(
 *     private val chatClient: ChatClient,
 *     private val promptTemplate: PromptTemplate
 * ) {
 *     fun generateResponse(topic: String): String {
 *         val template = """
 *             You are a helpful assistant.
 *             Please explain {topic} in simple terms.
 *             Provide 3 key points.
 *         """.trimIndent()
 *         
 *         val prompt = PromptTemplate(template)
 *             .create(mapOf("topic" to topic))
 *         
 *         return chatClient.call(prompt).content
 *     }
 * }
 * 
 * 🔧 ADVANCED TEMPLATES:
 * ======================
 * 
 * // Few-shot learning
 * val fewShotTemplate = """
 *     Examples:
 *     Q: What is 2+2?
 *     A: 4
 *     
 *     Q: What is 3+5?
 *     A: 8
 *     
 *     Q: What is {question}?
 *     A:
 * """.trimIndent()
 * 
 * // Chain-of-thought
 * val cotTemplate = """
 *     Solve this step by step:
 *     Problem: {problem}
 *     
 *     Step 1:
 *     Step 2:
 *     Step 3:
 *     
 *     Answer:
 * """.trimIndent()
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class PromptTemplatePattern {
    public static void main(String[] args) {
        SpringApplication.run(PromptTemplatePattern.class, args);
    }
}

@Service
class PromptTemplateService {
    public Map<String, Object> getTemplateInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("pattern", "Prompt Template");
        info.put("purpose", "Structured LLM interactions");
        info.put("features", Arrays.asList(
            "Variable substitution",
            "Role-based prompting",
            "Few-shot examples",
            "Chain-of-thought",
            "Prompt composition",
            "Template reusability"
        ));
        info.put("use_cases", Arrays.asList(
            "Content generation",
            "Question answering",
            "Code generation",
            "Data extraction",
            "Summarization",
            "Translation"
        ));
        return info;
    }
    
    public List<String> getTemplateExamples() {
        return Arrays.asList(
            "Basic: 'Explain {topic} in simple terms'",
            "Few-shot: 'Examples: Q: ... A: ... Now: {question}'",
            "Chain-of-thought: 'Solve step by step: {problem}'",
            "Role-based: 'You are a {role}. Help with {task}'",
            "Structured: 'Format: JSON with fields {fields}'"
        );
    }
}

@RestController
@RequestMapping("/api/ai/prompt-template")
class PromptTemplateController {
    private final PromptTemplateService service;
    
    public PromptTemplateController(PromptTemplateService service) {
        this.service = service;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return service.getTemplateInfo();
    }
    
    @GetMapping("/examples")
    public List<String> getExamples() {
        return service.getTemplateExamples();
    }
}
