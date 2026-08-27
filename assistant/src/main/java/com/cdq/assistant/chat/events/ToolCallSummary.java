package com.cdq.assistant.chat.events;

public record ToolCallSummary(String name, String arguments, String result) {
}
