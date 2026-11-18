package org.example;

import org.example.http.ApiHttpClient;
import org.example.model.MediaRecord;
import org.example.source.DeezerSource;
import org.example.source.NagerDateSource;
import org.example.source.TvmazeSource;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ApiHttpClient httpClient = new ApiHttpClient();

        TvmazeSource tvmaze = new TvmazeSource(httpClient);
        System.out.println("=== TVmaze ===");
        List<MediaRecord> tvRecords = tvmaze.fetch("breaking bad");
        printRecords(tvRecords);

        DeezerSource deezer = new DeezerSource(httpClient);
        System.out.println("\n=== Deezer ===");
        List<MediaRecord> musicRecords = deezer.fetch("eminem");
        printRecords(musicRecords);

        NagerDateSource nager = new NagerDateSource(httpClient);
        System.out.println("\n=== Nager.Date ===");
        List<MediaRecord> holidayRecords = nager.fetch("US");
        printRecords(holidayRecords);
    }

    private static void printRecords(List<MediaRecord> records) {
        System.out.println("Найдено: " + records.size());
        for (MediaRecord r : records) {
            System.out.println("---");
            System.out.println("Источник: " + r.source());
            System.out.println("Категория: " + r.category());
            System.out.println("Запрос: " + r.key());
            r.fields().forEach((k, v) -> System.out.println("  " + k + ": " + v));
        }
    }
}