package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnBaseEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppRemoveActionEvent extends AppBaseEvent {

    public AppRemoveActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
