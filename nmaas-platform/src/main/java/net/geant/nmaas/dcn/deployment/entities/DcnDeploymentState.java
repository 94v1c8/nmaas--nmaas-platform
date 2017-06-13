package net.geant.nmaas.dcn.deployment.entities;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum DcnDeploymentState {

    INIT,
    REQUESTED,
    REQUEST_VERIFIED,
    REQUEST_VERIFICATION_FAILED,
    DEPLOYMENT_INITIATED,
    ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED,
    ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED,
    DEPLOYED,
    DEPLOYMENT_FAILED,
    VERIFICATION_INITIATED,
    VERIFIED,
    VERIFICATION_FAILED,
    REMOVED,
    REMOVAL_INITIATED,
    ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED,
    ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED,
    REMOVAL_FAILED,
    UNKNOWN,
    ERROR;

}
