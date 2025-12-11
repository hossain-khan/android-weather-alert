# Developer Portal

## Overview

The Developer Portal is a comprehensive suite of debugging and testing tools available exclusively in **internal debug builds** of the Weather Alert app. It provides developers and QA engineers with powerful capabilities to test features, generate test data, inspect app state, and debug issues without waiting for real weather conditions or manual setup.

**Key Benefits:**
- ⚡ **Faster Testing**: Create test scenarios instantly
- 🎯 **Targeted Debugging**: Reproduce specific edge cases
- 🔍 **State Inspection**: View database and preferences directly
- 🧪 **Reliable Testing**: Generate consistent, reproducible test data

## Accessing the Portal

The Developer Portal is accessible **only in internal debug builds** (not in production):

1. Open the **About App** screen from the navigation menu
2. Tap **"🔧 Developer Portal"** button
3. Browse and select from 6 available developer tools

**Build Variants:**
- ✅ Available: `internalDebug` builds
- ❌ Not available: `prod` builds (production releases)

## Available Tools

The portal provides 6 specialized tools for different testing needs:

### 1. 🔔 Notification Tester

**Purpose**: Test weather alert notifications without waiting for real weather conditions.

**Features:**
- Preview notification appearance
- Test notification actions (View Details, Snooze)
- See notification in system tray
- Test rich notification format with weather data

**How to Use:**
1. Navigate to Notification Tester
2. Click **"Send Test Notification"**
3. View the notification in your device's notification tray
4. Test notification actions (tap to open, snooze, etc.)

**Test Data:**
- City: Seattle, WA
- Alert Type: Snow Alert (4.0mm threshold)
- Forecast: 8.5mm of snow
- Includes realistic forecast data for next 24 hours

**Use Cases:**
- Verify notification appearance before release
- Test notification actions work correctly
- Check notification priority and sound
- Validate rich notification content
- Test notification on different Android versions

### 2. ⚙️ WorkManager Tester

**Purpose**: Manually trigger background workers and observe their execution.

**Features:**
- List all scheduled workers with their status
- Trigger workers on-demand
- View worker execution results
- Test one-time and periodic workers

**Available Workers:**
- **Weather Check Worker**: Fetch weather data and check alert conditions
- **Database Cleanup Worker**: Clean up old forecast data

**How to Use:**
1. Navigate to WorkManager Tester
2. Review list of available workers and their schedules
3. Click **"Run Now"** to trigger a worker immediately
4. Observe execution results and timing
5. Check logs for detailed worker output

**Use Cases:**
- Test weather checking logic without waiting for schedule
- Verify database cleanup works correctly
- Debug worker failures
- Test worker constraints (network, battery)
- Validate worker retry logic

### 3. 📋 Alert Simulator

**Purpose**: Create test weather alerts quickly for testing alert-related features.

**Features:**
- Create alerts with quick presets
- Generate custom alerts with specific parameters
- Delete all test alerts at once
- All test data marked with [TEST] prefix

**Quick Presets:**
- **Test Snow Alert**: Seattle snow alert (4mm threshold)
- **Test Rain Alert**: Portland rain alert (10mm threshold)
- **Multiple Alerts**: 3 alerts across different cities
- **Custom Alert**: Specify your own parameters

**How to Use:**
1. Navigate to Alert Simulator
2. Choose a preset or click "Custom Alert"
3. For custom: select city, weather type, and threshold
4. Click **"Create Alert"**
5. View created alerts in the main Alerts screen
6. Use **"Delete All Test Alerts"** to clean up

**Test Data Marking:**
- All simulated alerts prefixed with `[TEST]`
- Example: `[TEST] Seattle` instead of `Seattle`
- Easy to identify and clean up test data

**Use Cases:**
- Test alert list UI with multiple alerts
- Verify alert editing works correctly
- Test alert deletion
- Check alert threshold validation
- Test snooze functionality on alerts

### 4. 📜 Alert History Simulator

