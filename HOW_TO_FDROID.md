# How to Submit Weather Alert to F-Droid

This document provides step-by-step instructions on how to submit the Weather Alert Android app to the F-Droid app store.

## Prerequisites

- [ ] Ensure the app is fully open-source and complies with F-Droid's [inclusion criteria](https://f-droid.org/docs/Inclusion_Policy/)
- [ ] Verify all dependencies are free and open-source (FOSS)
- [ ] Test the F-Droid build variant locally
- [ ] Prepare app metadata and screenshots

## F-Droid Compatibility Changes Made

The following changes have been made to ensure F-Droid compatibility:

### 1. Added F-Droid Build Variant
- Created `fdroid` product flavor in `app/build.gradle.kts`
- Disabled Firebase services for F-Droid builds using `FIREBASE_ENABLED = false`
- Added application ID suffix `.fdroid` to avoid conflicts

### 2. Removed Proprietary Dependencies
- Firebase dependencies are only included for `internal` and `prod` flavors
- F-Droid builds exclude:
  - Firebase Analytics (`firebase-analytics`)
  - Firebase Crashlytics (`firebase-crashlytics`)
  - Google Services plugin

### 3. Created No-Op Implementations
- `NoOpAnalytics`: Replaces Firebase Analytics for F-Droid builds
- `NoOpCrashlyticsTree`: Replaces Crashlytics logging for F-Droid builds
- F-Droid specific source sets in `app/src/fdroid/`

### 4. Conditional Code Loading
- App conditionally loads Firebase services based on `BuildConfig.FIREBASE_ENABLED`
- Graceful fallback to no-op implementations for F-Droid builds

## Build Instructions for F-Droid

### Local Testing
To test the F-Droid build locally:

```bash
# Clean previous builds
./gradlew clean

# Build F-Droid release APK
./gradlew assembleFdroidRelease

# Generated APK location:
# app/build/outputs/apk/fdroid/release/app-fdroid-release.apk
```

### Verify F-Droid Compatibility
```bash
# Check for proprietary dependencies
./gradlew fdroidReleaseDebugDependencies | grep -i -E "(firebase|google|gms)"

# Should return no results for F-Droid build

# Note: Firebase plugins may show warnings but can be ignored 
# as F-Droid flavor excludes all Firebase dependencies
```

## F-Droid Submission Process

### Step 1: Fork F-Droid Data Repository

