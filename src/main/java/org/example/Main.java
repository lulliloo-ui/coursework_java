package org.example;

import org.example.http.ApiHttpClient;
import org.example.http.HttpClientWrapper;
import org.example.model.MediaRecord;
import org.example.source.DeezerSource;
import org.example.source.NagerDateSource;
import org.example.source.TvmazeSource;
import org.example.storage.CsvDataStorage;
import org.example.storage.DataStorage;
import org.example.storage.JsonDataStorage;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        HttpClientWrapper httpClient = new ApiHttpClient();

        // Собираем все данные в один список
        List<MediaRecord> allRecords = new ArrayList<>();

        TvmazeSource tvmaze = new TvmazeSource(httpClient);
        allRecords.addAll(tvmaze.fetch("breaking bad"));

        DeezerSource deezer = new DeezerSource(httpClient);
        allRecords.addAll(deezer.fetch("eminem"));

        NagerDateSource nager = new NagerDateSource(httpClient);
        allRecords.addAll(nager.fetch("US"));

        System.out.println("Всего собрано записей: " + allRecords.size());

        // 1. Сохраняем в JSON
        DataStorage jsonStorage = new JsonDataStorage();
        jsonStorage.save(allRecords, "data.json", false);
        System.out.println("Сохранено в data.json");

        // 2. Сохраняем в CSV
        DataStorage csvStorage = new CsvDataStorage();
        csvStorage.save(allRecords, "data.csv", false);
        System.out.println("Сохранено в data.csv");

        // 3. Читаем обратно из JSON
        List<MediaRecord> fromJson = jsonStorage.load("data.json");
        System.out.println("Прочитано из JSON: " + fromJson.size());

        // 4. Читаем обратно из CSV
        List<MediaRecord> fromCsv = csvStorage.load("data.csv");
        System.out.println("Прочитано из CSV: " + fromCsv.size());

        // 5. Выводим на экран то, что прочитали из JSON
        System.out.println("\n=== Проверка: данные из JSON ===");
        printRecords(fromJson);

        // 6. Выводим на экран то, что прочитали из CSV
        System.out.println("\n=== Проверка: данные из CSV ===");
        printRecords(fromCsv);
    }

    private static void printRecords(List<MediaRecord> records) {
        System.out.println("Найдено: " + records.size());
        for (MediaRecord r : records) {
            System.out.println("---");
            System.out.println("Источник: " + r.source());
            System.out.println("Категория: " + r.category());
            System.out.println("Запрос: " + r.query());
            r.fields().forEach((k, v) -> System.out.println("  " + k + ": " + v));
        }
    }
}