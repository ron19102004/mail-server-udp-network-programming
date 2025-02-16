package com.ronial.app.conf;

import com.ronial.app.exceptions.ConfigException;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class ConfigManagementImpl implements ConfigManagement {
    private Map<String, Object> configs;

    public ConfigManagementImpl() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getResourceAsStream("/application.yml");
        if (inputStream == null) {
            throw new FileNotFoundException("Application.yml not found");
        }
        this.configs = yaml.load(inputStream);
    }

    public <T> Optional<T> value(String key) {
        try {
            String[] keys = key.split("\\.");
            T value = null;
            Map<String, Object> values = this.configs;
            for (int i = 0; i < keys.length; i++) {
                if (i == keys.length - 1) {
                    value = (T) values.get(keys[i]);
                } else {
                    values = (Map<String, Object>) values.get(keys[i]);
                }
            }
            return value == null ? Optional.empty() : Optional.of(value);
        } catch (Exception e) {
            throw new ConfigException(e.getMessage());
        }
    }
}
