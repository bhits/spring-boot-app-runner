package gov.samhsa.bhits.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class ConfigManager {

    public static final int MIN_PORT_LIMIT = 0;
    public static final int MAX_PORT_LIMIT = 65536;
    public static final String CONFIG_FILE_NAME = "SpringBootAppRunnerConfig.json";

    @Value("${bhits.apprunner.config.path}")
    private String configFolderBasePath;
    private Path configFilePath;

    @Autowired
    private ObjectMapper mapper;
    private volatile AppConfigContainer configContainer;

    @PostConstruct
    public void afterPropertiesSet() {
        this.configFolderBasePath = getConfigFolderBasePath(this.configFolderBasePath);
        this.configFilePath = Paths.get(this.configFolderBasePath + CONFIG_FILE_NAME);
        Assert.hasText(this.configFolderBasePath);
        initConfigFolder();
        initConfigContainer();
        Assert.notNull(this.configContainer);
    }

    public String getConfigFolderBasePath() {
        return configFolderBasePath;
    }

    public synchronized void saveAppConfig(AppConfig appConfig) {
        this.configContainer.save(appConfig);
        persistsConfigContainer();
    }

    public synchronized InstanceConfig saveInstanceConfig(String groupId, String artifactId, InstanceConfig instanceConfig) {
        Assert.isTrue(instanceConfig.getPort() > MIN_PORT_LIMIT && instanceConfig.getPort() < MAX_PORT_LIMIT, "port number must be: 'port > " + MIN_PORT_LIMIT + " && port < " + MAX_PORT_LIMIT + "'");
        InstanceConfig savedInstance = this.configContainer.save(groupId, artifactId, instanceConfig);
        persistsConfigContainer();
        return savedInstance;
    }

    public synchronized void deleteConfig(AppConfig appConfig) {
        this.configContainer.deleteIfExists(appConfig);
        persistsConfigContainer();
    }

    public AppConfigContainer getConfigContainer() {
        return configContainer;
    }

    private void persistsConfigContainer() {
        try {
            String configJsonString = mapper.writeValueAsString(this.configContainer);
            byte[] configJsonBytes = configJsonString.getBytes(StandardCharsets.UTF_8);
            Files.deleteIfExists(this.configFilePath);
            Files.write(this.configFilePath, configJsonBytes, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new ConfigManagerException(e);
        }
    }

    private void loadConfigContainer() {
        try {
            byte[] configJsonBytes = Files.readAllBytes(this.configFilePath);
            String configJsonString = new String(configJsonBytes, StandardCharsets.UTF_8);
            AppConfigContainer appConfig = this.mapper.readValue(configJsonString, AppConfigContainer.class);
            this.configContainer = appConfig;
        } catch (IOException e) {
            throw new ConfigManagerException(e);
        }
    }


    private String getConfigFolderBasePath(String configBasePath) {
        return configBasePath.endsWith("/") || configBasePath.endsWith("\\") ? configBasePath : configBasePath + "/";
    }

    private final void initConfigFolder() {
        Path configFolderPath = Paths.get(this.configFolderBasePath);
        if (!Files.exists(configFolderPath)) {
            try {
                Files.createDirectories(configFolderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initConfigContainer() {
        this.configContainer = new AppConfigContainer();
        if (Files.exists(this.configFilePath) && Files.isRegularFile(this.configFilePath)) {
            loadConfigContainer();
        } else {
            persistsConfigContainer();
        }
    }
}
