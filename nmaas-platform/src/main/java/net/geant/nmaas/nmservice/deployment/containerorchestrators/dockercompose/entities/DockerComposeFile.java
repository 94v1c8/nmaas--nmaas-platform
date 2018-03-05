package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_compose_file")
public class DockerComposeFile {

    public final static String DEFAULT_DOCKER_COMPOSE_FILE_NAME = "docker-compose.yml";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String composeFileContent;

    public DockerComposeFile() {
    }

    public DockerComposeFile(String composeFileContent) {
        this.composeFileContent = composeFileContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComposeFileContent() {
        return composeFileContent;
    }

    public void setComposeFileContent(String composeFileContent) {
        this.composeFileContent = composeFileContent;
    }
}
