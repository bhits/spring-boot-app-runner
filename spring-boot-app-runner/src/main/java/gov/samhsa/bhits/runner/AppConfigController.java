package gov.samhsa.bhits.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
public class AppConfigController {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private JarFileManager jarFileManager;

    @Autowired
    private ProcessRunner processRunner;

    @RequestMapping(value = "/appConfigs")
    public AppConfigContainer getApps() {
        return configManager.getConfigContainer();
    }

    @RequestMapping(value = "/appConfigs", method = RequestMethod.POST)
    public AppConfigContainer postApp(@RequestParam("groupId") String groupId,
                                      @RequestParam("artifactId") String artifactId,
                                      @RequestParam("version") String version,
                                      @RequestParam("args") String args,
                                      @RequestParam("file") MultipartFile file) throws IOException {
        AppConfig appConfig = new AppConfig();
        appConfig.setGroupId(groupId);
        appConfig.setArtifactId(artifactId);
        appConfig.setVersion(version);
        appConfig.setArgs(mapper.readValue(args, Map.class));
        this.configManager.getConfigContainer().findAppConfigAsOptional(groupId, artifactId).ifPresent(app -> {
            app.stopProcess();
            appConfig.setInstanceConfigs(app.getInstanceConfigs());
        });
        this.jarFileManager.saveFile(appConfig, file);
        this.configManager.saveAppConfig(appConfig);
        this.processRunner.startProcess(groupId, artifactId);
        return this.configManager.getConfigContainer();
    }

    @RequestMapping(value = "/appConfigs/{groupId}/{artifactId}", method = RequestMethod.DELETE)
    public AppConfigContainer deleteApp(@PathVariable("groupId") String groupId,
                                        @PathVariable("artifactId") String artifactId) throws IOException {
        AppConfig appConfig = this.configManager.getConfigContainer().findAppConfig(groupId, artifactId);
        appConfig.stopProcess();
        this.jarFileManager.deleteFile(appConfig);
        this.configManager.deleteAppConfig(appConfig);
        return this.configManager.getConfigContainer();
    }

    @RequestMapping(value = "/appConfigs/{groupId}/{artifactId}/instanceConfigs", method = RequestMethod.POST)
    public AppConfigContainer postInstance(@PathVariable("groupId") String groupId,
                                           @PathVariable("artifactId") String artifactId,
                                           @RequestBody InstanceConfig instanceConfig) {
        this.configManager.getConfigContainer().findInstanceConfigAsOptional(groupId, artifactId, instanceConfig.getPort()).ifPresent(InstanceConfig::stopProcess);
        InstanceConfig savedInstance = this.configManager.saveInstanceConfig(groupId, artifactId, instanceConfig);
        return processRunner.startProcess(groupId, artifactId, savedInstance);
    }

    @RequestMapping(value = "/appConfigs/{groupId}/{artifactId}/instanceConfigs/ports/{port}", method = RequestMethod.DELETE)
    public AppConfigContainer deleteInstance(@PathVariable("groupId") String groupId,
                                             @PathVariable("artifactId") String artifactId,
                                             @PathVariable("port") int port) {
        return this.configManager.deleteInstanceConfig(groupId, artifactId, port);
    }
}
