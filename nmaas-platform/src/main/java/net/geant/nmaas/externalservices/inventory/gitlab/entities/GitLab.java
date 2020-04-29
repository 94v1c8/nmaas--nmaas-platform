package net.geant.nmaas.externalservices.inventory.gitlab.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "gitlab")
public class GitLab {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String server;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Integer port;

    public GitLab(String server, String token, Integer port) {
        this.server = server;
        this.token = token;
        this.port = port;
    }

    @JsonIgnore
    public String getApiUrl(){
        return String.format("http://%s:%d", this.server, this.port);
    }

}
