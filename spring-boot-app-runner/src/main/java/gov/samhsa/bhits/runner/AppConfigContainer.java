package gov.samhsa.bhits.runner;


import java.util.ArrayList;
import java.util.List;

public class AppConfigContainer {

    private volatile List<AppConfig> appConfigs;

    public AppConfigContainer() {
        this.appConfigs = new ArrayList<>();
    }

    public List<AppConfig> getAppConfigs() {
        return appConfigs;
    }

    public boolean contains(AppConfig appConfig){
        return this.appConfigs.stream().anyMatch(app -> isSameApp(app, appConfig));
    }

    public synchronized void save(AppConfig appConfig){
        deleteIfExists(appConfig);
        this.appConfigs.add(appConfig);
    }

    public synchronized void deleteIfExists(AppConfig appConfig){
        this.appConfigs.stream().filter(app -> isSameApp(app, appConfig)).findFirst().ifPresent(this.appConfigs::remove);
    }

    public boolean isSameApp(AppConfig app1, AppConfig app2){
        return app1.getGroupId().equals(app2.getGroupId()) && app1.getArtifactId().equals(app2.getArtifactId());
    }

    public AppConfig getAppConfigByAppKey(String appKey){
        return this.appConfigs.stream().filter(app -> app.key().equals(appKey)).findAny().get();
    }
}
