package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class GitHubRepositoryManagementBuilderTests {

    private GitHubRepositoryManagementBuilder builder = new GitHubRepositoryManagementBuilder();

    @Test
    void shouldReturnFalseWhenUrlIsEmpty() {
        then(builder.build(OptionsBuilder.builder().build())).isNull();
    }

    @Test
    void shouldReturnFalseWhenUrlDoesNotContainGitHub() {
        then(builder.build(OptionsBuilder.builder().rootUrl("foo").build())).isNull();
    }

    @Test
    void shouldReturnTrueWhenRepositoryIsGitHubAsEnum() {
        then(githubBuilder().build(OptionsBuilder.builder()
            .rootUrl("foo")
            .provider(RepositoryProvider.GITHUB)
            .build())).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenRepositoryProviderIsGitHub() {
        then(githubBuilder().build(OptionsBuilder.builder()
            .rootUrl("foo")
            .provider("github")
            .build())).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenUrlContainsGitHub() {
        then(githubBuilder().build(OptionsBuilder.builder()
            .rootUrl("https://github")
            .build())).isNotNull();
    }


    private GitHubRepositoryManagementBuilder githubBuilder() {
        return new GitHubRepositoryManagementBuilder() {
            @Override
            public RepositoryManagement createNewRepositoryManagement(Options options) {
                return new RepositoryManagement() {
                    @Override
                    public List<Repository> repositories(String orgOrWorkspace) {
                        return null;
                    }

                    @Override
                    public String fileContent(String orgOrWorkspace, String repo, String branch, String filePath) {
                        return null;
                    }
                };
            }
        };
    }
}
