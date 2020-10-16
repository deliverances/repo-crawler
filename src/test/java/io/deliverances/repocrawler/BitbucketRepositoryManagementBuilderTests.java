package io.deliverances.repocrawler;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.BDDAssertions.then;

class BitbucketRepositoryManagementBuilderTests {

    private BitbucketRepositoryManagementBuilder builder = new BitbucketRepositoryManagementBuilder();

    @Test
    void whenUrlIsEmptyShouldReturnNull() {
        then(builder.build(OptionsBuilder.builder()
            .build())).isNull();
    }

    @Test
    void whenUrlNotContainsBitbucketShouldReturnFalse() {
        then(builder.build(OptionsBuilder.builder()
            .rootUrl("foo")
            .token("foo")
            .build())).isNull();
    }

    @Test
    void whenSetRepositoryProviderAsEnumShouldReturnNotNull() {
        then(builder().build(OptionsBuilder.builder()
            .token("foo")
            .rootUrl("foo")
            .provider(RepositoryProvider.BITBUCKET)
            .build())).isNotNull();
    }

    @Test
    void whenSetRepositoryProviderAsStringShouldReturnNotNull() {
        then(builder.build(OptionsBuilder.builder()
            .username("foo").password("bar")
            .rootUrl("foo")
            .provider("bitbucket")
            .build())).isNotNull();
    }

    @Test
    void whenUrlContainsBitbucketShouldReturnNotNull() {
        then(builder.build(OptionsBuilder.builder()
            .token("foo")
            .rootUrl("https://bitbucket.com")
            .build())).isNotNull();
    }

    private BitbucketRepositoryManagementBuilder builder() {
        return new BitbucketRepositoryManagementBuilder() {
            RepositoryManagement createNewRepoManagement(Options options) {
                return new BitbucketRepositoryManagement(options) {
                    @Override
                    Response callRepositories(String workspace, int page) throws IOException {
                        File file = new File(BitbucketRepositoryManagementBuilderTests.class.getResource("/projects.json").getFile());
                        String body = new String(Files.readAllBytes(file.toPath()));
                        return new Response.Builder()
                            .request(new Request.Builder()
                                .url("http://www.foo.com/")
                                .get()
                                .build())
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message(body)
                            .body(ResponseBody.create(body, MediaType.get("application/json")))
                            .build();
                    }

                    @Override
                    String getDescriptor(String workspace, String repo, String branch,
                                         String filePath) {
                        return "hello";
                    }
                };
            }
        };
    }

}