**Purpose**: Generate historical alert data for testing history views and analytics.

**Features:**
- Quick history templates (Today, This Week, This Month)
- Custom history generation with date ranges
- Specify number of history entries
- View generated history immediately
- Delete all test history at once

**History Templates:**
- **Today's Alerts**: 5 alerts triggered today
- **This Week**: 15 alerts over the past 7 days
- **This Month**: 30 alerts spread across 30 days
- **Custom Range**: Specify exact dates and count

**How to Use:**
1. Navigate to Alert History Simulator
2. Select a template or click "Custom History"
3. For custom: specify date range and entry count
4. Click **"Generate History"**
5. View generated history in Alert History screen
6. Use **"Delete All Test History"** to clean up

**Generated Data:**
- Random mix of snow and rain alerts
- Distributed across multiple cities (Seattle, Portland, San Francisco)
- Realistic threshold values (2-15mm)
- Timestamps spread evenly across date range
- All marked with [TEST] prefix

**Use Cases:**
- Test history list with various data volumes
- Verify date filtering works correctly
- Test history statistics calculations
- Check infinite scroll or pagination
- Validate history export features

### 5. 🗄️ Database Inspector

**Purpose**: Inspect the Room database state and export data for analysis.

**Features:**
- View database statistics (counts, size, last modified)
- Quick data previews (alerts, history, cities)
- Display database file path
- Export database to Downloads folder
- Copy database path to clipboard

**Database Statistics:**
- **Cities**: Total number of cities in database
- **Alerts**: Count of user-configured alerts
- **History Entries**: Total alert history records
- **Cached Forecasts**: Number of cached weather forecasts
- **Database Size**: File size in MB
- **Last Modified**: When database was last updated

**Quick Data Views:**
- **View All Alerts**: Preview first 3 alerts with details
- **View Recent History**: Show last 10 history entries
- **View Cities**: Sample 20 cities from database

**Export Features:**
- Export complete database to Downloads folder
- Filename format: `weather_alert_YYYYMMDD_HHmmss.db`
- Exported file can be opened with SQLite browser
- Useful for debugging and data analysis

**How to Use:**
1. Navigate to Database Inspector
2. Review statistics dashboard at top
3. Click quick view buttons to preview data
4. Use **"Copy Database Path"** to get file location
5. Click **"Export Database to Downloads"** to save copy
6. Open exported file with DB Browser for SQLite

**Use Cases:**
- Verify data is persisted correctly
- Debug database corruption issues
- Analyze data distribution across tables
- Export data for bug reports
- Inspect foreign key relationships
- Validate database migrations

**Note**: Forecast count shows sample data due to DAO limitations. The database inspector provides an approximate count for cached forecasts.

### 6. ⚙️ State Management

**Purpose**: View and reset app preferences and cached data.

**Features:**
- View all current app preferences
- View API keys (safely masked)
- Reset individual preferences
- Clear all preferences at once
- Manage cached weather data
- All with confirmation dialogs for safety

**Preferences Viewer:**
- **Weather Service**: Currently selected provider (OpenWeather, Tomorrow.io, etc.)
- **Update Frequency**: Weather check interval (6/12/18 hours)
- **Onboarding Status**: Whether user completed onboarding
- **Last Check Time**: When weather was last checked
- **API Keys**: Masked view (shows last 4 chars: `****abc1`)

**Reset Actions:**
- **Reset Onboarding**: Clear onboarding flag (user sees onboarding again)
- **Reset Update Frequency**: Restore default 6-hour interval
- **Clear API Keys**: Remove all user-provided API keys
- **Clear All Preferences**: Reset everything to defaults

**Cache Management:**
- View cached forecast count (approximate)
- Clear weather cache (requires DAO enhancement)
- Note: Cache clearing currently disabled due to DAO limitations

**How to Use:**
1. Navigate to State Management
2. Review current preferences in "Current Preferences" card
3. Click individual reset buttons to reset specific values
4. Use **"Clear All Preferences"** for complete reset
5. Confirm actions in dialog prompts
6. Restart app to see onboarding after reset

