package net.geant.nmaas.externalservices.inventory.gitlab;

import static com.google.common.base.Preconditions.checkArgument;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.model.GitLabView;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabNotFoundException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitLabSupportedException;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Log4j2
public class GitLabManager {

    private GitLabRepository repository;

    private ModelMapper modelMapper;

    private GitLabApi gitLabApi;

    @Autowired
    public GitLabManager(GitLabRepository repository, ModelMapper modelMapper){
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public List<GitLabView> getAllGitlabConfig(){
        return this.repository.findAll().stream()
                .map(config -> modelMapper.map(config, GitLabView.class))
                .collect(Collectors.toList());
    }

    public GitLab getGitlabConfigById(Long id) {
        return this.repository.findById(id)
                .orElseThrow(()->new GitLabNotFoundException("GitLab configuration with id " + id + " not found in repository"));
    }

    public Long addGitlabConfig(GitLabView gitLabConfig) {
        if(repository.count() > 0){
            throw new OnlyOneGitLabSupportedException("GitLab config already exists. It can be either removed or updated.");
        }
        GitLab gitLab = modelMapper.map(gitLabConfig, GitLab.class);
        this.repository.save(gitLab);
        return gitLab.getId();
    }

    public void updateGitlabConfig(Long id, GitLabView updatedGitLabConfig) {
        Optional<GitLab> gitLabConfig = repository.findById(id);
        if(!gitLabConfig.isPresent()){
            throw new GitLabNotFoundException("GitLab config with id "+id+" not found in repository.");
        }
        repository.save(modelMapper.map(updatedGitLabConfig, GitLab.class));
    }

    public void removeGitlabConfig(Long id) {
        GitLab gitLabConfig = repository.findById(id)
                .orElseThrow(() -> new GitLabNotFoundException("GitLab config with id "+id+" not found in repository."));
        repository.delete(gitLabConfig);
    }

    public String getGitLabApiUrl(){
        return loadSingleGitlabConfig().getApiUrl();
    }

    public String getGitLabApiToken(){
        return loadSingleGitlabConfig().getToken();
    }

    public String getGitLabRepositoryAccessUsername(){
        return loadSingleGitlabConfig().getRepositoryAccessUsername();
    }

    public String getGitlabServer() {
        return loadSingleGitlabConfig().getServer();
    }

    public int getGitlabPort() {
        return loadSingleGitlabConfig().getPort();
    }

    public void validateGitLabInstance() {
        GitLab gitLabInstance = this.loadSingleGitlabConfig();
        checkArgument(gitLabInstance.getToken()!= null && !gitLabInstance.getRepositoryAccessUsername().isEmpty(), "Repository access username is null or empty");
        checkArgument(gitLabInstance.getPort() != null, "GitLab port is null");
        checkArgument(gitLabInstance.getServer() != null && !gitLabInstance.getServer().isEmpty(), "GitLab server is null or empty");
        checkArgument(gitLabInstance.getToken() != null && !gitLabInstance.getToken().isEmpty(), "GitLab token is null or empty");
        this.createGitLabApi(gitLabInstance.getApiUrl(), gitLabInstance.getToken());
        try {
            this.gitLabApi.getVersion();
            log.debug("GitLab instance is running");
        } catch (GitLabApiException e){
            throw new GitLabInvalidConfigurationException("GitLab instance is not running -> " + e.getMessage());
        }
    }

    private GitLab loadSingleGitlabConfig(){
        if(repository.count() != 1){
            throw new IllegalStateException("Found " + repository.count() + " gitlab config instead of one");
        }
        return repository.findAll().get(0);
    }

    private void createGitLabApi(String apiUrl, String token){
        if(this.gitLabApi == null || !this.gitLabApi.getGitLabServerUrl().equals(apiUrl) || !this.gitLabApi.getAuthToken().equals(token)){
            this.gitLabApi = new GitLabApi(GitLabApi.ApiVersion.V4, apiUrl, token);
        }
    }
}
