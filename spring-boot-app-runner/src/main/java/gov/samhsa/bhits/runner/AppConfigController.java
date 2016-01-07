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


    @RequestMapping(value = "/appConfigs")
    public AppConfigContainer getApps() {
        return configManager.getConfigContainer();
    }

    @RequestMapping(value = "/appConfigs", method = RequestMethod.POST)
    public AppConfig postApp(@RequestParam("groupId") String groupId,
                             @RequestParam("artifactId") String artifactId,
                             @RequestParam("version") String version,
                             @RequestParam("args") String args,
                             @RequestParam("file") MultipartFile file) throws IOException {
        AppConfig appConfig = new AppConfig();
        appConfig.setGroupId(groupId);
        appConfig.setArtifactId(artifactId);
        appConfig.setVersion(version);
        appConfig.setArgs(mapper.readValue(args, Map.class));
        this.jarFileManager.saveFile(appConfig, file);
        this.configManager.saveConfig(appConfig);
        return appConfig;
    }
}
