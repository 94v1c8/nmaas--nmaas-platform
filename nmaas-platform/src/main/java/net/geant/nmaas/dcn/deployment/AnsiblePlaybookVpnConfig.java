package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;

public class AnsiblePlaybookVpnConfig {

    private Type type;

    private String targetRouter;
    private String vrfId;
    private String logicalInterface;
    private String vrfRd;
    private String vrfRt;
    private String bgpGroupId;
    private String bgpNeighborIp;
    private String asn;
    private String physicalInterface;
    private String interfaceUnit;
    private String interfaceVlan;
    private String bgpLocalIp;
    private String bgpLocalCidr;
    private String policyCommunityOptions;
    private String policyStatementConnected;
    private String policyStatementImport;
    private String policyStatementExport;

    public AnsiblePlaybookVpnConfig(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getTargetRouter() {
        return targetRouter;
    }

    public void setTargetRouter(String targetRouter) {
        this.targetRouter = targetRouter;
    }

    public String getVrfId() {
        return vrfId;
    }

    public void setVrfId(String vrfId) {
        this.vrfId = vrfId;
    }

    public String getLogicalInterface() {
        return logicalInterface;
    }

    public void setLogicalInterface(String logicalInterface) {
        this.logicalInterface = logicalInterface;
    }

    public String getVrfRd() {
        return vrfRd;
    }

    public void setVrfRd(String vrfRd) {
        this.vrfRd = vrfRd;
    }

    public String getVrfRt() {
        return vrfRt;
    }

    public void setVrfRt(String vrfRt) {
        this.vrfRt = vrfRt;
    }

    public String getBgpGroupId() {
        return bgpGroupId;
    }

    public void setBgpGroupId(String bgpGroupId) {
        this.bgpGroupId = bgpGroupId;
    }

    public String getBgpNeighborIp() {
        return bgpNeighborIp;
    }

    public void setBgpNeighborIp(String bgpNeighborIp) {
        this.bgpNeighborIp = bgpNeighborIp;
    }

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public String getPhysicalInterface() {
        return physicalInterface;
    }

    public void setPhysicalInterface(String physicalInterface) {
        this.physicalInterface = physicalInterface;
    }

    public String getInterfaceUnit() {
        return interfaceUnit;
    }

    public void setInterfaceUnit(String interfaceUnit) {
        this.interfaceUnit = interfaceUnit;
    }

    public String getInterfaceVlan() {
        return interfaceVlan;
    }

    public void setInterfaceVlan(String interfaceVlan) {
        this.interfaceVlan = interfaceVlan;
    }

    public String getBgpLocalIp() {
        return bgpLocalIp;
    }

    public void setBgpLocalIp(String bgpLocalIp) {
        this.bgpLocalIp = bgpLocalIp;
    }

    public String getBgpLocalCidr() {
        return bgpLocalCidr;
    }

    public void setBgpLocalCidr(String bgpLocalCidr) {
        this.bgpLocalCidr = bgpLocalCidr;
    }

    public String getPolicyCommunityOptions() {
        return policyCommunityOptions;
    }

    public void setPolicyCommunityOptions(String policyCommunityOptions) {
        this.policyCommunityOptions = policyCommunityOptions;
    }

    public String getPolicyStatementConnected() {
        return policyStatementConnected;
    }

    public void setPolicyStatementConnected(String policyStatementConnected) {
        this.policyStatementConnected = policyStatementConnected;
    }

    public String getPolicyStatementImport() {
        return policyStatementImport;
    }

    public void setPolicyStatementImport(String policyStatementImport) {
        this.policyStatementImport = policyStatementImport;
    }

    public String getPolicyStatementExport() {
        return policyStatementExport;
    }

    public void setPolicyStatementExport(String policyStatementExport) {
        this.policyStatementExport = policyStatementExport;
    }

    public void validate() throws ConfigNotValidException {
        if (targetRouter == null || targetRouter.isEmpty())
            exception(message("Target Router"));
        if (vrfId == null || vrfId.isEmpty())
            exception(message("VRF ID"));
        if (logicalInterface == null || logicalInterface.isEmpty())
            exception(message("Logical Interface"));
        if (vrfRd == null || vrfRd.isEmpty())
            exception(message("VRF RD"));
        if (vrfRt == null || vrfRt.isEmpty())
            exception(message("VRF RT"));
        if (bgpGroupId == null || bgpGroupId.isEmpty())
            exception(message("BGP Group ID"));
        if (bgpNeighborIp == null || bgpNeighborIp.isEmpty())
            exception(message("BGP Neighbor ID"));
        if (asn == null || asn.isEmpty())
            exception(message("ASN"));
        if (physicalInterface == null || physicalInterface.isEmpty())
            exception(message("Physical Interface"));
        if (interfaceUnit == null || interfaceUnit.isEmpty())
            exception(message("Interface Unit"));
        if (interfaceVlan == null || interfaceUnit.isEmpty())
            exception(message("Interface VLAN"));
        if (bgpLocalIp == null || bgpLocalIp.isEmpty())
            exception(message("BGP Local IP"));
        if (bgpLocalCidr == null || bgpLocalCidr.isEmpty())
            exception(message("BGP Local CIDR"));
        if (type.equals(Type.CLOUD_SIDE)) {
            if (policyCommunityOptions == null || policyCommunityOptions.isEmpty())
                exception(message("Policy Community Options"));
            if (policyStatementConnected == null || policyStatementConnected.isEmpty())
                exception(message("Policy Statement Connected"));
            if (policyStatementImport == null || policyStatementImport.isEmpty())
                exception(message("Policy Statement Import"));
            if (policyStatementExport == null || policyStatementExport.isEmpty())
                exception(message("Policy Statement Export"));
        }
    }

    public void merge(ContainerNetworkDetails networkDetails) {
        this.interfaceUnit = String.valueOf(networkDetails.getVlanNumber());
        this.interfaceVlan = String.valueOf(networkDetails.getVlanNumber());
        this.bgpLocalIp = networkDetails.getIpAddresses().getGateway();
        this.bgpNeighborIp = networkDetails.getIpAddresses().getIpAddressOfContainer();
        this.logicalInterface = this.physicalInterface + "." + this.interfaceUnit;
    }

    private void exception(String message) throws ConfigNotValidException {
        throw new ConfigNotValidException(message);
    }

    private String message(String fieldName) {
        return new StringBuilder().append(fieldName).append(" is NULL or empty").toString();
    }

    public AnsiblePlaybookVpnConfig copy() {
        AnsiblePlaybookVpnConfig copy = new AnsiblePlaybookVpnConfig(this.type);
        copy.setTargetRouter(this.targetRouter);
        copy.setVrfId(this.vrfId);
        copy.setLogicalInterface(this.logicalInterface);
        copy.setVrfRd(this.vrfRd);
        copy.setVrfRt(this.vrfRt);
        copy.setBgpGroupId(this.bgpGroupId);
        copy.setBgpNeighborIp(this.bgpNeighborIp);
        copy.setAsn(this.asn);
        copy.setPhysicalInterface(this.physicalInterface);
        copy.setInterfaceUnit(this.interfaceUnit);
        copy.setInterfaceVlan(this.interfaceVlan);
        copy.setBgpLocalIp(this.bgpLocalIp);
        copy.setBgpLocalCidr(this.bgpLocalCidr);
        copy.setPolicyCommunityOptions(this.policyCommunityOptions);
        copy.setPolicyStatementConnected(this.policyStatementConnected);
        copy.setPolicyStatementImport(this.policyStatementImport);
        copy.setPolicyStatementExport(this.policyStatementExport);
        return copy;
    }

    public enum Type {
        CLIENT_SIDE,
        CLOUD_SIDE;
    }

    public enum Action {
        ADD,
        REMOVE;
    }

    public class ConfigNotValidException extends Exception {

        public ConfigNotValidException(String message) {
            super(message);
        }
    }
}
