package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Network Management Service deployment information for application deployed on Kubernetes cluster.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class KubernetesNmServiceInfo extends NmServiceInfo {

    /**
     * Kubernetes template for this service
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

    /**
     * External URL to be used to access service from outside of the cluster
     */
    private String serviceExternalUrl;

    public KubernetesNmServiceInfo () {
        super();
    }

    public KubernetesNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, KubernetesTemplate kubernetesTemplate) {
        super(deploymentId, deploymentName, domain);
        this.kubernetesTemplate = kubernetesTemplate;
    }

    public KubernetesTemplate getKubernetesTemplate() {
        return kubernetesTemplate;
    }

    public void setKubernetesTemplate(KubernetesTemplate kubernetesTemplate) {
        this.kubernetesTemplate = kubernetesTemplate;
    }

    public String getServiceExternalUrl() {
        return serviceExternalUrl;
    }

    public void setServiceExternalUrl(String serviceExternalUrl) {
        this.serviceExternalUrl = serviceExternalUrl;
    }
}
