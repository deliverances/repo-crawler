package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTests {

    @Test
    void whenNotEqualsShouldReturnFalse() {
        Project projectA = new Project("ProjectA", "foo-service", "master");

        Project projectB = new Project("ProjectA", "foo-service", "develop");
        assertThat(projectA.getBranch()).isEqualTo("master");
        assertThat(projectA.equals(projectB)).isFalse();
    }

    @Test
    void whenEqualsShouldReturnTrue() {
        Project projectA = new Project("ProjectA");

        Project projectB = new Project("ProjectA");
        assertThat(projectA.equals(projectB)).isTrue();
    }

}
