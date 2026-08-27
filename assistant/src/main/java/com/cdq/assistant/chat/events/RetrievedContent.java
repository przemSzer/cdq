package com.cdq.assistant.chat.events;

import java.util.List;

public record RetrievedContent(int count, List<RetrievedDocument> documents) {
}
