package net.geant.nmaas.portal.auth.basic;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.domain.Domain;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/api/auth/basic/registration")
public class RegistrationController {
	private static final Logger log = LogManager.getLogger(RegistrationController.class);
	@Autowired
	UserService users;
	
	@Autowired
	DomainService domains;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	ModelMapper modelMapper;
	
	@PostMapping
	@Transactional
	public void signup(@RequestBody final Registration registration) throws SignupException {
		if(registration == null || StringUtils.isEmpty(registration.getUsername()) || StringUtils.isEmpty(registration.getPassword()) )
			throw new SignupException("Invalid credentials.");
							
		User newUser = null;
		try {
			newUser = users.register(registration.getUsername());
			if(newUser == null)
				throw new SignupException("Unable to register new user.");
			if(!registration.getTermsOfUseAccepted()){
				throw new SignupException("Terms of Use were not accepted.");
			}
		} catch (ObjectAlreadyExistsException e) {
			throw new SignupException("User already exists.");
		} catch (MissingElementException e) {
			throw new SignupException("Domain not found.");
		}
		
		newUser.setPassword(passwordEncoder.encode(registration.getPassword()));
		newUser.setEmail(registration.getEmail());
		newUser.setFirstname(registration.getFirstname());
		newUser.setLastname(registration.getLastname());
		newUser.setEnabled(false);
		newUser.settermsOfUseAcceptedFlag(registration.getTermsOfUseAccepted());



		try {
			users.update(newUser);
            log.info(String.format("The user with user name - %s, first name - %s, last name - %s, email - %s have signed up with domain id - %s.",
                    registration.getUsername(),
                    registration.getFirstname(),
                    registration.getLastname(),
                    registration.getEmail(),
                    registration.getDomainId()));
			if(registration.getDomainId() != null)
				domains.addMemberRole(registration.getDomainId(), newUser.getId(), Role.ROLE_GUEST);
		} catch (ObjectNotFoundException e) {
			throw new SignupException("Domain not found."); 
		} catch (ProcessingException e) {
			throw new SignupException("Unable to update newly registered user.");
		} 
	}
	
	@GetMapping("/domains")
	@Transactional(readOnly=true)
	public List<Domain> getDomains() {
		Optional<Domain> globalDomain = domains.getGlobalDomain().map(domain -> modelMapper.map(domain, Domain.class));
		final Long globalDomainId;
		
		if(globalDomain.isPresent())
			globalDomainId = globalDomain.get().getId();
		else
			globalDomainId = null;
		
		return domains.getDomains().stream()
						.map(domain -> modelMapper.map(domain, Domain.class))
						.filter(domain -> !domain.getId().equals(globalDomainId))
						.collect(Collectors.toList());
		
	}
	
}
