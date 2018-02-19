package net.geant.nmaas.portal.persistent.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String username;
	
	//@NotNull
	private String password;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true, mappedBy="id.user")
	private List<UserRole> roles = new ArrayList<UserRole>();

	protected User() {
	}
	
	public User(String username) {
		this.username = username;
	}
	
	public User(String username, String password, Domain domain, Role role) {
		this(username);
		this.password = password;
		this.roles.add(new UserRole(this, domain, role));
	}

	public User(String username, String password, Domain domain, List<Role> roles) {
		this.username = username;
		this.password = password;
		for (Role role : roles) {
			this.roles.add(new UserRole(this, domain, role));
		}	
	}
	
	protected User(Long id, String username, Domain domain, Role role) {
		this.id = id;
		this.username = username;
		this.getRoles().add(new UserRole(this, domain, role));
	}
	
	protected User(Long id, String username, String password, List<UserRole> roles) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.roles = roles;
	}


	public Long getId() {
		return id;
	}


	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setNewRoles(Set<UserRole> roles) {
		this.roles.clear();
		this.roles.addAll(roles);
	}
	
	public void setRoles(List<UserRole> roles) {
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}


	public List<UserRole> getRoles() {
		return roles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}	
	
	
}
