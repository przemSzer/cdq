package com.cdq.assistant.chat.events;

public record ResponseSummary(int responseTokenCount, int messageTokenCount, String modelName) {
}
