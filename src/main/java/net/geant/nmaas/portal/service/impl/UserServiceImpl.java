package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.Base64;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_SYSTEM_ADMIN;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepo;
	
	private final UserRoleRepository userRoleRepo;

	private final PasswordEncoder passwordEncoder;

	private final ConfigurationManager configurationManager;

	private final ModelMapper modelMapper;

	@Autowired
	public UserServiceImpl(UserRepository userRepo, UserRoleRepository userRoleRepo, PasswordEncoder passwordEncoder, ConfigurationManager configurationManager, ModelMapper modelMapper){
		this.userRepo = userRepo;
		this.userRoleRepo = userRoleRepo;
		this.passwordEncoder = passwordEncoder;
		this.configurationManager = configurationManager;
		this.modelMapper = modelMapper;
	}
	
	@Override
	public boolean hasPrivilege(User user, Domain domain, Role role) {
		if(user == null || domain == null || role == null)
			return false;
		
		UserRole userRole = userRoleRepo.findByDomainAndUserAndRole(domain, user, role);		
		
		return (userRole != null);
	}

	@Override
	public boolean canUpdateData(String username, final List<UserRole> userRoles){
		checkParam(username);
		User user = findByUsername(username).orElseThrow(() -> new MissingElementException("User with username " + username + " not found"));
		return isAdmin(user) || isDomainAdminInUserDomain(user, userRoles);
	}

	private boolean isDomainAdminInUserDomain(User admin, final List<UserRole> userRoles){
		return admin.getRoles().stream()
				.filter(role -> role.getRole().equals(ROLE_DOMAIN_ADMIN))
				.anyMatch(role -> userRoles.stream().anyMatch(userRole -> userRole.getDomain().equals(role.getDomain())));
	}

	private boolean isAdmin(User user){
		return user.getRoles().stream().anyMatch(role -> role.getRole().equals(ROLE_SYSTEM_ADMIN));
	}

	@Override
	public List<User> findAll() {		
		return userRepo.findAll();
	}

	@Override
	public Page<User> findAll(Pageable pageable) {		
		return userRepo.findAll(pageable);
	}



	@Override
	public Optional<User> findByUsername(String username) {
		return (username != null ? userRepo.findByUsername(username) : Optional.empty());
	}
	
	@Override
	public Optional<User> findById(Long id) {
		return (id != null ? userRepo.findById(id) : Optional.empty());
	}

	@Override
	public Optional<User> findBySamlToken(String token) {
		return (token != null ? userRepo.findBySamlToken(token) : Optional.empty());
	}

	@Override
	public User findByEmail(String email){
		return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User with mail "+email+ " not found"));
	}

	@Override
	public boolean existsByUsername(String username) {
		checkParam(username);
		return userRepo.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepo.existsByEmail(email);
	}

	@Override
	public boolean existsById(Long id) {
		checkParam(id);
		return userRepo.existsById(id);
	}

	@Override
	public User register(Registration registration, Domain globalDomain, Domain domain){

		if(userRepo.existsByUsername(registration.getUsername()) || userRepo.existsByEmail(registration.getEmail())){
			throw new SignupException("User already exists");
		}

		User newUser = new User(registration.getUsername(), false, passwordEncoder.encode(registration.getPassword()), globalDomain, Role.ROLE_GUEST);
		newUser.setEmail(registration.getEmail());
		newUser.setFirstname(registration.getFirstname());
		newUser.setLastname(registration.getLastname());
		newUser.setEnabled(false);
		if(domain != null){
			newUser.setNewRoles(ImmutableSet.of(new UserRole(newUser, domain, Role.ROLE_GUEST)));
		}
		newUser.setTermsOfUseAccepted(registration.getTermsOfUseAccepted());
		newUser.setPrivacyPolicyAccepted(registration.getPrivacyPolicyAccepted());
		newUser.setSelectedLanguage(this.configurationManager.getConfiguration().getDefaultLanguage());
		userRepo.save(newUser);

		return newUser;
	}

	@Override
	public User register(UserSSOLogin userSSO, Domain globalDomain){
		byte[] array = new byte[16]; // random password
		new SecureRandom().nextBytes(array);
		String generatedString = Base64.getEncoder().encodeToString(array);
		User newUser = new User("thirdparty-"+System.currentTimeMillis(), true, generatedString, globalDomain, Role.ROLE_INCOMPLETE);
		newUser.setSamlToken(userSSO.getUsername()); //Check user ID TODO: check if it's truly unique!
		newUser.setSelectedLanguage(this.configurationManager.getConfiguration().getDefaultLanguage());
		userRepo.save(newUser);

		return newUser;
	}
	
	@Override
	public void update(User user) {
		checkParam(user);
		checkParam(user.getId());
				
		if(!userRepo.existsById(user.getId()))
			throw new ProcessingException("User (id=" + user.getId() + " does not exists.");
		
		userRepo.saveAndFlush(user);
	}

	@Override
	public void delete(User user) {
		checkParam(user);
		checkParam(user.getId());
		
		userRepo.delete(user);
	}

	@Override
	public void deleteById(Long userId) {
		checkParam(userId);

		userRepo.deleteById(userId);
	}

	@Override
	@Transactional
	public void setEnabledFlag(Long userId, boolean isEnabled) {
		userRepo.setEnabledFlag(userId, isEnabled);
	}

	@Override
	@Transactional
	public void setUserLanguage(Long userId, final String userLanguage){
		userRepo.setUserLanguage(userId, userLanguage);
	}

	@Override
	@Transactional
	public void setTermsOfUseAcceptedFlag(Long userId, boolean termsOfUseAcceptedFlag){ userRepo.setTermsOfUseAcceptedFlag(userId, termsOfUseAcceptedFlag);}

	@Override
	@Transactional
	public void setTermsOfUseAcceptedFlagByUsername(String username, boolean termsOfUseAcceptedFlag) {
		User user = userRepo.findByUsername(username).orElseThrow(()
				-> new UsernameNotFoundException("User " + username + " not found."));
		userRepo.setTermsOfUseAcceptedFlag(user.getId(), termsOfUseAcceptedFlag);
	}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag){ userRepo.setPrivacyPolicyAcceptedFlag(userId, privacyPolicyAcceptedFlag);}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlagByUsername(String username, boolean privacyPolicyAcceptedFlag) {
		User user = userRepo.findByUsername(username).orElseThrow(()
				-> new UsernameNotFoundException("User " + username + " not found."));
		userRepo.setPrivacyPolicyAcceptedFlag(user.getId(), privacyPolicyAcceptedFlag);
	}

	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	private void checkParam(String username) {
		if(username == null)
			throw new IllegalArgumentException("username is null");
	}
	
	private void checkParam(User user) {
		if(user == null)
			throw new IllegalArgumentException("user is null");
	}

	@Override
	@Transactional
	public List<UserView> findAllUsersWithAdminRole(){
		return findAll().stream()
				.filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_SYSTEM_ADMIN.name())))
				.map(user -> modelMapper.map(user, UserView.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public List<UserView> findUsersWithRoleSystemAdminAndOperator(){
		return findAll().stream()
				.filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_SYSTEM_ADMIN.name()) || role.getRole().name().equalsIgnoreCase(Role.ROLE_OPERATOR.name()) ))
				.map(user -> modelMapper.map(user, UserView.class))
				.collect(Collectors.toList());
	}
}
