# Contributing to OG4Dev Spring API Response

Thank you for your interest in contributing to the OG4Dev Spring API Response library! 🎉

## 📜 License Agreement

This project is licensed under the **Apache License 2.0**. By contributing, you agree that:

1. **Your contributions will be licensed** under the Apache License 2.0
2. **You grant** a perpetual, worldwide license to use, modify, and distribute your contributions
3. **You retain copyright** to your contributions
4. **You confirm** that you have the right to submit the contribution
5. **Your work is protected** - No one can claim exclusive ownership of your contributions or publish them elsewhere as their own

### What This Means

✅ **You CAN:**
- Keep copyright to your contributions
- Have your name attributed in the project
- Use your contributions elsewhere
- Contribute freely without restrictions

🛡️ **Protection:**
- No one (including the project maintainer) can claim exclusive ownership of your work
- Your contributions cannot be copyrighted by others
- The project must remain open source under Apache 2.0
- Derivative works must maintain the Apache 2.0 License

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher (Java 21 LTS recommended)
- Maven 3.6+
- Git
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Development Setup

1. **Fork the repository**
   - Visit https://github.com/OG4Dev/og4dev-spring-response
   - Click "Fork" button in top-right corner

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/og4dev-spring-response.git
   cd og4dev-spring-response
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/OG4Dev/og4dev-spring-response.git
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Generate JavaDoc**
   ```bash
   mvn javadoc:javadoc
   ```

## 📝 Contribution Workflow

### 1. Create a Feature Branch

```bash
# Update your fork
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes

- Follow existing code style and conventions
- Add comprehensive JavaDoc comments with @param, @return, @throws tags
- Include explicit constructor documentation
- Keep changes focused and atomic
- Test your changes thoroughly

### 3. Commit Your Changes

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```bash
# Format: <type>(<scope>): <description>

# Examples:
git commit -m "feat(dto): add pagination support to ApiResponse"
git commit -m "fix(exception): resolve NPE in GlobalExceptionHandler"
git commit -m "docs(readme): update installation guide for v1.0.0"
git commit -m "test(response): add unit tests for factory methods"
git commit -m "refactor(filter): improve TraceIdFilter performance"
```

**Commit Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, whitespace)
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Maintenance tasks (dependencies, build)
- `perf` - Performance improvements

## 📋 Code Quality Standards

### Java Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Keep methods short and focused (single responsibility)
- Avoid deep nesting (max 3 levels)
- Use Java 17+ features appropriately
- No Lombok - use pure Java implementations

### JavaDoc Requirements

All public classes, methods, and fields must have comprehensive JavaDoc comments:

- Complete @param, @return, @throws tags
- Explicit constructor documentation
- Usage examples where applicable
- Thread safety notes for shared classes
- @author, @version, @since tags
- Zero warnings when generating JavaDoc

### Build Validation

Before submitting your PR, verify:

```bash
# Clean build
mvn clean install

# JavaDoc generation (should have zero warnings)
mvn javadoc:javadoc

# Check for compilation errors
mvn compile

# Run tests (when available)
mvn test
```

## 🎯 What We're Looking For

### Priority Contributions

1. **Bug Fixes** 🐛 - Fix reported issues and improve error handling
2. **Documentation** 📝 - Improve README, add code examples, enhance JavaDoc
3. **Tests** 🧪 - Add unit and integration tests
4. **Features** ✨ - Pagination, WebFlux support, i18n, response compression

### Feature Ideas

- Spring WebFlux adapter
- Pagination metadata support
- OpenAPI/Swagger schema generation
- Internationalization (i18n) support
- Response caching utilities
- Metrics and monitoring integration

## 🤝 Code of Conduct

### Our Standards

✅ **Do:**
- Be respectful and inclusive
- Provide constructive feedback
- Welcome newcomers
- Help others learn
- Follow the Apache License 2.0 terms

❌ **Don't:**
- Use offensive language
- Harass others
- Make personal attacks
- Violate the license terms

## 📞 Getting Help

- 💬 [GitHub Discussions](https://github.com/OG4Dev/og4dev-spring-response/discussions)
- 🐛 [GitHub Issues](https://github.com/OG4Dev/og4dev-spring-response/issues)
- 📧 Email: pasinduogdev@gmail.com

## 💖 Sponsorship

This project is developed and maintained in my free time. If you find this library valuable and would like to support its continued development, please consider sponsoring!

### Why Sponsor?

Your sponsorship helps:
- 🔧 **Ongoing Maintenance** - Bug fixes, dependency updates, security patches
- ✨ **New Features** - Pagination, WebFlux support, i18n, and more
- 📚 **Better Documentation** - Comprehensive guides, tutorials, and examples
- 🧪 **Quality Assurance** - More tests, better coverage, and reliability
- ⚡ **Faster Response Times** - Quicker issue resolution and feature requests

### How to Sponsor

You can support this project through:

- 💝 **GitHub Sponsors** - [Sponsor @pasinduog](https://github.com/sponsors/pasinduog)
- ☕ **Buy Me a Coffee** - [buymeacoffee.com/pasinduog](https://buymeacoffee.com/pasinduog)
- 💳 **PayPal** - [paypal.me/pasinduog](https://paypal.me/pasinduog)

### Sponsor Benefits

As a sponsor, you'll receive:

- 🏆 **Recognition** - Your name/company logo in the README (for monthly sponsors)
- 🎯 **Priority Support** - Faster response to issues and feature requests
- 📢 **Early Access** - Get notified of new features before public release
- 💬 **Direct Communication** - Direct access for questions and discussions
- 🤝 **Influence** - Help shape the project's roadmap

### Current Sponsors

A huge thank you to our sponsors! 🙏

<!-- Sponsors list will be added here -->
*Become the first sponsor and see your name here!*

**Every contribution matters** - whether it's code, documentation, bug reports, or financial support. Thank you for making this project better! ❤️

## 🙏 Thank You!

Your contributions make this project better for everyone!

---

**License:** By contributing, you agree to license your contributions under the Apache License 2.0.

