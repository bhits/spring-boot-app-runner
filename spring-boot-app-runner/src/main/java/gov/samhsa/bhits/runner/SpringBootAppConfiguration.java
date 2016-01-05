package gov.samhsa.bhits.runner;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class SpringBootAppConfiguration {
    public static final String DELIMITER = "-";
    private String groupId;
    private String artifactId;
    private String version;
    private Map<String, String> args;

    public SpringBootAppConfiguration() {
        this.args = new HashMap<>();
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
        this.args = args;
    }

    public String createKey(){
        Assert.hasText(this.groupId);
        Assert.hasText(this.artifactId);
        return this.groupId + DELIMITER + this.artifactId + DELIMITER;
    }
}
