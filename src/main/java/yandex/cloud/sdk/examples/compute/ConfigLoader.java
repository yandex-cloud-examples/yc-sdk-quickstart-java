package yandex.cloud.sdk.examples.compute;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

// ConfigLoader.java class for loading JSON file with VM configuration
public class ConfigLoader {
    public static Config loadConfig(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Config main() {
        // Path to config.json
        String filePath = "src/main/resources/config/config.json";
        return loadConfig(filePath);
    }
}