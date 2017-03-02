package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.AppInstance;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceSubscription;
import net.geant.nmaas.portal.api.domain.Id;

@RestController("/portal/api/apps/instance")
public class AppInstanceController {

	@RequestMapping(method=RequestMethod.GET)
	public List<AppInstance> getInstances(Principal principal) {
		return new ArrayList<AppInstance>();
	}
	
	@RequestMapping(value="/{appInstanceId}", method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#appInstanceId, 'appInstance', 'owner')")
	public AppInstance getAppInstance(@PathVariable(value="appInstanceId", required=true) Long appInstanceId) {
		return null;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public Id runAppInstance(@RequestBody(required=true) AppInstanceSubscription appInstanceSubscription, Principal principal) {
		return null;
	}
	
	@RequestMapping(value="/{appInstanceId}", method=RequestMethod.DELETE)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#appInstanceId, 'appInstance', 'owner')")	
	public void deleteAppInstance(@PathVariable(value="appInstanceId", required=true) Long appInstanceId) {
		
	}
	
	@RequestMapping(value="/{appInstanceId}/state", method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#appInstanceId, 'appInstance', 'owner')")		
	public AppInstanceState getState(@PathVariable(value="appInstanceId", required=true) Long appInstanceId) {
		return null;
	}
	
}
