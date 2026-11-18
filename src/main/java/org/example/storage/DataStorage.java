package org.example.storage;

import org.example.model.MediaRecord;
import java.util.List;

public interface DataStorage {
    void save(List<MediaRecord> records, String filePath, boolean append);

    List<MediaRecord> load(String filePath);
}
