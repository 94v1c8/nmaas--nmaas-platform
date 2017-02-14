package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DefaultAppDeploymentMonitor implements AppDeploymentMonitor, AppDeploymentStateChangeListener {

    @Autowired
    private AppLifecycleRepository repository;

    @Override
    public AppLifecycleState state(Identifier deploymentId) throws InvalidDeploymentIdException {
        return retrieveCurrentState(deploymentId);
    }

    @Override
    public AppUiAccessDetails userAccessDetails(Identifier deploymentId) throws InvalidAppStateException, InvalidDeploymentIdException {
        if (AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED.equals(retrieveCurrentState(deploymentId)))
            return retrieveAccessDetails(deploymentId);
        else
            throw new InvalidAppStateException("Application deployment process didn't finish yet.");
    }

    @Override
    public void notifyStateChange(Identifier deploymentId, DcnDeploymentState state) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(state);
            repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            System.out.println("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(deploymentId, AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    @Override
    public void notifyStateChange(Identifier deploymentId, NmServiceDeploymentState state) {
        try {
            AppDeploymentState newDeploymentState = repository.loadCurrentState(deploymentId).nextState(state);
            repository.updateDeploymentState(deploymentId, newDeploymentState);
        } catch (InvalidAppStateException e) {
            System.out.println("State notification failure -> " + e.getMessage());
            repository.updateDeploymentState(deploymentId, AppDeploymentState.INTERNAL_ERROR);
        } catch (InvalidDeploymentIdException e) {
            System.out.println("State notification failure -> " + e.getMessage());
        }
    }

    private AppLifecycleState retrieveCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadCurrentState(deploymentId).lifecycleState();
    }

    private AppUiAccessDetails retrieveAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.loadAccessDetails(deploymentId);
    }

}
