# Weather Forecast Client

## Overview
This Java application acts as a client for a weather forecast system, allowing users to connect to a server via sockets. The application supports two types of users: **admin** and **client**.

- **Admins** can manage cities and weather forecasts (add, modify, or delete data).
- **Clients** can request weather forecasts based on latitude and longitude.

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
# Weather Forecast Client

## Overview
This Java application acts as a client for a weather forecast system, allowing users to connect to a server via sockets. The application supports two types of users: **admin** and **client**.

- **Admins** can manage cities and weather forecasts (add, modify, or delete data).
- **Clients** can request weather forecasts based on latitude and longitude.

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
   CREATE DATABASE weather_forecast;
   ```
4. Connect to the newly created database:
   ```sql
   \\c weather_forecast;
   ```
5. Create the required tables:
   ```sql
   CREATE TABLE cities (
       id SERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       latitude DOUBLE PRECISION NOT NULL,
       longitude DOUBLE PRECISION NOT NULL
   );

   CREATE TABLE forecasts (
       id SERIAL PRIMARY KEY,
       city_id INT REFERENCES cities(id) ON DELETE CASCADE,
       date DATE NOT NULL,
       temperature DOUBLE PRECISION NOT NULL,
       weather_condition VARCHAR(50) NOT NULL,
       wind_speed DOUBLE PRECISION NOT NULL,
       humidity INT NOT NULL
   );
   ```
6. Ensure that the database is properly configured and accessible from the Java application.

## Validations
- **City details validation**: Ensures the correct format for city ID, latitude, and longitude.
- **Weather forecast validation**: Checks temperature, wind speed, and humidity values.
- **Geographical coordinates validation**: Ensures latitude and longitude are within valid ranges.

## Requirements
- Java 8 or higher
- PostgreSQL installed and configured
- A running server listening on `localhost:12345`

## Error Handling
- Handles socket connection issues gracefully.
- Validates input before sending it to the server.
- Displays appropriate error messages for invalid commands or incorrect data formats.
