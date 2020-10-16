package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTests {

    @Test
    void constructorShouldReturnNonEmpty() {
        Repository repository = new Repository("foo-service", "git@chumbucket.com:company/foo-service.git", "https://chumbucket.com/foo-service.git", "master");
        assertThat(repository).isNotNull();
        assertThat(repository.getName()).isEqualTo("foo-service");
        assertThat(repository.getSshUrl()).isEqualTo("git@chumbucket.com:company/foo-service.git");
        assertThat(repository.getCloneUrl()).isEqualTo("https://chumbucket.com/foo-service.git");
        assertThat(repository.getRequestedBranch()).isEqualTo("master");
    }

}
