package io.deliverances.repocrawler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class RepositoryCrawlerTests {

    @BeforeEach
    void setup() {
        TestRepositoryManagementBuilder.EXECUTED = false;
    }

    @AfterEach
    void tearDown() {
        TestRepositoryManagementBuilder.EXECUTED = false;
    }

    @Test
    void shouldCallForRepositories() {
        new RepositoryCrawler(OptionsBuilder.builder()
            .provider(RepositoryProvider.OTHER)
            .rootUrl("https://github.com/")
            .build()).repositories("foo");
        then(TestRepositoryManagementBuilder.EXECUTED).isFalse();
    }

    @Test
    void shouldCallForPath() {
        new RepositoryCrawler(OptionsBuilder.builder()
            .provider(RepositoryProvider.OTHER)
            .rootUrl("https://github.com/")
            .build())
            .fileContent("org", "repo", "branch", "path");

        then(TestRepositoryManagementBuilder.EXECUTED).isFalse();
    }

}
