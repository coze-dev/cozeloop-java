# How to contribute

## Set Up Environment
Make sure you have Java installed (version 8 or higher, Java 11+ recommended) and Maven 3.6+.

## Dependencies Management
We use Maven for dependency management.
Install dependencies:
```bash
mvn clean install
```

## Build
To build the project, run:
```bash
mvn clean install
```

## Code Style
We use Checkstyle for code style checking. The configuration file is `checkstyle.xml` in the root directory.

To check code style:
```bash
mvn checkstyle:check
```

## Running Tests
To run all tests:
```bash
mvn test
```

## Project Structure
- `cozeloop-core/` - Core SDK module
- `cozeloop-spring-boot-starter/` - Spring Boot integration module
- `examples/` - Example code demonstrating SDK usage

## Submitting Changes
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure all tests pass
5. Ensure code style checks pass
6. Submit a pull request

