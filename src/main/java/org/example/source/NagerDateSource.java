package org.example.source;

import org.example.http.ApiHttpClient;
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

    private final ApiHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NagerDateSource(ApiHttpClient httpClient) {
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
            String url = "https://date.nager.at/api/v3/publicholidays/2026/" + countryCode;

            String jsonResponse = httpClient.get(url);

            JsonNode root = objectMapper.readTree(jsonResponse);

            if (!root.isArray()) {
                return result;
            }

            for (JsonNode holiday : root) {
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
        } catch (RuntimeException e) {
            throw new RuntimeException("Ошибка при запросе к Nager.Date: " + e.getMessage(), e);
        }

        return result;
    }
}