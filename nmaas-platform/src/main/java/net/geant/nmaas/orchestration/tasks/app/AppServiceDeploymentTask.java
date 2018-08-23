package net.geant.nmaas.orchestration.tasks.app;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.orchestration.events.app.AppDeployServiceActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Slf4j
public class AppServiceDeploymentTask {

    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    public AppServiceDeploymentTask(NmServiceDeploymentProvider serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(AppDeployServiceActionEvent event) throws CouldNotDeployNmServiceException {
        try{
            serviceDeployment.deployNmService(event.getRelatedTo());
        }catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

}
