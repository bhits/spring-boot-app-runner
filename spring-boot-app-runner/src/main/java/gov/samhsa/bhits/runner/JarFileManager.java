package gov.samhsa.bhits.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JarFileManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigManager configManager;

    public void saveFile(AppConfig appConfig, MultipartFile file) {
        String name = appConfig.jarName();
        String jarFilePathString = jarFilePath(appConfig);
        Path jarFilePath = Paths.get(jarFilePathString);
        if (!file.isEmpty()) {
            try {
                deleteFile(appConfig);
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(jarFilePathString)));
                stream.write(bytes);
                stream.close();
                logger.info("Saved " + jarFilePath + "!");
            } catch (Exception e) {
                String err = "Failed to save " + jarFilePath + " => " + e.getMessage();
                logger.error(err);
                throw new JarFileManagerException(err, e);
            }
        } else {
            String err = "Failed to save " + jarFilePath + " because the file was empty.";
            logger.error(err);
            throw new JarFileManagerException(err);
        }
    }

    public void deleteFile(AppConfig appConfig) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Failed sleeping before deleting the jar file", e);
        }
        Path filePath = Paths.get(jarFilePath(appConfig));
        AtomicInteger counter = new AtomicInteger(0);
        Optional<JarFileManagerException> error = Optional.empty();
        while (Files.exists(filePath) && counter.incrementAndGet() < 5) {
            try {
                logger.info("Trying to delete " + filePath + "; trial: " + counter.get());
                Files.deleteIfExists(filePath);
                logger.info("Deleted " + filePath + " if it already existed");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                error = Optional.of(new JarFileManagerException(e));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        error.ifPresent(e -> {
            if (Files.exists(filePath)) {
                logger.error("Could not delete " + filePath + " in " + counter.get() + " trials");
                throw e;
            }
        });
    }

    private String jarFilePath(AppConfig appConfig) {
        return configManager.getConfigFolderBasePath() + appConfig.jarName();
    }
}
