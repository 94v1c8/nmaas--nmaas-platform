package net.geant.nmaas.externalservices.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.api.DockerHostManagerRestController;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryInit;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.repositories.DockerHostRepository;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerHostManagerRestControllerTest {

    private final static String THIRD_HOST_NAME = "GN4-DOCKER-3";
    private final static String FOURTH_HOST_NAME = "GN4-DOCKER-1";
    private final static String NEW_DOCKER_HOST_NAME = "GN4-DOCKER-X";
    private final static String WRONG_DOCKER_HOST_NAME = "WRONG-DOCKER-HOST-NAME";
    private final static String URL_PREFIX = "/platform/api/management/dockerhosts";

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private MockMvc mvc;

    @Before
    public void init() {
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
        mvc = MockMvcBuilders.standaloneSetup(new DockerHostManagerRestController(dockerHostRepositoryManager)).build();
    }

    @After
    public void clean() {
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
    }

    @Test
    public void shouldAddAndRemoveNewDockerHost() throws Exception {
        int sizeBefore = dockerHostRepositoryManager.loadAll().size();
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost(NEW_DOCKER_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(sizeBefore + 1, dockerHostRepositoryManager.loadAll().size());
        mvc.perform(delete(URL_PREFIX + "/{name}", NEW_DOCKER_HOST_NAME))
                .andExpect(status().isNoContent());
        assertEquals(sizeBefore, dockerHostRepositoryManager.loadAll().size());
    }

    @Test
    public void shouldNotAddExistingDockerHost() throws Exception {
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost(FOURTH_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldNotRemoveNotExistingDockerHost() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/{name}", WRONG_DOCKER_HOST_NAME))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void shouldUpdateDockerHost() throws Exception {
        mvc.perform(put(URL_PREFIX + "/{name}", THIRD_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost(THIRD_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldNotUpdateNotExistingDockerHost() throws Exception {
        mvc.perform(put(URL_PREFIX + "/{name}", WRONG_DOCKER_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost(WRONG_DOCKER_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateDockerHostWithWrongName() throws Exception {
        mvc.perform(put(URL_PREFIX + "/{name}", THIRD_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost(WRONG_DOCKER_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldListAllHDockerHosts() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();
        //noinspection unchecked
        assertEquals(
                dockerHostRepositoryManager.loadAll().size(),
                ((List<DockerHost>) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<List<DockerHost>>() {})).size());
    }

    @Test
    public void shouldFetchDockerHostByName() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX + "/{name}", FOURTH_HOST_NAME))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                "GN4-DOCKER-1",
                ((DockerHost) new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<DockerHost>() {})).getName());
    }

    @Test
    public void shouldNotFetchNotExistingDockerHostByName() throws Exception {
        mvc.perform(get(URL_PREFIX + "/{name}", WRONG_DOCKER_HOST_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldFetchPreferredDockerHost() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX + "/firstpreferred"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                "GN4-DOCKER-1",
                ((DockerHost) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<DockerHost>() {})).getName());
    }

    private DockerHost initNewDockerHost(String dockerHostName) throws UnknownHostException {
        return new DockerHost(
                dockerHostName,
                InetAddress.getByName("192.168.0.1"),
                9999,
                InetAddress.getByName("192.168.0.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("192.168.1.1"),
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                false);
    }
}
