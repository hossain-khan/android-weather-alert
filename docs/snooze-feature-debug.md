# Debug Snooze Feature - Testing Guide

## New Debug Function Added ✅

I've added a `debugSnooze()` function to test the snooze functionality without needing to wait for a real notification!

### Location
`/app/src/main/java/dev/hossain/weatheralert/notification/Notification.kt`

## How to Use

### Step 1: Uncomment the Debug Call in `WeatherAlertApp.kt`

In `WeatherAlertApp.onCreate()`, uncomment the debug line:

```kotlin
override fun onCreate() {
    super.onCreate()

    installLoggingTree()

    createAppNotificationChannel(context = this)
    scheduleWeatherAlertsWork(context = this, appGraph.preferencesManager.preferredUpdateIntervalSync)

    // Debug functions - uncomment to test manually:
    // dev.hossain.weatheralert.notification.debugNotification(context = this)
    dev.hossain.weatheralert.notification.debugSnooze(context = this, alertId = 1)  // ← Uncomment this
    // scheduleOneTimeWeatherAlertWorkerDebug(context = this)
}
```

### Step 2: Make Sure You Have an Alert with ID = 1

The debug function defaults to alert ID 1. You can:
- **Option A**: Create an alert in the app (it will likely have ID = 1 if it's your first alert)
- **Option B**: Change the `alertId` parameter to match an existing alert ID

### Step 3: Run the App

1. Build and run the app
2. The snooze will be applied automatically when `onCreate()` runs
3. Navigate to the Alert Details screen for the alert
4. You should now see the **Snooze Status UI** 🎉

## Available Snooze Duration Options

You can customize the snooze duration by changing the `snoozeDuration` parameter:

```kotlin
// Snooze for 1 hour
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_1_HOUR)

// Snooze for 3 hours
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_3_HOURS)

// Snooze until tomorrow at 8 AM (default)
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_TOMORROW)

// Snooze for 1 week
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_1_WEEK)
```

## What Happens Under the Hood

The `debugSnooze()` function:
1. ✅ Creates an Intent with the snooze action
2. ✅ Broadcasts it to `SnoozeAlertReceiver`
3. ✅ The receiver updates the `snoozed_until` column in the database
4. ✅ Logs the action to Logcat: `"Debug snooze triggered for alertId=1 with duration=..."`

## Clearing the Snooze

Once you've tested the snooze UI, you can clear it by:
- Tapping the **"Clear Snooze"** button in the Snooze Status UI, OR
- Commenting out the debug call and restarting the app, then using the app's built-in clear functionality

## Testing Different Scenarios

### Scenario 1: Test with Short Duration (1 Hour)
```kotlin
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_1_HOUR)
```
- Good for quick testing
- Snooze will expire in 1 hour

### Scenario 2: Test with Long Duration (Tomorrow)
```kotlin
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_TOMORROW)
```
- Default option
- Snooze until next day at 8 AM
- Best for preview testing

### Scenario 3: Test with Multiple Alerts
```kotlin
debugSnooze(context = this, alertId = 1, snoozeDuration = SnoozeAlertReceiver.SNOOZE_TOMORROW)
debugSnooze(context = this, alertId = 2, snoozeDuration = SnoozeAlertReceiver.SNOOZE_3_HOURS)
```
- Test multiple snoozed alerts at once

## Troubleshooting

### "Snooze UI still not showing"
- ✅ Check Logcat for: `"Debug snooze triggered for alertId=X"`
- ✅ Verify the alert with that ID exists in your database
- ✅ Make sure you're viewing the correct Alert Details screen
- ✅ Try force-closing and reopening the app

### "Alert not found"
- The alert ID doesn't exist yet
- Create an alert first, then use its ID in the debug function

### "BroadcastReceiver not registered"
- This shouldn't happen as `SnoozeAlertReceiver` is registered in AndroidManifest.xml
- Check if the receiver is properly declared

## Don't Forget!

⚠️ **Remember to comment out the debug call before committing or releasing!**

The debug functions are meant for development testing only and should not be active in production builds.
