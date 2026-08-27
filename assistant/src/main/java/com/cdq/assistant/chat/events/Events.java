package com.cdq.assistant.chat.events;

public enum Events {
    INFERENCE_STARTED("inferenceStarted"),
    ERROR("error"),
    MESSAGE("message"),
    RETRIEVED("retrieved"),
    PARTIAL_THINKING("partialThinking"),
    TOOL_EXECUTION("toolExecution"),
    DONE("done");

    private final String value;

    Events(String value) {
        this.value = value;
    }

    public String getName() {
        return value;
    }
}
