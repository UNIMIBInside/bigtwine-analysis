package it.unimib.disco.bigtwine.services.analysis.validation;

import javax.validation.ValidationException;

public class InvalidAnalysisInputProvidedException extends ValidationException {
    public InvalidAnalysisInputProvidedException(String message) {
        super(message);
    }
}
