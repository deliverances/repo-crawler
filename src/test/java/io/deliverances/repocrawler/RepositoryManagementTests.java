package io.deliverances.repocrawler;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

class RepositoryManagementTests {

    @Test
    void testDefaultMethods() {
        RepositoryManagement management = Mockito.spy(RepositoryManagement.class);
        assertThat(management.fileContent("", "", "", "")).isEmpty();
        assertThat(management.repositories("")).isEmpty();
        assertThat(management.sshKey("", new Project("test"))).isEmpty();
        assertThat(management.host()).isEmpty();
        assertThat(management.cloneUrl("",  new Project("test"))).isEmpty();
    }

}
