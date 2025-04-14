generated adn downloaded from https://10xrules.ai/
filled up with https://10x-prompt-formatter.vercel.app/

# AI Rules for {{project-name}}

{{project-description}}

## CODING_PRACTICES

### Guidelines for SUPPORT_LEVEL

#### SUPPORT_EXPERT

- Favor elegant, maintainable solutions over verbose code. Assume understanding of language idioms and design patterns.
- Highlight potential performance implications and optimization opportunities in suggested code.
- Frame solutions within broader architectural contexts and suggest design alternatives when appropriate.
- Focus comments on 'why' not 'what' - assume code readability through well-named functions and variables.
- Proactively address edge cases, race conditions, and security considerations without being prompted.
- When debugging, provide targeted diagnostic approaches rather than shotgun solutions.
- Suggest comprehensive testing strategies rather than just example tests, including considerations for mocking, test organization, and coverage.


### Guidelines for VERSION_CONTROL

#### GIT

- Use conventional commits to create meaningful commit messages
- Use feature branches with descriptive names following {{branch_naming_convention}}
- Write meaningful commit messages that explain why changes were made, not just what
- Keep commits focused on single logical changes to facilitate code review and bisection
- Use interactive rebase to clean up history before merging feature branches
- Leverage git hooks to enforce code quality checks before commits and pushes

#### GITHUB

- Use pull request templates to standardize information provided for code reviews
- Implement branch protection rules for {{protected_branches}} to enforce quality checks
- Configure required status checks to prevent merging code that fails tests or linting
- Use GitHub Actions for CI/CD workflows to automate testing and deployment
- Implement CODEOWNERS files to automatically assign reviewers based on code paths
- Use GitHub Projects for tracking work items and connecting them to code changes


## FRONTEND

### Guidelines for ANGULAR

#### NGRX

- Use the createFeature and createReducer functions to simplify reducer creation
- Implement the facade pattern to abstract NgRx implementation details from components
- Use entity adapter for collections to standardize CRUD operations
- Leverage selectors with memoization to efficiently derive state and prevent unnecessary calculations
- Implement @ngrx/effects for handling side effects like API calls
- Use action creators with typed payloads to ensure type safety throughout the application
- Implement @ngrx/component-store for local state management in complex components
- Use @ngrx/router-store to connect the router to the store
- Leverage @ngrx/entity-data for simplified entity management
- Implement the concatLatestFrom operator for effects that need state with actions

#### ANGULAR_CODING_STANDARDS

- Use standalone components, directives, and pipes instead of NgModules
- Implement signals for state management instead of traditional RxJS-based approaches
- Use the new inject function instead of constructor injection
- Implement control flow with @if, @for, and @switch instead of *ngIf, *ngFor, etc.
- Leverage functional guards and resolvers instead of class-based ones
- Use the new deferrable views for improved loading states
- Implement OnPush change detection strategy for improved performance
- Use TypeScript decorators with explicit visibility modifiers (public, private)
- Leverage Angular CLI for schematics and code generation
- Implement proper lazy loading with loadComponent and loadChildren

#### ANGULAR_MATERIAL

- Create a dedicated module for Angular Material imports to keep the app module clean
- Use theme mixins to customize component styles instead of overriding CSS
- Implement OnPush change detection for performance critical components
- Leverage the CDK (Component Development Kit) for custom component behaviors
- Use Material's form field components with reactive forms for consistent validation UX
- Implement accessibility attributes and ARIA labels for interactive components
- Use the new Material 3 design system updates where available
- Leverage the Angular Material theming system for consistent branding
- Implement proper typography hierarchy using the Material typography system
- Use Angular Material's built-in a11y features like focus indicators and keyboard navigation


### Guidelines for STYLING

#### SCSS

- Use the ThemeProvider for consistent theming across components
- Implement the css helper for sharing styles between components
- Use props for conditional styling within template literals
- Leverage the createGlobalStyle for global styling
- Implement attrs method to pass HTML attributes to the underlying DOM elements
- Use the as prop for dynamic component rendering
- Leverage styled(Component) syntax for extending existing components
- Implement the css prop for one-off styling needs
- Use the & character for nesting selectors
- Leverage the keyframes helper for animations


## BACKEND

### Guidelines for JAVA

#### SPRING_BOOT

- Use Spring Boot for simplified configuration and rapid development with sensible defaults
- Prefer constructor-based dependency injection over `@Autowired`
- Avoid hardcoding values that may change externally, use configuration parameters instead
- For complex logic, use Spring profiles and configuration parameters to control which beans are injected instead of hardcoded conditionals
- If a well-known library simplifies the solution, suggest using it instead of generating a custom implementation
- Use DTOs as immutable `record` types
- Use Bean Validation annotations (e.g., `@Size`, `@Email`, etc.) instead of manual validation logic
- Use `@Valid` on request parameters annotated with `@RequestBody`
- Use custom exceptions for business-related scenarios
- Centralize exception handling with `@ControllerAdvice` and return a consistent error DTO: `{{error_dto}}`
- REST controllers should handle only routing and I/O mapping, not business logic
- Use SLF4J for logging instead of `System.out.println`
- Prefer using lambdas and streams over imperative loops and conditionals where appropriate
- Use `Optional` to avoid `NullPointerException`

#### SPRING_DATA_JPA

- Define repositories as interfaces extending `JpaRepository` or `CrudRepository`
- Never expose JPA entities in API responses – always map them to DTOs
- Use `@Transactional` at the service layer for state-changing methods, and keep transactions as short as possible
- Use `@Transactional(readOnly = true)` for read-only operations
- Use `@EntityGraph` or fetch joins to avoid the N+1 select problem
- Use `@Query` for complex queries
- Use projections (DTOs) in multi-join queries with `@Query`
- Use Specifications for dynamic filtering
- Use pagination when working with large datasets
- Use `@Version` for optimistic locking in concurrent updates
- Avoid `CascadeType.REMOVE` on large entity relationships
- Use HikariCP for efficient connection pooling

#### LOMBOK

- Use Lombok where it clearly simplifies the code
- Use constructor injection with `@RequiredArgsConstructor`
- Prefer Java `record` over Lombok’s `@Value` when applicable
- Avoid using `@Data` in non-DTO classes, instead, use specific annotations like `@Getter`, `@Setter`, and `@ToString`
- Apply Lombok annotations to fields rather than the class if only some fields require them
- Use Lombok’s `@Slf4j` to generate loggers


## DATABASE

### Guidelines for SQL

#### POSTGRES

- Use connection pooling to manage database connections efficiently
- Implement JSONB columns for semi-structured data instead of creating many tables for {{flexible_data}}
- Use materialized views for complex, frequently accessed read-only data



