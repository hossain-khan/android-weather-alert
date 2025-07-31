# GitHub Copilot Custom Instructions

This document provides context and coding guidelines for the Weather Alert Android App to help GitHub Copilot generate appropriate code suggestions.

## Project Overview

Weather Alert is a modern Android application that provides focused weather notifications based on user-configured thresholds. The app helps users prepare for specific weather conditions like snow and rain by sending timely alerts when conditions meet their criteria.

### Key Features
- Custom alerts for snowfall and rainfall with user-defined thresholds
- Multiple weather data sources (OpenWeatherMap, Tomorrow.io, OpenMeteo, WeatherAPI)
- Configurable alert frequency (6, 12, or 18 hours)
- Rich notifications with actionable information
- Minimalist tile-based UI design
- Background processing with WorkManager

## Architecture & Tech Stack

### Core Architecture
- **UI Framework**: Jetpack Compose with Material 3 design system
- **Architecture Pattern**: Circuit UDF (Unidirectional Data Flow) by Slack
- **Dependency Injection**: Metro (modern Kotlin-first DI framework)
- **Navigation**: Jetpack Navigation Compose
- **State Management**: Circuit Presenters and UI components

### Data Layer
- **Database**: Room with KSP code generation
- **Preferences**: DataStore (both Core and Preferences)
- **Network**: Retrofit 3 + OkHttp 4 with logging interceptor
- **JSON Parsing**: Moshi with Kotlin codegen
- **Serialization**: Kotlin Serialization + Moshi
- **Error Handling**: EitherNet for type-safe network results

### Background Processing
- **WorkManager**: For periodic weather checks and alert generation
- **Notifications**: Android notification system with rich content support

### Testing & Quality
- **Unit Testing**: JUnit 4 with Google Truth assertions
- **Android Testing**: Robolectric for unit tests requiring Android context
- **Code Coverage**: Kover (minimum 50% for release builds)
- **Linting**: Kotlinter (ktlint wrapper) with custom Compose rules
- **CI/CD**: GitHub Actions with multi-JDK testing (17, 21, 23)

### Module Structure
```
├── app/                    # Main Android application
├── data-model/            # Shared data models and DTOs
├── service/               # Weather API service modules
│   ├── openweather/       # OpenWeatherMap API integration
│   ├── tomorrowio/        # Tomorrow.io API integration  
│   ├── openmeteo/         # OpenMeteo API integration
│   └── weatherapi/        # WeatherAPI integration
```

## Code Style & Quality Standards

### Kotlin Conventions
- Follow official Kotlin coding conventions
- Use ktlint formatting enforced by kotlinter plugin
- Compose functions should be annotated with `@Composable` and ignore naming conventions
- Prefer extension functions for utility methods
- Use data classes for immutable data structures

### Compose Guidelines
- Follow Compose best practices for state management
- Use `remember` for local state, avoid mutable state in Composables
- Implement proper state hoisting patterns
- Use Circuit's Presenter pattern for complex state logic
- Prefer stateless Composables when possible

### Dependency Injection Patterns
- Use Metro for dependency injection with Kotlin-first design
- Define scopes using `@SingleIn(AppScope::class)` for singletons
- Use `@DependencyGraph` with `@BindingContainer` objects for organizing bindings
- Implement Factory patterns for Circuit Presenters and UIs
- Use `@Multibinds` for providing sets or maps of implementations

### Metro-Specific Guidelines
- Use `@Inject` constructor injection for classes
- Create `@BindingContainer` objects to organize related bindings
- Use `@Provides` functions for complex dependency creation
- Define main app graph with `@DependencyGraph(scope = AppScope::class)`
- Use `createGraphFactory` pattern for component creation
- Test graphs should also be scoped to `AppScope::class` to access scoped bindings

### Error Handling
- Use EitherNet's ApiResult for network operations
- Handle exceptions gracefully with proper error states
- Provide meaningful error messages to users
- Log errors using Timber for debugging

## Development Workflow

### Branch Strategy
- Main branch: `main`
- Feature branches: descriptive names
- Pull requests required for all changes
- CI/CD validation on all PRs

### Build Variants
- `internal`: Development builds with debug features
- `prod`: Production builds for release

### Testing Requirements
- Write unit tests for business logic
- Aim for minimum 50% code coverage
- Test Compose UIs with appropriate testing utilities
- Use Truth assertions for better error messages
- Mock external dependencies properly

