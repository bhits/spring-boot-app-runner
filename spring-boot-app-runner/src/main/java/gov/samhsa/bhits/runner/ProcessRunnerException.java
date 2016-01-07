package gov.samhsa.bhits.runner;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ProcessRunnerException extends RuntimeException {
    public ProcessRunnerException(String message) {
        super(message);
    }

    public ProcessRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessRunnerException(Throwable cause) {
        super(cause);
    }
}