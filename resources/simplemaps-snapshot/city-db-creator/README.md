## City Data Merger
This project is a simple data merger that takes in data from three different sources and combines them into a single data source. 

### How to run
You need the base DB with world cities data first. And then run following kotlin app to combine the data from USA and Canada cities data sources.
- [CityDbBuilder.kt](src/main/kotlin/CityDbBuilder.kt)

The data sources are:
* 🌐 World Cities - https://simplemaps.com/data/world-cities
* 🇺🇸 USA Cities - https://simplemaps.com/data/us-cities
* 🇨🇦 Canada Cities - https://simplemaps.com/data/canada-cities

### Merge Result 📈
* Original world cities: **`47,868`**
* Merged cities records: **`76,984`**
* Difference: **`29,116`** new cities added (60.83% more than original)
