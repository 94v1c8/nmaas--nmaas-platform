package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppRemoveActionEvent extends ApplicationEvent {

    private Identifier deploymentId;

    public AppRemoveActionEvent(Object source, Identifier deploymentId) {
        super(source);
        this.deploymentId = deploymentId;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }
}
