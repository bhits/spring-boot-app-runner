package gov.samhsa.bhits.runner;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class JarFileManagerException extends RuntimeException {
    public JarFileManagerException(String message) {
        super(message);
    }
    public JarFileManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
