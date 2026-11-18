package org.example.source;

import org.example.http.ApiHttpClient;
import org.example.model.MediaRecord;
import org.example.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeezerSource implements DataSource {

    private final ApiHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeezerSource(ApiHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceName() {
        return "Deezer";
    }

    @Override
    public List<MediaRecord> fetch(String query) {
        List<MediaRecord> result = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = "https://api.deezer.com/search?q=" + encodedQuery + "&limit=5";
            String jsonResponse = httpClient.get(url);

            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode dataArray = root.get("data");
            if (dataArray == null || !dataArray.isArray()) {
                return result;
            }

            for (JsonNode track : dataArray) {
                Map<String, Object> fields = new LinkedHashMap<>();

                fields.put("title", JsonUtils.text(track.get("title")));
                fields.put("deezer_url", JsonUtils.text(track.get("link")));
                fields.put("rank", JsonUtils.integer(track.get("rank")));
                fields.put("duration", JsonUtils.integer(track.get("duration")));

                JsonNode artist = track.get("artist");
                fields.put("artist", JsonUtils.text(artist != null ? artist.get("name") : null));

                JsonNode album = track.get("album");
                if (album != null) {
                    Map<String, Object> albumInfo = new LinkedHashMap<>();
                    albumInfo.put("id", JsonUtils.integer(album.get("id")));
                    albumInfo.put("title", JsonUtils.text(album.get("title")));
                    albumInfo.put("cover_url", JsonUtils.text(album.get("cover")));
                    fields.put("album", albumInfo);
                }

                result.add(new MediaRecord(
                        getSourceName(),
                        "music",
                        query,
                        Instant.now(),
                        fields
                ));
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось разобрать ответ Deezer: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Ошибка при запросе к Deezer: " + e.getMessage(), e);
        }

        return result;
    }
}