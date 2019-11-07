package net.geant.nmaas.nmservice.deployment;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
public abstract class NmServiceRepositoryManager<T extends NmServiceInfo> {

    @Autowired
    protected NmServiceInfoRepository<T> repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeService(T serviceInfo) {
        if(!repository.findByDeploymentId(serviceInfo.getDeploymentId()).isPresent())
            repository.save(serviceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateService(T serviceInfo) {
        repository.save(serviceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeService(Identifier deploymentId) {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        repository.delete(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeAllServices() {
        repository.deleteAll();
    }

    public T loadService(Identifier deploymentId) {
        return repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    @EventListener
    public void notifyStateChange(NmServiceDeploymentStateChangeEvent event) {
        try{
            updateServiceState(event.getDeploymentId(), event.getState());
        }catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateServiceState(Identifier deploymentId, NmServiceDeploymentState state) {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setState(state);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateManagedDevices(Identifier deploymentId, List<String> ipAddresses) {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setManagedDevicesIpAddresses(ipAddresses);
        repository.save(nmServiceInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateGitLabProject(Identifier deploymentId, GitLabProject gitLabProject) {
        T nmServiceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        nmServiceInfo.setGitLabProject(gitLabProject);
        repository.save(nmServiceInfo);
    }

    public NmServiceDeploymentState loadCurrentState(Identifier deploymentId) {
        return repository.getStateByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public String loadDomain(Identifier deploymentId) {
        return repository.getDomainByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public String loadDeploymentName(Identifier deploymentId) {
        return repository.getDeploymentNameByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

    public Identifier loadDescriptiveDeploymentId(Identifier deploymentId) {
        return repository.getDescriptiveDeploymentIdByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
    }

}
