package org.example.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.MediaRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonDataStorage implements DataStorage {

    private final ObjectMapper objectMapper;

    public JsonDataStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(List<MediaRecord> records, String filePath, boolean append) {
        try {
            File file = new File(filePath);
            List<MediaRecord> existingRecords = new ArrayList<>();

            // Если нужно дописать и файл существует — читаем старое
            if (append && file.exists()) {
                existingRecords = load(filePath);
            }

            existingRecords.addAll(records);

            // Записываем всё вместе
            objectMapper.writeValue(file, existingRecords);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении в JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MediaRecord> load(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(file, new TypeReference<List<MediaRecord>>() {});

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении JSON: " + e.getMessage(), e);
        }
    }
}
