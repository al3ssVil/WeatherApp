package server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Server {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/vreme";
    private static final String USER = "postgres";
    private static final String PASS = "1q2w3e";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Serverul este pornit și ascultă pe portul 12345...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client conectat: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String userType = in.readLine();
                if (userType == null) return;

                boolean isLogged = true; // Flag pentru menținerea conexiunii deschise

                while (isLogged) {

                    if (userType.equalsIgnoreCase("admin")) {
                        handleAdminCommands(connection, in, out);
                    } else if (userType.equalsIgnoreCase("client")) {
                        handleCommonClient(connection, in, out);
                    } else break;
                }

            } catch (IOException e) {
                System.out.println("Eroare la citirea de la client: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Eroare la conectarea la baza de date: " + e.getMessage());
            }
        }

        private void handleAdminCommands(Connection connection, BufferedReader in, PrintWriter out) throws IOException {
            String command;
            while ((command = in.readLine()) != null) {
                if (command.equalsIgnoreCase("EXIT")) {  // Clientul a ales să părăsească
                    out.println("Conexiune închisă.");
                    break;  // Oprim bucla și închidem conexiunea
                }
                System.out.println("Comanda primită de la admin: " + command);
                switch (command.toUpperCase()) {
                    case "ADAUGA_ORAS":
                        String addCityData = in.readLine();
                        handleAddCity(connection, addCityData, out);
                        break;

                    case "ADAUGA_PROGNOZA":
                        String addForecastData = in.readLine();
                        handleAddForecast(connection, addForecastData, out);
                        break;

                    case "MODIFICA_ORAS":
                        String updateData = in.readLine();
                        handleUpdate(connection, updateData, out);
                        break;

                    case "MODIFICA_PROGNOZA":
                        String update = in.readLine();
                        handleUpdateForecast(connection, update, out);
                        break;

                    case "STERGE":
                        String deleteId = in.readLine();
                        handleDelete(connection, deleteId, out);
                        break;

                }
            }
        }

        private void handleAddCity(Connection connection, String data, PrintWriter out) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO orase VALUES (?, ?, ?, ?)")) {
                String[] details = data.split("\\s*,\\s*");
                stmt.setInt(1, Integer.parseInt(details[0]));
                stmt.setString(2, details[1]);
                stmt.setDouble(3, Double.parseDouble(details[2]));
                stmt.setDouble(4, Double.parseDouble(details[3]));
                stmt.executeUpdate();
                out.println("Orașul a fost adăugat cu succes.");
            } catch (SQLException e) {
                out.println("Eroare la adăugarea orașului: " + e.getMessage());
            }
        }

        private void handleAddForecast(Connection connection, String data, PrintWriter out) {
            try {
                String[] parts = data.split("\\s*,\\s*");

                // Afișăm datele primite pentru a le putea verifica
                System.out.println("Date primite de la client: ");
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("Partea " + (i + 1) + ": " + parts[i]);
                }

                // Verificăm că există cel puțin 5 elemente (ID-ul orașului + prognozele pentru 4 zile)
                if (parts.length <= 5) {
                    out.println("Date insuficiente pentru a adăuga prognoza. Verificați formatul.");
                    return;
                }

                int cityId = Integer.parseInt(parts[0]); // ID-ul orașului

                // Verificăm dacă ID-ul orașului există în baza de date
                try (PreparedStatement checkCityStmt = connection.prepareStatement(
                        "SELECT COUNT(*) FROM orase WHERE id = ?")) {
                    checkCityStmt.setInt(1, cityId);
                    ResultSet rs = checkCityStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        out.println("ID-ul orașului nu există. Vă rugăm să introduceți un ID valid.");
                        return;
                    }
                }

                // Pornim tranzacția
                connection.setAutoCommit(false);

                // Verificăm și extragem datele pentru fiecare zi
                String[][] forecasts = {
                        {"azi", parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "", parts.length > 3 ? parts[3] : "", parts.length > 4 ? parts[4] : ""},
                        {"maine", parts.length > 5 ? parts[5] : "", parts.length > 6 ? parts[6] : "", parts.length > 7 ? parts[7] : "", parts.length > 8 ? parts[8] : ""},
                        {"poimaine", parts.length > 9 ? parts[9] : "", parts.length > 10 ? parts[10] : "", parts.length > 11 ? parts[11] : "", parts.length > 12 ? parts[12] : ""},
                        {"raspoimaine", parts.length > 13 ? parts[13] : "", parts.length > 14 ? parts[14] : "", parts.length > 15 ? parts[15] : "", parts.length > 16 ? parts[16] : ""}
                };

                // Iterăm prin fiecare prognoză și completăm valorile
                for (int i = 0; i < 4; i++) {
                    String[] forecast = forecasts[i];
                    System.out.println("Prognoza pentru ziua: " + forecast[0] + " -> Temperatura: " + forecast[1] + ", Conditie meteo: " + forecast[2] + ", Viteza vantului: " + forecast[3] + ", Umiditate: " + forecast[4]);

                    try {
                        // Verificăm dacă există prognoză pentru această zi
                        if (!forecast[1].isEmpty() && !forecast[2].isEmpty() && !forecast[3].isEmpty() && !forecast[4].isEmpty()) {
                            // Înlocuim split-ul incorect cu cel corect
                            String[] details = new String[4];
                            details[0] = forecast[1]; // Temperatura
                            details[1] = forecast[2]; // Condiția meteo
                            details[2] = forecast[3]; // Viteza vântului
                            details[3] = forecast[4]; // Umiditatea

                            // Verificăm dacă detaliile sunt complete
                            if (details.length == 4) {
                                float temperatura = Float.parseFloat(details[0]); // Temperatura
                                String conditieMeteo = details[1]; // Condiția meteo
                                float vitezaVantului = Float.parseFloat(details[2]); // Viteza vântului
                                float umiditate = Float.parseFloat(details[3]); // Umiditatea

                                // Adăugăm prognoza în baza de date
                                try (PreparedStatement stmt = connection.prepareStatement(
                                        "INSERT INTO prognoza (id_oras, ziua, temperatura, viteza_vantului, umiditate, conditie_meteo) VALUES (?, ?, ?, ?, ?, ?)")) {

                                    stmt.setInt(1, cityId); // ID-ul orașului
                                    stmt.setString(2, forecast[0]); // Ziua
                                    stmt.setFloat(3, temperatura); // Temperatura
                                    stmt.setFloat(4, vitezaVantului); // Viteza vântului
                                    stmt.setFloat(5, umiditate); // Umiditatea
                                    stmt.setString(6, conditieMeteo); // Condiția meteo
                                    stmt.executeUpdate(); // Execută insertul pentru fiecare prognoză
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Eroare la conversia valorilor pentru ziua: " + forecast[0] + " - " + e.getMessage());
                        continue; // Continuăm cu următoarea zi în caz de eroare
                    }
                }

                // Confirmăm tranzacția (commit)
                connection.commit();
                out.println("Prognoza a fost adăugată cu succes pentru zilele disponibile.");

            } catch (SQLException e) {
                // Anulăm tranzacția în caz de eroare (rollback)
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Eroare la rollback: " + rollbackEx.getMessage());
                }
                out.println("Eroare la adăugarea prognozei: " + e.getMessage());
            } catch (Exception e) {
                out.println("Eroare: " + e.getMessage());
            } finally {
                try {
                    // Restabilim auto-commit-ul pentru conexiune
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("Eroare la resetarea auto-commit: " + e.getMessage());
                }
            }
        }

        private void handleUpdate(Connection connection, String data, PrintWriter out) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE orase SET nume = ?, latitudine = ?, longitudine = ? WHERE id = ?")) {

                String[] details = data.split("\\s*,\\s*");

                // Verificăm dacă există exact 4 parametri: id_oras, nume, latitudine, longitudine
                if (details.length != 4) {
                    out.println("Eroare: Date incorecte pentru actualizarea locației. Formatul corect: id, nume, latitudine, longitudine.");
                    return;
                }

                // Verificăm că ID-ul orașului este valid
                int cityId = Integer.parseInt(details[0]);
                String cityName = details[1];
                double latitude = Double.parseDouble(details[2]);
                double longitude = Double.parseDouble(details[3]);

                stmt.setString(1, cityName);        // Numele orașului
                stmt.setDouble(2, latitude);        // Latitudine
                stmt.setDouble(3, longitude);      // Longitudine
                stmt.setInt(4, cityId);            // ID-ul orașului

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    out.println("Locația a fost actualizată cu succes.");
                } else {
                    out.println("Locația nu a fost găsită pentru ID-ul dat.");
                }
            } catch (SQLException e) {
                out.println("Eroare la actualizarea locației: " + e.getMessage());
            } catch (NumberFormatException e) {
                out.println("Eroare: Valori incorecte pentru latitudine/longitudine. Asigurați-vă că sunt numere valide.");
            }
        }

        private void handleUpdateForecast(Connection connection, String data, PrintWriter out) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE prognoza SET ziua = ?, temperatura = ?, conditie_meteo = ?, umiditate = ?, viteza_vantului = ? WHERE id_oras = ? AND ziua = ?")) {

                String[] details = data.split("\\s*,\\s*");

                // Verificăm dacă există exact 7 parametri: id_oras, ziua, temperatura, conditie_meteo, umiditate, viteza_vantului
                if (details.length != 7) {
                    out.println("Eroare: Detalii incorecte pentru actualizarea prognozei. Formatul corect: id_oras, ziua, temperatura, conditie_meteo, umiditate, viteza_vantului, ziua_care_se_actualizeaza.");
                    return;
                }

                // Extragem valorile din parametrii
                int cityId = Integer.parseInt(details[0]);      // ID-ul orașului
                String day = details[1];                        // Ziua pentru prognoza (ex. "azi", "maine")
                float temperature = Float.parseFloat(details[2]); // Temperatura
                String weatherCondition = details[3];           // Condiția meteo
                float humidity = Float.parseFloat(details[4]);   // Umiditate
                float windSpeed = Float.parseFloat(details[5]);  // Viteza vântului
                String updateDay = details[6];                   // Ziua care se actualizează (de exemplu "azi", "maine")

                stmt.setString(1, day);                          // Ziua
                stmt.setFloat(2, temperature);                   // Temperatura
                stmt.setString(3, weatherCondition);             // Condiția meteo
                stmt.setFloat(4, humidity);                      // Umiditate
                stmt.setFloat(5, windSpeed);                     // Viteza vântului
                stmt.setInt(6, cityId);                          // ID-ul orașului
                stmt.setString(7, updateDay);                    // Ziua care se actualizează

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    out.println("Prognoza a fost actualizată cu succes.");
                } else {
                    out.println("Prognoza nu a fost găsită pentru această locație.");
                }
            } catch (SQLException e) {
                out.println("Eroare la actualizarea prognozei: " + e.getMessage());
            } catch (NumberFormatException e) {
                out.println("Eroare: Valori incorecte pentru temperatură, umiditate sau viteză vântului.");
            }
        }


        private void handleDelete(Connection connection, String id, PrintWriter out) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM orase WHERE id = ?")) {
                stmt.setInt(1, Integer.parseInt(id));
                int rowsDeleted = stmt.executeUpdate();
                out.println(rowsDeleted > 0 ? "Locația a fost ștearsă." : "Locația nu a fost găsită.");
            } catch (SQLException e) {
                out.println("Eroare la ștergerea locației: " + e.getMessage());
            }
        }

        private void handleCommonClient(Connection connection, BufferedReader in, PrintWriter out) throws IOException {
            String command;
            while ((command = in.readLine()) != null) {
                if (command.equalsIgnoreCase("EXIT")) {  // Clientul a ales să părăsească
                    out.println("Conexiune închisă.");
                    break;  // Oprim bucla și închidem conexiunea
                }
                System.out.println("Commanda primita de la client:" + command);
                if ("PRIMESTE_PROGNOZA".equalsIgnoreCase(command)) {
                    String latitude = in.readLine();
                    String longitude = in.readLine();
                    handleWeatherRequest(connection, latitude, longitude, out);
                }
            }
        }

        private void handleWeatherRequest (Connection connection, String latitude, String longitude, PrintWriter out) {
            final double MAX_DISTANCE_KM = 50.0; // Pragul maxim pentru distanța acceptabilă (50 km)

            try {
                // Verificăm dacă există un oraș exact cu coordonatele date
                try (PreparedStatement stmtExact = connection.prepareStatement(
                        "SELECT id, nume " +
                                "FROM orase WHERE latitudine = ? AND longitudine = ?")) {

                    stmtExact.setDouble(1, Double.parseDouble(latitude));  // Latitudinea clientului
                    stmtExact.setDouble(2, Double.parseDouble(longitude)); // Longitudinea clientului
                    System.out.println("Latitudine primită: " + latitude);
                    System.out.println("Longitudine primită: " + longitude);
                    ResultSet rsExact = stmtExact.executeQuery();

                    if (rsExact.next()) {
                        // Orașul exact există
                        int cityId = rsExact.getInt("id");
                        String cityName = rsExact.getString("nume");
                        out.println("Locația exactă găsită: " + cityName);

                        // Găsește prognoza pentru această locație
                        try (PreparedStatement weatherStmt = connection.prepareStatement(
                                "SELECT * FROM prognoza WHERE id_oras = ? AND ziua IN ('azi', 'maine', 'poimaine', 'raspoimaine') " +
                                        "ORDER BY CASE " +
                                        "WHEN ziua = 'azi' THEN 1 " +
                                        "WHEN ziua = 'maine' THEN 2 " +
                                        "WHEN ziua = 'poimaine' THEN 3 " +
                                        "WHEN ziua = 'raspoimaine' THEN 4 " +
                                        "ELSE 5 END")) {
                            weatherStmt.setInt(1, cityId);
                            ResultSet weatherRs = weatherStmt.executeQuery();

                            boolean hasWeatherData = false;

                            // Verifică dacă există date
                            if (!weatherRs.isBeforeFirst()) {
                                out.println("Nu există prognoză pentru această locație.");
                            } else {
                                while (weatherRs.next()) {
                                    hasWeatherData = true;
                                    // Afișează datele meteo pentru fiecare rând
                                    out.println("Ziua: " + weatherRs.getString("ziua")
                                            + ", Temperatura: " + weatherRs.getFloat("temperatura")
                                            + ", Viteza Vântului: " + weatherRs.getFloat("viteza_vantului")
                                            + ", Umiditate: " + weatherRs.getFloat("umiditate")
                                            + ", Condiție Meteo: " + weatherRs.getString("conditie_meteo"));
                                }
                            }

                            if (!hasWeatherData) {
                                out.println("Nu există informații meteo pentru această locație.");
                            }
                        }
                    } else {
                        // Dacă nu există oraș exact, căutăm cel mai apropiat oraș
                        try (PreparedStatement stmtClosest = connection.prepareStatement(
                                "SELECT id, nume, " +
                                        "(6371 * acos(cos(radians(?)) * cos(radians(latitudine)) * cos(radians(longitudine) - radians(?)) + sin(radians(?)) * sin(radians(latitudine)))) AS distanta " +
                                        "FROM orase " +
                                        "ORDER BY distanta LIMIT 1")) {

                            stmtClosest.setDouble(1, Double.parseDouble(latitude));  // Latitudinea clientului
                            stmtClosest.setDouble(2, Double.parseDouble(longitude)); // Longitudinea clientului
                            stmtClosest.setDouble(3, Double.parseDouble(latitude));
                            ResultSet rsClosest = stmtClosest.executeQuery();

                            if (rsClosest.next()) {
                                int cityId = rsClosest.getInt("id");
                                String cityName = rsClosest.getString("nume");
                                double distance = rsClosest.getDouble("distanta");

                                // Verificăm dacă distanța este mai mare decât pragul acceptabil
                                if (distance > MAX_DISTANCE_KM) {
                                    out.println("Nu există un oraș valid în apropierea locației specificate.");
                                } else {
                                    out.println("Locația cea mai apropiată: " + cityName + " la " + distance + " km.");

                                    // Găsește prognoza pentru această locație
                                    try (PreparedStatement weatherStmt = connection.prepareStatement(
                                            "SELECT * FROM prognoza WHERE id_oras = ? AND ziua IN ('azi', 'maine', 'poimaine', 'raspoimaine') " +
                                                    "ORDER BY CASE " +
                                                    "WHEN ziua = 'azi' THEN 1 " +
                                                    "WHEN ziua = 'maine' THEN 2 " +
                                                    "WHEN ziua = 'poimaine' THEN 3 " +
                                                    "WHEN ziua = 'raspoimaine' THEN 4 " +
                                                    "ELSE 5 END")) {
                                        weatherStmt.setInt(1, cityId);
                                        ResultSet weatherRs = weatherStmt.executeQuery();

                                        boolean hasWeatherData = false;
                                        while (weatherRs.next()) {
                                            hasWeatherData = true;
                                            out.println("Ziua: " + weatherRs.getString("ziua")
                                                    + ", Temperatura: " + weatherRs.getFloat("temperatura")
                                                    + ", Viteza Vântului: " + weatherRs.getFloat("viteza_vantului")
                                                    + ", Umiditate: " + weatherRs.getFloat("umiditate")
                                                    + ", Condiție Meteo: " + weatherRs.getString("conditie_meteo"));
                                        }
                                        if (!hasWeatherData) {
                                            out.println("Nu există informații meteo pentru această locație.");
                                        }
                                    }
                                }
                            } else {
                                out.println("Nu există un oraș valid în apropierea locației specificate.");
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                out.println("Eroare la găsirea locației: " + e.getMessage());
            }
            out.println("null");
        }
    }
}
