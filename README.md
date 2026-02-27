<div align="center">

# OG4Dev Spring API Response

<p align="center">
<img src="https://skillicons.dev/icons?i=java,spring,maven,git,github,idea" alt="Tech Stack" />
</p>

<p align="center">
<em>A lightweight, zero-configuration REST API Response wrapper for Spring Boot applications</em>
</p>

<p align="center">
<a href="https://central.sonatype.com/artifact/io.github.og4dev/og4dev-spring-response">
<img src="https://img.shields.io/maven-central/v/io.github.og4dev/og4dev-spring-response.svg" alt="Maven Central">
</a>
<a href="https://opensource.org/licenses/Apache-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License">
</a>
<a href="https://www.oracle.com/java/">
<img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java">
</a>
<a href="https://spring.io/projects/spring-boot">
<img src="https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg" alt="Spring Boot">
</a>
<a href="https://github.com/OG4Dev/og4dev-spring-response">
<img src="https://img.shields.io/badge/Version-1.4.0-brightgreen.svg" alt="Version">
</a>
</p>

<p align="center">
A lightweight, type-safe API Response wrapper for Spring Boot applications. Standardize your REST API responses with consistent structure, automatic timestamps, distributed tracing support, and clean factory methods. Features zero-configuration Spring Boot auto-configuration, opt-in automatic response wrapping (@AutoResponse), and production-ready exception handling with comprehensive RFC 9457 ProblemDetail support covering 10 common error scenarios. No external dependencies required - uses pure Java with a custom builder pattern.
</p>

</div>

## 🔗 Quick Links

