# Agent Description for Flink UDFs Catalog Repository

## Repository Overview
This repository serves as a centralized catalog of User-Defined Functions (UDFs) for Apache Flink. Each UDF is designed to extend Flink's capabilities with custom functionality, following best practices for development, testing, and deployment.

## Repository Structure & Agent Responsibilities

### `/[udf_name]` - Individual UDF Projects
- **Purpose**: Each directory contains a complete UDF implementation
- **Agent Role**:
  - Maintain Maven project structure
  - Ensure proper Java package naming
  - Keep dependencies current and compatible
  - Manage comprehensive unit tests
  - Document UDF usage and examples

#### Key Components in Each UDF:
- **Source Code**: Java implementation of the UDF
- **Tests**: Unit tests with comprehensive coverage
- **Documentation**: Usage examples and technical details
- **Build Configuration**: Maven POM files

## Agent Capabilities & Responsibilities

### Core Competencies Required:
1. **Apache Flink Expertise**
   - Flink SQL and Table API
   - UDF development patterns
   - State management (if applicable)
   - Performance optimization

2. **Java Development**
   - Java best practices
   - Maven build system
   - Unit testing (JUnit)
   - Java packaging

3. **Documentation Management**
   - Technical documentation
   - SQL usage examples
   - Java API examples
   - Deployment guides

4. **Development Support**
   - Code review suggestions
   - Performance optimization
   - Testing coverage
   - Dependency management

### Primary Agent Tasks:

#### UDF Development
- Guide UDF implementation following best practices
- Ensure proper error handling and input validation
- Optimize performance where possible
- Maintain consistent code style

#### Testing
- Ensure comprehensive unit test coverage
- Validate edge cases and error conditions
- Verify performance characteristics
- Test integration with Flink runtime

#### Documentation
- Maintain clear usage examples
- Document deployment procedures
- Keep README files current
- Provide SQL and Java API examples

#### Build & Deployment
- Manage Maven configurations
- Ensure proper artifact generation
- Document deployment steps for different platforms
- Handle version management

### Agent Interaction Guidelines:
- Focus on UDF best practices and patterns
- Maintain clear documentation for each UDF
- Ensure proper testing across Flink versions
- Keep deployment guides platform-agnostic
- Document performance considerations

## Success Metrics:
- All UDFs have comprehensive tests
- Documentation is clear and complete
- Build process is reliable
- Deployment guides are accurate
- Performance characteristics are documented
- Examples are provided for all UDFs

## Platform-Specific Considerations

### Apache Flink
- Guide artifact creation and deployment
- Provide platform-specific SQL examples


### Confluent Cloud
- Guide artifact creation and deployment
- Document required roles and permissions
- Provide platform-specific SQL examples
- Handle cloud-specific configurations

### Confluent Platform
- Document on-premises deployment
- Handle local development setup
- Provide platform-specific configurations
- Manage version compatibility

## Quality Standards
1. **Code Quality**
   - Consistent code style
   - Proper error handling
   - Input validation
   - Performance optimization

2. **Documentation Quality**
   - Clear usage examples
   - Complete API documentation
   - Platform-specific guides
   - Up-to-date README files

3. **Test Quality**
   - High test coverage
   - Edge case testing
   - Performance validation
   - Integration testing

4. **Build Quality**
   - Clean Maven builds
   - Proper versioning
   - Dependency management
   - Artifact validation
