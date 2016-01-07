package gov.samhsa.bhits.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Service
public class ProcessRunner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigManager configManager;

    public synchronized AppConfigContainer startProcess(String groupId, String artifactId, InstanceConfig instanceConfig) {
        File jarPath = Paths.get(configManager.getConfigFolderBasePath()).toFile();
        assertJarPath(jarPath);
        AppConfig appConfig = configManager.getConfigContainer().findAppConfig(groupId, artifactId);
        startInstanceAsync(appConfig, instanceConfig, jarPath);
        sleepUntilInstanceStarts(instanceConfig);
        return configManager.getConfigContainer();
    }

    @PostConstruct
    public void afterPropertiesSet(){
        this.configManager.getConfigContainer().getAppConfigs().stream()
                .forEach(appConfig -> appConfig.getInstanceConfigs().stream()
                        .peek(instanceConfig -> logger.info("Starting up " + appConfig.key() + " at " + instanceConfig.getPort()))
                        .forEach(instanceConfig -> startProcess(appConfig.getGroupId(), appConfig.getArtifactId(), instanceConfig)));
    }

    @PreDestroy
    public void destroy() {
        this.configManager.getConfigContainer().getAppConfigs().stream()
                .map(AppConfig::getInstanceConfigs)
                .flatMap(List::stream)
                .map(InstanceConfig::getProcess)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(process -> logger.info("About to destroy: " + process.toString()))
                .forEach(Process::destroy);
    }

    private void assertJarPath(File jarPath) {
        if (!jarPath.exists() || !jarPath.isDirectory()) {
            throw new ProcessRunnerException("jar path doesn't exist or is not a directory");
        }
    }

    private void sleepUntilInstanceStarts(InstanceConfig instanceConfig) {
        while (instanceConfig.getProcess().map(Process::isAlive).isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new ProcessRunnerException(e);
            }
        }
    }

    private void startInstanceAsync(AppConfig appConfig, InstanceConfig instanceConfig, File jarPath) {
        new Thread(() -> {
            instanceConfig.getProcess().filter(Process::isAlive).ifPresent(Process::destroy);
            Stream<String> runJar = Stream.of("java", "-jar", appConfig.jarName(), "--server.port=" + instanceConfig.getPort());
            Stream<String> withAppArgs = appConfig.getArgs().entrySet().stream().map(ProcessRunner::toArg);
            Stream<String> withInstanceArgs = instanceConfig.getArgs().entrySet().stream().map(ProcessRunner::toArg);
            String[] cmdarray = Stream.concat(Stream.concat(runJar, withAppArgs), withInstanceArgs).toArray(String[]::new);

            Process process = null;
            try {
                process = Runtime.getRuntime().exec(cmdarray, null, jarPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            instanceConfig.setProcess(Optional.of(process));
            configManager.saveInstanceConfig(appConfig.getGroupId(), appConfig.getArtifactId(), instanceConfig);

            try (InputStream is = process.getInputStream()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String s = null;
                    try {
                        while ((s = reader.readLine()) != null) {
                            logger.info(appConfig.key() + "> " + s);
                        }
                    } catch (IOException e) {
                        logger.error(appConfig.key() + "> " + s, e);
                    }
                }
            } catch (IOException e) {
                logger.error(appConfig.key() + "> ", e);
            }
        }).start();
    }

    private static String toArg(Map.Entry<String, String> entry) {
        Assert.hasText(entry.getKey());
        return StringUtils.hasText(entry.getValue()) ? entry.getKey() + "=" + entry.getValue() : entry.getKey();
    }
}