### Code Quality Checks
1. **Lint**: `./gradlew lintKotlin` - Run kotlinter formatting checks
2. **Test**: `./gradlew testInternalDebugUnitTest` - Run unit tests
3. **Build**: `./gradlew assembleDebug` - Compile application
4. **Coverage**: `./gradlew koverHtmlReportDebug` - Generate coverage reports

## Weather API Integration Patterns

### Service Module Structure
Each weather service module should follow this pattern:
```kotlin
// API interface
interface WeatherService {
    suspend fun getForecast(lat: Double, lon: Double): ApiResult<ForecastResponse>
}

// Implementation with Retrofit
class WeatherServiceImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherService {
    // Implementation
}

// Metro binding container
@BindingContainer
object WeatherServiceModule {
    @Provides
    fun provideWeatherService(impl: WeatherServiceImpl): WeatherService = impl
    
    // Retrofit and OkHttp setup
}
```

### Data Model Guidelines
- Use consistent data models in `:data-model` module
- Convert API responses to internal models
- Use sealed classes for different weather conditions
- Include proper null safety and validation

### Background Processing
- Use WorkManager for periodic weather data fetching
- Implement proper retry policies for network failures
- Handle device constraints (battery, network) appropriately
- Generate notifications based on user-configured thresholds

## Database Patterns

### Room Database Guidelines
- Use entities with proper annotations
- Implement DAOs with suspend functions for async operations
- Use Room's type converters for complex data types
- Generate schemas for migration support

### DataStore Usage
- Store user preferences using DataStore Preferences
- Use Flow for reactive data access
- Implement proper serialization for complex preferences

## UI Development Guidelines

### Circuit Integration
- Create Presenter classes extending `Presenter<State, Event>` with `@Inject` constructor
- Implement UI components as `@Composable` functions with `@CircuitInject`
- Use `@AssistedFactory` with `@CircuitInject` for presenter factories
- Handle loading, success, and error states appropriately
- Use `@Assisted` for screen-specific dependencies like Navigator

### Compose Best Practices
- Use proper preview annotations for design-time rendering
- Implement accessibility features (content descriptions, semantics)
- Follow Material 3 design guidelines
- Use appropriate animation and transition APIs

### Navigation
- Use Jetpack Navigation Compose for screen navigation
- Define clear navigation graphs
- Handle deep linking appropriately

## Testing Strategies

### Unit Testing
- Test business logic in isolation
- Mock external dependencies using test doubles
- Use parameterized tests for multiple scenarios
- Test edge cases and error conditions

### UI Testing
- Test Compose components with proper test utilities
- Verify state changes and user interactions
- Use semantic matchers for accessibility testing

### Integration Testing
- Test API integrations with mock servers
- Verify database operations end-to-end
- Test WorkManager job execution

## Debugging & Monitoring

### Analytics & Monitoring
- Use `LaunchedImpressionEffect` for screen view tracking
- Implement analytics event logging for user interactions
- Track tutorial completion and app usage patterns
- Use Firebase Analytics for user behavior insights

### Logging
- Use Timber for structured logging
- Include relevant context in log messages
- Use appropriate log levels (Debug, Info, Warning, Error)
- Avoid logging sensitive user data

### Crash Reporting
- Firebase Crashlytics is integrated for crash reporting
- Handle exceptions gracefully to prevent crashes
- Provide meaningful error context in crash reports

### Performance
- Monitor app performance using appropriate tools
- Optimize Compose recomposition with stable parameters
- Use proper coroutine scopes for background operations

## Release Guidelines

### Build Preparation
1. Update version codes and names
2. Run full test suite: `./gradlew test`
3. Generate coverage report: `./gradlew koverHtmlReportDebug`
4. Verify minimum coverage threshold (50%)
5. Run lint checks: `./gradlew lintKotlin`

### Code Review
- Ensure all tests pass
- Verify proper error handling
- Check for potential memory leaks
- Validate user experience improvements
- Confirm API key security practices

## Security Considerations

### API Keys
- Store weather API keys in `local.properties` file (not committed to version control)
- Use BuildConfig for API key injection: `BuildConfig.OPEN_WEATHER_API_KEY`
- Support multiple weather service API keys:
  - `OPEN_WEATHER_API_KEY` for OpenWeatherMap
  - `TOMORROW_IO_API_KEY` for Tomorrow.io  
  - `WEATHERAPI_API_KEY` for WeatherAPI
- Include Git commit hash in BuildConfig for build traceability
- Implement proper key rotation strategies

