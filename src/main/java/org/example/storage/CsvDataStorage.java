package org.example.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.example.model.MediaRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvDataStorage implements DataStorage {

    private final ObjectMapper objectMapper;

    public CsvDataStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void save(List<MediaRecord> records, String filePath, boolean append) {
        boolean isNewFile = !new java.io.File(filePath).exists();

        try (Writer writer = new FileWriter(filePath, append);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            // Если файл новый, пишем заголовок
            if (isNewFile) {
                printer.printRecord("source", "category", "query", "timestamp", "fields");
            }

            for (MediaRecord r : records) {
                // Превращаем Map fields в строку JSON
                String fieldsJson = objectMapper.writeValueAsString(r.fields());

                printer.printRecord(
                        r.source(),
                        r.category(),
                        r.query(),
                        r.timestamp().toString(),
                        fieldsJson
                );
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении в CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MediaRecord> load(String filePath) {
        List<MediaRecord> result = new ArrayList<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : parser) {
                String source = record.get("source");
                String category = record.get("category");
                String query = record.get("query");
                Instant timestamp = Instant.parse(record.get("timestamp"));
                String fieldsJson = record.get("fields");

                // Превращаем строку JSON обратно в Map
                Map<String, Object> fields = objectMapper.readValue(
                        fieldsJson,
                        new TypeReference<LinkedHashMap<String, Object>>() {}
                );

                result.add(new MediaRecord(source, category, query, timestamp, fields));
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении CSV: " + e.getMessage(), e);
        }

        return result;
    }
}
