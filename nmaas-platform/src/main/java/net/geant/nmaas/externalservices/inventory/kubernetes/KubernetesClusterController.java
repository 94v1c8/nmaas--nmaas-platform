package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RESTful API for managing Kubernetes cluster instances.
 */
@RestController
@RequestMapping(value = "/api/management/kubernetes")
public class KubernetesClusterController {

    private KubernetesClusterManager clusterManager;

    private ModelMapper modelMapper;

    @Autowired
    public KubernetesClusterController(KubernetesClusterManager clusterManager, ModelMapper modelMapper) {
        this.clusterManager = clusterManager;
        this.modelMapper = modelMapper;
    }

    /**
     * List all {@link KCluster} stored in repository
     * @return list of {@link KubernetesClusterView} objects
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping
    public List<KubernetesClusterView> listAllKubernetesClusters() {
        return clusterManager.getAllClusters();
    }

    /**
     * Fetch {@link KCluster} instance by id
     * @param id Unique {@link KCluster} id
     * @return {@link KCluster} instance
     * @throws KubernetesClusterNotFoundException when cluster with given id does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/{id}")
    public KClusterView getKubernetesCluster(@PathVariable("id") Long id) {
        return modelMapper.map(clusterManager.getClusterById(id), KClusterView.class);
    }

    /**
     * Store new {@link KCluster} instance. In current implementation only a single Kubernetes cluster in
     * the system is supported.
     * @param clusterView new {@link KCluster} data
     * @throws OnlyOneKubernetesClusterSupportedException when trying to add new cluster while one already exists (HttpStatus.NOT_ACCEPTABLE)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long addKubernetesCluster(@RequestBody KClusterView clusterView) {
        KCluster cluster = modelMapper.map(clusterView, KCluster.class);
        cluster.validate();
        clusterManager.addNewCluster(cluster);
        return cluster.getId();
    }

    /**
     * Update {@link KCluster} instance
     * @param id Unique {@link KCluster} id
     * @param clusterView {@link KCluster} instance pass to update
     * @throws KubernetesClusterNotFoundException when cluster with given id does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @PutMapping(value = "/{id}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateKubernetesCluster(@PathVariable("id") Long id, @RequestBody KClusterView clusterView) {
        KCluster cluster = modelMapper.map(clusterView, KCluster.class);
        cluster.validate();
        clusterManager.updateCluster(id, cluster);
    }

    /**
     * Remove {@link KCluster} instance
     * @param id Unique {@link KCluster} id
     * @throws KubernetesClusterNotFoundException when Kubernetes cluster does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeKubernetesCluster(@PathVariable("id") Long id) {
        clusterManager.removeCluster(id);
    }

    @ExceptionHandler(KubernetesClusterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleKubernetesClusterNotFoundExceptionException(KubernetesClusterNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(OnlyOneKubernetesClusterSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleOnlyOneKubernetesClusterSupportedException(OnlyOneKubernetesClusterSupportedException ex) {
        return ex.getMessage();
    }

}