**Use Cases:**
- Test onboarding flow from fresh state
- Switch weather services for testing
- Debug preference-related bugs
- Clear corrupt preference data
- Test app with different API keys
- Validate preference persistence
- Test default value fallbacks

**Safety Features:**
- Confirmation dialogs for destructive actions
- Success/error feedback via snackbars
- Non-destructive preview mode
- API keys always masked in UI
- No accidental data loss

## Best Practices

### Test Data Management

**Always Clean Up:**
- Use "Delete All Test Data" buttons in each tool
- Test data is prefixed with `[TEST]` for identification
- Clean up before sharing screenshots or demos
- Don't commit test data to production

**When to Use What:**
- **Alert Simulator**: Testing alert creation, editing, deletion
- **History Simulator**: Testing history views, date filtering, statistics
- **Notification Tester**: Verifying notification appearance and actions
- **WorkManager Tester**: Debugging background job execution
- **Database Inspector**: Investigating data persistence issues
- **State Management**: Resetting app to clean state

### Performance Considerations

**Large Data Sets:**
- History Simulator: Generating 100+ entries may take 5-10 seconds
- Database exports: Large databases (>10MB) may take time
- Quick views: Limited to small previews to avoid UI lag

**Database Operations:**
- All database operations run on background thread (Dispatchers.IO)
- UI remains responsive during data generation
- Progress indicators shown for long operations

### Development Workflow

**Typical Testing Session:**
1. **Setup**: Use Alert Simulator to create test alerts
2. **History**: Generate history data if testing analytics
3. **Test**: Exercise the feature you're developing
4. **Debug**: Use Database Inspector to verify data
5. **Cleanup**: Delete all test data before committing

**Bug Reproduction:**
1. Use State Management to reset to known state
2. Use simulators to create specific data scenario
3. Reproduce the bug with consistent test data
4. Export database if needed for bug report
5. Document exact steps using portal tools

## Troubleshooting

### Common Issues

**Problem: "Developer Portal" button not visible**
- **Solution**: Ensure you're running an `internalDebug` build variant
- **Check**: Build → Select Build Variant → Select `internalDebug`
- **Note**: Button is intentionally hidden in production builds

**Problem: Notification doesn't appear**
- **Solution**: Check notification permissions are granted
- **Check**: System Settings → Apps → Weather Alert → Notifications → Enabled
- **Note**: Test on Android 13+ requires explicit permission

**Problem: Worker doesn't execute**
- **Solution**: Check device constraints (network, battery)
- **Check**: Logs for WorkManager execution details
- **Note**: Doze mode may delay execution even with "Run Now"

**Problem: Database export fails**
- **Solution**: Check storage permissions granted
- **Check**: Available storage space in Downloads folder
- **Note**: Android 10+ uses scoped storage, should work without permissions

**Problem: Test data appears in production**
- **Solution**: This should never happen - portal code not in production builds
- **Check**: Verify build variant is `prod`, not `internal`
- **Prevention**: Test data always prefixed with `[TEST]`

**Problem: Preferences not resetting**
- **Solution**: Force stop app and restart after reset
- **Check**: Verify DataStore is working correctly
- **Note**: Some preferences cached in memory until restart

### Getting Help

**Logs:**
- All dev portal actions logged with tag `DevPortal`
- Filter logcat: `adb logcat -s DevPortal`
- Check for exceptions or error messages

**Database Inspection:**
- Export database and inspect with DB Browser for SQLite
- Download: https://sqlitebrowser.org/
- Useful for deep debugging of data issues

**Code References:**
- All portal code: `app/src/internal/java/dev/hossain/weatheralert/ui/devtools/`
- Circuit screens follow standard Circuit UDF pattern
- Metro DI bindings in same package

## Architecture

### Technology Stack

**UI Framework:**
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Latest Material Design components
- **Circuit**: Unidirectional Data Flow architecture by Slack

