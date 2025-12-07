# Accessibility Features

## Overview

Weather Alert is designed to be accessible to all users, including those who rely on assistive technologies like TalkBack screen readers. This document outlines the accessibility features implemented in the app.

## Key Accessibility Features

### 1. Content Descriptions for All Icons

All icons throughout the app have meaningful content descriptions that provide context to screen reader users:

- **Weather alert icons**: "Snowfall alert icon", "Rainfall alert icon"
- **Action icons**: "Add new weather alert", "Delete alert", "Refresh forecast data"
- **Status icons**: "Alert active", "Alert snoozed", "City location"
- **Navigation icons**: "Go back", "More options", "Settings", "History"
- **Service logos**: Clear descriptions for all weather service provider logos

### 2. Semantic Properties for Complex UI Elements

Alert cards include comprehensive semantic descriptions that provide full context:

```kotlin
// Example: Alert card semantics
"Snowfall alert for Toronto, Canada. 
Threshold: 25mm. 
Next 24 hours forecast: 11mm. 
Alert is active. 
Double tap to view details, swipe left to delete."
```

This ensures TalkBack users understand:
- The type of weather alert
- The location
- The configured threshold
- The current forecast status
- Available actions

### 3. Proper Focus Order

The app follows a logical navigation order for keyboard and TalkBack navigation:
- Top-to-bottom, left-to-right reading order
- Alert list items are individually focusable
- Form fields in proper sequence
- Menu items in logical groupings

### 4. Typography and Text Scaling

The app uses Material 3 typography system which:
- Supports Android's system font size settings
- Scales text appropriately from 100% to 200%
- Uses relative sizing (sp) rather than fixed sizes
- Maintains readability at all text sizes

### 5. Color Contrast and Visual Indicators

- High contrast between text and backgrounds
- Alert states indicated by color AND icons (not color alone)
- Dark mode support for reduced eye strain
- Visual feedback for interactive elements

## Testing with TalkBack

To test the app's accessibility:

1. **Enable TalkBack**:
   - Go to Settings > Accessibility > TalkBack
   - Turn on TalkBack
   - Use the tutorial to learn TalkBack gestures

2. **Test Key Screens**:
   - **Alert List**: Navigate through alerts, hear full descriptions
   - **Add Alert Form**: Navigate through form fields in order
   - **Settings**: Select weather services and update frequency
   - **Alert Details**: Review detailed forecast information

3. **Test Key Actions**:
   - Adding a new alert
   - Viewing alert details
   - Deleting an alert (swipe to dismiss)
   - Accessing menu items
   - Changing settings

## Implementation Guidelines

For developers maintaining or extending this app:

### Icons and Images

Always provide content descriptions:

```kotlin
Icon(
    painter = painterResource(R.drawable.icon),
    contentDescription = "Descriptive text",
    modifier = Modifier.size(24.dp)
)
```

### Interactive Elements

Use semantics for complex components:

```kotlin
Card(
    modifier = Modifier
        .clickable { /* action */ }
        .semantics {
            contentDescription = "Full description with context and available actions"
        }
)
```

### Text Fields

Provide labels and hints:

```kotlin
OutlinedTextField(
    value = value,
    onValueChange = { /* update */ },
    label = { Text("Field Label") },
    placeholder = { Text("Helpful placeholder text") }
)
```

### Buttons

Use clear, action-oriented text:

```kotlin
Button(
    onClick = { /* action */ },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Add Alert Settings") // Clear action description
}
```

## Resources

- [Android Accessibility Guide](https://developer.android.com/guide/topics/ui/accessibility)
- [Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)
- [Material Design Accessibility](https://m3.material.io/foundations/accessible-design/overview)
- [TalkBack Testing Guide](https://support.google.com/accessibility/android/answer/6283677)

## Future Improvements

Potential enhancements for accessibility:

1. **Custom TalkBack gestures** for quick actions
2. **Voice commands** for adding alerts
3. **Haptic feedback** for important state changes
4. **Accessibility shortcuts** for power users
5. **Sound notifications** in addition to visual alerts
6. **High contrast mode** option

## Feedback

If you encounter any accessibility issues or have suggestions for improvements, please [open an issue](https://github.com/hossain-khan/android-weather-alert/issues) on GitHub.
