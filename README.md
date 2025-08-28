```txt
            .__                       ___.                                
            |  |__   ___________  ____\_ |__   ____ _____    ____   ______
            |  |  \_/ __ \_  __ \/  _ \| __ \_/ __ \\__  \  /    \ /  ___/
            |   Y  \  ___/|  | \(  <_> ) \_\ \  ___/ / __ \|   |  \\___ \ 
            |___|  /\___  >__|   \____/|___  /\___  >____  /___|  /____  >
                 \/     \/                 \/     \/     \/     \/     \/ 

            :: 2025 Hero Tech Course
               
               e-commerce service
```

A modern coffee shop backend built with Kotlin and Spring Boot. <br>
Team project [Ann](https://github.com/AnnLvu), [Mina](https://github.com/miooo0o), [Petra](https://github.com/pebencze), and [Vito](https://github.com/VittorioDeMarzi) <br>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
<br/>
<img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=json-web-tokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/Stripe-008CDD?style=flat-square&logo=stripe&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaMail-EA4335?style=flat-square&logo=gmail&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat-square&logo=github-actions&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS-FF9900?style=flat-square&logo=amazon-aws&logoColor=white"/>
  <img src="https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white"/>
<br/>
<img src="https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white"/>
  <img src="https://img.shields.io/badge/Mockito-red?style=flat-square"/>
  <img src="https://img.shields.io/badge/RestAssured-2496ED?style=flat-square"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white"/>
</p>

## Contents

- Introduction
- Product
- 3 Weeks Sprint Deliverables

## Introduction

Our Stripe-based e-commerce service was designed with real-world deployment challenges in mind, emphasizing **high availability, scalability, and smooth user experience**.
To achieve **zero downtime during releases**, we implemented a **Blue-Green deployment** strategy, allowing seamless traffic switching.


## Product
**Our Product** : [link](https://www.herobeans.com/)

### Business Model
1. **Early-Stage Startup** : We're just starting our business journey as a micro-scale coffee platform
2. **Limited Traffic** : Currently operating with minimal traffic as we build our customer base
3. **Hyper-Local Focus** : Exclusively serving the Berlin area with neighborhood-level operations
4. **Curated Selection** : Offering ~20 carefully selected coffee varieties - quality over quantity
5. **Multiple Formats** : Available in 250g, 500g, and 1000g packages
   Growth Aspirations: Plans to gradually expand throughout Germany as we establish our brand

### Pricing & Shipping
1. **Transparent Pricing** : All prices include VAT, excluding [shipping costs](https://github.com/VittorioDeMarzi/hero-beans/wiki/Shipping-Delivery)
2. **Free Shipping** : Available for orders above a certain threshold
3. **Local Delivery** : Currently limited to Berlin area

---

## 3 Weeks Sprint Deliverables
### Sprint 1: Project Setup & Development Environment

1. **Development Documentation** : [Ground rule](https://github.com/VittorioDeMarzi/hero-beans/wiki/Ground-Rules-1.0), [Code conventions](), [Commit message format](), etc.
2. **Code Review Process** : Establish and apply a code review process (including [PR template](https://github.com/VittorioDeMarzi/hero-beans/blob/main/docs/PULL_REQUEST_TEMPLATE.md))
3. **CI Setup** : Set up CI with [GitHub Actions](https://github.com/VittorioDeMarzi/hero-beans/wiki/CI-Workflow-%E2%80%94-Detailed-Documentation) for automated build and test


### Sprint 2: Feature Development & Production Readiness
1. **API Documentation** : Document APIs with [Swagger](https://github.com/VittorioDeMarzi/hero-beans/wiki/Swagger-OpenAPI-for-Hero-Beans)   
2. **Logging Strategy** : Define and apply [Logging strategy](https://github.com/VittorioDeMarzi/hero-beans/wiki/Simple-Logging) (including log levels: info, warn, error)
3. **Production Deployment** : Deploy service to production server (with HTTPS and Custom domain)
4. **Production Database** : [mySQL](https://github.com/VittorioDeMarzi/hero-beans/wiki/PostrgeSQL-vs.-MySQL)), [Liquibase](https://github.com/VittorioDeMarzi/hero-beans/wiki/Liquibase) and [Database Migrations with JPA](https://github.com/VittorioDeMarzi/hero-beans/wiki/Database-Migrations-with-JPA-Buddy-&-Liquibase)
5. **Swagger** : [Swagger](https://www.herobeans.com/swagger-ui/index.html#/)



### Sprint 3: Performance Optimization & Scalability Planning
1. **Zero-Downtime Deployment** : Implement deployment strategy using [Blue-Green deployment]() by [shell](https://github.com/VittorioDeMarzi/hero-beans/blob/main/scripts/deploy_blue_green.sh) and [Nginx]() to avoid service interruption
2. **Concurrency Handling** : Prevent data conflicts under concurrent access using [optimistic/pessimistic locking](https://github.com/users/VittorioDeMarzi/projects/1?pane=issue&itemId=124742166&issue=VittorioDeMarzi%7Chero-beans%7C81)
3. **Monitoring** : Dashboard monitors with [Cloud watch](https://github.com/VittorioDeMarzi/hero-beans/wiki/Monitoring-Dashboard-Documentation) & [Actuator Health Check](https://github.com/VittorioDeMarzi/hero-beans/wiki/Actuator-Health-Check-Endpoint)
---

