package net.geant.nmaas.orchestration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope("prototype")
public class AppConfigurationOrchestratorTask implements Runnable {

    @Autowired
    private NmServiceConfigurationProvider serviceConfiguration;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper;

    @Autowired
    private NmServiceRepository nmServiceRepository;

    private Identifier deploymentId;

    private AppConfiguration configuration;

    public void populateProperties(Identifier deploymentId, AppConfiguration configuration) {
        this.deploymentId = deploymentId;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        configure();
    }

    private void configure() {
        verifyIfAllPropertiesAreSet();
        try {
            String serviceName = deploymentIdToNmServiceNameMapper.nmServiceName(deploymentId);
            NmServiceInfo serviceInfo = nmServiceRepository.loadService(serviceName);
            serviceConfiguration.configureNmService(deploymentId, configuration, (DockerHost) serviceInfo.getHost());
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            e.printStackTrace();
        } catch (NmServiceRepository.ServiceNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void verifyIfAllPropertiesAreSet() {
        if (deploymentId == null || configuration == null)
            throw new NullPointerException();
    }

}
