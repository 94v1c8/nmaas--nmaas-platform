package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpec;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultAppLifecycleManagerTest {

    @Autowired
    private DefaultAppLifecycleManager appLifecycleManager;

    @MockBean
    private ApplicationRepository appRepository;
    @MockBean
    AppDeploymentRepository appDepRepository;
    @MockBean
    AppDeploymentRepositoryManager appDepRepositoryManager;

    @Test
    public void shouldGenerateProperIdentifier() {
        when(appDepRepositoryManager.load(Matchers.any())).thenReturn(Optional.empty());
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

    @Test
    public void shouldFailToDeployApplicationInstance() throws InterruptedException {
        when(appRepository.findById(1L)).thenReturn(Optional.of(new Application("appName")));
        when(appDepRepository.findByDeploymentId(Matchers.any())).thenReturn(Optional.of(appDeployment()));
        when(appDepRepositoryManager.load(Matchers.any())).thenReturn(Optional.empty());
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setConfigFileRepositoryRequired(true);
        appDeploymentSpec.setDefaultStorageSpace(20);
        AppDeployment appDeployment = AppDeployment.builder().applicationId(Identifier.newInstance(1L)).domain("domain1").deploymentName("deploymentName").storageSpace(appDeploymentSpec.getDefaultStorageSpace()).build();
        appLifecycleManager.deployApplication(appDeployment);
        Thread.sleep(200);
    }

    private AppDeployment appDeployment() {
        return AppDeployment.builder()
                .deploymentId(Identifier.newInstance(1L))
                .domain("domain")
                .applicationId(Identifier.newInstance("app"))
                .deploymentName("deploy")
                .configFileRepositoryRequired(false)
                .storageSpace(20).build();
    }

}
