package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AppDeploymentRepositoryManager {

    private AppDeploymentRepository repository;

    @Autowired
    public AppDeploymentRepositoryManager(AppDeploymentRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(AppDeployment appDeployment) {
        if(!repository.findByDeploymentId(appDeployment.getDeploymentId()).isPresent()){
            repository.save(appDeployment);
            addChangeOfStateToHistory(appDeployment, null, appDeployment.getState());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(AppDeployment appDeployment) {
        repository.save(appDeployment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(Identifier deploymentId, AppDeploymentState currentState) {
        AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        addChangeOfStateToHistory(appDeployment, appDeployment.getState(), currentState);
        appDeployment.setState(currentState);
        repository.save(appDeployment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppDeploymentState loadState(Identifier deploymentId) {
        return repository.getStateByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String loadStateErrorMessage(Identifier deploymentId) {
        return repository.getErrorMessageByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateConfiguration(Identifier deploymentId, AppConfiguration configuration) {
        AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        appDeployment.setConfiguration(configuration);
        repository.save(appDeployment);
    }

    public Optional<AppDeployment> load(Identifier deploymentId) {
        return repository.findByDeploymentId(deploymentId);
    }

    public List<AppDeployment> loadAll() {
        return repository.findAll();
    }

    public List<AppDeployment> loadAllWaitingForDcn(String domain) {
        return repository.findByDomainAndState(domain, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
    }

    public String loadDomainByDeploymentId(Identifier deploymentId) {
        return repository.getDomainByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    public Optional<AppDeployment> loadByDeploymentNameAndDomain(String deploymentName, String domain){
        return repository.findByDeploymentNameAndDomain(deploymentName, domain);
    }

    public void removeAll() {
        repository.deleteAll();
    }

    public List<AppDeploymentHistory> getAppStateHistoryByDeploymentId(Identifier deploymentId){
        AppDeployment app = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
        return app.getHistory();
    }

    private void addChangeOfStateToHistory(AppDeployment appDeployment, AppDeploymentState previousState, AppDeploymentState currentState){
        appDeployment.getHistory().add(new AppDeploymentHistory(appDeployment, new Date(), previousState, currentState));
    }

    public void reportErrorStatusAndSaveInEntity(Identifier deploymentId , String error) {
        AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
        appDeployment.setErrorMessage(error);
        repository.save(appDeployment);
    }

    private String deploymentNotFoundMessage(Identifier deploymentId) {
        return "Deployment with id " + deploymentId + " not found in the repository. ";
    }
}
