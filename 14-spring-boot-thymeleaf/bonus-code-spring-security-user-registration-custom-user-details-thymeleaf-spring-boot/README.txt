SPRING SECURITY, THYMELEAF and Spring Boot
==========================================

A student asked for a Thymeleaf version of the application. This version makes use of Spring Boot

Full solution code is available at this link:


For docs on Spring MVC and Thymeleaf integration, see this link
- https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html

For docs on Spring Security and Thymeleaf integration, see this link
- https://www.thymeleaf.org/doc/articles/springsecurity.html


A couple of high-level changes

1. Add the Thymeleaf pom entries

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-thymeleaf</artifactId>
		</dependency>
		
		<dependency>
			<groupId>org.thymeleaf.extras</groupId>
			<artifactId>thymeleaf-extras-springsecurity5</artifactId>
		</dependency>

2. Spring Boot Auto-Configuration
When Spring Boot finds Thymeleaf dependency in the Maven POM file, it automatically configures Thymeleaf template engine. 
No need to manually configure Thymeleaf in our code since it is auto-configured by Spring Boot.

3. Create Thymeleaf views
See code examples in: src/main/resources/templates


