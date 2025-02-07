[![Android CI](https://github.com/hossain-khan/android-weather-alert/actions/workflows/android.yml/badge.svg)](https://github.com/hossain-khan/android-weather-alert/actions/workflows/android.yml) [![Post Merge Check](https://github.com/hossain-khan/android-weather-alert/actions/workflows/android-lint.yml/badge.svg)](https://github.com/hossain-khan/android-weather-alert/actions/workflows/android-lint.yml) [![codecov](https://codecov.io/gh/hossain-khan/android-weather-alert/graph/badge.svg?token=09IAE88BBC)](https://codecov.io/gh/hossain-khan/android-weather-alert)

# Android - Weather Alert
A simple app to alert you about specific weather condition based on your configured threshold.

![](resources/banner-art/app-screenshot-array.png)

> [!NOTE]  
> _The app has been co-created with the help of GitHub Copilot, ChatGPT and Google Gemini._ 🤖


<img width="300" align="right" src="https://github.com/user-attachments/assets/79dc8278-9e12-4325-a16d-0e2ab89b3e3a" alt="Weather Alert Banner Image">

This app helps you to prepare for weather conditions like snow and rain, for example if there would be heavy snow ❄️ 
this will allow you charge your snow blower batteries, put car in the garage, and other related activities.

> _This app is a result from personal need to have focused alert compared to all existing apps available in the Play Store._


<a href="https://play.google.com/store/apps/details?id=dev.hossain.weatheralert&pcampaignid=web_share" target="_blank"><img src="resources/google-play/GetItOnGooglePlay_Badge_Web_color_English.png" height="45"></a>

### **App Summary**  
**Set it and forget it — get alerted when it matters!**  

Weather Alert is a simple, no-frills app designed to notify you when specific weather conditions meet the thresholds you set. Whether it’s a snowy driveway or a rainy day, stay ahead with timely alerts that help you prepare with ease.  

<details>
<summary>🎨 Key App Features</summary>

🌨 **Custom Alerts for Specific Weather Conditions:**  
Set thresholds for snowfall or rainfall (e.g., 5cm snow, 10mm rain) and receive notifications only when they’re met.  

🌐 **Choose Your Data Source:**  
Pick between **OpenWeatherMap** and **Tomorrow.io** for reliable and accurate forecasts.  

🔑 **Add Your Own API Key:**  
Ensure uninterrupted service by using your own API key for weather data.  

⏰ **Set Your Alert Frequency:**  
Control how often the app checks for weather updates—every 6, 12, or 18 hours—so you get timely notifications without unnecessary checks.  

📲 **Rich Notifications:**  
Receive simple, clear notifications with all the details you need to take action.  

🛠 **Minimalist Interface:**  
Easily configure and view your alerts in a simple, intuitive tile-based design.  

**Why Choose Weather Alert?**  
- Focused on delivering only what you need: alerts that meet your criteria.  
- Lightweight and efficient, with no unnecessary extras.  
- Built for people who want actionable weather notifications, hassle-free.  
</details>

Try out **Weather Alert** today and let the app do the work for you!  

## Tech Stack 📱
Simple application generated from Android App template that uses:
* Jetpack Compose
* ⚡️ Circuit - UDF Architecture
* Jetpack libraries like - Room, Datastore, Material 3, and so on
* Dagger + Anvil
* ... and few more. See [`libs.versions.toml`](https://github.com/hossain-khan/android-weather-alert/blob/main/gradle/libs.versions.toml) to get more idea.

Here is simple diagram of Gradle modules for this app.

```mermaid
flowchart TB
    subgraph Service[:service - Weather Forecast APIs]
        direction LR  'Layout within the subgraph
        Service_OW[:openweather]
        Service_TI[:tomorrowio]
        Service_OM[:openmeteo]
        Service_WA[:weatherapi]
    end

    DTO[:data-model]
    App[:app]

    DTO --> Service
    DTO ---> App
    Service ---> App
```
