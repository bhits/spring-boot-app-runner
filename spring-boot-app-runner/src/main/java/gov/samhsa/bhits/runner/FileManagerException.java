package gov.samhsa.bhits.runner;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileManagerException extends RuntimeException {
    public FileManagerException(String message) {
        super(message);
    }
    public FileManagerException(String message, Throwable cause) {
        super(message, cause);
    }
    public FileManagerException(Throwable cause) {
        super(cause);
    }
}