**Architecture Pattern:**
- **Circuit Presenter**: Business logic and state management
- **Circuit UI**: Pure presentational Compose functions
- **Metro DI**: Kotlin-first dependency injection by Square

**Data Layer:**
- **Room DAOs**: Direct database access (Alert, AlertHistory, City, CityForecast)
- **PreferencesManager**: DataStore-based preference management
- **WorkManager**: Background job scheduling and execution

### Code Organization

```
app/src/internal/java/dev/hossain/weatheralert/ui/devtools/
├── DeveloperPortalScreen.kt      # Main portal hub with navigation
├── AlertSimulatorScreen.kt       # Alert creation tool
├── HistorySimulatorScreen.kt     # History generation tool
├── NotificationTesterScreen.kt   # Notification testing tool
├── WorkerTesterScreen.kt         # WorkManager testing tool
├── DatabaseInspectorScreen.kt    # Database inspection tool
└── StateManagementScreen.kt      # Preferences management tool
```

### Circuit Pattern

Each screen follows the same structure:

```kotlin
@Parcelize
data object ScreenName : Screen {
    // UI State - data needed for rendering
    data class State(
        val data: SomeData,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState
    
    // User Events - actions from UI
    sealed class Event : CircuitUiEvent {
        data object SomeAction : Event()
    }
}

// Presenter - handles business logic
@AssistedInject
class ScreenPresenter(
    @Assisted private val navigator: Navigator,
    private val someDao: SomeDao,
) : Presenter<ScreenName.State> {
    @Composable
    override fun present(): ScreenName.State {
        // Collect state, handle events
        return ScreenName.State(data) { event ->
            when (event) {
                // Handle events
            }
        }
    }
}

// UI - pure presentation
@CircuitInject
@Composable
fun Screen(state: ScreenName.State) {
    // Render UI using state
}
```

### Dependency Injection

**Metro DI Bindings:**
- Screens automatically bound via `@CircuitInject` annotation
- Presenters use `@AssistedInject` for factory creation
- DAOs and managers injected directly
- Everything scoped to `AppScope`

**Example:**
```kotlin
@CircuitInject(ScreenName::class, AppScope::class)
@AssistedFactory
fun interface Factory {
    fun create(navigator: Navigator): ScreenPresenter
}
```

### Test Data Conventions

**Marking Test Data:**
- All simulated data prefixed with `[TEST]`
- Applied to city names: `[TEST] Seattle`
- Easy filtering in queries
- Clear visual indication in UI

**Data Generation:**
- Random selection from realistic values
- Distributed timestamps for history
- Consistent test data across runs (where appropriate)
- Realistic thresholds and forecast values

## Development

### Adding New Tools

To add a new developer tool:

1. **Create Screen File**: `NewToolScreen.kt` in `devtools/` package

2. **Follow Circuit Pattern**:
   ```kotlin
   @Parcelize
   data object NewToolScreen : Screen {
       data class State(...) : CircuitUiState
       sealed class Event : CircuitUiEvent { ... }
   }
   ```

3. **Implement Presenter**:
   ```kotlin
   @AssistedInject
   class NewToolPresenter(...) : Presenter<State> {
       @Composable
       override fun present(): State { ... }
       
       @CircuitInject(NewToolScreen::class, AppScope::class)
       @AssistedFactory
       fun interface Factory {
           fun create(navigator: Navigator): NewToolPresenter
       }
   }
   ```

4. **Implement UI**:
   ```kotlin
   @OptIn(ExperimentalMaterial3Api::class)
   @CircuitInject(NewToolScreen::class, AppScope::class)
   @Composable
   fun NewToolScreen(state: State) { ... }
   ```

5. **Add Navigation**: Update `DeveloperPortalScreen.kt`:
   ```kotlin
   sealed class Event : CircuitUiEvent {
       // ... existing events
       data object OpenNewTool : Event()
   }
   
   // In eventSink handler
   Event.OpenNewTool -> navigator.goTo(NewToolScreen)
   ```

