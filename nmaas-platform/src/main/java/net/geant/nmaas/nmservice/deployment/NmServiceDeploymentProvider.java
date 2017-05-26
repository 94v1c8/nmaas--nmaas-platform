package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;

/**
 * Defines a set of methods to manage NM service deployment lifecycle.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceDeploymentProvider {

    /**
     * Creates new object representing the NM service deployment and verifies if the request can be executed.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param clientId identifier of the client requesting the deployment
     * @param template additional information specific to given application deployment
     * @throws NmServiceRequestVerificationException if service can't be deployed or some input parameters are missing
     */
    void verifyRequest(Identifier deploymentId, Identifier applicationId, Identifier clientId, AppDeploymentSpec template) throws NmServiceRequestVerificationException;

    /**
     * Coordinates deployment environment preparation (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotPrepareEnvironmentException if environment could't be prepared for some reason
     */
    void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareEnvironmentException;

    /**
     * Coordinates NM service deployment (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotDeployNmServiceException if NM service couldn't be deployed for some reason
     */
    void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException;

    /**
     * Coordinates NM service deployment verification (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotVerifyNmServiceException if NM service deployment verification failed
     */
    void verifyNmService(Identifier deploymentId) throws CouldNotVerifyNmServiceException;

    /**
     * Coordinates NM service removal (delegates tasks to attached {@link ContainerOrchestrator}).
     *
     * @param deploymentId unique identifier of service deployment
     * @throws CouldNotRemoveNmServiceException if NM service couldn't be removed for some reason
     */
    void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException;

}