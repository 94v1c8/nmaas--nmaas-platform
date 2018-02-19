package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileTemplateRepository;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test-compose.properties")
public class DockerComposeFileTemplateAdminRestControllerTest extends BaseControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private Filter springSecurityFilterChain;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DockerComposeFileTemplateRepository templateRepository;

    private MockMvc mvc;
    private Long appId;
    private String templateContent = "some test template content";

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
        Application application = new Application("testApp");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        appId = applicationRepository.save(application).getId();
    }

    @After
    public void cleanRepository() {
        applicationRepository.deleteAll();
    }

    @Test
    @Transactional
    public void shouldStoreAndLoadTemplate() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_SUPERADMIN);
        assertThat(templateRepository.count(), equalTo(0L));
        mvc.perform(post("/platform/api/management/apps/{appId}/dockercompose/template", appId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson()))
                .andExpect(status().isCreated());
        assertThat(templateRepository.count(), equalTo(1L));
        assertThat(applicationRepository.findOne(appId).getAppDeploymentSpec().getDockerComposeFileTemplate().getComposeFileTemplateContent(),
                equalTo(templateContent));
        MvcResult result = mvc.perform(get("/platform/api/management/apps/{appId}/dockercompose/template", appId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), is(notNullValue()));
        applicationRepository.deleteAll();
        assertThat(templateRepository.count(), equalTo(0L));
    }

    @Test
    @Transactional
    public void shouldReturnProperCodesOnExceptions() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_SUPERADMIN);
        mvc.perform(get("/platform/api/management/apps/{appId}/dockercompose/template", 100)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        Application application = new Application("testApp2");
        appId = applicationRepository.save(application).getId();
        mvc.perform(get("/platform/api/management/apps/{appId}/dockercompose/template", appId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        applicationRepository.save(application);
        mvc.perform(get("/platform/api/management/apps/{appId}/dockercompose/template", appId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        mvc.perform(get("/platform/api/management/apps/{appId}/dockercompose/template", appId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String templateJson() {
        return "{" +
                "  \"composeFileTemplateContent\":\"" + templateContent + "\"" +
                "}";
    }
}
