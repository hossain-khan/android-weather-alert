# Metro DI Multi-Module Architecture

This document describes the Metro DI multi-module architecture implemented in this project.

## Overview

The project now follows Metro DI best practices for multi-module dependency injection, where each service module contributes its own dependencies to the application graph using the `@ContributesTo` pattern.

## Architecture Pattern

### Before (Centralized)
- All service dependencies were defined in `app/di/NetworkBindings.kt`
- App module had to know about all service implementations
- Adding a new service required modifying the app module

### After (Decentralized)
- Each service module owns its DI configuration
- Service modules use `@ContributesTo(AppScope::class)` to contribute bindings
- App module only provides shared dependencies (OkHttpClient)
- Adding a new service only requires creating the service module with its DI module

## Module Structure

```
app/
  └── di/
      ├── AppGraph.kt                    # Main dependency graph
      ├── NetworkBindings.kt             # Shared network dependencies (OkHttpClient)
      └── ...

service/openweather/
  └── org/openweathermap/api/
      ├── di/
      │   └── OpenWeatherModule.kt      # Service-specific DI module
      └── OpenWeatherService.kt          # Service interface

service/tomorrowio/
  └── io/tomorrow/api/
      ├── di/
      │   └── TomorrowIoModule.kt       # Service-specific DI module
      └── TomorrowIoService.kt           # Service interface

service/openmeteo/
  └── com/openmeteo/api/
      ├── di/
      │   └── OpenMeteoModule.kt        # Service-specific DI module
      └── OpenMeteoService.kt            # Service interface

service/weatherapi/
  └── com/weatherapi/api/
      ├── di/
      │   └── WeatherApiModule.kt       # Service-specific DI module
      └── WeatherApiService.kt           # Service interface
```

## Service Module Pattern

Each service module follows this pattern:

```kotlin
@ContributesTo(AppScope::class)
interface ServiceNameModule {
    companion object {
        // Test backdoor for mock server URLs
        var serviceBaseUrl: HttpUrl = "https://api.service.com/".toHttpUrl()

        @Provides
        @SingleIn(AppScope::class)
        @Named("ServiceName")
        fun provideServiceRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit.Builder()
                .baseUrl(serviceBaseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        @Provides
        @SingleIn(AppScope::class)
        fun provideService(
            @Named("ServiceName") retrofit: Retrofit
        ): ServiceName = retrofit.create(ServiceName::class.java)
    }
}
```

## Key Annotations

- `@ContributesTo(AppScope::class)` - Automatically contributes bindings to the app-scoped dependency graph
- `@Provides` - Marks a function that provides a dependency
- `@SingleIn(AppScope::class)` - Marks a dependency as a singleton scoped to the application
- `@Named("...")` - Qualifies dependencies when multiple instances of the same type exist

## Adding a New Service

To add a new weather service:

1. Create a new service module under `service/newservice/`
2. Add Metro plugin and javax.inject dependency to the module's `build.gradle.kts`
3. Create the service interface
4. Create a DI module with `@ContributesTo(AppScope::class)`
5. The service will be automatically available in the app

No changes to the app module are required!

## Testing

Service modules expose mutable base URL variables for testing:
```kotlin
// In test setup
ServiceModule.serviceBaseUrl = mockWebServer.url("/")
```

This allows tests to inject mock server URLs without modifying production code.

## References

- [Metro DI Documentation](https://zacsweers.github.io/metro/)
- [CatchUp Reference Implementation](https://github.com/ZacSweers/CatchUp) - Production app using this pattern
