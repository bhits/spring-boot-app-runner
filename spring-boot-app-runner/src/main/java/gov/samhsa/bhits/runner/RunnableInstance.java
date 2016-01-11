package gov.samhsa.bhits.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RunnableInstance implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean running;
    private AppConfig appConfig;
    private InstanceConfig instanceConfig;
    private File jarPath;
    private ConfigManager configManager;

    public RunnableInstance(ConfigManager configManager, AppConfig appConfig, InstanceConfig instanceConfig, File jarPath) {
        this.appConfig = appConfig;
        this.instanceConfig = instanceConfig;
        this.jarPath = jarPath;
        this.configManager = configManager;
    }

    public void terminate() {
        this.running = false;
    }

    @Override
    public void run() {
        this.running = true;
        Stream<String> runJar = Stream.of("java", "-jar", appConfig.fileName(), "--server.port=" + instanceConfig.getPort());
        Stream<String> withAppArgs = appConfig.getArgs().entrySet().stream().map(RunnableInstance::toArg);
        Stream<String> withInstanceArgs = instanceConfig.getArgs().entrySet().stream().map(RunnableInstance::toArg);
        String[] cmdarray = Stream.concat(Stream.concat(runJar, withAppArgs), withInstanceArgs).toArray(String[]::new);

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdarray, null, jarPath);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ProcessRunnerException(e);
        }
        instanceConfig.setProcess(Optional.of(process));
        configManager.saveInstanceConfig(appConfig.getGroupId(), appConfig.getArtifactId(), instanceConfig);
        final String logPrefix = appConfig.key() + "-" + instanceConfig.getPort() + "> ";
        try (InputStream is = process.getInputStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String s = null;
                try {
                    while (this.running && (s = reader.readLine()) != null) {
                        logger.info(logPrefix + s);
                    }
                } catch (IOException e) {
                    logger.error(logPrefix + s, e);
                }
            }
        } catch (IOException e) {
            logger.error(logPrefix, e);
        }
    }

    private static String toArg(Map.Entry<String, String> entry) {
        Assert.hasText(entry.getKey());
        return StringUtils.hasText(entry.getValue()) ? entry.getKey() + "=" + entry.getValue() : entry.getKey();
    }
}
