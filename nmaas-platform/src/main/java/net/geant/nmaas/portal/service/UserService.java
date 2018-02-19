package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;

public interface UserService {
	
	boolean hasPriviledge(User user, Domain domain, Role role);
	User findByUsername(String username);
	User findById(Long id);
	
	User register(String username) throws ObjectAlreadyExistsException, MissingElementException;
	User register(String username, String password, Long domainId) throws ObjectAlreadyExistsException, MissingElementException;
	
	List<User> findAll();
	Page<User> findAll(Pageable pageable);

	void delete(User user) throws MissingElementException, ProcessingException;	
	void update(User user) throws ProcessingException;
}
