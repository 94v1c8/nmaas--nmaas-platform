package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKClusterValidator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.DefaultIngressControllerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.DefaultIngressResourceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesManagerCheckServiceTest {

    private KubernetesManager manager;
    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private DefaultKClusterValidator clusterValidator = mock(DefaultKClusterValidator.class);
    private KServiceLifecycleManager serviceLifecycleManager = mock(HelmKServiceManager.class);
    private KServiceOperationsManager serviceOperationsManager = mock(DefaultKServiceOperationsManager.class);
    private KClusterIngressManager clusterIngressManager = mock(KClusterIngressManager.class);
    private IngressControllerManager ingressControllerManager = mock(DefaultIngressControllerManager.class);
    private IngressResourceManager ingressResourceManager = mock(DefaultIngressResourceManager.class);
    private KClusterDeploymentManager deploymentManager = mock(KClusterDeploymentManager.class);
    private GitLabManager gitLabManager = mock(GitLabManager.class);
    private JanitorService janitorService = mock(JanitorService.class);

    @Before
    public void setup() {
        manager = new KubernetesManager(repositoryManager,
                clusterValidator,
                serviceLifecycleManager,
                serviceOperationsManager,
                clusterIngressManager,
                ingressControllerManager,
                ingressResourceManager,
                deploymentManager,
                gitLabManager,
                janitorService);

        KubernetesNmServiceInfo serviceMock = mock(KubernetesNmServiceInfo.class);
        when(repositoryManager.loadService(any())).thenReturn(serviceMock);
        when(serviceMock.getDomain()).thenReturn("domain");
    }

    @Test
    public void shouldVerifyThatServiceIsDeployed() throws Exception {
        when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(true);
        when(janitorService.checkIfReady(any(), any())).thenReturn(true);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

    @Test(expected = ContainerCheckFailedException.class)
    public void shouldThrowExceptionSinceServiceNotDeployed() throws Exception {
        when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(false);
        when(janitorService.checkIfReady(any(), any())).thenReturn(false);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

}
