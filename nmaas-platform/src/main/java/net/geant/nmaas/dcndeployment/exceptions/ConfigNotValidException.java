package net.geant.nmaas.dcndeployment.exceptions;

public class ConfigNotValidException extends Exception {

    public ConfigNotValidException(String message) {
        super(message);
    }

    public ConfigNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

}
