package io.deliverances.repocrawler;

import java.util.HashSet;
import java.util.Set;

public class OptionsBuilder {

    private final Set<Project> projects = new HashSet<>();
    private final Set<Project> renamedProjects = new HashSet<>();
    private final Set<String> excludedProjectsRegex = new HashSet<>();
    private String username;
    private String password;
    private String token;
    private String rootUrl;
    private RepositoryProvider repositoryProvider = RepositoryProvider.OTHER;

    public static OptionsBuilder builder() {
        return new OptionsBuilder();
    }

    public OptionsBuilder username(String username) {
        this.username = username;
        return this;
    }

    public OptionsBuilder password(String password) {
        this.password = password;
        return this;
    }

    public OptionsBuilder token(String token) {
        this.token = token;
        return this;
    }

    public OptionsBuilder rootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
        return this;
    }

    public OptionsBuilder provider(RepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
        return this;
    }

    public OptionsBuilder provider(String provider) {
        this.repositoryProvider = RepositoryProvider.valueOf(provider.toUpperCase());
        return this;
    }

    public OptionsBuilder provider(String projectName, String branch) {
        this.projects.add(new Project(projectName, projectName, branch));
        return this;
    }

    public OptionsBuilder project(String project) {
        this.projects.add(new Project(project));
        return this;
    }

    public OptionsBuilder project(String project, String projectName, String branch) {
        this.projects.add(new Project(project, projectName, branch));
        return this;
    }

    public OptionsBuilder projectName(String projectName, String newProjectName) {
        this.renamedProjects.add(new Project(projectName, newProjectName));
        return this;
    }

    public OptionsBuilder exclude(String regex) {
        this.excludedProjectsRegex.add(regex);
        return this;
    }

    public Options build() {
        return new Options(this.username, this.password, this.token, this.rootUrl, this.repositoryProvider,
            this.projects, this.renamedProjects, this.excludedProjectsRegex);
    }

}
