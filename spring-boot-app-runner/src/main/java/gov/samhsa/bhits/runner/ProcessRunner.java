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
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toList;


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
    public void afterPropertiesSet() {
        logger.info("ProcessRunner.afterPropertiesSet(): " + this.configManager.getConfigContainer().getAppConfigs().size());
        Map<AppConfig, List<InstanceConfig>> copyOfConfigs = this.configManager.getConfigContainer().getAppConfigs().stream()
                .collect(toMap(app -> app, app -> app.getInstanceConfigs().stream().collect(toList())));
        copyOfConfigs.forEach((app, instances) -> instances.forEach(instance -> startProcess(app.getGroupId(), app.getArtifactId(), instance)));
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
        AtomicInteger counter = new AtomicInteger(0);
        while (!instanceConfig.getProcess().map(Process::isAlive).isPresent()) {
            try {
                Thread.sleep(1000);
                if(counter.incrementAndGet() == 5){
                    break;
                }
            } catch (InterruptedException e) {
                throw new ProcessRunnerException(e);
            }
        }
    }

    private void startInstanceAsync(AppConfig appConfig, InstanceConfig instanceConfig, File jarPath) {
        Assert.isTrue(available(instanceConfig.getPort()), "Port " + instanceConfig.getPort() + " is not available, cannot start app " + appConfig.key());
        new Thread(() -> {
            instanceConfig.stopProcess();
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

    public static boolean available(int port) {
        if (port < ConfigManager.MIN_PORT_NUMBER || port > ConfigManager.MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }
        return false;
    }

    private static String toArg(Map.Entry<String, String> entry) {
        Assert.hasText(entry.getKey());
        return StringUtils.hasText(entry.getValue()) ? entry.getKey() + "=" + entry.getValue() : entry.getKey();
    }
}
