package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class GitLabInvalidConfigurationException extends RuntimeException {
    public GitLabInvalidConfigurationException(String message){
        super(message);
    }
}
