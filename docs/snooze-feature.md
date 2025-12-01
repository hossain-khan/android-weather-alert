# Weather Alert Snooze Feature - How It Works

## Current Implementation

### When Does the Snooze UI Show Up?

The snooze status UI (`WeatherAlertSnoozeStatusUi`) in the `WeatherAlertDetailsScreen` **only appears** when:

1. ✅ An alert has been snoozed via **notification actions**
2. ✅ The `snoozedUntil` timestamp is in the future (checked via `alert.isSnoozed()`)

### Current Snooze Flow

```
1. Weather condition triggers alert threshold
   ↓
2. App sends notification with snooze action buttons
   ↓
3. User taps "Snooze 1 day" or "Snooze 1 week" in notification
   ↓
4. SnoozeAlertReceiver updates database: snoozedUntil = now + duration
   ↓
5. User opens app → WeatherAlertDetailsScreen
   ↓
6. If alert.isSnoozed() returns true → Snooze UI appears
```

### Key Code Locations

**Snooze Check in UI** (`WeatherAlertDetails.kt:345-351`):
```kotlin
// Show snooze status if alert is snoozed
if (alert.isSnoozed()) {
    item {
        WeatherAlertSnoozeStatusUi(
            snoozedUntil = alert.snoozedUntil!!,
            onClearSnooze = { state.eventSink(WeatherAlertDetailsScreen.Event.ClearSnooze) },
        )
    }
}
```

**Snooze Validation** (`Alert.kt:52`):
```kotlin
fun isSnoozed(): Boolean = snoozedUntil.isSnoozed()
```

**Extension Function** (`TimeUtil.kt:50`):
```kotlin
fun Long?.isSnoozed(): Boolean = this != null && this > System.currentTimeMillis()
```

**Notification Actions** (`Notification.kt:159-162`):
```kotlin
// Add snooze actions - limited to 2 actions to fit notification constraints
.addAction(R.drawable.snooze_24dp, "Snooze 1 day", snooze1DayIntent)
.addAction(R.drawable.snooze_24dp, "Snooze 1 week", snooze1WeekIntent)
```

## Why You're Not Seeing the Snooze UI

There are several possible reasons:

### 1. **No Notification Received Yet** ⚠️ Most Likely
- The alert threshold hasn't been met yet
- Weather data hasn't triggered a notification
- Notifications are disabled for the app

### 2. **Notification Received But Not Snoozed**
- You dismissed the notification without using snooze actions
- You tapped the main notification (opens app) instead of snooze buttons

### 3. **Snooze Expired**
- You snoozed it previously, but the time has passed
- The `snoozedUntil` timestamp is now in the past

### 4. **Database Issue**
- The `snoozed_until` column is NULL (never set)
- Database migration issue

## How to Test the Snooze Feature

### Option 1: Trigger a Real Notification
1. Create an alert with a very low threshold (e.g., 0.1 mm for rain/snow)
2. Wait for weather data to be fetched
3. If conditions meet threshold, you'll get a notification
4. Tap "Snooze 1 day" or "Snooze 1 week" button in the notification
5. Open the app and navigate to Alert Details
6. You should now see the Snooze Status UI

### Option 2: Debug Notification (Recommended)
Check if there's a debug notification function available in the code:
```kotlin
// Look for: debugNotification(context) in Notification.kt
```

### Option 3: Manually Set Database Value
Use Android Studio's Database Inspector:
1. Open Database Inspector
2. Find `alerts` table
3. Update `snoozed_until` column to a future timestamp
4. Example: `System.currentTimeMillis() + (24 * 60 * 60 * 1000)` (24 hours from now)
5. Reopen the Alert Details screen

### Option 4: Add Manual Snooze Button (Feature Enhancement)
See the suggestion below.

---

## 🚀 Suggested Enhancement: Manual Snooze from UI

Currently, users can **only snooze via notification actions**. Consider adding a manual snooze option in the Alert Details screen for better UX.

### Benefits:
- ✅ Users can proactively snooze alerts before they trigger
- ✅ More control over alert management
- ✅ Easier testing during development
- ✅ Better user experience

### Proposed UI Location:
Add a "Snooze Alert" button in the `WeatherAlertDetailsScreen` (similar to Edit Note button) with snooze duration options:
- 1 hour
- 3 hours
- 12 hours
- 24 hours
- Until tomorrow (8 AM)

### Implementation:
Would you like me to add this manual snooze feature to the app?
