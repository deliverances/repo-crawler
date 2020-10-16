package io.deliverances.repocrawler;

import java.util.Set;

/**
 * Contains options required to connect to a password.
 */
public class Options {

    private final String username;

    private final String password;

    private final String token;

    private final String rootUrl;

    private final RepositoryProvider repositoryProvider;

    private final Set<Project> projects;

    private final Set<Project> renamedProjects;

    private final Set<String> excludedProjectsRegex;

    public Options(String username, String password, String token, String rootUrl, RepositoryProvider repositoryProvider,
                   Set<Project> projects, Set<Project> renamedProjects,
                   Set<String> excludedProjectsRegex) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.rootUrl = rootUrl;
        this.repositoryProvider = repositoryProvider;
        this.projects = projects;
        this.renamedProjects = renamedProjects;
        this.excludedProjectsRegex = excludedProjectsRegex;
    }

    boolean isIgnored(String project) {
        return this.excludedProjectsRegex.stream().anyMatch(project::matches);
    }

    String projectName(String projectName) {
        return this.renamedProjects.stream()
            .filter(renamed -> renamed.getProject().equals(projectName))
            .findFirst()
            .map(Project::getProjectName)
            .orElse(projectName);
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getToken() {
        return token;
    }

    String getRootUrl() {
        return rootUrl;
    }

    RepositoryProvider getRepositoryProvider() {
        return repositoryProvider;
    }

    Set<Project> getProjects() {
        return projects;
    }

    Set<Project> getRenamedProjects() {
        return renamedProjects;
    }

    Set<String> getExcludedProjectsRegex() {
        return excludedProjectsRegex;
    }

}
