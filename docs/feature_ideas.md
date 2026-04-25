# Feature Ideas

## 1. Support for More Weather Conditions

**Current Functionality:**

The application currently provides weather alerts specifically for:

*   Snowfall
*   Rainfall

**Proposal:**

We propose extending the application's capabilities to support a wider range of weather conditions. This would allow users to receive alerts and information for conditions such as:

*   **Wind Speed:** Alerts for high wind speeds, which can be crucial for safety and planning outdoor activities.
*   **UV Index:** Notifications for high UV index levels, helping users take precautions against sun exposure.
*   **Air Quality Index (AQI):** Alerts for poor air quality, which is increasingly important for health-conscious users.
*   Other potential conditions: Fog, hail, thunderstorms, extreme temperatures (high/low).

**Impact:**

Implementing this feature would require modifications across several parts of the application:

*   **Data Model:** The existing data model would need to be updated to accommodate new data points for these additional weather conditions. This might involve adding new fields to existing tables or creating new tables to store this information.
*   **User Interface (UI):** The UI would need to be redesigned to:
    *   Allow users to select and configure alerts for these new conditions.
    *   Display information related to these conditions in a clear and intuitive manner.
*   **Alert Logic:** The backend alert generation logic would need to be significantly updated to process these new conditions and trigger alerts accordingly. This includes fetching new data sources if required and implementing the rules for each new alert type.

This enhancement would significantly increase the value proposition of our weather application, making it a more comprehensive and indispensable tool for users.

## 2. Location-Based Alerts

**Current Functionality:**

Currently, users need to manually input and manage the locations for which they want to receive weather alerts. The application does not automatically detect the user's current location.

**Proposal:**

We propose implementing location-based alerts. This feature would enable the application to:

*   Optionally, use the device's GPS or other location services (with user permission) to determine the user's current geographical location.
*   Provide timely and relevant weather alerts for this automatically detected current location.
*   Users could still have the option to add and receive alerts for manually specified locations in addition to their current location.

**Impact:**

Adding this feature will involve several key changes:

*   **Permissions:** The application will need to request location permissions from the user (e.g., on Android and iOS).
*   **Location Services Integration:** We will need to integrate with the device's native location services to fetch the current location reliably and efficiently (considering battery life).
*   **UI/UX Changes:**
    *   Provide clear options for users to enable/disable location-based alerts.
    *   Display information indicating that alerts are for the current location.
*   **Backend Logic:** The logic for fetching and processing weather data will need to be adapted to handle dynamically changing locations. This includes potentially more frequent API calls to weather services for the current location.
*   **Privacy Considerations:** We must clearly communicate how location data is used and ensure user privacy is protected.

Location-based alerts would make the application significantly more convenient and contextually aware, providing users with immediate weather information relevant to their surroundings without manual input.

## 3. ✅ Snooze/Dismiss Alert Functionality *(Implemented)*

**Status:** This feature has been implemented as of v2.10.

**Implemented Functionality:**

Users can snooze active weather alert notifications directly from the notification:

*   **Snooze 1 day**: Tap the "Snooze 1 day" action button in the notification to suppress it for 24 hours.
*   **Snooze 1 week**: Tap the "Snooze 1 week" action button in the notification to suppress it for 7 days.

The snooze status is visible in the Alert Details screen, where users can also clear an active snooze using the "Clear Snooze" button. Snoozed alerts are skipped during background weather checks until the snooze period expires.
