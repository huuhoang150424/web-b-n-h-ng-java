Project Overview
This is an e-commerce web application designed to provide a seamless online shopping experience for users. The project supports key features such as product management, category management, order processing, product reviews, and order history tracking. The goal is to build a robust, scalable, and user-friendly platform.

Key Features
Product Management: View product list, product details, add/delete/edit products (for admin).
Category Management: Display categories with product counts, retrieve the top 5 categories with the most products.
Order Management: Create orders, track statuses (not confirmed, processing, shipped, received, cancelled), view order change history.
Product Reviews: Users can leave comments and ratings; reviews are displayed even if the product is deleted.
Role-Based Access: Regular users can view and purchase; admins have full system management rights.
Pagination: Support pagination for product review lists.
Technologies Used
Backend
Java: Core programming language (Java 23 or compatible version).
Spring Boot: Main framework for RESTful APIs, dependency management, and configuration.
Spring Data JPA: Handles database queries and persistence.
MySQL: Relational database for storing products, orders, users, etc.
Lombok: Reduces boilerplate code (getters, setters, constructors).
Frontend (Assumed - Customize as needed)
React: User interface framework.
Tool: 
Gralde: Dependency management 

Clone the Repository:
git clone https://github.com/huuhoang150424/web-b-n-h-ng-java
cd web-b-n-h-ng-java

application.yml
spring.application.name=
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update  

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.cache.type=redis

jwt.secret=
jwt.access-token-expiration=3600000  
jwt.refresh-token-expiration=31536000000


spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss
spring.jackson.serialization.write-dates-as-timestamps=false

