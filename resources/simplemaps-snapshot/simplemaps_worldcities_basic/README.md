## How to recreate bundeld DB

Use the SQLite Command-Line Interface (CLI) to import the CSV data directly into a SQLite database. Here’s how you can do it:

---

### Steps to Import CSV Data Using SQLite CLI

1. **Prepare Your Environment**
   - Ensure SQLite is installed on your system. You can check by running:
     ```bash
     sqlite3 --version
     ```
   - If not installed, you can download it from the [SQLite official website](https://www.sqlite.org/download.html) or use your system's package manager (`apt`, `yum`, `brew`, etc.).

2. **Create a SQLite Database File**
   - Create or open a SQLite database file:
     ```bash
     sqlite3 cities.db
     ```

3. **Create the Table**
   - Define the table to match the structure of your CSV file. For example:
     ```sql
     CREATE TABLE IF NOT EXISTS `cities` (
        `city` TEXT NOT NULL,
        `city_ascii` TEXT NOT NULL,
        `lat` REAL NOT NULL,
        `lng` REAL NOT NULL,
        `country` TEXT NOT NULL,
        `iso2` TEXT NOT NULL,
        `iso3` TEXT NOT NULL,
        `admin_name` TEXT,
        `capital` TEXT,
        `population` INTEGER,
        `id` INTEGER NOT NULL, PRIMARY KEY(`id`)
     );
     ```
     Exit the SQLite prompt by typing `.quit` or continue with the next step.

4. **Import the CSV File**
   - Use the `.import` command to load the CSV file into the table:
     ```bash
     sqlite3 cities.db
     ```
     Within the SQLite prompt, run:
     ```sql
     .mode csv
     .import /path/to/your/file.csv cities
     ```
   - Replace `/path/to/your/file.csv` with the path to your actual CSV file, and `cities` with the table name.

5. **Verify the Data**
   - After importing, verify the data by running:
     ```sql
     SELECT * FROM cities LIMIT 10;
     ```

6. **Index the Table for Faster Queries (Optional)**
   - As discussed earlier, create indexes for efficient searching:
     ```sql
     CREATE INDEX idx_city ON cities(city);
     CREATE INDEX idx_city_ascii ON cities(city_ascii);
     ```

---

### Notes and Tips:
- **Header Handling**: If your CSV file contains a header row (like in the example), SQLite CLI will treat it as data. To exclude it, remove the header from the CSV file or skip the first row in SQL queries.
  - Alternatively, if you're using a recent SQLite version (3.37.0 or later), you can use:
    ```sql
    .import --csv --skip 1 /path/to/your/file.csv cities
    ```

- **Data Types**: Ensure the table schema matches the CSV file's column data types to prevent errors during import.

- **Escaped Characters**: If the CSV contains special characters like quotes or commas, make sure they are properly escaped.

---

This process allows you to quickly load CSV data into SQLite using the command line!
