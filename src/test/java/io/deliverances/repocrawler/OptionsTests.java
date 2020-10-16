package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class OptionsTests {

    @Test
    void whenProjectIgnoredShouldReturnTrue() {
        Options build = OptionsBuilder.builder()
            .exclude("^.*github\\.io$").build();

        then(build.isIgnored("foo.github.io")).isTrue();
        then(build.isIgnored("foo")).isFalse();
    }

    @Test
    void whenRenameProjectNameThenShouldOverride() {
        Options build = OptionsBuilder.builder()
            .projectName("foo", "bar")
            .build();

        then(build.projectName("foo")).isEqualTo("bar");
    }
}
