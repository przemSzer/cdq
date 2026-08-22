package com.cdq.assistant.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

//TODO: migrate to records
@ConfigurationProperties(prefix = "assistant")
public class AssistantProperties {

    private final CountriesMcp countriesMcp = new CountriesMcp();
    private final Weather weather = new Weather();

    public CountriesMcp getCountriesMcp() {
        return countriesMcp;
    }

    public Weather getWeather() {
        return weather;
    }

    public List<String> weatherLaunchCommand() {
        return List.of(resolvedWeatherCommand(), "tsx", resolvedWeatherScript());
    }

    String resolvedWeatherCommand() {
        String command = weather.getCommand();
        if (command != null && !command.isBlank()) {
            return command;
        }
        return windows() ? "npx.cmd" : "npx";
    }

    String resolvedWeatherScript() {
        String directory = weather.getDirectory();
        String script = weather.getScript() == null || weather.getScript().isBlank()
                ? "src/index.ts"
                : weather.getScript();
        if (directory != null && !directory.isBlank()) {
            return Path.of(directory, script).toAbsolutePath().toString();
        }
        if (!Path.of(script).isAbsolute()) {
            throw new IllegalStateException(
                    "Set WEATHER_MCP_DIR or assistant.weather.script to the mcp-weather entrypoint.");
        }
        return script;
    }

    private static boolean windows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
    //TODO: migrate to records
    public static class CountriesMcp {

        private String url = "http://localhost:8081/mcp";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Weather {

        private String command;
        private String directory;
        private String script = "src/index.ts";
        private String apiKey;
        private String apiUrl = "https://api.weatherapi.com/v1/current.json";

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }
}
