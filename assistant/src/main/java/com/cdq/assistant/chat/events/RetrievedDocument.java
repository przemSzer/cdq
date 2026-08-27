package com.cdq.assistant.chat.events;

import com.cdq.assistant.MetadataUtils;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;

public record RetrievedDocument(String title, String url, String text, Double score) {

    public static RetrievedDocument toRetrievedDocument(Content content) {
        if (content == null || content.textSegment() == null) {
            return new RetrievedDocument("", "", "", null);
        }
        var segment = content.textSegment();
        Object score = content.metadata() == null ? null : content.metadata().get(ContentMetadata.SCORE);
        Double scoreValue = score instanceof Number number ? number.doubleValue() : null;
        return new RetrievedDocument(
                MetadataUtils.metadataValue(segment.metadata(), "title"),
                MetadataUtils.metadataValue(segment.metadata(), "url"),
                segment.text() == null ? "" : segment.text(),
                scoreValue
        );
    }
}
