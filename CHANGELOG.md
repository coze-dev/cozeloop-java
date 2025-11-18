# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2025-11-11

### Added

- Initial CozeLoop Java SDK release
- Core SDK module (`cozeloop-core`) with trace reporting and prompt management
- Spring Boot starter module (`cozeloop-spring-boot-starter`) for seamless integration
- Trace reporting functionality built on OpenTelemetry SDK
- Prompt management with caching and formatting support
- Support for Jinja template engine in prompts
- AOP annotation `@CozeTrace` for declarative tracing
- OAuth JWT and PAT (Personal Access Token) authentication
- Example code demonstrating SDK usage (`examples/` directory)
  - Initialization examples (PAT, OAuth JWT, error handling)
  - Trace examples (simple, parent-child, trace with prompt)
  - Prompt examples (prompt hub, Jinja templates)
- Comprehensive documentation and README files
  - English and Chinese (简体中文) README files
  - CONTRIBUTING.md guide
  - LICENSE file (MIT License)
  - Examples README with detailed usage instructions
- Maven multi-module project structure
- Checkstyle configuration for code quality

### Changed

- **Trace Export**: Improved `CozeLoopSpanExporter` to export spans in batches of 25 to remote server
  - Spans are now automatically split into batches of 25 before sending to CozeLoop platform
  - Enhanced error handling: individual batch failures don't prevent other batches from being exported
  - Improved logging with batch-level statistics and detailed error reporting
  - Better resilience and observability for trace export operations

### Features

- **Trace Reporting**: Automatic batch reporting of traces to CozeLoop platform
  - Multi-level batching: OpenTelemetry BatchSpanProcessor + custom 25-span batches
  - Robust error handling with per-batch failure isolation
  - Comprehensive logging for monitoring and debugging
- **Prompt Management**: Pull, cache, and format prompts from the platform
- **Spring Boot Integration**: Auto-configuration and AOP support
- **Thread-Safe**: All core components are thread-safe
- **Java 8+ Compatible**: Wide compatibility with Java 8 and above

