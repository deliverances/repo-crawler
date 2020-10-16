package io.deliverances.repocrawler;

import com.github.tomakehurst.wiremock.WireMockServer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class BitbucketRepositoryManagementTests {

    private final String ROOT_URL = "http://localhost:8090";

    private BitbucketRepositoryManagement management;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        this.wireMockServer = new WireMockServer(8090);
        this.wireMockServer.start();
        setupStub();

        this.management = new BitbucketRepositoryManagement(OptionsBuilder.builder()
            .rootUrl(ROOT_URL)
            .username("random-username")
            .password("my-secrecy")
            .build());
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    void setupStub() {
        // repositories stub
        this.wireMockServer.stubFor(get(urlEqualTo("/2.0/repositories/test?page=1")).atPriority(5)
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBodyFile("bitbucket_projects.json")));

        // raw file contents stub
        this.wireMockServer.stubFor(get(urlEqualTo("/2.0/repositories/test/repo/src/master/path"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody("rawContent")));
    }

    @Test
    void constructorShouldConnectUsingToken() throws Exception {
        assertHttpClientIsNotNull();
    }

    @Test
    void constructorShouldConnectUsingCredential() throws Exception {
        management = new BitbucketRepositoryManagement(OptionsBuilder.builder()
            .rootUrl(ROOT_URL)
            .username("random-username")
            .password("my-secrecy")
            .build());
        assertHttpClientIsNotNull();
    }

    @Test
    void constructorShouldThrowIllegalException() {
        assertThatIllegalStateException().isThrownBy(() -> new BitbucketRepositoryManagement(
            OptionsBuilder.builder()
                .build()))
            .withMessageContaining("Neither token nor username and password passed");
    }

    @Test
    void repositoriesShouldReturnNonEmptyList() {
        List<Repository> repositories = management.repositories("test");
        assertThat(repositories.size()).isEqualTo(2);
    }

    @Test
    void repositoriesShouldThrowException() {
        assertThatIllegalStateException().isThrownBy(() -> management.repositories("check"));
    }

    @Test
    void fileContentShouldReturnNonEmptyString() {
        String content = management.fileContent("test", "repo", "master", "path");
        assertThat(content).isNotNull();
    }

    @Test
    void sshKeyShouldReturnNonEmptyString() {
        String sshKey = management.sshKey("test", mockProject());
        assertThat(sshKey).isEqualTo("git@localhost:test/check.git");
    }

    @Test
    void hostShouldReturnNonEmptyString() {
        assertThat(management.host()).isEqualTo("localhost");
    }

    @Test
    void cloneUrlShouldReturnNonEmptyString() {
        String cloneUrl = management.cloneUrl("test", mockProject());
        assertThat(cloneUrl).isEqualTo("https://localhost/test/check.git");
    }

    private void assertHttpClientIsNotNull() throws Exception {
        // check private variable
        Field f = BitbucketRepositoryManagement.class.getDeclaredField("httpClient");
        f.setAccessible(true);
        OkHttpClient okHttpClient = (OkHttpClient) f.get(management);
        assertThat(okHttpClient).isNotNull();
    }

    private Project mockProject() {
        return new Project("check");
    }

}
