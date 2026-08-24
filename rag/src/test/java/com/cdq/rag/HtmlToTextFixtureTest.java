package com.cdq.rag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;

class HtmlToTextFixtureTest {

    @Test
    void stripsChromeAndKeepsFraudGuardText() throws IOException {
        String html;
        try (InputStream in = getClass().getResourceAsStream("fraud-guard-fixture.html")) {
            html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        String text = new HtmlToTextDocumentTransformer(null, null, true)
                .transform(Document.from(html))
                .text();

        assertTrue(text.contains("Fraud Guard"));
        assertTrue(text.contains("risky business partners"));
        assertFalse(text.contains("alert("));
        assertFalse(text.contains("<script"));
        assertFalse(text.contains("<nav"));
    }
}
