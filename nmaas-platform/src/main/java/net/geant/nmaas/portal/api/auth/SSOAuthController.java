package net.geant.nmaas.portal.api.auth;

import com.google.common.collect.ImmutableMap;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.externalservices.inventory.shibboleth.ShibbolethConfigManager;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/api/auth/sso")
public class SSOAuthController {

	private UserService users;

	private DomainService domains;

	private JWTTokenService jwtTokenService;

	private ConfigurationManager configurationManager;

	private ShibbolethConfigManager shibbolethConfigManager;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public SSOAuthController(UserService users, DomainService domains, JWTTokenService jwtTokenService, ConfigurationManager configurationManager, ShibbolethConfigManager shibbolethConfigManager, ApplicationEventPublisher eventPublisher){
		this.users = users;
		this.domains = domains;
		this.jwtTokenService = jwtTokenService;
		this.configurationManager = configurationManager;
		this.shibbolethConfigManager = shibbolethConfigManager;
		this.eventPublisher = eventPublisher;
	}

	@PostMapping(value="/login")
	public UserToken login(@RequestBody final UserSSOLogin userSSOLoginData) {
		ConfigurationView configuration = this.configurationManager.getConfiguration();
		if(!configuration.isSsoLoginAllowed())
			throw new SignupException("SSO login method is not enabled");

		if(userSSOLoginData == null)
			throw new AuthenticationException("Received user SSO login data is incorrect");

		if(StringUtils.isEmpty(userSSOLoginData.getUsername()))
			throw new AuthenticationException("Missing username");

		shibbolethConfigManager.checkParam();
		userSSOLoginData.validate(shibbolethConfigManager.getKey(), shibbolethConfigManager.getTimeout());

		User user = users.findBySamlToken(userSSOLoginData.getUsername()).orElseGet(() -> registerNewUser(userSSOLoginData));

		if(!user.isEnabled())
			throw new AuthenticationException("User is not active.");

		if(configuration.isMaintenance() && user.getRoles().stream().noneMatch(value -> value.getRole().authority().equals("ROLE_SYSTEM_ADMIN")))
			throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");

		return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}

	private User registerNewUser(UserSSOLogin userSSOLoginData){
		try{
			User temp = users.register(userSSOLoginData, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
			this.sendMail(temp);
			return temp;
		} catch (ObjectAlreadyExistsException e) {
			throw new SignupException("User already exists");
		} catch (MissingElementException e) {
			throw new SignupException("Domain not found");
		}
	}

	private void sendMail(User user){
		MailAttributes mailAttributes = MailAttributes
				.builder()
				.otherAttributes(ImmutableMap.of("newUser", user.getUsername()))
				.mailType(MailType.NEW_SSO_LOGIN)
				.build();
		this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
	}
}
