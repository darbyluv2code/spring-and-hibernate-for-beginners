SPRING BOOT, SPRING SECURITY AND THYMELEAF
==========================================

A student asked for a Spring Security version of the Thymeleaf Employee Directory application. This version makes use of JDBC Authentication with encrypted passwords.

For docs on Spring Security and Thymeleaf integration, see this link
- https://www.thymeleaf.org/doc/articles/springsecurity.html


SQL Scripts
===========
These are the same SQL scripts used in the course.
- employee.sql: Creates the employee table and loads sample data

- setup-spring-security-bcrypt-demo-database.sql: Creates login accounts with encrypted passwords

+---------+----------+-----------------------------+
| user id | password |            roles            |
+---------+----------+-----------------------------+
| john    | fun123   | ROLE_EMPLOYEE               |
| mary    | fun123   | ROLE_EMPLOYEE, ROLE_MANAGER |
| susan   | fun123   | ROLE_EMPLOYEE, ROLE_ADMIN   |
+---------+----------+-----------------------------+



MAVEN PROJECT UPDATES
=====================
We need to add entries for Spring Security and Thymeleaf Security

1. Add Spring Security starter

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>


2. Add the Thymeleaf pom entries for Spring Security

		<dependency>
			<groupId>org.thymeleaf.extras</groupId>
			<artifactId>thymeleaf-extras-springsecurity5</artifactId>
		</dependency>


CREATE BEANS FOR DATABASE ACCESS
================================

This Spring Boot project will make use of two different datasources
1. Main datasource for the app. This is for accessing the "employee" database
2. Another datasource for the application security. This is for accessing the security info database.

3. The database configs are in the file: application.properties

For the main application, these are the database connection properties

#
# JDBC properties
#
app.datasource.jdbc-url=jdbc:mysql://localhost:3306/employee_directory?useSSL=false&serverTimezone=UTC
app.datasource.username=springstudent
app.datasource.password=springstudent


For the JPA configuration, the file has the following

# Spring Data JPA properties
spring.data.jpa.repository.packages=com.luv2code.springboot.thymeleafdemo.dao
spring.data.jpa.entity.packages-to-scan=com.luv2code.springboot.thymeleafdemo.entity

For the Security database, the file has the following

#
# SECURITY JDBC properties
#
security.datasource.jdbc-url=jdbc:mysql://localhost:3306/spring_security_demo_bcrypt?useSSL=false&serverTimezone=UTC
security.datasource.username=springstudent
security.datasource.password=springstudent


4. The datasources are configured in the file: DemoDataSourceConfig.java

@Configuration
@EnableJpaRepositories(basePackages={"${spring.data.jpa.repository.packages}"})
public class DemoDataSourceConfig {
	
	@Primary
	@Bean
	@ConfigurationProperties(prefix="app.datasource")
	public DataSource appDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	@ConfigurationProperties(prefix="spring.data.jpa.entity")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource appDataSource) {
		return builder
				.dataSource(appDataSource)
				.build();
	}

	@Bean
	@ConfigurationProperties(prefix="security.datasource")
	public DataSource securityDataSource() {
		return DataSourceBuilder.create().build();
	}
}

Let's explain what's going on in this file.

a. Configure Spring Data JPA
@EnableJpaRepositories(basePackages={"${spring.data.jpa.repository.packages}"})

This tells the app that we are using JPA repositories defined in the given package. The package name is read from the application.properties file.

spring.data.jpa.repository.packages=com.luv2code.springboot.thymeleafdemo.dao

In this case, the package name is: com.luv2code.springboot.thymeleafdemo.dao, so Spring Data JPA will scan for JPA repositories in this package. Spring Data JPA makes use of a entity manager factory bean and transacation manager. 
By default it will use a bean named, "entityManagerFactory". We manually configure this bean in this class. Also, by default, Spring Data JPA will use a bean named "transactionManager". The "transactionManager" bean is autoconfigured by Spring Boot.

b. Configure application DataSource

	@Primary
	@Bean
	@ConfigurationProperties(prefix="app.datasource")
	public DataSource appDataSource() {
		return DataSourceBuilder.create().build();
	}

This code creates a datasource. This datasource is for our main application data. It will read data "employee" data from the database. This datasource is configured with the following:

	@ConfigurationProperties(prefix="app.datasource")

The @ConfigurationProperties will read properties from the config file (application.properties). It will read the properties from the file with the prefix: "app.datasource". So it will read the following:

app.datasource.jdbc-url=jdbc:mysql://localhost:3306/employee_directory?useSSL=false&serverTimezone=UTC
app.datasource.username=springstudent
app.datasource.password=springstudent

c. Configure EntityManagerFactory

	@Bean
	@ConfigurationProperties(prefix="spring.data.jpa.entity")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource appDataSource) {
		return builder
				.dataSource(appDataSource)
				.build();
	}

The entity manager factory tells Spring Data JPA which packages to scan for JPA entities. The @ConfigurationProperties will read properties from the config file (application.properties). It will read the properties from the file with the prefix: "spring.data.jpa.entity". So it will read the following:

