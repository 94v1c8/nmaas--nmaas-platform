package net.geant.nmaas.servicedeployment.exceptions;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CouldNotConnectContainerToNetworkException extends Exception {

    public CouldNotConnectContainerToNetworkException(String message) {
        super(message);
    }

    public CouldNotConnectContainerToNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

}
