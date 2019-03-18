package net.geant.nmaas.portal.service.impl;

import java.util.Arrays;
import java.util.Collections;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationTemplateService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ConfigTemplate;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationServiceImpl applicationService;

    @Mock
    NmServiceConfigurationTemplateService templateService;

    private ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    public void setup(){
        applicationService = new ApplicationServiceImpl(applicationRepository, templateService, new ModelMapper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowExceptionDueToNullRequest(){
        applicationService.create(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToIncorrectName(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setName("");
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToIncorrectVersion(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setVersion("");
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToIncorrectOwner(){
        ApplicationView applicationView = getDefaultAppView();
        applicationService.create(applicationView, null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionDueToIncorrectNameAndVersion(){
        ApplicationView applicationView = getDefaultAppView();
        when(applicationRepository.existsByNameAndVersion(applicationView.getName(), applicationView.getVersion())).thenReturn(true);
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToAppDeploymentSpec(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setAppDeploymentSpec(null);
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToIncorrectConfigTemplate(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setConfigTemplate(null);
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToIncorrectDescriptions(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setDescriptions(null);
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDueToEmptyDescriptions(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setDescriptions(Collections.emptyList());
        applicationService.create(applicationView, "admin");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenEnglishDescriptionIsMissing(){
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("pl", "test", "test")));
        applicationService.create(applicationView, "admin");
    }

    @Test
    public void createMethodShouldReturnApplicationObject(){
        Application application = new Application("test","testversion","owner");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        ApplicationView applicationView = getDefaultAppView();
        Application result = applicationService.create(applicationView,"owner");
        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    @Test
    public void shouldAddMissingDescriptions(){
        Application application = new Application("test","testversion","owner");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setDescriptions(Arrays.asList(new AppDescriptionView("pl", "", ""), new AppDescriptionView("en", "test", "testfull")));
        Application result = applicationService.create(applicationView,"owner");
        assertTrue(StringUtils.isNotEmpty(result.getDescriptions().get(1).getBriefDescription()));
        assertTrue(StringUtils.isNotEmpty(result.getDescriptions().get(1).getFullDescription()));
        assertEquals(result.getDescriptions().get(1).getBriefDescription(), result.getDescriptions().get(0).getBriefDescription());
        assertEquals(result.getDescriptions().get(1).getFullDescription(), result.getDescriptions().get(0).getFullDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.update(null);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToEmptyName(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setName("");
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToEmptyVersion(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setVersion("");
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToEmptyOwner(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setOwner("");
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullAppDeploymentSpec(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setAppDeploymentSpec(null);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullConfigTemplate(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setConfigTemplate(null);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToEmptyConfigTemplate(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setConfigTemplate(new ConfigTemplate(""));
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullDescriptions(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setDescriptions(null);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToEmptyDescriptions(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setDescriptions(Collections.emptyList());
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullKubernetesTemplate(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.getAppDeploymentSpec().setKubernetesTemplate(null);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToDefaultStorageSpaceLowerThanZero(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.getAppDeploymentSpec().setDefaultStorageSpace(-3);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullDefaultStorageSpace(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.getAppDeploymentSpec().setDefaultStorageSpace(null);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullKubernetesChart(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.getAppDeploymentSpec().getKubernetesTemplate().setChart(null);
        applicationService.update(app);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToEmptyKubernetesChartName(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.getAppDeploymentSpec().getKubernetesTemplate().getChart().setName("");
        applicationService.update(app);
    }

    @Test
    public void updateMethodShouldReturnApplicationObject(){
        Application application = new Application("test", "testversion","owner");
        application.setId(1L);
        application.setLicense("MIT");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        application.setLicense("Apache-2.0");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.getAppDeploymentSpec().setDefaultStorageSpace(1);
        application.getAppDeploymentSpec().setKubernetesTemplate(new KubernetesTemplate());
        application.getAppDeploymentSpec().getKubernetesTemplate().setChart(new KubernetesChart("chart", "version"));
        application.setConfigTemplate(new ConfigTemplate("test-template"));
        application.setDescriptions(Collections.singletonList(new AppDescription()));
        Application result = applicationService.update(application);
        assertNotNull(result);
        assertNotEquals("MIT", result.getLicense());
    }

    @Test
    public void deleteMethodShouldTrowExceptionDueToNullPassedAsId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.delete(null);
        });
    }

    @Test
    public void deleteMethodShouldSetApplicationAsDeleted(){
        Application application = new Application("test", "testversion","owner");
        application.setId((long) 0);
        application.setState(ApplicationState.ACTIVE);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        applicationService.delete((long) 0);
        verify(applicationRepository).findById(anyLong());
        verify(applicationRepository).save(isA(Application.class));
    }

    @Test
    public void findApplicationShouldThrowExceptionDueToNullId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.findApplication(null);
        });
    }

    @Test
    public void findApplicationShouldReturnApplicationObject(){
        Application application = new Application("test", "testversion","owner");
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        Optional<Application> result = applicationService.findApplication((long) 0);
        assertTrue(result.isPresent());
    }

    @Test
    public void findAllShouldReturnList(){
        List<Application> testList = new ArrayList<>();
        Application test = new Application("test", "testversion","owner");
        testList.add(test);
        when(applicationRepository.findAll()).thenReturn(testList);
        List<Application> result = applicationService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldChangeApplicationState(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setState(ApplicationState.NEW);
        applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
        verify(applicationRepository, times(1)).save(any());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotChangeApplicationStateDueToForbiddenStateChange(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setState(ApplicationState.DELETED);
        applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
    }

    private ApplicationView getDefaultAppView(){
        ApplicationView applicationView = new ApplicationView();
        applicationView.setName("test");
        applicationView.setVersion("testversion");
        applicationView.setOwner("owner");
        applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("en", "test", "testfull")));
        net.geant.nmaas.portal.api.domain.AppDeploymentSpec appDeploymentSpec = new net.geant.nmaas.portal.api.domain.AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplateView(new KubernetesChartView("name", "version"), "archive"));
        appDeploymentSpec.setDefaultStorageSpace(1);
        applicationView.setAppDeploymentSpec(appDeploymentSpec);
        applicationView.setConfigTemplate(new net.geant.nmaas.portal.api.domain.ConfigTemplate("template"));
        return applicationView;
    }

}
