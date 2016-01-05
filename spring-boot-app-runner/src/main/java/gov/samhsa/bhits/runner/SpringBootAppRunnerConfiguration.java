package gov.samhsa.bhits.runner;


import java.util.List;

public class SpringBootAppRunnerConfiguration {

    private List<SpringBootAppConfiguration> apps;

    public List<SpringBootAppConfiguration> getApps() {
        return apps;
    }

    public void setApps(List<SpringBootAppConfiguration> apps) {
        this.apps = apps;
    }
}
