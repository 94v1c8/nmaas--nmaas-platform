package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.domain.AppRateView;
import net.geant.nmaas.portal.api.domain.ApplicationBaseView;
import net.geant.nmaas.portal.api.domain.ApplicationStateChangeRequest;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MarketException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.RatingRepository;
import net.geant.nmaas.portal.service.impl.ApplicationServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/apps")
@Log4j2
public class ApplicationController extends AppBaseController {

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@Setter
	public static class ApplicationDTO {
		@Valid
		private ApplicationBaseView applicationBase;
		@Valid
		private ApplicationView application;
	}

	private final ApplicationEventPublisher eventPublisher;

	private final RatingRepository ratingRepository;

	/*
	 * Application Base Part
	 */

	@GetMapping("/base")
	@Transactional
	public List<ApplicationBaseView> getAllActiveApplicationBase() {
		return appBaseService.findAllActiveApps().stream()
				.map(app -> modelMapper.map(app, ApplicationBaseView.class))
				.map(this::setAppRating)
				.collect(Collectors.toList());
	}

	@GetMapping("/base/all")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public List<ApplicationBaseView> getAllApplicationBaseBasedOnRole(Principal principal){
		// user with Tool Manager role should only see applications he owns
		boolean isSystemAdmin = this.getUser(principal.getName()).getRoles().stream()
				.anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN));

		return appBaseService.findAll().stream()
				// system admin should see all the applications
				.filter(app -> isSystemAdmin || app.getOwner().equals(principal.getName()))
				.map(app -> modelMapper.map(app, ApplicationBaseView.class))
				.map(this::setAppRating)
				.collect(Collectors.toList());
	}

	private ApplicationBaseView setAppRating(ApplicationBaseView baseView) {
		Integer[] rating = this.ratingRepository.getApplicationRating(baseView.getId());
		baseView.setRate(this.createAppRateView(rating));
		return baseView;
	}

	private AppRateView createAppRateView(Integer[] rating) {
		return new AppRateView(
				Arrays.stream(rating).mapToInt(Integer::intValue).average().orElse(0.0),
				Arrays.stream(rating).collect(Collectors.groupingBy(s -> s, Collectors.counting()))
		);
	}

	@GetMapping(value = "/base/{id}")
	@Transactional
	public ApplicationBaseView getApplicationBase(@PathVariable Long id) {
		ApplicationBaseView app = modelMapper.map(appBaseService.getBaseApp(id), ApplicationBaseView.class);
		return this.setAppRating(app);
	}

	@PatchMapping(value = "/base")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplicationBase(@RequestBody ApplicationBaseView baseView, Principal principal) {
		// only system admin and owner can update application base
		this.applicationBaseOwnerCheck(baseView.getName(), principal);
		appBaseService.update(modelMapper.map(baseView, ApplicationBase.class));
	}

	@PatchMapping(value = "/base/{id}/owner/{owner}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplicationBaseOwner(@PathVariable Long id, @PathVariable String owner, Principal principal) {
		// only system admin and owner can update application base
		log.info("Upate owner for application {} to {}", id, owner);
		this.applicationBaseOwnerCheck(id, principal);
		appBaseService.updateOwner(id, owner);
	}

	@DeleteMapping(value = "/base/{id}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void deleteApplicationBase(@PathVariable Long id, Principal principal) {
		ApplicationBase base = appBaseService.getBaseApp(id);
		// only system admin and owner can update application base
		this.applicationBaseOwnerCheck(base.getName(), principal);
		ApplicationState state = ApplicationState.DELETED;
        for (ApplicationVersion appVersion : base.getVersions()) {
            Application app = getApp(appVersion.getAppVersionId());
            applicationService.changeApplicationState(app, state);
            appVersion.setState(state);
        }

		appBaseService.deleteAppBase(base);
	}

	/*
	 * Application part
	 */

	@PostMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody @Valid ApplicationDTO request, Principal principal) {
		ApplicationBaseView creationRequest = request.getApplicationBase();
		creationRequest.setOwner(principal.getName());
		// create new application base
		ApplicationBase base = this.appBaseService.create(modelMapper.map(creationRequest, ApplicationBase.class));

		this.addApplicationVersion(request.getApplication(), principal);

		return new Id(base.getId());
	}

	@GetMapping(value = "/{name}/latest")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public ApplicationDTO getLatestAppVersion(@PathVariable String name) {
		ApplicationBase base = this.appBaseService.findByName(name);
		Application application = this.applicationService.findApplicationLatestVersion(name);
		return new ApplicationDTO(
				modelMapper.map(base, ApplicationBaseView.class),
				modelMapper.map(application, ApplicationView.class)
		);
	}

	@GetMapping(value="/{id}")
	@Transactional
	public ApplicationDTO getApplicationDTO(@PathVariable Long id) {
		Application app = getApp(id);
		ApplicationBase base = this.appBaseService.findByName(app.getName());
		return new ApplicationDTO(
				modelMapper.map(base, ApplicationBaseView.class),
				modelMapper.map(app, ApplicationView.class)
		);
	}

	@GetMapping(value="/versions/{id}")
	@Transactional
	public Set<ApplicationVersion> getApplicationVersion(@PathVariable Long id) {
		return this.getVersions(id);
	}

	@GetMapping(value="/version/{id}")
	@Transactional
	public ApplicationView getApplication(@PathVariable Long id) {
		Application app = getApp(id);
		return modelMapper.map(app, ApplicationView.class);
	}

	/**
	 * Use this method to add new ApplicationVersion and Application for existing ApplicationBaseEntity
	 * @param view - application entity view
	 * @param principal - security object (used to retrieve creator)
	 */
	@PostMapping(value = "/version")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void addApplicationVersion(@RequestBody @Valid ApplicationView view, Principal principal) {

		this.applicationBaseOwnerCheck(view.getName(), principal);

		// validate
		// application base with given name must exist
		ApplicationBase base = appBaseService.findByName(view.getName());
		// specified version for this application base must not exist
		boolean hasVersion = base.getVersions()
				.stream()
				.anyMatch(v -> v.getVersion().equals(view.getVersion())
						&& !v.isDeleted());
		// application specified name and version must not exist
		if (hasVersion) {
			log.error("Cannot add application version, object already exists");
			throw new ObjectAlreadyExistsException("App version already exists");
		}

		// create application stub to avoid problems with circular dependencies
		// see application -> app config spec -> config file template -> application (id) :)
		Application temp = this.applicationService.create(new Application(view.getName(), view.getVersion()));
		Long appId = temp.getId();

		// create application entity & set properties
		Application application = modelMapper.map(view, Application.class);
		application.setId(appId);
		application.setState(ApplicationState.NEW);
		application.setCreationDate(LocalDateTime.now());
		this.applicationService.setMissingProperties(application, appId);
		ApplicationServiceImpl.clearIds(application);
		this.applicationService.update(application);

		// create, add and persist new application version
		ApplicationVersion version = new ApplicationVersion(application.getVersion(), ApplicationState.NEW, appId);
		base.getVersions().add(version);
		appBaseService.update(base);

		this.sendMails(application, new ApplicationStateChangeRequest(application.getState(), "", false));
	}

	@PatchMapping(value = "/version")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplicationVersion(@RequestBody @Valid ApplicationView view, Principal principal) {

		this.applicationBaseOwnerCheck(view.getName(), principal);

		// check if id exists
		if(view.getId() == null) {
			log.error("ID is not present in Application update");
			throw new ProcessingException("Cannot update application without id");
		}

		// application with specified name and version must exist
		Optional<Application> optId = applicationService.findApplication(view.getId());
		Optional<Application> optNameVersion = applicationService.findApplication(view.getName(), view.getVersion());

		if(optId.isEmpty() || optNameVersion.isEmpty()) {
			log.error("Requested application does not exist");
			throw new MissingElementException("Application does not exist");
		}

		if(!optId.get().equals(optNameVersion.get())) {
			log.error("Retrieved different applications using id and name&version, update aborted");
			throw new ProcessingException("You cannot change application name, version and id");
		}

		// application base with given name must exist
		ApplicationBase base = appBaseService.findByName(view.getName());

		// you cannot really change version label
		Optional<ApplicationVersion> version = base.getVersions().stream()
				.filter(v -> v.getVersion().equals(view.getVersion()) && v.getAppVersionId().equals(view.getId()))
				.findFirst();

		if (version.isEmpty()) {
			log.error("Application version cannot be updated (no matching versions available in ApplicationBase)");
			throw new ProcessingException("Cannot update application version");
		}

		Application application = modelMapper.map(view, Application.class);
		// rewrite creation date
		application.setCreationDate(optId.get().getCreationDate());
		applicationService.update(application);
	}

	/*
	 * both
	 */

	/**
	 *
	 * @param id application id (not an ApplicationBase or ApplicationVersion id)
	 * @param stateChangeRequest request object
	 */
	@PatchMapping(value = "/state/{id}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	@Transactional
	public void changeApplicationState(@PathVariable long id, @RequestBody ApplicationStateChangeRequest stateChangeRequest) {
		Application app = getApp(id);
		applicationService.changeApplicationState(app, stateChangeRequest.getState());
		appBaseService.updateApplicationVersionState(app.getName(), app.getVersion(), stateChangeRequest.getState());
		this.sendMails(app, stateChangeRequest);
	}

	/**
	 * Deletes application entity, labels application version as deleted
	 * @param id application id (not an ApplicationBase or ApplicationVersion id)
	 */
	@DeleteMapping(value="/{id}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void deleteApplication(@PathVariable long id, Principal principal) {
		Application app = getApp(id);
		this.applicationBaseOwnerCheck(app.getName(), principal);
		applicationService.delete(id);
		appBaseService.updateApplicationVersionState(app.getName(), app.getVersion(), ApplicationState.DELETED);
	}

	/*
	 * Utilities
	 */
	private void sendMails(Application app, ApplicationStateChangeRequest stateChangeRequest) {
		String appBaseName = app.getName().contains("_DELETED_")
                ? app.getName().substring(0, app.getName().indexOf("_DELETED_"))
                : app.getName();

		ImmutableMap<String, Object> attributes = ImmutableMap.of(
				"app_name", appBaseName,
				"app_version", app.getVersion(),
				"reason", stateChangeRequest.getReason() == null ? "" : stateChangeRequest.getReason(),
				"message", stateChangeRequest.getNotificationText() == null ? "" : stateChangeRequest.getNotificationText());
		if (!stateChangeRequest.getState().equals(ApplicationState.NEW)) {
			ApplicationBase applicationBase = appBaseService.findByName(appBaseName);
			UserView owner = modelMapper.map(userService.findByUsername(applicationBase.getOwner()).orElseThrow(() -> new IllegalArgumentException("Owner not found")), UserView.class);
			MailAttributes mailAttributes = MailAttributes.builder()
					.mailType(stateChangeRequest.getState().getMailType())
					.addressees(Collections.singletonList(owner))
					.otherAttributes(attributes)
					.build();
			this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
		}
		if (stateChangeRequest.getState().equals(ApplicationState.ACTIVE) && stateChangeRequest.shouldSendNotification()) {
			List<UserView> users = userService.findAll()
					.stream()
					.filter(User::isEnabled)
					.map(user -> modelMapper.map(user, UserView.class))
					.collect(Collectors.toList());
			MailAttributes mailAttributes = MailAttributes.builder()
					.mailType(MailType.NEW_ACTIVE_APP)
					.addressees(users)
					.otherAttributes(attributes)
					.build();
			this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
		}
	}

	private void applicationBaseOwnerCheck(ApplicationBase applicationBase, Principal principal) {
		boolean isSystemAdmin = this.getUser(principal.getName()).getRoles().stream()
				.anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN));
		boolean isOwner = applicationBase.getOwner().equals(principal.getName());
		if (!isOwner && !isSystemAdmin) {
			throw new MarketException("The user is not application owner");
		}
	}

	private void applicationBaseOwnerCheck(String applicationBaseName, Principal principal) {
		ApplicationBase applicationBase = this.appBaseService.findByName(applicationBaseName);
		this.applicationBaseOwnerCheck(applicationBase, principal);
	}

	private void applicationBaseOwnerCheck(Long Id, Principal principal) {
		ApplicationBase applicationBase = this.appBaseService.getBaseApp(Id);
		this.applicationBaseOwnerCheck(applicationBase, principal);
	}

}
