package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cypher Query Pattern
 *
 * Demonstrates the use of the @Query annotation to execute custom Cypher queries
 * within Spring Data Neo4j repositories. This pattern allows for complex and optimized
 * graph traversals that go beyond what derived queries can offer.
 *
 * Key Features:
 * - Full control over the Cypher query logic.
 * - Ability to use any Cypher feature, including pathfinding, aggregations, and subqueries.
 * - Mapping of query results to domain entities, projections, or simple types.
 * - Use of named parameters for safe and clear query construction.
 *
 * Use Cases:
 * - Complex graph algorithms (e.g., shortest path, community detection).
 * - Performance-critical queries requiring specific index hints or query structures.
 * - Queries involving multiple relationship types and complex filtering.
 * - Returning custom data structures (DTOs) or projections.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class CypherQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(CypherQueryPattern.class, args);
    }
}

// Domain Models
@Node
class Movie {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private int released;

    public Movie() {}

    public Movie(String title, int released) {
        this.title = title;
        this.released = released;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getReleased() { return released; }
    public void setReleased(int released) { this.released = released; }
}

@Node
class Actor {
    @Id @GeneratedValue
    private Long id;
    private String name;

    public Actor() {}

    public Actor(String name) {
        this.name = name;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

@RelationshipProperties
class ActedIn {
    @Id @GeneratedValue
    private Long id;
    @TargetNode
    private Movie movie;
    private List<String> roles;

    public ActedIn(Movie movie, List<String> roles) {
        this.movie = movie;
        this.roles = roles;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

// Repository Interface
@Repository
interface MovieRepository extends Neo4jRepository<Movie, Long> {

    /**
     * Custom Cypher query to find movies released within a specific year range.
     */
    @Query("MATCH (m:Movie) WHERE m.released >= $startYear AND m.released <= $endYear RETURN m")
    Collection<Movie> findMoviesByReleaseYearRange(@Param("startYear") int startYear, @Param("endYear") int endYear);

    /**
     * Custom Cypher query to find actors who acted in a specific movie.
     */
    @Query("MATCH (a:Actor)-[:ACTED_IN]->(m:Movie) WHERE m.title = $movieTitle RETURN a")
    Collection<Actor> findActorsByMovieTitle(@Param("movieTitle") String movieTitle);

    /**
     * Custom Cypher query to find movies a specific actor has acted in.
     */
    @Query("MATCH (a:Actor {name: $actorName})-[:ACTED_IN]->(m:Movie) RETURN m")
    Collection<Movie> findMoviesByActorName(@Param("actorName") String actorName);

    /**
     * Custom Cypher query with a projection to get movie titles and their release years.
     */
    @Query("MATCH (m:Movie) RETURN m.title as title, m.released as releasedYear")
    Collection<MovieProjection> findAllMovieProjections();

    /**
     * Custom Cypher query to find co-actors of a given actor.
     */
    @Query("""
        MATCH (actor:Actor {name: $actorName})-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(coActor:Actor)
        WHERE actor <> coActor
        RETURN DISTINCT coActor
    """)
    Collection<Actor> findCoActors(@Param("actorName") String actorName);
    
    /**
     * Custom Cypher query to get the number of movies per decade.
     */
    @Query("""
        MATCH (m:Movie)
        WITH m.released / 10 * 10 AS decade
        RETURN decade, count(m) as movieCount
        ORDER BY decade
    """)
    Collection<Map<String, Object>> countMoviesPerDecade();
}

// Projection Interface
interface MovieProjection {
    String getTitle();
    int getReleasedYear();
}

// Service Layer
@Service
class MovieGraphService {
    private final MovieRepository movieRepository;

    public MovieGraphService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Collection<Movie> getMoviesByYearRange(int start, int end) {
        return movieRepository.findMoviesByReleaseYearRange(start, end);
    }

    public Collection<Actor> getActorsByMovie(String title) {
        return movieRepository.findActorsByMovieTitle(title);
    }

    public Collection<Movie> getMoviesByActor(String name) {
        return movieRepository.findMoviesByActorName(name);
    }

    public Collection<MovieProjection> getMovieProjections() {
        return movieRepository.findAllMovieProjections();
    }

    public Collection<Actor> getCoActors(String name) {
        return movieRepository.findCoActors(name);
    }
    
    public Collection<Map<String, Object>> getMoviesPerDecade() {
        return movieRepository.countMoviesPerDecade();
    }
    
    public Optional<Movie> findMovieById(Long id) {
        return movieRepository.findById(id);
    }
    
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/cypher-query")
class MovieGraphController {
    private final MovieGraphService service;

    public MovieGraphController(MovieGraphService service) {
        this.service = service;
    }
    
    @PostMapping("/movies")
    public Movie createMovie(@RequestBody Movie movie) {
        return service.saveMovie(movie);
    }

    @GetMapping("/movies/by-year")
    public Collection<Movie> getMoviesByYear(@RequestParam int start, @RequestParam int end) {
        return service.getMoviesByYearRange(start, end);
    }

    @GetMapping("/actors/by-movie")
    public Collection<Actor> getActorsByMovie(@RequestParam String title) {
        return service.getActorsByMovie(title);
    }

    @GetMapping("/movies/by-actor")
    public Collection<Movie> getMoviesByActor(@RequestParam String name) {
        return service.getMoviesByActor(name);
    }

    @GetMapping("/movies/projections")
    public Collection<MovieProjection> getMovieProjections() {
        return service.getMovieProjections();
    }

    @GetMapping("/actors/co-actors")
    public Collection<Actor> getCoActors(@RequestParam String name) {
        return service.getCoActors(name);
    }
    
    @GetMapping("/movies/decade-counts")
    public Collection<Map<String, Object>> getDecadeCounts() {
        return service.getMoviesPerDecade();
    }
    
    @GetMapping("/movies/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return service.findMovieById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Cypher Query Pattern",
            "description", "Executes custom Cypher queries using the @Query annotation in repositories.",
            "features", "Complex traversals, projections, aggregations, named parameters.",
            "endpoints", "8 REST endpoints demonstrating various custom Cypher queries."
        );
    }
}