### Data Privacy
- Follow Android privacy guidelines
- Handle location data appropriately
- Provide clear privacy disclosures

## GitHub Copilot Configuration

### Firewall Configuration
- Firewall rules are configured in `.github/copilot-firewall.yml`
- Essential domains are allowlisted for Android development:
  - `dl.google.com` - Android Gradle Plugin and dependencies
  - `maven.google.com` - Google Maven repository
  - `repo1.maven.org` - Maven Central
  - `services.gradle.org` - Gradle distributions
  - `jitpack.io` - GitHub-based dependencies
- Weather API endpoints are included for runtime functionality
- Block-by-default policy ensures security while allowing necessary build dependencies

### Development Environment Setup
- The project includes comprehensive Copilot instructions for better code suggestions
- Custom instructions cover architecture patterns, coding standards, and common examples
- Firewall configuration ensures secure access to necessary external resources

## Common Patterns & Examples

### Circuit Presenter Example
```kotlin
@Parcelize
data object AboutAppScreen : Screen {
    data class State(
        val appVersion: String,
        val showLearnMoreSheet: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
        data object OpenGitHubProject : Event()
        data object OpenAppEducationDialog : Event()
        data object CloseAppEducationDialog : Event()
    }
}

@Inject
class AboutAppPresenter constructor(
    @Assisted private val navigator: Navigator,
    private val analytics: Analytics,
) : Presenter<AboutAppScreen.State> {
    
    @Composable
    override fun present(): AboutAppScreen.State {
        val uriHandler = LocalUriHandler.current
        var showLearnMoreBottomSheet by remember { mutableStateOf(false) }
        
        val appVersion = buildString {
            append("v")
            append(BuildConfig.VERSION_NAME)
            append(" (")
            append(BuildConfig.GIT_COMMIT_HASH)
            append(")")
        }
        
        // Analytics tracking
        LaunchedImpressionEffect {
            analytics.logScreenView(AboutAppScreen::class)
        }
        
        return AboutAppScreen.State(
            appVersion,
            showLearnMoreSheet = showLearnMoreBottomSheet,
        ) { event ->
            when (event) {
                AboutAppScreen.Event.GoBack -> {
                    navigator.pop()
                }
                AboutAppScreen.Event.OpenGitHubProject -> {
                    uriHandler.openUri("https://github.com/hossain-khan/android-weather-alert")
                }
                AboutAppScreen.Event.OpenAppEducationDialog -> {
                    showLearnMoreBottomSheet = true
                    analytics.logViewTutorial(isComplete = false)
                }
                AboutAppScreen.Event.CloseAppEducationDialog -> {
                    showLearnMoreBottomSheet = false
                    analytics.logViewTutorial(isComplete = true)
                }
            }
        }
    }
    
    @CircuitInject(AboutAppScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): AboutAppPresenter
    }
}

// UI Component
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AboutAppScreen::class, AppScope::class)
@Composable
fun AboutAppScreen(
    state: AboutAppScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About App") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(AboutAppScreen.Event.GoBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
            )
        },
    ) { contentPaddingValues ->
        // Screen content
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPaddingValues)
                .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding)
        ) {
            // UI content here
        }
    }
}
```

### Circuit Pattern Key Points
- **Screen Definition**: Use `@Parcelize data object` for simple screens that extend `Screen`
- **State**: Define state as a data class implementing `CircuitUiState` with an `eventSink`
- **Events**: Use sealed class hierarchy extending `CircuitUiEvent` for all user interactions
- **Presenter**: Use `@Inject` constructor with `@Assisted` for dependencies that vary per screen instance
- **Factory**: Use `@CircuitInject` with `@AssistedFactory` for presenter creation
- **UI Component**: Use `@CircuitInject` to bind the UI component to the screen and scope
- **State Management**: Use `remember { mutableStateOf() }` for local UI state in presenters
- **Analytics**: Use `LaunchedImpressionEffect` for screen view tracking
- **Navigation**: Handle navigation through events processed in the presenter

### Repository Pattern
```kotlin
@SingleIn(AppScope::class)
class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val weatherDao: WeatherDao
) : WeatherRepository {
    
    override suspend fun getForecast(
        location: Location
    ): ApiResult<Forecast> {
        // Implementation with caching
    }
}
```

### WorkManager Example
```kotlin
class WeatherCheckWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // Background weather checking logic
    }
}
```

This document should guide development decisions and ensure consistency across the codebase. When in doubt, refer to existing code patterns and architectural decisions already established in the project.