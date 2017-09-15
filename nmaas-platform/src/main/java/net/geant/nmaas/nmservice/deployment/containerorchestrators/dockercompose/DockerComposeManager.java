package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DcnAttachedContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeServiceComponent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.InternalErrorException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkResourceManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("docker-compose")
public class DockerComposeManager implements ContainerOrchestrator {

    @Autowired
    private NmServiceRepositoryManager repositoryManager;
    @Autowired
    private DockerHostRepositoryManager dockerHosts;
    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;
    @Autowired
    private DockerComposeFilePreparer composeFilePreparer;
    @Autowired
    private DockerComposeCommandExecutor composeCommandExecutor;
    @Autowired
    private DockerNetworkLifecycleManager dockerNetworkLifecycleManager;
    @Autowired
    private DockerNetworkResourceManager dockerNetworkResourceManager;
    @Autowired
    private StaticRoutingConfigManager routingConfigManager;
    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupport(List<AppDeploymentEnv> supportedDeploymentEnvironments)
            throws NmServiceRequestVerificationException {
        if(!supportedDeploymentEnvironments.contains(AppDeploymentEnv.DOCKER_COMPOSE))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            final Identifier clientId = repositoryManager.loadClientId(deploymentId);
            declareNewNetworkForClientIfNotExists(clientId);
            final DockerHostNetwork network = dockerNetworkLifecycleManager.networkForClient(clientId);
            repositoryManager.updateDockerHost(deploymentId, network.getHost());
            String assignedHostVolume = constructHostVolumeDirectoryName(network.getHost().getVolumesPath(), deploymentId.value());
            DockerComposeService dockerComposeService = new DockerComposeService();
            dockerComposeService.setAttachedVolumeName(assignedHostVolume);
            dockerComposeService.setPublicPort(dockerNetworkResourceManager.obtainPortForClientNetwork(clientId, deploymentId));
            repositoryManager.updateDockerComposeService(deploymentId, dockerComposeService);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment -> " + dockerHostNotFoundException.getMessage());
        }
    }

    private void declareNewNetworkForClientIfNotExists(Identifier clientId)
            throws ContainerOrchestratorInternalErrorException, DockerHostNotFoundException {
        if (!dockerNetworkLifecycleManager.networkForClientAlreadyConfigured(clientId))
            dockerNetworkLifecycleManager.declareNewNetworkForClientOnHost(clientId, dockerHosts.loadPreferredDockerHost());
    }

    private String constructHostVolumeDirectoryName(String baseVolumePath, String deploymentDirectory) {
        return baseVolumePath + "/" + deploymentDirectory + "-1";
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            String networkName = deployNetworkForClientOnDockerHostIfNotDoneBefore(service);
            service.getDockerComposeService().setDcnNetworkName(networkName);
            DockerComposeFileTemplate dockerComposeFileTemplate = loadDockerComposeFileTemplateForApplication(service.getApplicationId());
            for(DcnAttachedContainer container : dockerComposeFileTemplate.getDcnAttachedContainers()) {
                DockerComposeServiceComponent component = new DockerComposeServiceComponent();
                component.setName(container.getName());
                component.setDeploymentName(deploymentId.value() + "-" + container.getName());
                component.setDescription(container.getDescription());
                component.setIpAddressOfContainer(dockerNetworkResourceManager.assignNewIpAddressForContainer(service.getClientId()));
                service.getDockerComposeService().getServiceComponents().add(component);
            }
            repositoryManager.updateDockerComposeService(deploymentId, service.getDockerComposeService());
            buildAndStoreComposeFile(service.getDeploymentId(), service.getDockerComposeService(), dockerComposeFileTemplate);
            downloadComposeFileOnDockerHost(service);
            downloadContainerImageOnDockerHost(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        } catch (DockerComposeFileTemplateHandlingException dockerComposeFileTemplateHandlingException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred during Docker Compose file preparation -> " + dockerComposeFileTemplateHandlingException.getMessage());
        } catch (DockerComposeFileTemplateNotFoundException
                | InternalErrorException exception) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred while loading Docker Compose template file -> " + exception.getMessage());
        }
    }

    private NmServiceInfo loadService(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repositoryManager.loadService(deploymentId);
    }

    private String deployNetworkForClientOnDockerHostIfNotDoneBefore(NmServiceInfo service)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        return dockerNetworkLifecycleManager.deployNetworkForClient(service.getClientId());
    }

    private DockerComposeFileTemplate loadDockerComposeFileTemplateForApplication(Identifier applicationId)
            throws DockerComposeFileTemplateNotFoundException, InternalErrorException {
        Application application = applicationRepository.findOne(applicationId.longValue());
        if (application == null)
            throw new InternalErrorException("Application with id " + applicationId + " not found in repository");
        AppDeploymentSpec appDeploymentSpec = application.getAppDeploymentSpec();
        if (appDeploymentSpec == null)
            throw new InternalErrorException("Application deployment spec for application with id " + applicationId + " is not set");
        DockerComposeFileTemplate template = appDeploymentSpec.getDockerComposeFileTemplate();
        if (template == null)
            throw new DockerComposeFileTemplateNotFoundException(applicationId.value());
        return template;
    }

    private void buildAndStoreComposeFile(Identifier deploymentId, DockerComposeService service, DockerComposeFileTemplate dockerComposeFileTemplate)
            throws DockerComposeFileTemplateHandlingException, DockerComposeFileTemplateNotFoundException, InvalidDeploymentIdException, InternalErrorException {
        composeFilePreparer.buildAndStoreComposeFile(deploymentId, service, dockerComposeFileTemplate);
    }

    private void downloadComposeFileOnDockerHost(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeFileDownloadCommand(service.getDeploymentId(), service.getHost());
    }

    private void downloadContainerImageOnDockerHost(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposePullCommand(service.getDeploymentId(), service.getHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            deployContainers(service);
            configureRoutingOnStartedContainer(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem with docker compose command execution on remote host -> " + commandExecutionException.getMessage());
        }
    }

    private void deployContainers(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeUpCommand(service.getDeploymentId(), service.getHost());
    }

    private void configureRoutingOnStartedContainer(NmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException, CommandExecutionException {
        routingConfigManager.configure(service);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        // TODO implement relevant checks
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            stopAndRemoveContainers(service);
            removeNetworkIfNoContainerAttached(service.getClientId());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotRemoveNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute docker compose remove command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotRemoveContainerNetworkException couldNotRemoveContainerNetworkException) {
            throw new CouldNotRemoveNmServiceException(
                    "Failed to remove network -> " + couldNotRemoveContainerNetworkException.getMessage());
        }
    }

    private void stopAndRemoveContainers(NmServiceInfo service) throws CommandExecutionException, ContainerOrchestratorInternalErrorException {
        composeCommandExecutor.executeComposeStopCommand(service.getDeploymentId(), service.getHost());
        composeCommandExecutor.executeComposeRemoveCommand(service.getDeploymentId(), service.getHost());
        for(DockerComposeServiceComponent component : service.getDockerComposeService().getServiceComponents())
            dockerNetworkResourceManager.removeAddressAssignment(service.getClientId(), component.getIpAddressOfContainer());
    }

    private void removeNetworkIfNoContainerAttached(Identifier clientId)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        if (repositoryManager.loadAllRunningClientServices(clientId).isEmpty())
            dockerNetworkLifecycleManager.removeNetwork(clientId);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerCompose Container Orchestrator";
    }
}
