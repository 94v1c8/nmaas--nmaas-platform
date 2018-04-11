package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkConfigBuilder;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.exceptions.DockerNetworkDetailsVerificationException;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerNetworkConfigBuildingTest {

    private DockerHostNetwork testDockerHostNetwork1;

    @Before
    public void setup() throws UnknownHostException {
        DockerHost testDockerHost1 = new DockerHost(
                "testHost1",
                InetAddress.getByName("1.1.1.1"),
                1234,
                InetAddress.getByName("1.1.1.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.10.0.0"),
                "/data/scripts",
                "/data/volumes",
                true);
        testDockerHostNetwork1 = new DockerHostNetwork("domain", testDockerHost1, 123, "10.10.1.0/24", "10.10.1.254");
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingDockerHost() throws DockerNetworkDetailsVerificationException {
        testDockerHostNetwork1.setHost(null);
        DockerNetworkConfigBuilder.build(testDockerHostNetwork1);
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingVLAN() throws DockerNetworkDetailsVerificationException {
        testDockerHostNetwork1.setVlanNumber(0);
        DockerNetworkConfigBuilder.build(testDockerHostNetwork1);
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingSubnet() throws DockerNetworkDetailsVerificationException {
        testDockerHostNetwork1.setSubnet(null);
        DockerNetworkConfigBuilder.build(testDockerHostNetwork1);
    }

    @Test(expected = DockerNetworkDetailsVerificationException.class)
    public void shouldThrowExceptionOnMissingGateway() throws DockerNetworkDetailsVerificationException {
        testDockerHostNetwork1.setGateway(null);
        DockerNetworkConfigBuilder.build(testDockerHostNetwork1);
    }

    @Test
    public void shouldBuildCorrectNetworkConfig() throws DockerNetworkDetailsVerificationException {
        DockerNetworkConfigBuilder.build(testDockerHostNetwork1);
    }

}