* 📦 [Maven Central Repository](https://central.sonatype.com/artifact/io.github.og4dev/og4dev-spring-response)
* 📚 [JavaDoc Documentation](https://javadoc.io/doc/io.github.og4dev/og4dev-spring-response)
* 🐛 [Report Issues](https://github.com/OG4Dev/og4dev-spring-response/issues)
* 💡 [Feature Requests](https://github.com/OG4Dev/og4dev-spring-response/issues/new)
* 🤝 [Contributing Guide](#-contributing)

## 📑 Table of Contents

* [Quick Links](#-quick-links)
* [Key Highlights](#-key-highlights)
* [Features](#-features)
* [Requirements](#-requirements)
* [What Makes This Different?](#-what-makes-this-different)
* [Installation](#-installation)
* [Project Structure](#-project-structure)
* [Quick Start](#-quick-start)
* [Auto-Configuration](#%EF%B8%8F-auto-configuration)
* [Opt-in Automatic Wrapping (@AutoResponse)](#-opt-in-automatic-wrapping-autoresponse)
* [Built-in Security Features](#-built-in-security-features)
* [Built-in Exception Handling](#%EF%B8%8F-built-in-exception-handling)
* [Usage](#-usage)
* [Real-World Examples](#-real-world-examples)
* [API Reference](#-api-reference)
* [Response Structure](#-response-structure)
* [Best Practices](#-best-practices)
* [Testing](#-testing)
* [Architecture & Design](#%EF%B8%8F-architecture--design-principles)
* [OpenAPI/Swagger Integration](#-openapiswagger-integration)
* [Compatibility Matrix](#-compatibility-matrix)
* [Troubleshooting](#-troubleshooting)
* [FAQ](#-faq)
* [Performance & Best Practices](#-performance--best-practices)
* [Migration Guide](#-migration-guide)
* [Security Considerations](#-security-considerations)
* [Contributing](#-contributing)
* [License](#-license)
* [Contact](#-contact)
* [Acknowledgments](#-acknowledgments)
* [Version History](#-version-history)

## 🎯 Key Highlights

* 🚀 **Truly Zero Configuration** - Spring Boot 3.x/4.x auto-configuration with META-INF imports
* 🎁 **Zero Boilerplate** - Opt-in `@AutoResponse` to automatically wrap raw return types
* 🎯 **Production-Ready** - Built-in RFC 9457 ProblemDetail with 10 comprehensive exception handlers
* 🛡️ **Complete Error Coverage** - Handles validation, JSON parsing, 404s, method mismatches, media types, and more
* 🔍 **Trace IDs in Errors** - Error responses include traceId for debugging
* 🔒 **Type-Safe & Immutable** - Thread-safe design with generic type support
* 📦 **Ultra-Lightweight** - Only ~10KB JAR size with provided dependencies
* 🔍 **Microservices-Ready** - Built-in trace IDs for distributed tracing
* ✅ **Battle-Tested** - Used in production Spring Boot applications
* 📋 **Professional-Grade Javadoc** - 100% coverage with comprehensive method documentation
* 🔐 **Opt-in Security Features** - Fine-grained control with field-level annotations
* 🚫 **Zero External Dependencies** - Pure Java, no Lombok required

## ✨ Features

* 🎯 **Consistent Structure** - All responses follow the same format: `status`, `message`, `content`, `timestamp`
* 🎁 **@AutoResponse Wrapping** - Return plain DTOs; let the library wrap them automatically (Opt-in)
* 🔒 **Type-Safe** - Full generic type support with compile-time type checking
* 🔍 **Distributed Tracing** - Trace IDs in error responses with MDC integration for request tracking
* ⏰ **Auto Timestamps** - Automatic RFC 3339 UTC formatted timestamps on every response
* 🏭 **Factory Methods** - Clean static methods: `success()`, `created()`, `status()`
* 🚀 **Zero Config** - Spring Boot Auto-Configuration for instant setup
* 🪶 **Lightweight** - Only ~10KB JAR with single provided dependency (Spring Web)
* 📦 **Immutable** - Thread-safe with final fields
* 🔌 **Spring Native** - Built on `ResponseEntity` and `HttpStatus`
* 📋 **RFC 9457 Compliance** - Standard ProblemDetail format (supersedes RFC 7807)
* 📚 **Complete JavaDoc** - Every class and method fully documented with comprehensive examples
* 🔐 **Opt-in Security Features** - Fine-grained JSON request protection via field annotations
* ✅ **Strict JSON Validation** - Rejects unknown properties to prevent mass assignment attacks (automatic)
* ✅ **XSS Prevention** - HTML tag detection and rejection via `@XssCheck` annotation (opt-in)
* ✅ **Smart String Trimming** - Whitespace trimming via `@AutoTrim` annotation (opt-in)
* ✅ **Case-Insensitive Enums** - Flexible enum handling for better API usability (automatic)
* 🛡️ **Comprehensive Exception Handling** - 10 built-in handlers covering all common scenarios

## 📦 Requirements

* Java 17 or higher
* Spring Boot 3.2.0 or higher (tested up to 4.0.3)
* No additional dependencies required (pure Java implementation)

## 🌟 What Makes This Different?

Unlike other response wrapper libraries, this one offers:

* ✅ **Native Spring Boot 3.x/4.x Auto-Configuration** - No manual setup required
* ✅ **Zero-Boilerplate @AutoResponse** - Return raw objects, let the library wrap them automatically while preserving
  your HTTP Status codes.
* ✅ **RFC 9457 ProblemDetail Support** - Industry-standard error responses (latest RFC)
* ✅ **Opt-in Security Features** - Fine-grained control via field-level annotations (`@XssCheck`, `@AutoTrim`)
* ✅ **Zero External Dependencies** - Pure Java implementation, won't conflict with your application
* ✅ **Extensible Exception Handling** - Create custom business exceptions easily
* ✅ **Trace ID Support** - Built-in distributed tracing capabilities
* ✅ **Professional-Grade Documentation** - 100% Javadoc coverage

## 🚀 Installation

### Maven (Latest - v1.4.0)

```xml

<dependency>
    <groupId>io.github.og4dev</groupId>
    <artifactId>og4dev-spring-response</artifactId>
    <version>1.4.0</version>
</dependency>

```

### Gradle (Latest - v1.4.0)

```gradle
implementation 'io.github.og4dev:og4dev-spring-response:1.4.0'

```

### Gradle Kotlin DSL (Latest - v1.4.0)

```kotlin
implementation("io.github.og4dev:og4dev-spring-response:1.4.0")

```

---

## 📁 Project Structure

The library is organized into six main packages:

```
io.github.og4dev
├── advice/
│   └── GlobalResponseWrapper.java           # Automatic response wrapper interceptor
├── annotation/
│   ├── AutoResponse.java                    # Opt-in annotation for automatic wrapping
│   ├── AutoTrim.java                        # Opt-in annotation for string trimming
│   └── XssCheck.java                        # Opt-in annotation for XSS validation
├── config/
│   └── ApiResponseAutoConfiguration.java    # Spring Boot auto-configuration
├── dto/
│   └── ApiResponse.java                     # Generic response wrapper
├── exception/
│   ├── ApiException.java                    # Abstract base for custom exceptions
│   └── GlobalExceptionHandler.java          # RFC 9457 exception handler
└── filter/
    └── TraceIdFilter.java                   # Request trace ID generation

```

## 🎯 Quick Start

You can use the library in two ways: **Explicit Factory Methods** or **Automatic Wrapping**.

### Method 1: Explicit Factory Methods

```java

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ApiResponse.success("User retrieved successfully", user);
    }
}

```

### Method 2: Automatic Wrapping (New in v1.4.0) 🎁

Tired of typing `ResponseEntity<ApiResponse<T>>`? Use `@AutoResponse`!

```java

@RestController
@RequestMapping("/api/users")
@AutoResponse // Applies to all methods in this controller
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // Just return the raw object!
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Preserves custom status codes!
    public User createUser(@RequestBody UserDto dto) {
        return userService.create(dto);
    }
}

```

**Both methods produce the exact same JSON:**

```json
{
  "status": 200,
  // or 201 for POST
  "message": "Success",
  "content": {
    "id": 1,
    "name": "John Doe"
  },
  "timestamp": "2026-02-28T10:30:45.123Z"
}

```

## ⚙️ Auto-Configuration

The library features **Spring Boot Auto-Configuration** for truly zero-config setup!

✅ **GlobalExceptionHandler** - Automatic exception handling

✅ **GlobalResponseWrapper** - Automatic payload wrapping via `@AutoResponse`

✅ **Security Customizers** - Jackson configuration for `@AutoTrim` and `@XssCheck`

**No configuration needed!** Just add the dependency.

## 🎁 Opt-in Automatic Wrapping (@AutoResponse)

Introduced in **v1.4.0**, you can eliminate boilerplate code by letting the library wrap your controller responses
automatically.

### How to use it:

Add the `@AutoResponse` annotation to your controller class (applies to all methods) or to specific methods.

```java

@RestController
@AutoResponse
public class ProductController {
    // ...
}

```

### Key Capabilities:

* ✅ **Status Code Preservation:** Intelligently reads custom HTTP status codes set via `@ResponseStatus` (e.g., 201
  Created) and reflects them in the `ApiResponse`.
* ✅ **Double-Wrap Prevention:** Safely skips wrapping if you explicitly return an `ApiResponse` or `ResponseEntity`.
* ✅ **Error Compatibility:** Bypasses `ProblemDetail` responses, ensuring standard error handling is never broken.
* ✅ **String Safety:** Skips raw `String` returns to prevent `ClassCastException` with Spring's internal string message
  converters.

## 🔐 Built-in Security Features

The library provides fine-grained security and data processing features through field-level annotations. By default, **fields are NOT modified** unless explicitly annotated.

### 1. Strict Property Validation 🛡️ (Automatic)

Rejects JSON payloads containing unexpected fields to prevent mass assignment attacks.

### 2. Opt-in XSS Prevention with @XssCheck 🔒

Fail-fast HTML tag detection and rejection using regex pattern `(?s).*<\s*[a-zA-Z/!].*`.

```java

@XssCheck
private String comment; // Rejects "<script>alert(1)</script>"

```

### 3. Opt-in String Trimming with @AutoTrim ✂️

Automatic whitespace removal for specific fields.

```java

@AutoTrim
private String username; // "  john_doe  " -> "john_doe"

```

*(See full Security details in the Javadocs and examples above).*

## 🛡️ Built-in Exception Handling

The library includes a **production-ready `GlobalExceptionHandler**` that automatically handles 10 common exceptions
using Spring Boot's **ProblemDetail (RFC 9457)** standard.

* **Automatic Logging:** SLF4J integration for all errors.
* **Trace ID Consistency:** Logs and responses always have matching trace IDs.
* **Custom Business Exceptions:** Extend the abstract `ApiException` class to create domain-specific exceptions.

```java
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with ID: %d", resource, id), HttpStatus.NOT_FOUND);
    }
}

```

## 🌍 Real-World Examples

### Example 1: Clean CRUD Controller (Using @AutoResponse)

```java

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@AutoResponse // ✨ Zero boilerplate for the whole controller!
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<Product> getAllProducts(Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // ✨ 201 Created preserved automatically
    public Product createProduct(@Valid @RequestBody ProductDto dto) {
        return productService.create(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        // Returns empty content with 200 OK automatically
    }
}

```

## 📚 API Reference

*(Refer to Javadoc for full details)*

## 📈 Version History

### 1.4.0 (February 2026) - Current Release

✨ **New Features:**

* **@AutoResponse Annotation & GlobalResponseWrapper**
* Opt-in automatic response wrapping to eliminate boilerplate code.
* Returns raw DTOs from controllers and automatically wraps them in `ApiResponse<T>`.
* Preserves HTTP status codes from `@ResponseStatus`.
* Intelligently skips `ResponseEntity`, `ApiResponse`, `ProblemDetail`, and `String` to prevent double-wrapping and
  casting errors.


* **package-info.java documentation** added for the new `advice` package.

### 1.3.0 (February 2026)

* **Security Philosophy Change:** Complete redesign from automatic to opt-in approach for JSON sanitization.
* Added **@AutoTrim** annotation for explicit string trimming.
* Added **@XssCheck** annotation for explicit fail-fast XSS validation.
* Extensive Javadoc and README updates regarding the new security model.

### 1.2.0 (February 2026)

* Added `@NoTrim` annotation (Legacy - removed in 1.3.0 in favor of opt-in model).
* Enhanced Advanced String Deserializer.

### 1.1.0 & 1.1.1 (February 2026)

* Added `GlobalExceptionHandler` and RFC 9457 Support.
* Added `TraceIdFilter` and MDC Integration.
* Verified Spring Boot 4.0.3 Compatibility.

### 1.0.0 (February 2026)

* Initial Release. Core `ApiResponse` wrapper.

---

## 🤝 Contributing

We welcome contributions! Please see the Contributing section above for details on our Apache 2.0 license terms and PR
process.

## 📄 License

Licensed under the Apache License 2.0.

## 📧 Contact

**Pasindu OG** | [pasinduogdev@gmail.com](mailto:pasinduogdev@gmail.com) |
GitHub: [@pasinduog](https://github.com/pasinduog)

---

**⭐ If you find this library helpful, please give it a star on GitHub!**