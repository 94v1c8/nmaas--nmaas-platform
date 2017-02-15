package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentNetworkDetails;

/**
 * Stores information about network details assigned for particular container deployment.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNetworkDetails implements NmServiceDeploymentNetworkDetails {

    private final int publicPort;

    private final ContainerNetworkIpamSpec ipAddresses;

    private final int vlanNumber;

    /**
     * Identifier of the network assigned by orchestrator
     */
    private String deploymentId;

    public ContainerNetworkDetails(int publicPort, ContainerNetworkIpamSpec ipAddresses, int vlanNumber) {
        this.publicPort = publicPort;
        this.ipAddresses = ipAddresses;
        this.vlanNumber = vlanNumber;
    }

    public ContainerNetworkIpamSpec getIpAddresses() {
        return ipAddresses;
    }

    public int getVlanNumber() {
        return vlanNumber;
    }

    public int getPublicPort() {
        return publicPort;
    }

    @Override
    public void setId(String id) {
        deploymentId = id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }
}
