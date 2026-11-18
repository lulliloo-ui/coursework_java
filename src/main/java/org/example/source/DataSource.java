package org.example.source;

import org.example.model.MediaRecord;
import java.util.List;

public interface DataSource {
    String getSourceName();

    List<MediaRecord> fetch(String query);
}