package io.deliverances.repocrawler;

import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;

class GitHubRepositoryManagementTests {

    private GitHubRepositoryManagement management;

    private MkGithub github;

    private Repo repo;

    private File folder;

    private File repoXml;

    @BeforeEach
    void setUp() throws IOException {
        this.folder = createTempDirectory("foo").toFile();
        this.repoXml = new File(this.folder, "foo.xml");
        this.github = new MkGithub(new MkStorage.InFile(this.repoXml), "test");
        this.repo = createDummyRepo();
        this.management = new GitHubRepositoryManagement(this.github, OptionsBuilder.builder().build());
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(this.folder);
    }

    @Test
    void shouldReturnListOfNamesOfRepoForAnOrg() {
        then(new GitHubRepositoryManagement(this.github, OptionsBuilder.builder()
            .exclude("^.*github\\.io$").build()) {
            @Override
            List<String> orgRepo(String orgOrWorkspace) throws IOException {
                URL resource = GitHubRepositoryManagementTests.class.getResource("/jirehsoft_repos.json");
                return Collections.singletonList(new String(Files.readAllBytes(new File(resource.getFile()).toPath())));
            }
        }.repositories("test")).hasSize(29)
            .extracting("name").doesNotContain("spring-cloud.github.io");
    }

    @Test
    void shouldReturnListOfNamesOfRepoForAnOrgWithManual() {
        then(new GitHubRepositoryManagement(this.github, OptionsBuilder.builder()
            .project("spring-cloud-gdpr")
            .projectName("spring-cloud-kubernetes-connector", "foo")
            .exclude("^.*github\\.io$").build()) {
            @Override
            List<String> orgRepo(String org) throws IOException {
                URL resource = GitHubRepositoryManagementTests.class.getResource("/jirehsoft_repos.json");
                return Collections.singletonList(new String(Files.readAllBytes(new File(resource.getFile()).toPath())));
            }
        }.repositories("test")).hasSize(30)
            .extracting("name", "sshUrl", "cloneUrl")
            .contains(tuple("spring-cloud-gdpr",
                "git@github.com:test/spring-cloud-gdpr.git",
                "https://github.com/test/spring-cloud-gdpr.git"))
            .contains(tuple("foo",
                "git@github.com:spring-cloud/spring-cloud-kubernetes-connector.git",
                "https://github.com/spring-cloud/spring-cloud-kubernetes-connector.git"))
            .doesNotContain((tuple("spring-cloud-kubernetes-connector",
                "git@github.com:spring-cloud/spring-cloud-kubernetes-connector.git",
                "https://github.com/spring-cloud/spring-cloud-kubernetes-connector.git")));
    }

    @Test
    void shouldReturnListOfOnlyManuallyAddedProjects() {
        then(new GitHubRepositoryManagement(this.github,
            OptionsBuilder.builder()
                .project("spring-cloud-gdpr")
                .exclude("^.*$").build()) {
            @Override
            List<String> orgRepo(String org) throws IOException {
                URL resource = GitHubRepositoryManagementTests.class
                    .getResource("/jirehsoft_repos.json");
                return Collections.singletonList(new String(
                    Files.readAllBytes(new File(resource.getFile()).toPath())));
            }
        }.repositories("test")).hasSize(1)
            .extracting("name").contains("spring-cloud-gdpr");
    }

    @Test
    void shouldReturnFileContentWhenFileExists() throws IOException {
        File file = new File(this.folder, "sc-pipelines.yml");
        file.createNewFile();
        Files.write(file.toPath(), "hello: world".getBytes());

        then(new GitHubRepositoryManagement(this.github, OptionsBuilder.builder().build()) {
            @Override
            InputStream getFileContent(String org, String repo, String branch,
                                       String filePath) throws IOException {
                return new FileInputStream(file);
            }

            @Override boolean descriptorExists(String org, String repo, String branch,
                                               String filePath) {
                return true;
            }
        }.fileContent("test",
            "foo-service", "master", "sc-pipelines.yml")).isEqualTo("hello: world");
    }

    @Test
    void sshKeyShouldReturnNonEmptyString() {
        String sshKey = management.sshKey("test", mockProject());
        assertThat(sshKey).isEqualTo("git@github.com:test/check.git");
    }

    @Test
    void hostShouldReturnNull() {
        assertThat(management.host()).isNull();
    }

    @Test
    void cloneUrlShouldReturnNonEmptyString() {
        String cloneUrl = management.cloneUrl("test", mockProject());
        assertThat(cloneUrl).isEqualTo("https://github.com/test/check.git");
    }

    private Repo createDummyRepo() throws IOException {
        return this.github.repos().create(new Repos.RepoCreate("foo-service", false));
    }

    private Project mockProject() {
        return new Project("check");
    }

}
