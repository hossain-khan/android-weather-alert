# Data Models
Contains common data models used by app and weather services.

```mermaid
flowchart TD
    A[data-model] -->|Used by| B(Forecast Services)
    A -->|Used by| C(Android App)
    B -->|✔️ On| D[OpenWeather]
    B -->|✔️ On| E[Tomorrow.io]
    B -->|❌ Off| F[OpenMeteo]
```