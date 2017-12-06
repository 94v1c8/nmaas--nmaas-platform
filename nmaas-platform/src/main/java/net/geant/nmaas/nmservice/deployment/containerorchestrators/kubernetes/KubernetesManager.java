package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements service deployment mechanism on Kubernetes cluster.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class KubernetesManager implements ContainerOrchestrator {

    static final String HELM_INSTALL_OPTION_PERSISTENCE_NAME = "persistence.name";
    static final String HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS = "persistence.storageClass";
    static final String HELM_INSTALL_OPTION_NMAAS_CONFIG_REPOURL = "nmaas.config.repourl";

    @Autowired
    private KubernetesNmServiceRepositoryManager repositoryManager;

    @Autowired
    private HelmCommandExecutor helmCommandExecutor;

    @Value("${kubernetes.persistence.class}")
    private String kubernetesPersistenceStorageClass;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId, AppDeploymentSpec appDeploymentSpec)
            throws NmServiceRequestVerificationException {
        if(!appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.KUBERNETES))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
        repositoryManager.storeService(new KubernetesNmServiceInfo(deploymentId, applicationId, clientId, KubernetesTemplate.copy(appDeploymentSpec.getKubernetesTemplate())));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        // nothing to do
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        // nothing to do
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            KubernetesNmServiceInfo serviceInfo = repositoryManager.loadService(deploymentId);
            KubernetesTemplate template = serviceInfo.getKubernetesTemplate();
            String repoUrl = serviceInfo.getGitLabProject().getCloneUrl();
            Map<String, String> arguments = new HashMap<>();
            arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_NAME, deploymentId.value());
            arguments.put(HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, kubernetesPersistenceStorageClass);
            arguments.put(HELM_INSTALL_OPTION_NMAAS_CONFIG_REPOURL, repoUrl);
            helmCommandExecutor.executeHelmInstallCommand(
                    deploymentId,
                    template.getArchive(),
                    arguments
            );
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new CouldNotDeployNmServiceException("Helm command execution failed -> " + commandExecutionException.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        try {
            HelmPackageStatus status = helmCommandExecutor.executeHelmStatusCommand(deploymentId);
            if (!status.equals(HelmPackageStatus.DEPLOYED))
                throw new ContainerCheckFailedException("Helm package is not deployed");
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerCheckFailedException("Helm command execution failed -> " + commandExecutionException.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            helmCommandExecutor.executeHelmDeleteCommand(deploymentId);
        } catch (CommandExecutionException commandExecutionException) {
            throw new CouldNotRemoveNmServiceException("Helm command execution failed -> " + commandExecutionException.getMessage());
        }
    }

    @Override
    public String info() {
        return "Kubernetes Container Orchestrator";
    }

    @Override
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException {
        return null;
    }

}