6. **Add Tool Tile**: In `DeveloperPortalScreen.kt` UI:
   ```kotlin
   DeveloperToolCard(
       title = "New Tool",
       description = "What it does",
       icon = "🔧",
       onClick = { state.eventSink(Event.OpenNewTool) }
   )
   ```

7. **Test**: Run internal build and verify navigation works

### Code Quality Guidelines

**Required:**
- ✅ Follow Circuit + Metro DI patterns
- ✅ Use Material 3 components
- ✅ Handle errors gracefully with try-catch
- ✅ Provide user feedback (Snackbars, dialogs)
- ✅ Run on IO dispatcher for database operations
- ✅ Add proper logging with `DevPortal` tag
- ✅ Format code with kotlinter

**Recommended:**
- 📝 Add KDoc comments for public functions
- 🧪 Write unit tests for complex logic
- 🎨 Match existing UI patterns and styling
- ♿ Add content descriptions for accessibility
- 🔍 Log important actions and errors

### Testing Developer Tools

**Manual Testing:**
1. Build internal debug variant
2. Navigate to each tool
3. Exercise all features and presets
4. Verify data appears correctly in main app
5. Test cleanup functions work
6. Check error handling with invalid inputs

**Automated Testing:**
- Unit tests for presenters (state management logic)
- Integration tests for DAO operations
- UI tests for navigation (optional)
- Use Truth assertions for readability

**Coverage Goals:**
- Repository operations: 80%+
- Presenter logic: 60%+
- Overall dev portal code: 50%+

## Security Considerations

**API Key Display:**
- Keys always masked in UI (`****abc1`)
- Only last 4 characters visible
- Uses `PreferencesManager.savedApiKey()` which returns masked value
- Never log full API keys

**Database Export:**
- Exported files contain full database including API keys
- Stored in user-accessible Downloads folder
- User responsible for exported file security
- Consider this when exporting on shared devices

**Test Data Isolation:**
- Test data clearly marked with `[TEST]` prefix
- Should be cleaned up before production
- No production data affected by portal actions
- Portal code not included in production builds

## Future Enhancements

**Potential Additions:**
- 📊 Analytics event viewer
- 🌐 Network inspector for API calls
- 📸 Screenshot testing tool
- 🔄 State restore from exported database
- 📱 Device info display
- 🎨 Theme switcher for UI testing
- 📊 Crash report simulator
- 🔐 Encrypted database viewer support

**Improvements:**
- Add forecast count accuracy (requires DAO enhancement)
- Enable cache clearing (needs `deleteAllCityForecasts()` DAO method)
- Add CSV export for history data
- Implement state snapshots/restore
- Add performance metrics dashboard

## Resources

### External Documentation
- [Circuit Architecture](https://slackhq.github.io/circuit/) - UDF pattern by Slack
- [Metro DI](https://github.com/JakeWharton/dagger-reflect) - Kotlin DI by Square
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI toolkit
- [Room Database](https://developer.android.com/training/data-storage/room) - SQLite wrapper
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Background jobs

### Internal Documentation
- `README.md` - Main project documentation
- `docs/METRO_DI_ARCHITECTURE.md` - Metro DI patterns used in app
- `.github/copilot-instructions.md` - Coding standards and patterns

### Useful Commands
```bash
# Build internal debug variant
./gradlew assembleInternalDebug

# Run unit tests
./gradlew testInternalDebugUnitTest

# Format code
./gradlew formatKotlin

# Lint check
./gradlew lintKotlin

# Generate coverage report
./gradlew koverHtmlReportDebug

# Install to device
./gradlew installInternalDebug
```

---

**Questions or Issues?**
- Check troubleshooting section above
- Review code in `app/src/internal/java/dev/hossain/weatheralert/ui/devtools/`
- Search logs with `adb logcat -s DevPortal`
- Reach out to the development team

**Happy Testing! 🚀**
