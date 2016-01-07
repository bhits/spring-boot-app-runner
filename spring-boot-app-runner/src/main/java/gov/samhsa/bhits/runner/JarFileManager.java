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

@Service
public class JarFileManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

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
            String err = "Failed to save "  + jarFilePath + " because the file was empty.";
            logger.error(err);
            throw new JarFileManagerException(err);
        }
    }

    public void deleteFile(AppConfig appConfig) {
        try {
            Path filePath = Paths.get(jarFilePath(appConfig));
            Files.deleteIfExists(filePath);
            logger.info("Deleted " + filePath + " if it already existed");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new JarFileManagerException(e.getMessage(), e);
        }
    }

    private String jarFilePath(AppConfig appConfig) {
        return configManager.getConfigFolderBasePath() + appConfig.jarName();
    }
}
