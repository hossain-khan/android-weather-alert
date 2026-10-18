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
Pick between **OpenWeatherMap**, **Tomorrow.io**, **OpenMeteo**, and **WeatherAPI** for reliable and accurate forecasts.  

🔑 **Add Your Own API Key:**  
Ensure uninterrupted service by using your own API key for weather data.  

⏰ **Set Your Alert Frequency:**  
Control how often the app checks for weather updates—every 6, 12, or 18 hours—so you get timely notifications without unnecessary checks.  

📲 **Rich Notifications:**  
Receive simple, clear notifications with all the details you need to take action.  

🛠 **Minimalist Interface:**  
Easily configure and view your alerts in a simple, intuitive tile-based design.  

♿ **Accessible Design:**  
Full TalkBack screen reader support with proper content descriptions and semantic properties for visually impaired users. See [Accessibility Documentation](docs/ACCESSIBILITY.md) for details.

**Why Choose Weather Alert?**  
- Focused on delivering only what you need: alerts that meet your criteria.  
- Lightweight and efficient, with no unnecessary extras.  
- Built for people who want actionable weather notifications, hassle-free.  
- Designed with accessibility in mind for all users.  
</details>

Try out **Weather Alert** today and let the app do the work for you!  

## Developer Portal 🔧

For developers and QA engineers, the app includes a comprehensive **Developer Portal** available exclusively in internal debug builds. This portal provides powerful testing and debugging tools to accelerate development and testing workflows.

**Available Tools:**
- 🔔 **Notification Tester** - Test alert notifications without real weather
- ⚙️ **WorkManager Tester** - Manually trigger background workers
- 📋 **Alert Simulator** - Create test alerts with quick presets
- 📜 **History Simulator** - Generate historical alert data
- 🗄️ **Database Inspector** - View and export database state
- ⚙️ **State Management** - Reset preferences and app state

**Access**: Navigate to About Screen → "🔧 Developer Portal" (internal builds only)

**Documentation**: See [`docs/developer-portal.md`](docs/developer-portal.md) for comprehensive guide including:
- How to access and use each tool
- Testing workflows and best practices
- Troubleshooting common issues
- Architecture and code patterns

**Note**: The Developer Portal is **not included** in production builds and is only available in `internalDebug` build variants.

## AI Automation 🤖

This repository uses [JetBrains Junie](https://github.com/JetBrains/junie-github-action) powered by OpenRouter for automated PR code reviews and interactive issue/PR assistance. See [`docs/JUNIE_AUTOMATION.md`](docs/JUNIE_AUTOMATION.md) for full setup and usage details.

## Tech Stack 📱
Simple application generated from Android App template that uses:
* Jetpack Compose
* ⚡️ Circuit - UDF Architecture
* Jetpack libraries like - Room, Datastore, Material 3, and so on
* 🚇 Metro for dependency injection
* ... and few more. See [`libs.versions.toml`](https://github.com/hossain-khan/android-weather-alert/blob/main/gradle/libs.versions.toml) to get more idea.

Here is a diagram of Gradle modules and architecture for this app.

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'primaryColor': '#E3F2FD',
    'primaryTextColor': '#1565C0',
    'primaryBorderColor': '#1976D2',
    'lineColor': '#42A5F5',
    'secondaryColor': '#FFF3E0',
    'tertiaryColor': '#E8F5E9',
    'background': '#FFFFFF',
    'mainBkg': '#FFFFFF',
    'secondBkg': '#FFFFFF',
    'tertiaryBkg': '#FFFFFF'
  }
}}%%
flowchart TB
    subgraph ExternalAPIs["🌐 External Weather APIs"]
        direction LR
        API_OW[("OpenWeatherMap")]
        API_TI[("Tomorrow.io")]
        API_OM[("Open-Meteo")]
        API_WA[("WeatherAPI")]
    end

    subgraph ServiceModule[":service - Weather Service Modules"]
        direction LR
        Service_OW[":openweather<br/>📦"]
        Service_TI[":tomorrowio<br/>📦"]
        Service_OM[":openmeteo<br/>📦"]
        Service_WA[":weatherapi<br/>📦"]
    end

    subgraph DataModel[":data-model"]
        DTO["Shared DTOs &<br/>Data Classes<br/>📋"]
    end

    subgraph AppModule[":app - Main Application"]
        direction TB
        subgraph UI["UI Layer"]
            Compose["Jetpack Compose<br/>Material 3<br/>🎨"]
            Circuit["Circuit UDF<br/>Presenters<br/>⚡"]
        end
        subgraph Data["Data Layer"]
            Room["Room DB<br/>🗄️"]
            DataStore["DataStore<br/>Preferences<br/>⚙️"]
            Repo["Weather<br/>Repository<br/>📊"]
        end
        subgraph Background["Background Processing"]
            WorkManager["WorkManager<br/>⏰"]
            Notifications["Notifications<br/>🔔"]
        end
        subgraph DI["Dependency Injection"]
            Metro["Metro DI<br/>🚇"]
        end
    end

    %% External API connections
    API_OW -.->|"HTTP/REST"| Service_OW
    API_TI -.->|"HTTP/REST"| Service_TI
    API_OM -.->|"HTTP/REST"| Service_OM
    API_WA -.->|"HTTP/REST"| Service_WA

    %% Module dependencies
    DTO -->|"provides models"| Service_OW
    DTO -->|"provides models"| Service_TI
    DTO -->|"provides models"| Service_OM
    DTO -->|"provides models"| Service_WA
    DTO -->|"provides models"| AppModule

    Service_OW -->|"forecast data"| Repo
    Service_TI -->|"forecast data"| Repo
    Service_OM -->|"forecast data"| Repo
    Service_WA -->|"forecast data"| Repo

    %% Internal app connections
    Repo --> WorkManager
    WorkManager --> Notifications
    Room --> Repo
    DataStore --> Repo
    Circuit --> Compose
    Metro -.->|"injects"| UI
    Metro -.->|"injects"| Data
    Metro -.->|"injects"| Background

    %% Styling
    classDef external fill:#FFECB3,stroke:#FF8F00,stroke-width:2px
    classDef service fill:#E3F2FD,stroke:#1976D2,stroke-width:2px
    classDef data fill:#E8F5E9,stroke:#388E3C,stroke-width:2px
    classDef ui fill:#FCE4EC,stroke:#C2185B,stroke-width:2px
    classDef background fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px
    classDef di fill:#ECEFF1,stroke:#546E7A,stroke-width:2px

    %% Apply styling classes
    class API_OW external
    class API_TI external
    class API_OM external
    class API_WA external

    class Service_OW service
    class Service_TI service
    class Service_OM service
    class Service_WA service
    class DTO service

    class Room data
    class DataStore data
    class Repo data

    class Compose ui
    class Circuit ui

    class WorkManager background
    class Notifications background

    class Metro di
```
