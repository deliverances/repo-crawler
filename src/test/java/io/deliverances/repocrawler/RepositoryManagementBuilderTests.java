package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class RepositoryManagementBuilderTests {

    @Test
    void shouldDoNothingByDefault() {
        then(new RepositoryManagementBuilder() {
        }.build(OptionsBuilder.builder().build())).isNull();
    }

}
