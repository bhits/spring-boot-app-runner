package gov.samhsa.bhits.runner;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppConfigContainer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile List<AppConfig> appConfigs;

    public AppConfigContainer() {
        this.appConfigs = new ArrayList<>();
    }

    public List<AppConfig> getAppConfigs() {
        return appConfigs;
    }

    public boolean contains(AppConfig appConfig) {
        return this.appConfigs.stream().anyMatch(app -> isSameApp(app, appConfig));
    }

    public synchronized void save(AppConfig appConfig) {
        deleteIfExists(appConfig);
        this.appConfigs.add(appConfig);
    }

    public synchronized InstanceConfig save(String groupId, String artifactId, InstanceConfig instanceConfig) {
        AppConfig appConfig = findAppConfig(groupId, artifactId);
        deleteIfExists(appConfig, instanceConfig);
        appConfig.getInstanceConfigs().add(instanceConfig);
        return instanceConfig;
    }

    public synchronized void deleteIfExists(AppConfig appConfig, InstanceConfig instanceConfig) {
        appConfig.getInstanceConfigs().stream()
                .filter(instance -> instance.getPort() == instanceConfig.getPort())
                .findAny()
                .ifPresent(instance -> appConfig.getInstanceConfigs().remove(instance));
    }

    public synchronized void deleteIfExists(AppConfig appConfig) {
        this.appConfigs.stream()
                .filter(app -> isSameApp(app, appConfig))
                .findFirst()
                .ifPresent(this.appConfigs::remove);
    }

    public boolean isSameApp(AppConfig app1, AppConfig app2) {
        return app1.getGroupId().equals(app2.getGroupId()) && app1.getArtifactId().equals(app2.getArtifactId());
    }

    public AppConfig findAppConfigByAppKey(String appKey) {
        return this.appConfigs.stream().filter(app -> app.key().equals(appKey)).findAny().get();
    }

    public AppConfig findAppConfig(String groupId, String artifactId) {
        return findAppConfigAsOptional(groupId, artifactId).get();
    }

    public Optional<AppConfig> findAppConfigAsOptional(String groupId, String artifactId) {
        logger.info("looking for " + groupId + "/" + artifactId);
        return this.appConfigs.stream()
                .peek(app -> logger.info("peeking at " + app.getGroupId() + "/" + app.getArtifactId()))
                .filter(app -> app.getGroupId().equals(groupId) && app.getArtifactId().equals(artifactId))
                .findFirst();
    }

    public InstanceConfig findInstanceConfig(String groupId, String artifactId, int port) {
        return findInstanceConfigAsOptional(groupId, artifactId, port).get();
    }

    public Optional<InstanceConfig> findInstanceConfigAsOptional(String groupId, String artifactId, int port) {
        return findAppConfig(groupId, artifactId).getInstanceConfigs().stream()
                .filter(instance -> instance.getPort() == port)
                .findFirst();
    }
}
