# Testing Notification with Snooze Buttons 🔔

## Setup Complete! ✅

I've enabled `debugNotification(context = this)` in your `WeatherAlertApp.kt` file.

## What Will Happen When You Run the App

1. **App starts** → `debugNotification()` is called automatically
2. **Notification appears** with:
    - Title: "Snow Alert for Toronto"
    - Message: Weather details
    - **Two action buttons**: "Snooze 1h" and "Snooze 3h"
    - Reminder notes in expanded view

## How to Test the Full Snooze Flow

### Step 1: Clear the Existing Snooze (if alert ID 1 is already snoozed)
Since you mentioned alert ID 1 is already snoozed from your previous test:
1. Open the app
2. Go to **Alert Details** for alert ID 1
3. Tap the **"Clear Snooze"** button in the Snooze Status UI
4. Now the alert is no longer snoozed

### Step 2: Close and Reopen the App
- **Force close** the app (swipe away from recent apps)
- **Reopen** the app
- A new notification will appear (because `debugNotification` runs on app start)

### Step 3: Test the Notification Snooze Buttons
You'll see a notification with these options:
- 📱 **Tap the notification body** → Opens the app to Alert Details
- ⏰ **Tap "Snooze 1h"** → Snoozes for 1 hour, notification disappears
- ⏰ **Tap "Snooze 3h"** → Snoozes for 3 hours, notification disappears

### Step 4: Verify the Snooze UI
After tapping a snooze button:
1. Open the app (if not already open)
2. Navigate to **Alert Details** for alert ID 1
3. You'll see the **Snooze Status UI** showing:
    - "Snoozed until [time]"
    - "Clear Snooze" button

## Notification Details

The debug notification shows:
```
Title: "Snow Alert for Toronto"
Current: 30 mm/in (above threshold of 15 mm/in)
City: Toronto
Notes:
• Charge batteries
• Check tire pressure
• Order Groceries

Actions:
[Snooze 1h] [Snooze 3h]
```

## What Happens Behind the Scenes

When you tap a snooze button:
1. ✅ `SnoozeAlertReceiver` receives the broadcast
2. ✅ Database updated: `snoozed_until` = current time + duration
3. ✅ Notification is dismissed automatically
4. ✅ Next time you open Alert Details, you'll see the Snooze Status UI

## Testing Tips

### Test Multiple Times
To test multiple times:
1. Clear the snooze using the "Clear Snooze" button in the app
2. Close and reopen the app to get a fresh notification
3. Try different snooze durations (1h vs 3h)

### Test with Different Alert IDs
If you have multiple alerts, change the `userAlertId` parameter in `debugNotification()`:
```kotlin
// In Notification.kt, modify debugNotification() temporarily:
triggerNotification(
    context = context,
    userAlertId = 2,  // Change to test different alerts
    // ... rest of params
)
```

### Compare Both Methods
You now have two ways to test snooze:
1. **Via Notification** (current setup): More realistic, tests the full UX flow
2. **Via debugSnooze()**: Direct database update, faster for testing the UI

## Current Configuration

```kotlin
// WeatherAlertApp.kt - onCreate()

// ✅ ACTIVE: Triggers notification with snooze buttons
dev.hossain.weatheralert.notification.debugNotification(context = this)

// ❌ COMMENTED OUT: Direct snooze (not needed when testing notifications)
// dev.hossain.weatheralert.notification.debugSnooze(context = this, alertId = 1)
```

## Next Steps

1. **Build and run** the app
2. **Look for the notification** (should appear immediately)
3. **Tap "Snooze 1h"** or **"Snooze 3h"**
4. **Open the app** → Navigate to Alert Details
5. **Verify** the Snooze Status UI appears! 🎉

---

**Note**: Make sure you have notification permissions enabled for the app in your device settings, otherwise the notification won't appear!
