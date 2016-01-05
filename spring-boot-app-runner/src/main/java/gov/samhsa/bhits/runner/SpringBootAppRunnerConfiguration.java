package gov.samhsa.bhits.runner;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpringBootAppRunnerConfiguration {

    private Map<String, SpringBootAppConfiguration> apps;

    public SpringBootAppRunnerConfiguration() {
        this.apps = new HashMap<>();
    }

    public Map<String, SpringBootAppConfiguration> getApps() {
        return apps;
    }

    public void setApps(Map<String, SpringBootAppConfiguration> apps) {
        this.apps = apps;
    }
}
