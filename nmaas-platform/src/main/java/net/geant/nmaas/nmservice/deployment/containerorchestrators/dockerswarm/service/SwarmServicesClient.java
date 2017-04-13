package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClientFactory;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class SwarmServicesClient {

    public String deployService(ServiceSpec service, DockerSwarmManager swarmManager)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(swarmManager.getApiUri());
        try {
            ServiceCreateResponse response = apiClient.createService(service);
            return response.id();
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException("Could not create given service -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void destroyService(String serviceId, DockerSwarmManager swarmManager)
            throws CouldNotRemoveNmServiceException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(swarmManager.getApiUri());
        try {
            apiClient.removeService(serviceId);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotRemoveNmServiceException("Could not destroy service " + serviceId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public List<String> listServices(DockerSwarmManager swarmManager) throws ContainerOrchestratorInternalErrorException {
        DockerClient apiClient = DockerApiClientFactory.client(swarmManager.getApiUri());
        try {
            final List<com.spotify.docker.client.messages.swarm.Service> services = apiClient.listServices();
            return services.stream().map(s -> s.spec().name()).collect(Collectors.toList());
        } catch (DockerException dockerException) {
            throw new ContainerOrchestratorInternalErrorException("Could not connect to Docker Swarm -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new ContainerOrchestratorInternalErrorException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

}
