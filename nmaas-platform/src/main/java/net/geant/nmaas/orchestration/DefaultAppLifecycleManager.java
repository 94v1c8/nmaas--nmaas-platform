package net.geant.nmaas.orchestration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Default {@link AppLifecycleManager} implementation.
 */
@Service
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Log4j2
@AllArgsConstructor
public class DefaultAppLifecycleManager implements AppLifecycleManager {

    private AppDeploymentRepositoryManager repositoryManager;

    private ApplicationEventPublisher eventPublisher;

    private NmServiceInfoRepository nmServiceInfoRepository;

    private JanitorService janitorService;

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Identifier deployApplication(AppDeployment appDeployment) {
        Identifier deploymentId = generateDeploymentId();
        appDeployment.setDeploymentId(deploymentId);
        repositoryManager.store(appDeployment);
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
        return deploymentId;
    }

    Identifier generateDeploymentId() {
        Identifier generatedId;
        do {
            generatedId = new Identifier(UUID.randomUUID().toString());
        } while(deploymentDoesNotStartWithLetter(generatedId) || deploymentIdAlreadyInUse(generatedId));
        return generatedId;
    }

    private boolean deploymentDoesNotStartWithLetter(Identifier generatedId) {
        return !generatedId.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?");
    }

    private boolean deploymentIdAlreadyInUse(Identifier generatedId) {
        try {
            repositoryManager.load(generatedId);
        } catch(InvalidDeploymentIdException e) {
            return true;
        }
        return false;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void redeployApplication(Identifier deploymentId){
        eventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.INIT, ""));
        eventPublisher.publishEvent(new AppVerifyRequestActionEvent(this, deploymentId));
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfiguration(Identifier deploymentId, AppConfigurationView configuration) throws Throwable {
        AppDeployment appDeployment = repositoryManager.load(deploymentId);
        NmServiceInfo serviceInfo = (NmServiceInfo) nmServiceInfoRepository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException("No nm service info with provided identifier found."));
        appDeployment.setConfiguration(prepareAppConfiguration(serviceInfo.getDomain(), configuration.getJsonInput()));
        if(configuration.getStorageSpace() != null){
            appDeployment.setStorageSpace(configuration.getStorageSpace());
            serviceInfo.setStorageSpace(configuration.getStorageSpace());
        }
        if(isNotEmpty(configuration.getAdditionalParameters())){
            serviceInfo.addAdditionalParameters(replaceHashToDotsInMapKeys(getMapFromJson(configuration.getAdditionalParameters())));
        }
        if(isNotEmpty(configuration.getMandatoryParameters())){
            serviceInfo.addAdditionalParameters(replaceHashToDotsInMapKeys(getMapFromJson(configuration.getMandatoryParameters())));
        }
        if(isNotEmpty(configuration.getAccessCredentials())){
            changeBasicAuth(deploymentId, serviceInfo.getDomain(), configuration.getAccessCredentials());
        }
        repositoryManager.update(appDeployment);
        nmServiceInfoRepository.save(serviceInfo);
        if(appDeployment.getState().equals(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED)){
            eventPublisher.publishEvent(new AppApplyConfigurationActionEvent(this, deploymentId));
        }
    }

    private AppConfiguration prepareAppConfiguration(String domain, String configuration) {
        if(configuration.contains("inCluster")){
            Map<String, String> config = this.getMapFromJson(configuration);
            AppDeployment app = repositoryManager.load(config.get("inClusterInstance"), domain)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid inCluster instance name"));
            config.replace("source_addr", app.getDeploymentId().value());
            return new AppConfiguration(new Gson().toJson(config));
        }
        return new AppConfiguration(configuration);
    }

    Map<String, String> getMapFromJson(String inputJson){
        try {
            return new ObjectMapper().readValue(inputJson, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new UserConfigHandlingException("Wasn't able to map additional parameters to model map -> " + e.getMessage());
        }
    }

    Map<String, String> replaceHashToDotsInMapKeys(Map<String, String> map){
        Map<String, String> newMap = new HashMap<>();
        for(Map.Entry<String, String> entry: map.entrySet()){
            if(entry.getValue() != null && !entry.getValue().isEmpty()){
                newMap.put(entry.getKey().replace("#","."), entry.getValue());
            }
        }
        return newMap;
    }

    private void changeBasicAuth(Identifier deploymentId, String domain, String accessCredentials){
        Map<String, String> accessCredentialsMap = this.getMapFromJson(accessCredentials);
        janitorService.createOrReplaceBasicAuth(deploymentId, domain, accessCredentialsMap.get("accessUsername"), accessCredentialsMap.get("accessPassword"));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppRemoveActionEvent(this, deploymentId));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void updateApplication(Identifier deploymentId, Identifier applicationId) {
        throw new NotImplementedException();
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateConfiguration(Identifier deploymentId, AppConfigurationView configuration) {
        AppDeployment appDeployment = repositoryManager.load(deploymentId);
        if(isNotEmpty(configuration.getJsonInput())){
            appDeployment.getConfiguration().setJsonInput(configuration.getJsonInput());
            repositoryManager.update(appDeployment);
            eventPublisher.publishEvent(new AppUpdateConfigurationEvent(this, deploymentId));
        }
        if(isNotEmpty(configuration.getAccessCredentials())){
            changeBasicAuth(deploymentId, appDeployment.getDomain(), configuration.getAccessCredentials());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartApplication(Identifier deploymentId) {
        eventPublisher.publishEvent(new AppRestartActionEvent(this, deploymentId));
    }
}
