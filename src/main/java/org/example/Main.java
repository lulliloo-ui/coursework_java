package org.example;

import org.example.http.ApiHttpClient;
import org.example.model.MediaRecord;
import org.example.source.TvmazeSource;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ApiHttpClient httpClient = new ApiHttpClient();

        TvmazeSource tvmaze = new TvmazeSource(httpClient);

        System.out.println("Ищем сериал 'breaking bad'...");
        List<MediaRecord> records = tvmaze.fetch("breaking bad");

        System.out.println("Найдено записей: " + records.size());
        System.out.println("========================================");

        for (MediaRecord r : records) {
            System.out.println("Источник: " + r.source());
            System.out.println("Категория: " + r.category());
            System.out.println("Запрос: " + r.key());
            System.out.println("Время: " + r.timestamp());
            System.out.println("--- Данные (fields) ---");

            r.fields().forEach((k, v) -> System.out.println("  " + k + ": " + v));

            System.out.println("========================================");
        }
    }
}