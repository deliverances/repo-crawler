package io.deliverances.repocrawler;

import org.apache.commons.lang3.StringUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class GitLabRepositoryManagement implements RepositoryManagement {

    private static final Logger LOG = LoggerFactory.getLogger(GitLabRepositoryManagement.class);

    private final GitlabAPI gitlabAPI;

    private final Options options;

    GitLabRepositoryManagement(Options options) {
        this.options = options;
        this.gitlabAPI = connect(options);
        this.gitlabAPI.setResponseReadTimeout(5000);
    }

    GitLabRepositoryManagement(GitlabAPI gitlabAPI, Options options) {
        this.gitlabAPI = gitlabAPI;
        this.options = options;
    }

    GitlabAPI connect(Options options) {
        if (StringUtils.isNotBlank(options.getToken())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connect to GitLab using token");
            }
            return GitlabAPI.connect(options.getRootUrl(), options.getToken());
        } else if (StringUtils.isNotBlank(options.getUsername())) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Connect to GitLab using credential");
                }
                GitlabSession session = GitlabAPI
                    .connect(options.getRootUrl(), options.getUsername(), options.getPassword());
                return GitlabAPI.connect(options.getRootUrl(), session.getPrivateToken());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("Neither token nor username and password passed");
    }

    @Override
    public List<Repository> repositories(String orgOrWorkspace) {
        try {
            List<GitlabProject> gitlabProjects = groupProjects(orgOrWorkspace);
            if (gitlabProjects.isEmpty()) {
                throw new IllegalStateException("No projects found for the group [" + orgOrWorkspace + "]");
            }
            List<Repository> repositories = getAllProjects(gitlabProjects);
            return addManuallySetProjects(orgOrWorkspace, repositories);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    List<GitlabProject> groupProjects(String group) throws IOException {
        return this.gitlabAPI.getGroupProjects(this.gitlabAPI.getGroup(group));
    }

    private List<Repository> getAllProjects(List<GitlabProject> map) {
        return map.stream()
            .map(entry -> new Repository(options.projectName(entry.getName()), entry.getSshUrl(), entry.getHttpUrl(), "master"))
            .filter(repository -> !options.isIgnored(repository.getName()))
            .collect(Collectors.toList());
    }

    private List<Repository> addManuallySetProjects(String org, List<Repository> repositories) {
        repositories.addAll(this.options.getProjects()
            .stream()
            .map(project -> new Repository(options.projectName(project.getProjectName()),
                sshKey(org, project), cloneUrl(org, project), project.getBranch())).collect(Collectors.toSet()));
        return repositories;
    }

    @Override
    public String sshKey(String org, Project project) {
        return "git@" + host() + ":" + org + "/" + project.getProject() + ".git";
    }

    @Override
    public String host() {
        return URI.create(this.options.getRootUrl()).getHost();
    }

    @Override
    public String cloneUrl(String org, Project project) {
        return "https://" + host() + "/" + org + "/" + project.getProject() + ".git";
    }

    @Override
    public String fileContent(String orgOrWorkspace, String repo, String branch, String filePath) {
        try {
            byte[] bytes = getDescriptor(orgOrWorkspace, repo, branch, filePath);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    byte[] getDescriptor(String org, String repo, String branch, String filePath) throws IOException {
        GitlabProject project = this.gitlabAPI.getProject(org, repo);
        return this.gitlabAPI.getRawFileContent(project, branch, URLEncoder.encode(filePath, StandardCharsets.UTF_8.name()));
    }

}
