package org.example.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.http.ApiHttpClient;
import org.example.model.MediaRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.util.JsonUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TvmazeSource implements DataSource {

    private final ApiHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TvmazeSource(ApiHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceName() {
        return "TVmaze";
    }

    @Override
    public List<MediaRecord> fetch(String query) {
        List<MediaRecord> result = new ArrayList<>();

        try {
            String url = "https://api.tvmaze.com/search/shows?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            String jsonResponse = httpClient.get(url);

            JsonNode root = objectMapper.readTree(jsonResponse);

            int limit = Math.min(root.size(), 5);

            for (int i = 0; i < limit; i++) {
                JsonNode item = root.get(i);
                JsonNode show = item.get("show");
                if (show == null) continue;

                Map<String, Object> fields = new LinkedHashMap<>();
                fields.put("title", JsonUtils.text(show.get("name")));
                fields.put("url", JsonUtils.text(show.get("url")));
                fields.put("premiered", JsonUtils.text(show.get("premiered")));
                fields.put("rating", JsonUtils.number(show.path("rating").get("average")));

                // Жанры (массив)
                List<String> genres = new ArrayList<>();
                if (show.has("genres") && show.get("genres").isArray()) {
                    for (JsonNode g : show.get("genres")) {
                        genres.add(g.asText());
                    }
                }
                fields.put("genres", genres);

                JsonNode image = show.get("image");
                if (image != null && image.has("medium")) {
                    fields.put("image_url", JsonUtils.text(image.get("medium")));
                } else {
                    fields.put("image_url", "");
                }

                result.add(new MediaRecord(
                        getSourceName(),
                        "tv-show",
                        query,
                        Instant.now(),
                        fields
                ));
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось разобрать ответ TVmaze: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Ошибка при запросе к TVmaze: " + e.getMessage(), e);
        }

        return result;
    }
}
