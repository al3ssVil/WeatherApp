package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Scanner scanner = new Scanner(System.in);

            System.out.println("Introduceți tipul de utilizator (admin/client): ");
            String userType = scanner.nextLine();
            out.println(userType); // Trimite tipul de utilizator la server

            if (userType.equalsIgnoreCase("admin")) {
                boolean running = true;
                while (running) {
                    System.out.println("Comenzi disponibile:adauga_oras, adauga_prognoza, modifica_oras, modifica_prognoza, sterge, exit");
                    String command = scanner.nextLine();
                    out.println(command);
                    out.flush();
                    switch (command.toUpperCase()) {
                        case "ADAUGA_ORAS":
                            System.out.println("Introduceți detaliile orașului (id, nume, latitudine, longitudine):");
                            String cityDetails = scanner.nextLine();
                            if (validateCityDetails(cityDetails)) {
                                out.println(cityDetails);
                                System.out.println("Server: " + in.readLine());
                            } else {
                                System.out.println("Detalii oraș invalid.");
                            }
                            break;

                        case "ADAUGA_PROGNOZA":
                            System.out.println("Introduceți id-ul orașului:");
                            String cityId = scanner.nextLine();

                            System.out.println("Introduceți prognoza pentru azi (temperatura, stare vreme, viteză vânt, umiditate):");
                            String forecastToday = scanner.nextLine();

                            System.out.println("Introduceți prognoza pentru mâine (temperatura, stare vreme, viteză vânt, umiditate):");
                            String forecastTomorrow = scanner.nextLine();

                            System.out.println("Introduceți prognoza pentru poimâine (temperatura, stare vreme, viteză vânt, umiditate):");
                            String forecastDayAfterTomorrow = scanner.nextLine();

                            System.out.println("Introduceți prognoza pentru răspoimâine (temperatura, stare vreme, viteză vânt, umiditate):");
                            String forecastTwoDaysAfterTomorrow = scanner.nextLine();

                            if (validateForecastData(forecastToday, forecastTomorrow, forecastDayAfterTomorrow, forecastTwoDaysAfterTomorrow)) {
                                out.println(cityId + ", " + forecastToday + ", " + forecastTomorrow + ", " + forecastDayAfterTomorrow + ", " + forecastTwoDaysAfterTomorrow);
                                System.out.println("Server: " + in.readLine());
                            } else {
                                System.out.println("Date prognoză invalide.");
                            }
                            break;

                        case "MODIFICA_ORAS":
                            System.out.println("Introduceți id-ul locației de modificat și noile detalii (nume, latitudine, longitudine):");
                            String updateDetails = scanner.nextLine();
                            if (validateCityDetails(updateDetails)) {
                                out.println(updateDetails);
                                System.out.println("Server: " + in.readLine());
                            } else {
                                System.out.println("Detalii oraș invalid.");
                            }
                            break;

                        case "MODIFICA_PROGNOZA":
                            System.out.println("Introduceți id-ul locației, ziua, temperatura, conditia meteo, umiditatea, viteza vântului, ziua_care_se_actualizeaza:");
                            String updateForecastDetails = scanner.nextLine();
                            if (validateForecastData2(updateForecastDetails)) {
                                out.println(updateForecastDetails);
                                System.out.println("Server: " + in.readLine());
                            } else {
                                System.out.println("Date prognoză invalide.");
                            }
                            break;

                        case "STERGE":
                            System.out.println("Introduceți id-ul locației de șters:");
                            String deleteId = scanner.nextLine();
                            out.println(deleteId);
                            System.out.println("Server: " + in.readLine());
                            break;

                        case "EXIT":
                            running = false;
                            break;

                        default:
                            System.out.println("Comandă invalidă.");
                            break;
                    }
                }
            } else if (userType.equalsIgnoreCase("client")) {
                boolean running = true;
                while (running) {
                    System.out.println("Comenzi disponibile:primeste_prognoza, exit");
                    String command = scanner.nextLine();
                    if(command.toUpperCase().equals("PRIMESTE_PROGNOZA"))
                    {
                        out.println(command);
                        System.out.println("Introduceți latitudinea:");
                        String latitude = scanner.nextLine();
                        System.out.println("Introduceți longitudinea:");
                        String longitude = scanner.nextLine();

                        if (validateCoordinates(latitude, longitude)) {
                            out.println(latitude);
                            out.println(longitude);
                            String response = in.readLine();
                            while (response != null && !response.equals("null")) {
                                System.out.println(response);
                                response = in.readLine(); // continuă citirea până când ajungi la final
                            }
                        } else {
                            System.out.println("Coordenate invalide.");
                        }
                    }
                    else
                        if(command.toUpperCase().equals("EXIT")) {
                            running = false;
                } else
                    {
                        System.out.println("Comandă invalidă.");
                    }
                }
            }else {
                System.out.println("Tip de utilizator invalid.");
            }

        }  catch (SocketException e) {
            System.out.println("Avertisment: Conexiunea cu serverul a fost întreruptă. Verificați serverul.");
        } catch (IOException e) {
            System.out.println("Eroare de conectare: Serverul nu este disponibil sau conexiunea a fost întreruptă.");
            e.printStackTrace();
        }
    }
    // Validarea coordonatelor geografice
    private static boolean validateCoordinates(String latitude, String longitude) {
        try {
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validarea detaliilor orașului
    private static boolean validateCityDetails(String details) {
        String[] parts = details.split(",");
        if (parts.length != 4) return false;
        try {
            Integer.parseInt(parts[0].trim()); // id
            Double.parseDouble(parts[2].trim()); // latitudine
            Double.parseDouble(parts[3].trim()); // longitudine
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    // Validarea datelor prognozei
    private static boolean validateForecastData(String... forecasts) {
        for (String forecast : forecasts) {
            String[] parts = forecast.split(",");
            if (parts.length != 4) return false;
            try {
                Double.parseDouble(parts[0].trim()); // temperatura
                Integer.parseInt(parts[2].trim()); // viteză vânt
                Integer.parseInt(parts[3].trim()); // umiditate
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
    private static boolean validateForecastData2(String data) {
        String[] parts = data.split("\\s*,\\s*");

        // Verificăm că avem exact 7 valori
        if (parts.length != 7) {
            System.out.println("Eroare: Datele trebuie să conțină exact 7 valori.");
            return false;
        }

        try {
            // ID-ul locației
            int cityId = Integer.parseInt(parts[0]);

            // Ziua actualizată (verificăm validitatea)
            String day = parts[1];
            if (!day.matches("azi|maine|poimaine|raspoimaine")) {
                System.out.println("Eroare: Ziua nu este validă. Trebuie să fie una dintre: azi, maine, poimaine, raspoimaine.");
                return false;
            }

            // Temperatură, umiditate, viteza vântului
            float temperature = Float.parseFloat(parts[2]);
            float humidity = Float.parseFloat(parts[4]);
            float windSpeed = Float.parseFloat(parts[5]);

            // Ziua pentru actualizare
            String updateDay = parts[6];
            if (!updateDay.matches("azi|maine|poimaine|raspoimaine")) {
                System.out.println("Eroare: Ziua pentru actualizare nu este validă.");
                return false;
            }

            // Dacă toate validările trec
            return true;

        } catch (NumberFormatException e) {
            System.out.println("Eroare: ID-ul, temperatura, umiditatea sau viteza vântului nu sunt numere valide.");
            return false;
        }
    }
}