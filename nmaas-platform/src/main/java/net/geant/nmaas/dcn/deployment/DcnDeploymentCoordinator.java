package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.*;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.repositories.DockerNetworkRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.*;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DcnDeploymentCoordinator implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private final static Logger log = LogManager.getLogger(DcnDeploymentCoordinator.class);

    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DcnRepositoryManager dcnRepositoryManager;

    private ApplicationEventPublisher applicationEventPublisher;

    private AnsiblePlaybookVpnConfigRepository vpnConfigRepository;

    private DockerNetworkRepository dockerNetworkRepository;

    private DockerApiClient dockerApiClient;

    @Value("${ansible.docker.api.url}")
    private String ansibleDockerApiUrl;

    @Autowired
    public DcnDeploymentCoordinator(DcnRepositoryManager dcnRepositoryManager,
                                    AnsiblePlaybookVpnConfigRepository vpnConfigRepository,
                                    ApplicationEventPublisher applicationEventPublisher,
                                    DockerNetworkRepository dockerNetworkRepository,
                                    DockerApiClient dockerApiClient) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.vpnConfigRepository = vpnConfigRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dockerNetworkRepository = dockerNetworkRepository;
        this.dockerApiClient = dockerApiClient;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public boolean checkIfExists(Identifier clientId) {
        try {
            dcnRepositoryManager.loadCurrentState(clientId);
            return true;
        } catch (InvalidClientIdException e) {
            return false;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier clientId, DcnSpec dcnSpec) throws DcnRequestVerificationException {
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
        try {
            final DockerNetwork dockerNetwork = dockerNetworkRepository.findByClientId(dcnSpec.getClientId()).orElseThrow(() -> new InvalidClientIdException());
            final DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(dockerNetwork);
            dcnRepositoryManager.updateDcnCloudEndpointDetails(clientId, dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(clientId, vpnConfigRepository.loadDefaultCustomerVpnConfig());
            AnsiblePlaybookVpnConfig cloudSideRouterVpnConfig = vpnConfigRepository.loadDefaultCloudVpnConfig();
            cloudSideRouterVpnConfig.merge(dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(clientId, cloudSideRouterVpnConfig);
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFIED);
        } catch ( InvalidClientIdException
                | AnsiblePlaybookVpnConfigNotFoundException e) {
            log.error("Exception during DCN request verification -> " + e.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
            throw new DcnRequestVerificationException("Exception during DCN request verification -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(Identifier clientId) throws CouldNotDeployDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(clientId);
            removeOldAnsiblePlaybookContainers();
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfig(dcnInfo.getAnsiblePlaybookForClientSideRouter(), encodeForClientSideRouter(clientId.value())),
                    buildContainerForCloudSideRouterConfig(dcnInfo.getAnsiblePlaybookForCloudSideRouter(), encodeForCloudSideRouter(clientId.value())));
            notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYMENT_INITIATED);
        } catch ( InvalidClientIdException
                | InterruptedException
                | DockerException anyException) {
            log.error("Exception during DCN deployment -> " + anyException.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYMENT_FAILED);
            throw new CouldNotDeployDcnException("Exception during DCN deployment -> " + anyException.getMessage());
        }
    }

    private void removeOldAnsiblePlaybookContainers() {
        try {
            final List<Container> containers = dockerApiClient.listContainers(ansibleDockerApiUrl, DockerClient.ListContainersParam.withStatusExited());
            for (Container container : containers) {
                log.debug("Removing old container " + container.id());
                dockerApiClient.removeContainer(ansibleDockerApiUrl, container.id());
            }
        } catch (DockerException
                | InterruptedException e) {
            log.warn("Failed to remove old Ansible containers", e);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(Identifier clientId) throws CouldNotVerifyDcnException {
        try {
            notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFICATION_INITIATED);
            // TODO implement DCN verification functionality
            Thread.sleep(1000);
            notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFIED);
        } catch (InterruptedException e) {
            notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFICATION_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(Identifier clientId) throws CouldNotRemoveDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(clientId);
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfigRemoval(dcnInfo.getAnsiblePlaybookForClientSideRouter(), encodeForClientSideRouter(clientId.value())),
                    buildContainerForCloudSideRouterConfigRemoval(dcnInfo.getAnsiblePlaybookForCloudSideRouter(), encodeForCloudSideRouter(clientId.value())));
            notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVAL_INITIATED);
        } catch ( InvalidClientIdException
                | InterruptedException
                | DockerException e) {
            log.error("Exception during DCN removal -> " + e.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVAL_FAILED);
            throw new CouldNotRemoveDcnException("Exception during DCN removal -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier clientId, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, clientId, state));
    }

    private void deployAnsiblePlaybookContainers(ContainerConfig... ansibleContainerConfigs) throws DockerException, InterruptedException {
        for (ContainerConfig containerConfig : ansibleContainerConfigs) {
            String ansibleContainerId = dockerApiClient.createContainer(ansibleDockerApiUrl, containerConfig, ansibleContainerName());
            dockerApiClient.startContainer(ansibleDockerApiUrl, ansibleContainerId);
        }
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyPlaybookExecutionState(String encodedPlaybookIdentifier, AnsiblePlaybookStatus.Status status) {
        Identifier clientId = null;
        try {
            clientId = Identifier.newInstance(decode(encodedPlaybookIdentifier));
            DcnDeploymentState currentDcnDeploymentState = dcnRepositoryManager.loadCurrentState(clientId);
            DcnDeploymentState newDcnDeploymentState = null;
            switch (status) {
                case SUCCESS:
                    switch(currentDcnDeploymentState) {
                        case DEPLOYMENT_INITIATED:
                            if (wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case REMOVAL_INITIATED:
                            if (wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.REMOVED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.REMOVED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        default:
                            newDcnDeploymentState = DcnDeploymentState.ERROR;
                    }
                    break;
                case FAILURE:
                default:
                    newDcnDeploymentState = deploymentOrRemovalFailureDependingOnLastState(currentDcnDeploymentState);
            }
             notifyStateChangeListeners(clientId, newDcnDeploymentState);
        } catch ( InvalidClientIdException
                | AnsiblePlaybookIdentifierConverterException e) {
            log.error("Exception during playbook execution state reception -> " + e.getMessage());
            notifyStateChangeListeners(clientId, DcnDeploymentState.ERROR);
        }
    }

    DcnDeploymentState deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState currentDcnDeploymentState) {
        switch (currentDcnDeploymentState) {
            case DEPLOYMENT_INITIATED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return DcnDeploymentState.DEPLOYMENT_FAILED;
            case REMOVAL_INITIATED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return DcnDeploymentState.REMOVAL_FAILED;
            default:
                return DcnDeploymentState.ERROR;
        }
    }

    String getAnsibleDockerApiUrl() {
        return ansibleDockerApiUrl;
    }

    void setAnsibleDockerApiUrl(String ansibleDockerApiUrl) {
        this.ansibleDockerApiUrl = ansibleDockerApiUrl;
    }
}
