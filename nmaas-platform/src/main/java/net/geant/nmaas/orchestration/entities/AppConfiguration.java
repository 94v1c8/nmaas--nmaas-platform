package net.geant.nmaas.orchestration.entities;

import javax.persistence.*;

/**
 * Application configuration in Json format provided by the user/client.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="app_deployment_configuration")
public class AppConfiguration {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="id")
    private Long id;

    @Column(nullable = false)
    private String jsonInput;

    public AppConfiguration() {}

    public AppConfiguration(String jsonInput) {
        this.jsonInput = jsonInput;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setJsonInput(String jsonInput) {
        this.jsonInput = jsonInput;
    }

    public String getJsonInput() {
        return jsonInput;
    }

}
