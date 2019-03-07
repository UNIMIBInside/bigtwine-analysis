package it.unimib.disco.bigtwine.services.analysis.web.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException() {
        super("User not authenticated");
    }

    public UnauthenticatedException(String message) {
        super(message);
    }
}
