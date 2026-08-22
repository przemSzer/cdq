package com.cdq.assistant.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class AssistantPropertiesTest {

    @Test
    void weatherCommandUsesConfiguredCommandAndScript() {
        AssistantProperties properties = new AssistantProperties();
        properties.getWeather().setCommand("npx.cmd");
        properties.getWeather().setDirectory("C:/tools/mcp-weather");
        properties.getWeather().setScript("src/index.ts");

        List<String> command = properties.weatherLaunchCommand();

        assertEquals("npx.cmd", command.get(0));
        assertEquals("tsx", command.get(1));
        assertEquals(Path.of("C:/tools/mcp-weather", "src/index.ts").toAbsolutePath().toString(), command.get(2));
    }

    @Test
    void weatherCommandDefaultsToNpxCmdOnWindows() {
        assumeTrue(System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win"));

        AssistantProperties properties = new AssistantProperties();
        properties.getWeather().setDirectory("C:/tools/mcp-weather");

        assertEquals("npx.cmd", properties.weatherLaunchCommand().getFirst());
    }
}
