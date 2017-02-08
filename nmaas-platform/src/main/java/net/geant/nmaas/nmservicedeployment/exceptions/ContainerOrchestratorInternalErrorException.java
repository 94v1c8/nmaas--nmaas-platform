package net.geant.nmaas.nmservicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerOrchestratorInternalErrorException extends Exception {

    public ContainerOrchestratorInternalErrorException(String message) {
        super(message);
    }

    public ContainerOrchestratorInternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
