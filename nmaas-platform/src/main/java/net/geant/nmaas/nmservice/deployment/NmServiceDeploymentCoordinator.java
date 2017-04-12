package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState.*;

@Component
public class NmServiceDeploymentCoordinator implements NmServiceDeploymentProvider {

    private final static Logger log = LogManager.getLogger(NmServiceDeploymentCoordinator.class);

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @Autowired
    private NmServiceRepository serviceRepository;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Loggable(LogLevel.INFO)
    public NmServiceInfo verifyRequest(Identifier deploymentId, NmServiceSpec nmServiceSpec) throws NmServiceRequestVerificationException {
        final String nmServiceName = ((DockerContainerSpec) nmServiceSpec).getName();
        deploymentIdMapper.storeMapping(deploymentId, nmServiceName);
        serviceRepository.storeService(new NmServiceInfo(nmServiceName, INIT, nmServiceSpec));
        try {
            serviceRepository.updateServiceAppDeploymentId(nmServiceName, deploymentId.value());
            orchestrator.verifyRequestObtainTargetHostAndNetworkDetails(nmServiceName);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFIED);
            return serviceRepository.loadService(nmServiceName);
        } catch (CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException
                | NmServiceRequestVerificationException
                | NmServiceRepository.ServiceNotFoundException e) {
            log.error("NM Service request verification failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED);
            throw new NmServiceRequestVerificationException(e.getMessage());
        } catch (Exception e) {
            log.error("NM Service request verification failed -> Unknown exception", e);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED);
            throw new NmServiceRequestVerificationException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareEnvironmentException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.prepareDeploymentEnvironment(serviceName);
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | CouldNotPrepareEnvironmentException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            log.error("NM Service deployment environment preparation failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_FAILED);
            throw new CouldNotPrepareEnvironmentException("NM Service deployment environment preparation failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_INITIATED);
            orchestrator.deployNmService(serviceName);
            notifyStateChangeListeners(deploymentId, DEPLOYED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | CouldNotDeployNmServiceException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            log.error("NM Service deployment failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_FAILED);
            throw new CouldNotDeployNmServiceException("NM Service deployment failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyNmService(Identifier deploymentId) throws CouldNotVerifyNmServiceException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.checkService(serviceName);
            notifyStateChangeListeners(deploymentId, VERIFIED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | ContainerCheckFailedException
                | ContainerNetworkCheckFailedException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            log.error("NM Service deployment verification failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, VERIFICATION_FAILED);
            throw new CouldNotVerifyNmServiceException("NM Service deployment verification failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.removeNmService(serviceName);
            notifyStateChangeListeners(deploymentId, REMOVED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | CouldNotRemoveNmServiceException
                | ContainerOrchestratorInternalErrorException
                | CouldNotConnectToOrchestratorException e) {
            log.error("NM Service removal failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, REMOVAL_FAILED);
            throw new CouldNotRemoveNmServiceException("NM Service removal failed -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
