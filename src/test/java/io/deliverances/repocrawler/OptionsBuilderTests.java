package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class OptionsBuilderTests {

    @Test
    void givenUsernamePasswordProviderShouldReturnSame() {
        Options options = OptionsBuilder.builder()
            .username("tester")
            .password("secret")
            .project("aProject")
            .provider(RepositoryProvider.OTHER)
            .build();

        then(options.getUsername()).isEqualTo("tester");
        then(options.getPassword()).isEqualTo("secret");
        then(options.getRepositoryProvider()).isEqualTo(RepositoryProvider.OTHER);
    }

    @Test
    void givenRepositoryProviderShouldReturnSame() {
        Options options = OptionsBuilder.builder()
            .provider("bitbucket")
            .build();
        then(options.getRepositoryProvider()).isEqualTo(RepositoryProvider.BITBUCKET);
    }

    @Test
    void givenRepositoryProviderAndBranchShouldReturnSame() {
        Options options = OptionsBuilder.builder()
            .provider("aProject", "develop")
            .build();
        then(options.getProjects()).extracting("projectName").contains("aProject");
    }

    @Test
    void givenProjectNameShouldContainProjectNames() {
        Options options = OptionsBuilder.builder()
            .project("foo", "foo", "foo")
            .project("foo", "bar", "bar")
            .build();

        then(options.getProjects()).extracting("projectName")
            .contains("foo", "bar");
    }

}
