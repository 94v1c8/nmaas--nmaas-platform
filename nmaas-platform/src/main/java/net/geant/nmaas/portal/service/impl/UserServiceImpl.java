package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements net.geant.nmaas.portal.service.UserService {
	
	UserRepository userRepo;
	
	UserRoleRepository userRoleRepo;

	@Autowired
	public UserServiceImpl(UserRepository userRepo, UserRoleRepository userRoleRepo){
		this.userRepo = userRepo;
		this.userRoleRepo = userRoleRepo;
	}
	
	@Override
	public boolean hasPrivilege(User user, Domain domain, Role role) {
		if(user == null || domain == null || role == null)
			return false;
		
		UserRole userRole = userRoleRepo.findByDomainAndUserAndRole(domain, user, role);		
		
		return (userRole != null);
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
	public boolean existsByUsername(String username) {
		checkParam(username);
		return userRepo.existsByUsername(username);
	}

	@Override
	public boolean existsById(Long id) {
		checkParam(id);
		return userRepo.existsById(id);
	}

	@Override
	public User register(String username, Domain domain) throws ObjectAlreadyExistsException, MissingElementException {
		checkParam(username);
		return register(username, false, null, domain);
	}

	@Override
	public User register(String username, boolean enabled, String password, Domain domain) throws ObjectAlreadyExistsException, MissingElementException {

		checkParam(username);

		// check if user already exists
		Optional<User> user = userRepo.findByUsername(username);
		if(user.isPresent())
			throw new ObjectAlreadyExistsException("User already exists.");

		User newUser = new User(username, enabled, password, domain, Role.ROLE_GUEST);
		
		return userRepo.save(newUser);				
	}	
	
	@Override
	public void update(User user) throws ProcessingException {
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
	@Transactional
	public void setEnabledFlag(Long userId, boolean isEnabled) {
		userRepo.setEnabledFlag(userId, isEnabled);
	}

	@Override
	@Transactional
	public void setTermsOfUseAcceptedFlag(Long userId, boolean touAccept){ userRepo.setTermsOfUseAcceptedFlag(userId, touAccept);}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag){ userRepo.setPrivacyPolicyAcceptedFlag(userId, privacyPolicyAcceptedFlag);}

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

}
