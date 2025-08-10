package com.redizego.redi_ze_go;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for RediZeGo - A comprehensive ride-sharing platform.
 * 
 * RediZeGo is a full-featured ride-sharing application that connects riders with drivers,
 * providing a seamless transportation solution with advanced features including:
 * 
 * <h3>Core Features:</h3>
 * <ul>
 *   <li><b>Dual User Roles:</b> Support for both riders and drivers with role-based functionality</li>
 *   <li><b>Smart Ride Matching:</b> Algorithm-based driver matching considering ratings and proximity</li>
 *   <li><b>Multiple Payment Methods:</b> Cash, digital wallet, and card payment support</li>
 *   <li><b>Real-time Tracking:</b> GPS-based ride tracking and location services</li>
 *   <li><b>Rating System:</b> Bidirectional rating system for quality assurance</li>
 *   <li><b>Offline Support:</b> Network-aware processing with offline capability</li>
 * </ul>
 * 
 * <h3>Technical Architecture:</h3>
 * <ul>
 *   <li><b>Spring Boot:</b> Microservices-ready RESTful API architecture</li>
 *   <li><b>Spring Security:</b> JWT-based authentication and authorization</li>
 *   <li><b>Spring Data JPA:</b> Database abstraction with PostgreSQL + PostGIS</li>
 *   <li><b>Strategy Pattern:</b> Pluggable algorithms for fare calculation and driver matching</li>
 *   <li><b>Retry Mechanism:</b> Resilient processing with exponential backoff</li>
 *   <li><b>Scheduled Tasks:</b> Background processing for retry logic and maintenance</li>
 * </ul>
 * 
 * <h3>Key Annotations:</h3>
 * <ul>
 *   <li><b>@SpringBootApplication:</b> Enables auto-configuration, component scanning, and configuration</li>
 *   <li><b>@EnableRetry:</b> Activates retry capabilities for handling transient failures</li>
 *   <li><b>@EnableScheduling:</b> Enables scheduled task execution for background processing</li>
 * </ul>
 * 
 * <h3>Application Startup:</h3>
 * The application initializes with:
 * <ol>
 *   <li>Database connection and schema validation</li>
 *   <li>Security configuration and JWT setup</li>
 *   <li>Strategy pattern component registration</li>
 *   <li>Scheduled task activation for retry processing</li>
 *   <li>REST API endpoint exposure</li>
 * </ol>
 * 
 * <h3>Configuration:</h3>
 * Application behavior is controlled through:
 * <ul>
 *   <li><code>application.yml</code> - Main configuration file</li>
 *   <li><code>application-{profile}.yml</code> - Environment-specific settings</li>
 *   <li>Environment variables for sensitive data</li>
 *   <li>Java system properties for runtime parameters</li>
 * </ul>
 * 
 * <h3>Deployment:</h3>
 * The application can be deployed as:
 * <ul>
 *   <li>Standalone JAR with embedded Tomcat</li>
 *   <li>Docker container for containerized environments</li>
 *   <li>Cloud-native deployment (AWS, GCP, Azure)</li>
 *   <li>Traditional servlet container deployment</li>
 * </ul>
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.retry.annotation.EnableRetry
 * @see org.springframework.scheduling.annotation.EnableScheduling
 */
@SpringBootApplication // Enables Spring Boot auto-configuration, component scanning, and configuration
@EnableRetry          // Enables Spring Retry functionality for resilient processing
@EnableScheduling     // Enables Spring Task Scheduling for background operations
public class RediZeGoApplication {

	/**
	 * Main entry point for the RediZeGo ride-sharing application.
	 * 
	 * This method bootstraps the entire Spring Boot application by:
	 * <ol>
	 *   <li>Creating the Spring Application Context</li>
	 *   <li>Loading configuration from various sources</li>
	 *   <li>Initializing all Spring beans and components</li>
	 *   <li>Starting the embedded web server (Tomcat by default)</li>
	 *   <li>Exposing REST API endpoints</li>
	 *   <li>Activating scheduled tasks and retry mechanisms</li>
	 * </ol>
	 * 
	 * <h3>Startup Sequence:</h3>
	 * <pre>
	 * 1. JVM starts and loads the main class
	 * 2. SpringApplication.run() is called
	 * 3. Spring Boot auto-configuration kicks in
	 * 4. Component scanning discovers all beans
	 * 5. Database connections are established
	 * 6. Security configuration is applied
	 * 7. Web server starts on configured port
	 * 8. Application is ready to serve requests
	 * </pre>
	 * 
	 * <h3>Command Line Arguments:</h3>
	 * The application accepts various command line arguments:
	 * <ul>
	 *   <li><code>--server.port=8080</code> - Override default port</li>
	 *   <li><code>--spring.profiles.active=prod</code> - Set active profile</li>
	 *   <li><code>--logging.level.root=DEBUG</code> - Set logging level</li>
	 *   <li><code>--spring.datasource.url=...</code> - Override database URL</li>
	 * </ul>
	 * 
	 * <h3>Environment Variables:</h3>
	 * Key environment variables that can be set:
	 * <ul>
	 *   <li><code>DATABASE_URL</code> - PostgreSQL connection string</li>
	 *   <li><code>JWT_SECRET</code> - Secret key for JWT token signing</li>
	 *   <li><code>SPRING_PROFILES_ACTIVE</code> - Active Spring profile</li>
	 *   <li><code>SERVER_PORT</code> - HTTP server port</li>
	 * </ul>
	 * 
	 * @param args Command line arguments passed to the application.
	 *             These can include Spring Boot configuration overrides,
	 *             system properties, and application-specific parameters.
	 * 
	 * @throws RuntimeException if application fails to start due to:
	 *                         configuration errors, database connectivity issues,
	 *                         port conflicts, or other startup failures.
	 * 
	 * @implNote The application uses Spring Boot's default error handling
	 *           and will log detailed error messages to help with troubleshooting.
	 * 
	 * @see SpringApplication#run(Class, String[])
	 * @see org.springframework.boot.autoconfigure.SpringBootApplication
	 */
	public static void main(String[] args) {
		// Bootstrap the Spring Boot application with full context initialization
		// This single line handles the entire application startup process
		SpringApplication.run(RediZeGoApplication.class, args);
		
		// Note: After this call completes successfully, the application is:
		// - Fully initialized with all beans created
		// - Web server running and accepting requests
		// - Database connections established
		// - Scheduled tasks active
		// - Ready to serve ride-sharing operations
	}

}
