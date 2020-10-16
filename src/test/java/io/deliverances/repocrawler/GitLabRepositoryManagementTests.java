package io.deliverances.repocrawler;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitLabRepositoryManagementTests {

    private final String GITLAB_URL = "https://gitlab.com";

    private GitLabRepositoryManagement management;

    private GitlabAPI gitlabAPI;

    @BeforeEach
    void setUp() {
        this.gitlabAPI = mock(GitlabAPI.class);
        management = new GitLabRepositoryManagement(gitlabAPI, OptionsBuilder.builder()
            .rootUrl(GITLAB_URL)
            .build());
    }

    @Test
    void connectShouldReturnGitlabAPIFromUrlAndToken() {
        GitlabAPI api = management.connect(OptionsBuilder.builder()
            .rootUrl(GITLAB_URL)
            .token("random-string-for-token")
            .build());
        assertThat(api).isNotNull();
    }

    @Test
    void repositoriesShouldReturnListOfNamesOfRepoForGroup() throws IOException {
        when(gitlabAPI.getGroupProjects(any(GitlabGroup.class)))
            .thenReturn(mockGitlabProjects());
        when(gitlabAPI.getGroup(anyString()))
            .thenReturn(mockGitlabGroup());

        then(management.repositories("test")).hasSize(1);
    }

    @Test
    void repositoriesShouldThrowIllegalStateExceptionWithMessageContaining() throws IOException {
        when(gitlabAPI.getGroupProjects(any(GitlabGroup.class)))
            .thenReturn(new ArrayList<>());
        when(gitlabAPI.getGroup(anyString()))
            .thenReturn(mockGitlabGroup());

        assertThatIllegalStateException().isThrownBy(() -> management.repositories("check"))
            .withMessageContaining("No projects found for the group");
    }

    @Test
    void repositoriesShouldThrowIllegalStateExceptionFromIOException() throws IOException {
        when(gitlabAPI.getGroup(anyString()))
            .thenThrow(IOException.class);

        assertThatIllegalStateException().isThrownBy(() -> management.repositories("random"))
            .withCauseInstanceOf(IOException.class);
    }

    @Test
    void fileContentShouldReturnFileContentWhenFileExists() throws IOException {
        when(gitlabAPI.getProject(anyString(), anyString()))
            .thenReturn(mockGitlabProject());
        when(gitlabAPI.getRawFileContent(any(GitlabProject.class), anyString(), anyString()))
            .thenReturn(mockRawContents());

        then(management.fileContent("", "", "master", "")).isNotNull();
    }

    @Test
    void fileContentShouldThrowIllegalStateExceptionFromIOException() throws IOException {
        when(gitlabAPI.getProject(anyString(), anyString()))
            .thenThrow(IOException.class);
        when(gitlabAPI.getRawFileContent(any(GitlabProject.class), anyString(), anyString()))
            .thenReturn(mockRawContents());

        assertThatIllegalStateException().isThrownBy(() -> management.fileContent("", "", "master", ""))
            .withCauseInstanceOf(IOException.class);
    }

    @Test
    void sshKeyShouldReturnNonEmptyString() {
        String sshKey = management.sshKey("test", mockProject());
        assertThat(sshKey).isEqualTo("git@gitlab.com:test/check.git");
    }

    @Test
    void hostShouldReturnNonEmptyString() {
        assertThat(management.host()).isEqualTo("gitlab.com");
    }

    @Test
    void cloneUrlShouldReturnNonEmptyString() {
        String cloneUrl = management.cloneUrl("test", mockProject());
        assertThat(cloneUrl).isEqualTo("https://gitlab.com/test/check.git");
    }

    private List<GitlabProject> mockGitlabProjects() {
        List<GitlabProject> projects = new ArrayList<>();
        projects.add(mockGitlabProject());
        return projects;
    }

    private GitlabProject mockGitlabProject() {
        GitlabProject project = new GitlabProject();
        project.setName("A Gitlab project");
        return project;
    }

    private GitlabGroup mockGitlabGroup() {
        GitlabGroup group = new GitlabGroup();
        group.setFullName("Gitlab Tester");
        return group;
    }

    private byte[] mockRawContents() {
        return "mockRawContents".getBytes();
    }

    private Project mockProject() {
        return new Project("check");
    }

}
