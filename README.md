# Weather Forecast Client

## Overview
This Java application acts as a client for a weather forecast system, allowing users to connect to a server via sockets. The application supports two types of users: **admin** and **client**.

- **Admin** can manage cities and weather forecasts (add, modify, or delete data).
- **Client** can request weather forecasts based on latitude and longitude.

## Features
### Admin Functionalities:
- **Add a city** (`adauga_oras`): Provides city details including ID, name, latitude, and longitude.
- **Add a forecast** (`adauga_prognoza`): Adds weather forecasts for four days for a specific city.
- **Modify city details** (`modifica_oras`): Updates an existing city's details.
- **Modify a forecast** (`modifica_prognoza`): Updates a weather forecast for a specific city and day.
- **Delete a city** (`sterge`): Removes a city and its associated forecasts.
- **Exit** (`exit`): Closes the admin session.

### Client Functionalities:
- **Request a weather forecast** (`primeste_prognoza`): Retrieves weather information based on geographical coordinates.
- **Exit** (`exit`): Closes the client session.

## Usage
1. Run the Java client.
2. Enter the user type (`admin` or `client`).
3. Follow the command prompts to interact with the server.

## Database Setup
This application requires **PostgreSQL** to be installed and a database to be created following the required structure. Ensure you have PostgreSQL installed and set up the database accordingly before running the application.

### Steps to Set Up the Database
1. Install PostgreSQL and start the database server.
2. Open the PostgreSQL command-line tool or a database management tool (e.g., pgAdmin).
3. Create a new database:
   ```sql
   CREATE DATABASE vreme;
   ```
4. Connect to the newly created database:
   ```sql
   \\c vreme;
   ```
5. Create the required tables:
   ```sql
   CREATE TABLE cities (
       id SERIAL PRIMARY KEY,
       nume VARCHAR(100) NOT NULL,
       latitudine DOUBLE PRECISION NOT NULL,
       longitudine DOUBLE PRECISION NOT NULL
   );

   CREATE TABLE forecasts (
       id SERIAL PRIMARY KEY,
       id_oras INT REFERENCES cities(id) ON DELETE CASCADE,
       ziua VARCHAR(50) NOT NULL,
       temperatura DOUBLE PRECISION NOT NULL,
       viteza_vantului DOUBLE PRECISION NOT NULL,
       umiditate INT NOT NULL,
       conditie_meteo VARCHAR(50) NOT NULL,
   );
   ```
6. Ensure that the database is properly configured and accessible from the Java application.

## Validations
- **City details validation**: Ensures the correct format for city ID, latitude, and longitude.
- **Weather forecast validation**: Checks temperature, wind speed, and humidity values.
- **Geographical coordinates validation**: Ensures latitude and longitude are within valid ranges.

## Requirements
- Java 8 or higher
- PostgreSQL installed and configured, user `postgres` password `1q2w3e`, or you can change it in Server.jave file:
```code
private static final String DB_URL = "jdbc:postgresql://localhost:5432/vreme";
    private static final String USER = "postgres";// your user
    private static final String PASS = "1q2w3e";//your password
```
- A running server listening on `localhost:12345`
  

## Error Handling
- Handles socket connection issues gracefully.
- Validates input before sending it to the server.
- Displays appropriate error messages for invalid commands or incorrect data formats.
