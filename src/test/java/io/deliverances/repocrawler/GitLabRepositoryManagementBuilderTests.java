package io.deliverances.repocrawler;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class GitLabRepositoryManagementBuilderTests {

    private GitLabRepositoryManagementBuilder builder = new GitLabRepositoryManagementBuilder();

    @Test
    void shouldReturnFalseWhenUrlIsEmpty() {
        then(builder.build(OptionsBuilder.builder()
            .username("foo").password("bar").build())).isNull();
    }

    @Test
    void shouldReturnFalseWhenUrlDoesNotContainGitLab() {
        then(builder.build(OptionsBuilder.builder()
            .username("foo").password("bar")
            .rootUrl("foo").build())).isNull();
    }

    @Test
    void shouldReturnTrueWhenRepositoryProviderIsGitLabAsEnum() {
        then(gitlabBuilder().build(OptionsBuilder.builder().rootUrl("https://foo.com")
            .username("foo").password("bar")
            .provider(RepositoryProvider.GITLAB).build())).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenRepositoryIsGitLab() {
        then(gitlabBuilder().build(OptionsBuilder.builder()
            .rootUrl("foo").token("bar")
            .provider("gitlab").build())).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenUrlContainsGitLab() {
        then(gitlabBuilder().build(OptionsBuilder.builder()
            .rootUrl("https://gitlab.com")
            .token("foo").build())).isNotNull();
    }

    private GitLabRepositoryManagementBuilder gitlabBuilder() {
        return new GitLabRepositoryManagementBuilder() {
            @Override
            public RepositoryManagement createNewRepositoryManagement(Options options) {
                return new GitLabRepositoryManagement(options) {

                    @Override
                    GitlabAPI connect(Options options) {
                        return mock(GitlabAPI.class);
                    }

                    @Override
                    List<GitlabProject> groupProjects(String group) {
                        return Collections.singletonList(new GitlabProject());
                    }

                    @Override
                    byte[] getDescriptor(String org, String repo, String branch, String filePath) throws IOException {
                        return "".getBytes();
                    }
                };
            }
        };
    }

}
