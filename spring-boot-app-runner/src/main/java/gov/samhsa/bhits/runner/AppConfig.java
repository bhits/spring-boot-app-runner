package gov.samhsa.bhits.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String DELIMITER = "-";
    private String groupId;
    private String artifactId;
    private String version;
    private Map<String, String> args;
    private List<InstanceConfig> instanceConfigs;

    public AppConfig() {
        this.args = new HashMap<>();
        this.instanceConfigs = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = ArgsUtils.filterPortArgs(args);
    }

    public List<InstanceConfig> getInstanceConfigs() {
        return instanceConfigs;
    }

    public void setInstanceConfigs(List<InstanceConfig> instanceConfigs) {
        this.instanceConfigs = instanceConfigs;
    }

    public void stopProcess() {
        this.instanceConfigs.stream()
                .peek(instanceConfig -> logger.info("About to destroy " + key() + " at port " + instanceConfig.getPort()))
                .forEach(InstanceConfig::stopProcess);
    }

    public String key() {
        Assert.hasText(this.groupId);
        Assert.hasText(this.artifactId);
        return this.groupId + DELIMITER + this.artifactId;
    }

    public String jarName() {
        return key() + ".jar";
    }
}