spring.data.jpa.entity.packages-to-scan=com.luv2code.springboot.thymeleafdemo.entity

d. Configure Data Source for Security

	@Bean
	@ConfigurationProperties(prefix="security.datasource")
	public DataSource securityDataSource() {
		return DataSourceBuilder.create().build();
	}

Here we configure the datasource to access the security database. By default, Spring Security makes use of regular JDBC (no JPA). As a result, we only need a datasource so the configuration is a bit simpler.

The @ConfigurationProperties will read properties from the config file (application.properties). It will read the properties from the file with the prefix: "security.datasource". So it will read the following:

security.datasource.jdbc-url=jdbc:mysql://localhost:3306/spring_security_demo_bcrypt?useSSL=false&serverTimezone=UTC
security.datasource.username=springstudent
security.datasource.password=springstudent


CONFIGURE SPRING SECURITY FOR DATABASE AUTHENTICATION
=====================================================

Now that we have the Spring Security datasource set up, we need to use this datasource for authentication.

1. Update Spring Security Configuration

Update the file: DemoSecurityConfig.java to use this

@Configuration
@EnableWebSecurity
public class DemoSecurityConfig extends WebSecurityConfigurerAdapter {

	// add a reference to our security data source
	
	@Autowired
	@Qualifier("securityDataSource")
	private DataSource securityDataSource;
		
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		// use jdbc authentication ... oh yeah!!!		
		auth.jdbcAuthentication().dataSource(securityDataSource);
		
	}

	...
}

This injects the "securityDataSource" bean that was defined in the previous file: DemoDataSourceConfig.java. Then in the configure() method, we tell Spring Security to use this data source for JDBC authentication.



DISPLAY CONTENT BASED ON USER ROLE
==================================

In the application, we want to display content based on user role.

- Employee role: users in this role will only be allowed to list employees.
- Manager role: users in this role will be allowed to list, add and update employees.
- Admin role: users in this role will be allowed to list, add, update and delete employees. 

These restrictions are currently in place with the code: DemoSecurityConfig.java

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests()
			.antMatchers("/employees/showForm*").hasAnyRole("MANAGER", "ADMIN")
			.antMatchers("/employees/save*").hasAnyRole("MANAGER", "ADMIN")
			.antMatchers("/employees/delete").hasRole("ADMIN")
			.antMatchers("/employees/**").hasRole("EMPLOYEE")
			.antMatchers("/resources/**").permitAll()
			.and()
			.formLogin()
				.loginPage("/showMyLoginPage")
				.loginProcessingUrl("/authenticateTheUser")
				.permitAll()
			.and()
			.logout().permitAll()
			.and()
			.exceptionHandling().accessDeniedPage("/access-denied");
		
	}

We also, want to hide/display the links on the view page. For example, if the user has only the "EMPLOYEE" role, then we should only display links available for "EMPLOYEE" role.
Links for "MANAGER" and "ADMIN" role should not be displayed for the "EMPLOYEE".

We can make use of Thymeleaf Security to handle this for us. 


1. Add support for Thymeleaf Security
To use the Thymeleaf Security, we need to add the following to the XML Namespace

File: list-employees.html

<html lang="en" 
		xmlns:th="http://www.thymeleaf.org"
		xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5">

Note the reference for xmlns:sec


2. "Update" button
Only display the "Update" button for users with role of MANAGER OR ADMIN

					<div sec:authorize="hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')">

						<!-- Add "update" button/link -->
						<a th:href="@{/employees/showFormForUpdate(employeeId=${tempEmployee.id})}"
						   class="btn btn-info btn-sm">
							Update
						</a>

					</div>					

3. "Delete" buton

Only display the "Delete" button for users with role of ADMIN

					<div sec:authorize="hasRole('ROLE_ADMIN')">  
					
						<!-- Add "delete" button/link -->					
						<a th:href="@{/employees/delete(employeeId=${tempEmployee.id})}"
						   class="btn btn-danger btn-sm"
						   onclick="if (!(confirm('Are you sure you want to delete this employee?'))) return false">
							Delete
						</a>

					</div>

					

TEST THE APPLICATION
====================
0. Before running the application, make sure the database tables are set up (via SQL files).  Also, be sure to update application.properties for database connection (url, userid, pass)
 
1. Run the Spring Boot application: ThymeleafdemoApplication.java

2. Open a web browser for the app: http://localhost:8080

3. Log in using one of the accounts

+---------+----------+-----------------------------+
| user id | password |            roles            |
+---------+----------+-----------------------------+
| john    | fun123   | ROLE_EMPLOYEE               |
| mary    | fun123   | ROLE_EMPLOYEE, ROLE_MANAGER |
| susan   | fun123   | ROLE_EMPLOYEE, ROLE_ADMIN   |
+---------+----------+-----------------------------+

4. Confirm that you can login and access data based on the roles.

Congratulations! You have an app that uses Spring Boot, Spring Security (JDBC), Thymeleaf, Spring Data JPA
 					