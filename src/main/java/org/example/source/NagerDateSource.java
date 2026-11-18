package org.example.source;

import org.example.http.ApiHttpClient;
import org.example.http.HttpClientException;
import org.example.http.HttpClientWrapper;
import org.example.model.MediaRecord;
import org.example.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NagerDateSource implements DataSource {

    private final HttpClientWrapper httpClient;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://date.nager.at/api/v3/publicholidays/2026/";
    private static final int LIMIT = 5;

    public NagerDateSource(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceName() {
        return "Nager.Date";
    }

    @Override
    public List<MediaRecord> fetch(String query) {
        List<MediaRecord> result = new ArrayList<>();

        try {
            String countryCode = query.trim().toUpperCase();
            String url = BASE_URL + countryCode;

            String jsonResponse = httpClient.get(url);

            JsonNode root = objectMapper.readTree(jsonResponse);

            if (!root.isArray()) {
                throw new RuntimeException("Nager.Date вернул не список праздников");
            }

            int limit = Math.min(root.size(), LIMIT);
            for (int i = 0; i < limit; i++) {
                JsonNode holiday = root.get(i);
                Map<String, Object> fields = new LinkedHashMap<>();

                fields.put("localName", JsonUtils.text(holiday.get("localName")));
                fields.put("name", JsonUtils.text(holiday.get("name")));
                fields.put("date", JsonUtils.text(holiday.get("date")));
                fields.put("countryCode", JsonUtils.text(holiday.get("countryCode")));

                // Массив types
                List<String> types = new ArrayList<>();
                if (holiday.has("types") && holiday.get("types").isArray()) {
                    for (JsonNode type : holiday.get("types")) {
                        types.add(type.asText());
                    }
                }
                fields.put("types", types);

                result.add(new MediaRecord(
                        getSourceName(),
                        "holiday",
                        query,
                        Instant.now(),
                        fields
                ));
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось разобрать ответ Nager.Date: " + e.getMessage(), e);
        } catch (HttpClientException e) {
            throw new RuntimeException("Ошибка при запросе к Nager.Date: " + e.getMessage(), e);
        }

        return result;
    }
}