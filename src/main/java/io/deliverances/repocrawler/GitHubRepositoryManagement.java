package io.deliverances.repocrawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Response;
import com.jcabi.http.wire.RetryWire;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GitHubRepositoryManagement implements RepositoryManagement {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubRepositoryManagement.class);

    private final Github github;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Options options;

    GitHubRepositoryManagement(Options options) {
        this.github = new RtGithub(github(options)
            .entry().through(RetryWire.class));
        this.options = options;
    }

    GitHubRepositoryManagement(Github github, Options options) {
        this.github = github;
        this.options = options;
    }

    private Github github(Options options) {
        if (StringUtils.isNotBlank(options.getToken())) {
            LOG.info("Token passed to GitHub client");
            return new RtGithub(options.getToken());
        }
        if (StringUtils.isNotBlank(options.getUsername())) {
            LOG.info("Username and password passed to GitHub client");
            return new RtGithub(options.getUsername(), options.getPassword());
        }
        LOG.info("No security passed to GitHub client");
        return new RtGithub();
    }

    @Override
    public List<Repository> repositories(String orgOrWorkspace) {
        try {
            List<String> responses = orgRepo(orgOrWorkspace);
            List<Map> map = responses.stream()
                .map(this::read)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            List<Repository> repositories = getAllProjects(map);
            return addManuallySetProjects(orgOrWorkspace, repositories);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    List<String> orgRepo(String org) throws IOException {
        List<String> repositories = new ArrayList<>();
        Response response = null;
        int page = 1;
        while (response == null || hasNextLink(response)) {
            LOG.info("Grabbing page [{}]", page);
            response = fetchOrgRepository(org, page);
            if (response.status() == 404) {
                LOG.warn("Got 404, will assume that org is actually a user");
                response = fetchUserRepo(org, page);
                if (response.status() == 404) {
                    throw new IllegalStateException("Status [" + response.status() + "] was returned for org and user");
                }
            }
            repositories.add(response.body());
            page++;
        }
        return repositories;
    }

    private boolean hasNextLink(Response response) {
        List<String> links = response.headers().get("link");
        if (links == null || links.isEmpty()) {
            return false;
        }
        return links.get(0).contains("rel=\"next\"");
    }

    private Response fetchOrgRepository(String org, int page) throws IOException {
        return this.github.entry().method("GET").uri()
            .path("orgs/" + org + "/repos").queryParam("page", page).back().fetch();
    }

    private Response fetchUserRepo(String user, int page) throws IOException {
        return this.github.entry().method("GET").uri()
            .path("users/" + user + "/repos").queryParam("page", page).back().fetch();
    }

    @SuppressWarnings("unchecked")
    private List<Map> read(String response) {
        try {
            return this.objectMapper.readValue(response, List.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Repository> getAllProjects(List<Map> map) {
        return map.stream()
            .map(entry -> new Repository(
                options.projectName(entry.get("name").toString()),
                entry.get("ssh_url").toString(),
                entry.get("clone_url").toString(),
                "master"))
            .filter(repository -> !options.isIgnored(repository.getName()))
            .collect(Collectors.toList());
    }

    private List<Repository> addManuallySetProjects(String org, List<Repository> repositories) {
        repositories.addAll(this.options.getProjects()
            .stream()
            .map(project -> new Repository(options.projectName(project.getProjectName()),
                sshKey(org, project), cloneUrl(org, project), project.getBranch()))
            .collect(Collectors.toSet()));
        return repositories;
    }

    @Override
    public String sshKey(String org, Project project) {
        return "git@github.com:" + org + "/" + project.getProject() + ".git";
    }

    @Override
    public String host() {
        return null;
    }

    @Override
    public String cloneUrl(String org, Project project) {
        return "https://github.com/" + org + "/" + project.getProject() + ".git";
    }

    @Override
    public String fileContent(String orgOrWorkspace, String repo, String branch, String filePath) {
        try {
            String content = new Scanner(getFileContent(orgOrWorkspace, repo, branch, filePath))
                .useDelimiter("\\A").next();
            if (LOG.isDebugEnabled()) {
                LOG.debug("File [{}] for branch [{}] org [{}] and repo [{}] exists",
                    filePath, branch, orgOrWorkspace, repo);
            }
            return content;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (AssertionError e) {
            LOG.warn("Exception [{}] occurred when retrieving file [{}] for branch [{}] org [{}] and repository [{}]",
                e, filePath, branch, orgOrWorkspace, repo);
            return "";
        }
    }

    InputStream getFileContent(String org, String repository, String branch, String filePath) throws IOException {
        return this.github.repos().get(new Coordinates.Simple(org, repository))
            .contents().get(filePath, branch).raw();
    }

    boolean descriptorExists(String org, String repository, String branch, String filePath) throws IOException {
        return this.github.repos().get(new Coordinates.Simple(org, repository))
            .contents()
            .exists(filePath, branch);
    }
}
