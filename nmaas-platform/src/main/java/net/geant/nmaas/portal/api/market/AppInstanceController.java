package net.geant.nmaas.portal.api.market;

import lombok.AllArgsConstructor;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppInstanceRequest;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceStatus;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apps/instances")
@AllArgsConstructor
public class AppInstanceController extends AppBaseController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";

    private static final String MISSING_USER_MESSAGE = "User not found";

    private AppLifecycleManager appLifecycleManager;

    private AppDeploymentMonitor appDeploymentMonitor;

    private ApplicationInstanceService instances;

    private DomainService domains;

    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<AppInstanceView> getAllInstances(Pageable pageable) {
        if (pageable == null) {
            return instances.findAll().stream()
                    .map(this::mapAppInstance)
                    .collect(Collectors.toList());
        }
        return instances.findAll(pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/my")
    @Transactional
    public List<AppInstanceView> getMyAllInstances(@NotNull Principal principal, Pageable pageable) {
        User user = users.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        if(pageable == null) {
            return instances.findAllByOwner(user).stream()
                    .map(this::mapAppInstance)
                    .collect(Collectors.toList());
        }
        return instances.findAllByOwner(user, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getAllInstances(@PathVariable Long domainId, Pageable pageable) {
        Domain domain = domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        if (pageable == null) {
            return instances.findAllByDomain(domain).stream()
                    .map(this::mapAppInstance)
                    .collect(Collectors.toList());
        }
        return instances.findAllByDomain(domain, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/running/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getRunningAppInstances(@PathVariable(value = "domainId") long domainId, Principal principal) {
        Domain domain = this.domains.findDomain(domainId).orElseThrow(() -> new InvalidDomainException("Domain not found"));
        User owner = this.users.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException(MISSING_USER_MESSAGE));
        return getAllRunningInstancesByOwnerAndDomain(owner, domain);
    }

    private List<AppInstanceView> getAllRunningInstancesByOwnerAndDomain(User owner, Domain domain){
        return this.instances.findAllByOwnerAndDomain(owner, domain).stream()
                .filter(app -> appDeploymentMonitor.state(app.getInternalId()).equals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED))
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/domain/{domainId}/my")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
    @Transactional
    public List<AppInstanceView> getMyAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable) {
        if(pageable == null) {
            return getUserDomainAppInstances(domainId, principal.getName());
        }
        return getUserDomainAppInstances(domainId, principal.getName(), pageable);
    }

    @GetMapping("/domain/{domainId}/user/{username}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public List<AppInstanceView> getUserAllInstances(@PathVariable Long domainId, @PathVariable String username, Pageable pageable){
        if(pageable == null) {
            return getUserDomainAppInstances(domainId, username);
        }
        return getUserDomainAppInstances(domainId, username, pageable);
    }

    private List<AppInstanceView> getUserDomainAppInstances(Long domainId, String username, Pageable pageable) {
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instances.findAllByOwner(user, domain, pageable).getContent().stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    private List<AppInstanceView> getUserDomainAppInstances(Long domainId, String username) {
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain " + domainId + " not found"));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new MissingElementException(MISSING_USER_MESSAGE));
        return instances.findAllByOwnerAndDomain(user, domain).stream()
                .map(this::mapAppInstance)
                .collect(Collectors.toList());
    }

    @GetMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceView getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                          @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId)
                .orElseThrow(() -> new MissingElementException("App instance not found."));
        return mapAppInstance(appInstance);
    }

    @PostMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public Id createAppInstance(@RequestBody AppInstanceRequest appInstanceRequest,
                                @NotNull Principal principal, @PathVariable Long domainId) {
        Application app = getApp(appInstanceRequest.getApplicationId());
        Domain domain = domains.findDomain(domainId)
                .orElseThrow(() -> new MissingElementException("Domain not found"));
        AppInstance appInstance;
        try {
            appInstance = instances.create(domain, app, appInstanceRequest.getName());
        } catch (ApplicationSubscriptionNotActiveException e) {
            throw new ProcessingException("Unable to create instance. " + e.getMessage());
        }

        AppDeploymentSpec appDeploymentSpec = modelMapper.map(app.getAppDeploymentSpec(), AppDeploymentSpec.class);
        AppDeployment appDeployment = AppDeployment.builder()
                .domain(domain.getCodename())
                .instanceId(appInstance.getId())
                .applicationId(Identifier.newInstance(appInstance.getApplication().getId()))
                .deploymentName(appInstance.getName())
                .configFileRepositoryRequired(app.getAppConfigurationSpec().isConfigFileRepositoryRequired())
                .storageSpace(appDeploymentSpec.getDefaultStorageSpace())
                .owner(principal.getName())
                .appName(app.getName())
                .descriptiveDeploymentId(createDescriptiveDeploymentId(domain.getCodename(), app.getName(), appInstance.getId()))
                .build();

        Identifier internalId = appLifecycleManager.deployApplication(appDeployment);
        appInstance.setInternalId(internalId);

        instances.update(appInstance);

        return new Id(appInstance.getId());
    }

    private Identifier createDescriptiveDeploymentId(String domain, String appName, Long appInstanceNumber) {
        return Identifier.newInstance(
                String.join("-", domain, appName, String.valueOf(appInstanceNumber)).toLowerCase()
        );
    }

    @PostMapping("/{appInstanceId}/redeploy")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'CREATE')")
    @Transactional
    public void redeployAppInstance(@PathVariable Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.redeployApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @DeleteMapping("/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void deleteAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                  @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @DeleteMapping("/failed/{appInstanceId}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
    @Transactional
    public void removeFailedInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                  @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            appLifecycleManager.removeFailedApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @GetMapping("/{appInstanceId}/state")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                      @NotNull Principal principal) {
        AppInstance appInstance = getAppInstance(appInstanceId);
        return getAppInstanceState(appInstance);
    }

    @GetMapping("/{appInstanceId}/state/history")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public List<AppDeploymentHistoryView> getStateHistory(@PathVariable(value = "appInstanceId") Long appInstanceId, @NotNull Principal principal) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            return appDeploymentMonitor.appDeploymentHistory(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @PostMapping("/{appInstanceId}/restart")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void restartAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        try {
            AppInstance appInstance = getAppInstance(appInstanceId);
            this.appLifecycleManager.restartApplication(appInstance.getInternalId());
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    private AppInstanceStatus getAppInstanceState(AppInstance appInstance) {
        if (appInstance == null)
            throw new MissingElementException("App instance is null");

        try {
            return prepareAppInstanceStatus(
                    appInstance.getId(),
                    appDeploymentMonitor.state(appInstance.getInternalId()),
                    appDeploymentMonitor.previousState(appInstance.getInternalId()));
        } catch (InvalidDeploymentIdException e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    private AppInstanceStatus prepareAppInstanceStatus(Long appInstanceId, AppLifecycleState state, AppLifecycleState previousState) {
        AppInstanceState appInstanceState = mapAppInstanceState(state);

        return AppInstanceStatus.builder()
                .appInstanceId(appInstanceId)
                .details(state.name())
                .userFriendlyDetails(state.getUserFriendlyState())
                .state(appInstanceState)
                .previousState(mapAppInstanceState(previousState))
                .userFriendlyState(appInstanceState.getUserFriendlyState())
                .build();
    }

    private AppInstanceState mapAppInstanceState(AppLifecycleState state) {
        AppInstanceState appInstanceState;
        switch (state) {
            case REQUESTED:
                appInstanceState = AppInstanceState.REQUESTED;
                break;
            case REQUEST_VALIDATION_IN_PROGRESS:
            case REQUEST_VALIDATED:
                appInstanceState = AppInstanceState.VALIDATION;
                break;
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.PREPARATION;
                break;
            case DEPLOYMENT_ENVIRONMENT_PREPARED:
            case MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.CONNECTING;
                break;
            case MANAGEMENT_VPN_CONFIGURED:
            case APPLICATION_CONFIGURATION_IN_PROGRESS:
            case APPLICATION_CONFIGURED:
                appInstanceState = AppInstanceState.CONFIGURATION_AWAITING;
                break;
            case APPLICATION_DEPLOYMENT_IN_PROGRESS:
            case APPLICATION_DEPLOYED:
            case APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_UPDATED:
            case APPLICATION_RESTART_IN_PROGRESS:
            case APPLICATION_RESTARTED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.DEPLOYING;
                break;
            case APPLICATION_DEPLOYMENT_VERIFIED:
                appInstanceState = AppInstanceState.RUNNING;
                break;
            case APPLICATION_REMOVAL_IN_PROGRESS:
                appInstanceState = AppInstanceState.UNDEPLOYING;
                break;
            case APPLICATION_REMOVED:
            case APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_REMOVED:
                appInstanceState = AppInstanceState.DONE;
                break;
            case INTERNAL_ERROR:
            case REQUEST_VALIDATION_FAILED:
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
            case MANAGEMENT_VPN_CONFIGURATION_FAILED:
            case APPLICATION_CONFIGURATION_FAILED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
            case APPLICATION_REMOVAL_FAILED:
            case APPLICATION_RESTART_FAILED:
            case APPLICATION_CONFIGURATION_UPDATE_FAILED:
            case APPLICATION_DEPLOYMENT_FAILED:
            case APPLICATION_CONFIGURATION_REMOVAL_FAILED:
                appInstanceState = AppInstanceState.FAILURE;
                break;
            case FAILED_APPLICATION_REMOVED:
                appInstanceState = AppInstanceState.REMOVED;
                break;
            case UNKNOWN:
            default:
                appInstanceState = AppInstanceState.UNKNOWN;
                break;
        }
        return appInstanceState;
    }

    private AppInstance getAppInstance(Long appInstanceId) {
        if (appInstanceId == null)
            throw new MissingElementException("Missing app instance id.");
        return instances.find(appInstanceId).orElseThrow(() -> new MissingElementException("App instance not found."));
    }

    private AppInstanceView mapAppInstance(AppInstance appInstance) {
        if (appInstance == null)
            return null;
        AppInstanceView ai = modelMapper.map(appInstance, AppInstanceView.class);

        try {
            ai.setState(mapAppInstanceState(this.appDeploymentMonitor.state(appInstance.getInternalId())));
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
            ai.setDomainId(appInstance.getDomain().getId());
        } catch (Exception e) {
            ai.setState(AppInstanceState.UNKNOWN);
            ai.setUrl(null);
        }

        try {
            ai.setUrl(this.appDeploymentMonitor.userAccessDetails(appInstance.getInternalId()).getUrl());
        } catch (InvalidAppStateException
                | InvalidDeploymentIdException e) {
            ai.setUrl(null);
        }

        ai.setDescriptiveDeploymentId(this.appDeploymentRepositoryManager.load(appInstance.getInternalId()).getDescriptiveDeploymentId().value());

        ai.setConfigWizardTemplate(new ConfigWizardTemplateView(appInstance.getApplication().getConfigWizardTemplate().getTemplate()));

        return ai;
    }

}
