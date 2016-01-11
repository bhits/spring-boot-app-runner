package gov.samhsa.bhits.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


@Service
public class ProcessRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigManager configManager;

    public synchronized AppConfigContainer startProcess(AppConfig appConfig, InstanceConfig instanceConfig) {
        File jarPath = Paths.get(configManager.getConfigFolderBasePath()).toFile();
        assertJarPath(jarPath);
        startInstanceAsync(appConfig, instanceConfig, jarPath);
        sleepUntilInstanceStarts(instanceConfig);
        return configManager.getConfigContainer();
    }

    public synchronized AppConfigContainer startProcess(String groupId, String artifactId, InstanceConfig instanceConfig) {
        return startProcess(this.configManager.getConfigContainer().findAppConfig(groupId, artifactId), instanceConfig);
    }

    public synchronized AppConfigContainer startProcess(String groupId, String artifactId) {
        AppConfig appConfig = configManager.getConfigContainer().findAppConfig(groupId, artifactId);
        List<InstanceConfig> copyOfInstanceConfigs = new ArrayList<>(appConfig.getInstanceConfigs());
        copyOfInstanceConfigs.stream().forEach(instanceConfig -> startProcess(appConfig, instanceConfig));
        return configManager.getConfigContainer();
    }

    @PostConstruct
    public void afterPropertiesSet() {
        logger.info("ProcessRunner.afterPropertiesSet(): " + this.configManager.getConfigContainer().getAppConfigs().size());
        Map<AppConfig, List<InstanceConfig>> copyOfConfigs = this.configManager.getConfigContainer().getAppConfigs().stream()
                .collect(toMap(app -> app, app -> app.getInstanceConfigs().stream().collect(toList())));
        copyOfConfigs.forEach((app, instances) -> instances.forEach(instance -> startProcess(app, instance)));
    }


    @PreDestroy
    public void destroy() {
        this.configManager.getConfigContainer().getAppConfigs().stream()
                .peek(app -> logger.info("About to destroy: " + app.key() + " instance processes"))
                .forEach(AppConfig::stopProcess);
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
                if (counter.incrementAndGet() == 5) {
                    break;
                }
            } catch (InterruptedException e) {
                throw new ProcessRunnerException(e);
            }
        }
    }

    private void startInstanceAsync(AppConfig appConfig, InstanceConfig instanceConfig, File jarPath) {
        Assert.isTrue(available(instanceConfig.getPort()), "Port " + instanceConfig.getPort() + " is not available, cannot start app " + appConfig.key());
        RunnableInstance runnableInstance = new RunnableInstance(this.configManager, appConfig, instanceConfig, jarPath);
        Thread thread = new Thread(runnableInstance);
        thread.start();
        instanceConfig.setRunnableInstance(Optional.of(runnableInstance));
        instanceConfig.setThread(Optional.of(thread));
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
}
