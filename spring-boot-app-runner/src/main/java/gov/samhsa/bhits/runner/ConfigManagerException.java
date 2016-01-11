package gov.samhsa.bhits.runner;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ConfigManagerException extends RuntimeException {
    public ConfigManagerException(String message) {
        super(message);
    }

    public ConfigManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigManagerException(Throwable cause) {
        super(cause);
    }
}
