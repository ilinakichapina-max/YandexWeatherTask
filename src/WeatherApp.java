import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WeatherApp {
    public static void main(String[] args) throws Exception {
        // ВАШ КЛЮЧ (проверьте его в личном кабинете!)
        String apiKey = "56a75c60-b4ed-459e-bafe-6ae7000d6056";

        double lat = 56.820134;
        double lon = 35.833258;
        int limit = 7;

        String url = String.format("https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d", lat, lon, limit);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Ошибка запроса. Код ответа: " + response.statusCode());
            System.out.println(response.body());
            return;
        }

        String json = response.body();

        System.out.println("=== ПОЛНЫЙ ОТВЕТ СЕРВИСА ===");
        System.out.println(json);

        String currentTemp = extractValue(json, "\"fact\":", "\"temp\":");
        System.out.println("\n=== ТЕКУЩАЯ ТЕМПЕРАТУРА (fact.temp) ===");
        System.out.println(currentTemp + "°C");

        double sum = 0;
        int count = 0;
        String[] parts = json.split("\"temp_avg\":");
        for (int i = 1; i < parts.length; i++) {
            try {
                String numStr = parts[i].split("[,}]")[0].trim();
                sum += Double.parseDouble(numStr);
                count++;
            } catch (NumberFormatException e) {
                // пропускаем
            }
        }

        if (count > 0) {
            double average = sum / count;
            System.out.println("\n=== СРЕДНЯЯ ТЕМПЕРАТУРА (за " + limit + " дней) ===");
            System.out.printf("Средняя температура: %.2f°C%n", average);
        } else {
            System.out.println("Не удалось найти данные о средней температуре.");
        }
    }

    private static String extractValue(String json, String parentKey, String childKey) {
        int parentIdx = json.indexOf(parentKey);
        if (parentIdx == -1) return "Не найдено";
        int childIdx = json.indexOf(childKey, parentIdx);
        if (childIdx == -1) return "Не найдено";
        int start = childIdx + childKey.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return json.substring(start, end).replace("\"", "").trim();
    }
}