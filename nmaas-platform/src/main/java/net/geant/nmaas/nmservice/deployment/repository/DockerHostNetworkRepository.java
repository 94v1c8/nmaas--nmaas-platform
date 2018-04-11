package net.geant.nmaas.nmservice.deployment.repository;

import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DockerHostNetworkRepository extends JpaRepository<DockerHostNetwork, Long> {

    Optional<DockerHostNetwork> findByDomain(String domain);

}
