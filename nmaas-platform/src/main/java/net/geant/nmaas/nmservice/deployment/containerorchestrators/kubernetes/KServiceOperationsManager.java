package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;

public interface KServiceOperationsManager {

    void restartService(Identifier deploymentId);

}
