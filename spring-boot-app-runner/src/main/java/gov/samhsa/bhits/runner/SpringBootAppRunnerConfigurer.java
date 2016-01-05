package gov.samhsa.bhits.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Configuration
public class SpringBootAppRunnerConfigurer {

    public static final String CONFIG_FILE_NAME = "SpringBootAppRunnerConfig.json";

    @Value("${bhits.apprunner.config.path}")
    private String path;

    @Autowired
    private ObjectMapper mapper;

    private SpringBootAppConfiguration config;

    public SpringBootAppConfiguration getConfig() {
        return config;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        String configFolderPathString = this.path.endsWith("/") || this.path.endsWith("\\") ? this.path : this.path + "/";
        Path configFolderPath = Paths.get(configFolderPathString);
        if(!Files.exists(configFolderPath)){
            try {
                Files.createDirectories(configFolderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String configPath = this.path.endsWith("/") || this.path.endsWith("\\") ? this.path + CONFIG_FILE_NAME : this.path + "/" + CONFIG_FILE_NAME;

        Path path = Paths.get(configPath);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                byte[] configJsonBytes = Files.readAllBytes(path);
                String configJsonString = new String(configJsonBytes, StandardCharsets.UTF_8);
                this.config = mapper.readValue(configJsonString, SpringBootAppConfiguration.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.config = new SpringBootAppConfiguration();
            try {
                String configJsonString = mapper.writeValueAsString(this.config);
                byte[] configJsonBytes = configJsonString.getBytes(StandardCharsets.UTF_8);
                Files.write(path, configJsonBytes, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
