package net.geant.nmaas.portal.auth.basic;

import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class TokenAuthenticationService {

	private static final String AUTH_HEADER="Authorization";
	private static final String AUTH_METHOD="Bearer";
	
	private JWTTokenService jwtTokenService;

	private UserRepository userRepository;

	@Autowired
	public TokenAuthenticationService(JWTTokenService jwtTokenService, UserRepository userRepository) {
		this.jwtTokenService = jwtTokenService;
		this.userRepository = userRepository;
	}

	public Authentication getAuthentication(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader(AUTH_HEADER);
		if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(AUTH_METHOD + " "))
			throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");

		String token = authHeader.substring(AUTH_METHOD.length() + 1);

		String username = jwtTokenService.getClaims(token).getSubject();
		Object scopes = jwtTokenService.getClaims(token).get("scopes");

		Set<SimpleGrantedAuthority> authorities = null;

		if (scopes != null && scopes instanceof List<?>) {
			authorities = new HashSet<>();
			for (Map<String, String> authority : (List<Map<String, String>>) scopes)
				for (String role : authority.values())
					authorities.add(new SimpleGrantedAuthority(role.substring(role.indexOf(":") + 1)));
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

		return authentication;
	}

	public String getAnonymousAccessToken(){
		User systemComponent = userRepository.findByUsername("system_component").orElseThrow(() -> new AuthenticationException("User cannot be found"));
        return jwtTokenService.getToken(systemComponent);
    }

}
