package com.example.springaipatterns;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Embedding Client Pattern
 * 
 * Demonstrates the use of Spring AI's Embedding Client for converting text
 * into vector embeddings using models like text-embedding-ada-002.
 * 
 * Key Concepts:
 * - Text-to-vector conversion
 * - Embedding models (OpenAI, Azure OpenAI)
 * - Similarity calculations
 * - Batch embedding generation
 * - Embedding dimensions
 */
@SpringBootApplication
public class EmbeddingClientPattern {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddingClientPattern.class, args);
    }

    @Configuration
    static class EmbeddingConfiguration {
        
        @Bean
        public EmbeddingClient embeddingClient() {
            // Configure OpenAI API
            String apiKey = System.getenv("OPENAI_API_KEY");
            OpenAiApi openAiApi = new OpenAiApi(apiKey);
            
            // Configure embedding options
            OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .withModel("text-embedding-ada-002")  // 1536 dimensions
                .build();
            
            return new OpenAiEmbeddingClient(openAiApi, options);
        }
    }

    @Service
    static class EmbeddingService {
        
        private final EmbeddingClient embeddingClient;
        
        public EmbeddingService(EmbeddingClient embeddingClient) {
            this.embeddingClient = embeddingClient;
        }
        
        /**
         * Generate embedding for a single text
         */
        public List<Double> embed(String text) {
            EmbeddingResponse response = embeddingClient.embedForResponse(List.of(text));
            return response.getResults().get(0).getOutput();
        }
        
        /**
         * Generate embeddings for multiple texts
         */
        public List<List<Double>> embedBatch(List<String> texts) {
            EmbeddingResponse response = embeddingClient.embedForResponse(texts);
            return response.getResults().stream()
                .map(result -> result.getOutput())
                .collect(Collectors.toList());
        }
        
        /**
         * Calculate cosine similarity between two embeddings
         */
        public double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
            if (embedding1.size() != embedding2.size()) {
                throw new IllegalArgumentException("Embeddings must have the same dimension");
            }
            
            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;
            
            for (int i = 0; i < embedding1.size(); i++) {
                dotProduct += embedding1.get(i) * embedding2.get(i);
                norm1 += embedding1.get(i) * embedding1.get(i);
                norm2 += embedding2.get(i) * embedding2.get(i);
            }
            
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }
        
        /**
         * Calculate Euclidean distance between two embeddings
         */
        public double euclideanDistance(List<Double> embedding1, List<Double> embedding2) {
            if (embedding1.size() != embedding2.size()) {
                throw new IllegalArgumentException("Embeddings must have the same dimension");
            }
            
            double sum = 0.0;
            for (int i = 0; i < embedding1.size(); i++) {
                double diff = embedding1.get(i) - embedding2.get(i);
                sum += diff * diff;
            }
            
            return Math.sqrt(sum);
        }
        
        /**
         * Find most similar text from a list
         */
        public SimilarityResult findMostSimilar(String query, List<String> candidates) {
            List<Double> queryEmbedding = embed(query);
            
            double maxSimilarity = -1.0;
            String mostSimilar = null;
            int mostSimilarIndex = -1;
            
            for (int i = 0; i < candidates.size(); i++) {
                List<Double> candidateEmbedding = embed(candidates.get(i));
                double similarity = cosineSimilarity(queryEmbedding, candidateEmbedding);
                
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilar = candidates.get(i);
                    mostSimilarIndex = i;
                }
            }
            
            return new SimilarityResult(mostSimilar, mostSimilarIndex, maxSimilarity);
        }
        
        /**
         * Get embedding dimensions
         */
        public int getEmbeddingDimensions(String text) {
            return embed(text).size();
        }
    }

    @RestController
    @RequestMapping("/api/embedding")
    static class EmbeddingController {
        
        private final EmbeddingService embeddingService;
        
        public EmbeddingController(EmbeddingService embeddingService) {
            this.embeddingService = embeddingService;
        }
        
        @PostMapping("/embed")
        public EmbeddingResponse embed(@RequestBody EmbeddingRequest request) {
            List<Double> embedding = embeddingService.embed(request.text());
            return new EmbeddingResponse(embedding, embedding.size());
        }
        
        @PostMapping("/embed-batch")
        public BatchEmbeddingResponse embedBatch(@RequestBody BatchEmbeddingRequest request) {
            List<List<Double>> embeddings = embeddingService.embedBatch(request.texts());
            int dimensions = embeddings.isEmpty() ? 0 : embeddings.get(0).size();
            return new BatchEmbeddingResponse(embeddings, dimensions, embeddings.size());
        }
        
        @PostMapping("/similarity")
        public SimilarityResponse calculateSimilarity(@RequestBody SimilarityRequest request) {
            List<Double> embedding1 = embeddingService.embed(request.text1());
            List<Double> embedding2 = embeddingService.embed(request.text2());
            
            double cosineSim = embeddingService.cosineSimilarity(embedding1, embedding2);
            double euclideanDist = embeddingService.euclideanDistance(embedding1, embedding2);
            
            return new SimilarityResponse(cosineSim, euclideanDist);
        }
        
        @PostMapping("/find-similar")
        public SimilarityResult findMostSimilar(@RequestBody FindSimilarRequest request) {
            return embeddingService.findMostSimilar(request.query(), request.candidates());
        }
        
        @GetMapping("/dimensions")
        public DimensionsResponse getDimensions(@RequestParam String text) {
            int dimensions = embeddingService.getEmbeddingDimensions(text);
            return new DimensionsResponse(dimensions, "text-embedding-ada-002");
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Embedding Client Pattern",
                "description", "Text-to-vector conversion using embedding models",
                "model", "text-embedding-ada-002",
                "dimensions", 1536,
                "features", List.of(
                    "Single text embedding",
                    "Batch embedding",
                    "Cosine similarity",
                    "Euclidean distance",
                    "Similarity search"
                ),
                "endpoints", List.of(
                    "POST /api/embedding/embed",
                    "POST /api/embedding/embed-batch",
                    "POST /api/embedding/similarity",
                    "POST /api/embedding/find-similar",
                    "GET /api/embedding/dimensions",
                    "GET /api/embedding/info"
                )
            );
        }
    }

    // DTOs
    record EmbeddingRequest(String text) {}
    record EmbeddingResponse(List<Double> embedding, int dimensions) {}
    record BatchEmbeddingRequest(List<String> texts) {}
    record BatchEmbeddingResponse(List<List<Double>> embeddings, int dimensions, int count) {}
    record SimilarityRequest(String text1, String text2) {}
    record SimilarityResponse(double cosineSimilarity, double euclideanDistance) {}
    record FindSimilarRequest(String query, List<String> candidates) {}
    record SimilarityResult(String text, int index, double similarity) {}
    record DimensionsResponse(int dimensions, String model) {}
}