1. Go to [F-Droid Data repository](https://gitlab.com/fdroid/fdroiddata)
2. Fork the repository to your GitLab account
3. Clone your fork locally:
   ```bash
   git clone https://gitlab.com/YOUR_USERNAME/fdroiddata.git
   cd fdroiddata
   ```

### Step 2: Create App Metadata File

1. Create a new metadata file for Weather Alert:
   ```bash
   mkdir -p metadata/dev.hossain.weatheralert.fdroid
   ```

2. Create the metadata file `metadata/dev.hossain.weatheralert.fdroid.yml`:
   ```yaml
   Categories:
     - Internet
     - Science & Education
   License: MIT
   AuthorName: [YOUR_NAME_HERE]
   AuthorEmail: [YOUR_EMAIL_HERE]
   WebSite: https://github.com/hossain-khan/android-weather-alert
   SourceCode: https://github.com/hossain-khan/android-weather-alert
   IssueTracker: https://github.com/hossain-khan/android-weather-alert/issues
   Changelog: https://github.com/hossain-khan/android-weather-alert/releases

   AutoName: Weather Alert
   Summary: Custom weather alerts for specific conditions

   Description: |-
     Weather Alert is a simple, focused Android app that helps you prepare for specific weather conditions like snow and rain. Instead of overwhelming you with every weather detail, this app delivers clear, actionable notifications when conditions meet your configured thresholds.

     '''Features:'''
     * Custom weather alerts for snowfall and rainfall with user-defined thresholds
     * Multiple weather data sources (OpenWeatherMap, Tomorrow.io, OpenMeteo, WeatherAPI)
     * Configurable alert frequency (6, 12, or 18 hours)
     * Rich notifications with actionable information
     * Minimalist tile-based UI design
     * Background processing with WorkManager

     This F-Droid version is built without proprietary dependencies, ensuring privacy and open-source compliance.

     '''Note:''' The app requires API keys from weather service providers. Free tier API keys are available from all supported services.

   RepoType: git
   Repo: https://github.com/hossain-khan/android-weather-alert.git

   Builds:
     - versionName: 2.9
       versionCode: 18
       commit: [COMMIT_HASH_HERE]
       subdir: app
       sudo:
         - apt-get update
         - apt-get install -y openjdk-17-jdk-headless
         - update-java-alternatives -a
       gradle:
         - fdroid
       prebuild: echo 'OPEN_WEATHER_API_KEY=dummy' >> ../local.properties && echo 'TOMORROW_IO_API_KEY=dummy' >> ../local.properties && echo 'WEATHERAPI_API_KEY=dummy' >> ../local.properties

   AutoUpdateMode: Version
   UpdateCheckMode: Tags
   CurrentVersion: 2.9
   CurrentVersionCode: 18
   ```

### Step 3: Add Required Files

**Important:** You need to fill in the following placeholders:

- `[YOUR_NAME_HERE]`: Replace with the app author's name
- `[YOUR_EMAIL_HERE]`: Replace with the author's contact email
- `[COMMIT_HASH_HERE]`: Replace with the specific commit hash for version 2.9

To get the commit hash:
```bash
git log --oneline | head -1
# Copy the commit hash (first 7-8 characters)
```

### Step 4: Test the Build

Test the F-Droid build locally using fdroidserver:

```bash
# Install fdroidserver (requires Python)
pip3 install fdroidserver

# Test build the app
fdroid build dev.hossain.weatheralert.fdroid:18
```

### Step 5: Submit the Request

1. Commit your changes:
   ```bash
   git add metadata/dev.hossain.weatheralert.fdroid.yml
   git commit -m "Add Weather Alert - weather notifications app"
   git push origin master
   ```

2. Create a merge request on GitLab:
   - Go to your fork on GitLab
   - Click "Create merge request"
   - Target the original fdroiddata repository
   - Title: "Add Weather Alert"
   - Description: Brief description of the app and why it should be included

### Step 6: Respond to Review Feedback

F-Droid maintainers will review your submission and may request changes:

- Check for proper FOSS compliance
- Verify build reproducibility
- Test on different Android versions
- Review app functionality and metadata

Be prepared to:
- Update metadata based on feedback
- Fix any build issues
- Provide additional documentation if needed

## Post-Submission Maintenance

### Automatic Updates
- F-Droid will automatically detect new releases based on Git tags
- Ensure new releases maintain F-Droid compatibility
- Always test F-Droid builds before creating releases

### Manual Updates
If automatic updates fail:
1. Update the metadata file with new version information
2. Submit another merge request with the changes

## Troubleshooting Common Issues

### Build Failures
1. **Missing API Keys**: The build includes dummy API keys for compilation
2. **Firebase Dependencies**: Ensure F-Droid flavor excludes all proprietary dependencies
3. **Java Version**: F-Droid uses OpenJDK 17, ensure compatibility
4. **Android Gradle Plugin**: Ensure the AGP version is compatible with F-Droid build environment

### Metadata Issues
1. **License**: Ensure the license is properly specified and compatible
2. **Categories**: Use appropriate F-Droid categories
3. **Description**: Follow F-Droid formatting guidelines

### Testing F-Droid Build
```bash
# Install F-Droid APK on device
adb install app/build/outputs/apk/fdroid/release/app-fdroid-release.apk

# Verify no proprietary services are called
adb logcat | grep -i -E "(firebase|google|gms)"
```

## Important Notes

1. **API Keys**: The app requires API keys from weather services. Users will need to obtain these from:
   - OpenWeatherMap: https://openweathermap.org/api
   - Tomorrow.io: https://www.tomorrow.io/
   - WeatherAPI: https://www.weatherapi.com/
   - OpenMeteo: No API key required

2. **Privacy**: F-Droid build excludes all tracking and analytics

3. **Network Usage**: App only makes network requests to configured weather services

4. **Permissions**: App requires `INTERNET`, `ACCESS_NETWORK_STATE`, and `POST_NOTIFICATIONS`

## Resources

- [F-Droid Inclusion Policy](https://f-droid.org/docs/Inclusion_Policy/)
- [F-Droid Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/)
- [F-Droid Submitting Apps](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/)
- [Weather Alert Source Code](https://github.com/hossain-khan/android-weather-alert)

## Checklist for Submission

- [ ] Fill in author name and email in metadata
- [ ] Get the specific commit hash for the current version
- [ ] Test F-Droid build locally
- [ ] Verify no proprietary dependencies in F-Droid build
- [ ] Create F-Droid Data repository fork
- [ ] Create metadata file with correct information
- [ ] Test build with fdroidserver
- [ ] Submit merge request
- [ ] Respond to review feedback
- [ ] Monitor for approval and publication

Once approved, Weather Alert will be available in the F-Droid store and will receive automatic updates for future releases!