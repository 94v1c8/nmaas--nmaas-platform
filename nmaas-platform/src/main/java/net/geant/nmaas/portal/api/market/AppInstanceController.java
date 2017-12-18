package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/portal/api/domains/{domainId}/apps/instances")
public class AppInstanceController extends AppBaseController {

	@Autowired
	AppLifecycleManager appLifecycleManager;

	@Autowired
	AppDeploymentMonitor appDeploymentMonitor;

	@Autowired
	AppInstanceRepository appInstanceRepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	DomainService domains;

	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public List<AppInstance> getAllInstances(@PathVariable Long domainId, Pageable pageable) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.Domain domain = domains.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");

		return appInstanceRepo.findAllByDomain(domain, pageable).getContent().stream().map(appInstance -> mapAppInstance(appInstance)).collect(Collectors.toList());
	}

	@RequestMapping(value = "/my", method = RequestMethod.GET)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'ANY')")
	public List<AppInstance> getMyAllInstances(@PathVariable Long domainId, @NotNull Principal principal, Pageable pageable)
			throws MissingElementException {
		return getUserDomainAppInstances(domainId, principal.getName(), pageable);
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public List<AppInstance> getUserAllInstances(@PathVariable Long domainId, @PathVariable String username, Pageable pageable)
			throws MissingElementException {
		return getUserDomainAppInstances(domainId, username, pageable);
	}

	private List<AppInstance> getUserDomainAppInstances(Long domainId, String username, Pageable pageable) throws MissingElementException {
		
		net.geant.nmaas.portal.persistent.entity.Domain domain = domains.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		Optional<User> user = userRepo.findByUsername(username);
		if (!user.isPresent())
			throw new MissingElementException("User not found");

		return appInstanceRepo.findAllByOwner(user.get(), pageable).getContent().stream().map(appInstance -> mapAppInstance(appInstance)).collect(Collectors.toList());
	}
	
	@RequestMapping(value = "/{appInstanceId}", method = RequestMethod.GET)
	@PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
	public AppInstance getAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
			@NotNull Principal principal) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = appInstanceRepo.findOne(appInstanceId);
		if (appInstance == null)
			throw new MissingElementException("App instance not found.");

		return mapAppInstance(appInstance);
	}

	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasPermission(null, 'appInstance', 'CREATE')")
	@Transactional
	public Id createAppInstance(@RequestBody(required = true) AppInstanceSubscription appInstanceSubscription,
			@NotNull Principal principal, @PathVariable Long domainId) throws MissingElementException {
		Application app = getApp(appInstanceSubscription.getApplicationId());
		User user = getUser(principal.getName());

		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = new net.geant.nmaas.portal.persistent.entity.AppInstance(
				app, appInstanceSubscription.getName());
		
		net.geant.nmaas.portal.persistent.entity.Domain domain = domains.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		appInstance.setDomain(domain);

		Identifier internalId = appLifecycleManager.deployApplication(new Identifier(Long.toString(user.getId())),
				new Identifier(Long.toString(app.getId())));
		appInstance.setInternalId(internalId);

		appInstanceRepo.save(appInstance);

		return new Id(appInstance.getId());
	}

	@RequestMapping(value = "/{appInstanceId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'DELETE')")
	@Transactional
	public void deleteAppInstance(@PathVariable(value = "appInstanceId") Long appInstanceId,
			@NotNull Principal principal) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);

		try {
			appLifecycleManager.removeApplication(appInstance.getInternalId());
		} catch (InvalidDeploymentIdException e) {
			throw new ProcessingException("Missing app instance");
		}
	}

	@RequestMapping(value = "/{appInstanceId}/configure", method = RequestMethod.POST)
	@PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
	@Transactional
	public void applyConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
			@RequestBody String configuration, @NotNull Principal principal)
			throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);

		AppInstanceStatus status = getState(appInstanceId, principal);
		if (status.getState() != AppInstanceState.CONFIGURATION_AWAITING)
			throw new ProcessingException("App instance configuration cannot be applied in state " + status.getState());

		boolean valid = validJSON(configuration);
		if (!valid)
			throw new ProcessingException("Configuration is not in valid JSON format");

		appInstance.setConfiguration(configuration);
		appInstanceRepo.save(appInstance);

		try {
			appLifecycleManager.applyConfiguration(appInstance.getInternalId(), new AppConfiguration(configuration));
		} catch (InvalidDeploymentIdException e) {
			throw new ProcessingException("Missing app instance");
		}
	}

	@RequestMapping(value = "/{appInstanceId}/state", method = RequestMethod.GET)
	@PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
	public AppInstanceStatus getState(@PathVariable(value = "appInstanceId") Long appInstanceId,
			@NotNull Principal principal) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);

		return getAppInstanceState(appInstance);
	}

	private AppInstanceStatus getAppInstanceState(net.geant.nmaas.portal.persistent.entity.AppInstance appInstance) throws ProcessingException, MissingElementException {
		if(appInstance == null)
			throw new MissingElementException("App instance is null");
		
		AppLifecycleState state = AppLifecycleState.UNKNOWN;
		try {
			state = appDeploymentMonitor.state(appInstance.getInternalId());
		} catch (InvalidDeploymentIdException e) {
			throw new ProcessingException("Missing app instance");
		}

		return prepareAppInstanceStatus(appInstance.getId(), state);
	}

	private boolean validJSON(String json) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.readTree(json);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private AppInstanceStatus prepareAppInstanceStatus(Long appInstanceId, AppLifecycleState state) {
		AppInstanceStatus appInstanceStatus = new AppInstanceStatus();
		appInstanceStatus.setAppInstanceId(appInstanceId);
		appInstanceStatus.setDetails(state.name());

		AppInstanceState appInstanceState = mapAppInstanceState(state);

		appInstanceStatus.setState(appInstanceState);

		return appInstanceStatus;
	}

	private AppInstanceState mapAppInstanceState(AppLifecycleState state) {
		AppInstanceState appInstanceState;
		switch (state) {
		case REQUESTED:
			appInstanceState = AppInstanceState.SUBSCRIBED;
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
			appInstanceState = AppInstanceState.CONFIGURATION_AWAITING;
			break;
		case APPLICATION_CONFIGURATION_IN_PROGRESS:
		case APPLICATION_CONFIGURED:
		case APPLICATION_DEPLOYMENT_IN_PROGRESS:
		case APPLICATION_DEPLOYED:
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
			appInstanceState = AppInstanceState.DONE;
			break;
		case INTERNAL_ERROR:
		case GENERIC_ERROR:
		case REQUEST_VALIDATION_FAILED:
		case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
		case MANAGEMENT_VPN_CONFIGURATION_FAILED:
		case APPLICATION_CONFIGURATION_FAILED:
		case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
		case APPLICATION_REMOVAL_FAILED:
		case APPLICATION_DEPLOYMENT_FAILED:
			appInstanceState = AppInstanceState.FAILURE;
			break;
		case UNKNOWN:
		default:
			appInstanceState = AppInstanceState.UNKNOWN;
			break;
		}
		return appInstanceState;
	}

	private net.geant.nmaas.portal.persistent.entity.AppInstance getAppInstance(Long appInstanceId)
			throws MissingElementException {
		if (appInstanceId == null)
			throw new MissingElementException("Missing app instance id.");

		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = appInstanceRepo.findOne(appInstanceId);
		if (appInstance == null)
			throw new MissingElementException("App instance not found.");

		return appInstance;
	}

	private AppInstance mapAppInstance(net.geant.nmaas.portal.persistent.entity.AppInstance appInstance) {
		AppInstance ai = modelMapper.map(appInstance, AppInstance.class);
		try {
			ai.setState(mapAppInstanceState(this.appDeploymentMonitor.state(appInstance.getInternalId())));
		} catch (Exception e) {
			ai.setState(AppInstanceState.UNKNOWN);
			ai.setUrl(null);
		}
		
		try {
			ai.setUrl(this.appDeploymentMonitor.userAccessDetails(appInstance.getInternalId()).getUrl());
		} catch (InvalidAppStateException e) {
			ai.setUrl(null);
		} catch (InvalidDeploymentIdException e) {
			ai.setUrl(null);
		}
		
		return ai;		
	}
}
