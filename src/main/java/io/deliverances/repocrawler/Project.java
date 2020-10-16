package io.deliverances.repocrawler;

import java.util.Objects;

public class Project {

    private final String project;

    private final String projectName;

    private final String branch;

    public Project(String project) {
        this(project, project, "master");
    }

    public Project(String project, String newProject) {
        this(project, newProject, "master");
    }

    public Project(String project, String projectName, String branch) {
        this.project = project;
        this.projectName = projectName;
        this.branch = branch;
    }

    public String getProject() {
        return project;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getBranch() {
        return branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project that = (Project) o;
        return Objects.equals(project, that.project) &&
            Objects.equals(projectName, that.projectName) &&
            Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, projectName, branch);
    }

}
