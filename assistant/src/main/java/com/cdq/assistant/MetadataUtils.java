package com.cdq.assistant;

import dev.langchain4j.data.document.Metadata;

public class MetadataUtils {

    private MetadataUtils() {
        /* This utility class should not be instantiated */
    }

    public static String metadataValue(Metadata metadata, String key) {
        if (metadata == null || !metadata.containsKey(key)) {
            return "";
        }
        String value = metadata.getString(key);
        return value == null ? "" : value;
    }

}
