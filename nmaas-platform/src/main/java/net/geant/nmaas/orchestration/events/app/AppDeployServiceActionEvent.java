package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppDeployServiceActionEvent extends AppBaseEvent {

    public AppDeployServiceActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
