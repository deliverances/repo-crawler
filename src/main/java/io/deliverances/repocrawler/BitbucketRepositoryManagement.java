package io.deliverances.repocrawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BitbucketRepositoryManagement implements RepositoryManagement {

    private static final Logger LOG = LoggerFactory.getLogger(BitbucketRepositoryManagement.class);

    private static final String AUTHORIZATION = "Authorization";

    private final OkHttpClient httpClient;
    private final Options options;
    private final ObjectMapper objectMapper = new ObjectMapper();

    BitbucketRepositoryManagement(Options options) {
        this.httpClient = connect(options);
        this.options = options;
    }

    private OkHttpClient connect(Options options) {
        if (StringUtils.isNotBlank(options.getToken())) {
            return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .authenticator((route, response) -> {
                    if (response.request().header(AUTHORIZATION) != null) {
                        return null;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authenticating for response: {}", response);
                        LOG.debug("Challenges: {}", response.challenges());
                    }
                    return response.request().newBuilder()
                        .header("Authorization Bearer", options.getToken())
                        .build();
                }).build();
        } else if (StringUtils.isNotBlank(options.getUsername())) {
            return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .authenticator(((route, response) -> {
                    if (response.request().header(AUTHORIZATION) != null) {
                        return null;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authenticating for response: {}", response);
                        LOG.debug("Challenges: {}", response.challenges());
                    }

                    String credential = Credentials.basic(options.getUsername(), options.getPassword());
                    return response.request().newBuilder()
                        .header(AUTHORIZATION, credential)
                        .build();
                })).build();
        }
        throw new IllegalStateException("Neither token nor username and password passed");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Repository> repositories(String orgOrWorkspace) {
        try {
            List<Repository> repositories = new ArrayList<>();
            Map map = null;
            int page = 1;
            while (map == null || map.containsKey("next")) {
                LOG.info("Grabbing page [{}]", page);
                Response response = callRepositories(orgOrWorkspace, page);
                ResponseBody body = response.body();
                if (response.code() >= 400) {
                    throw new IllegalStateException("Status code [" + response.code() + "] and body [" + body + "]");
                }

                String json = body != null ? body.string() : "";
                map = this.objectMapper.readValue(json, Map.class);
                List<Repository> allProjects = getAllProjects((List<Map>) map.get("values"));
                repositories.addAll(addManuallySetProjects(orgOrWorkspace, allProjects));
                page++;
            }
            return repositories;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    Response callRepositories(String workspace, int page) throws IOException {
        return this.httpClient.newCall(new Request.Builder()
            .url(rootUrl() + "repositories/" + workspace + "?page=" + page)
            .get()
            .build()).execute();
    }

    private String rootUrl() {
        String url = this.options.getRootUrl().endsWith("/")
            ? this.options.getRootUrl() : this.options.getRootUrl() + "/";
        return url + "2.0/";
    }

    private List<Repository> getAllProjects(List<Map> map) {
        return map.stream()
            .map(entry -> new Repository(options.projectName((String) entry.get("name")), url(entry, "ssh"),
                url(entry, "https"), "master"))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private String url(Map project, String name) {
        Map links = (Map) project.get("links");
        List<Map<String, String>> clone = (List<Map<String, String>>) links.get("clone");
        List<String> strings = clone.stream()
            .filter(map -> name.equals(map.get("name")))
            .map(map -> map.get("href"))
            .collect(Collectors.toList());
        return strings.get(0);
    }

    private List<Repository> addManuallySetProjects(String org, List<Repository> repositories) {
        repositories.addAll(this.options.getProjects().stream()
            .map(project -> new Repository(options.projectName(project.getProjectName()),
                sshKey(org, project), cloneUrl(org, project), project.getBranch()))
            .collect(Collectors.toSet()));
        return repositories;
    }

    @Override
    public String sshKey(String org, Project project) {
        return "git@" + host() + ":" + org + "/" + project.getProject() + ".git";
    }

    @Override
    public String host() {
        return URI.create(this.options.getRootUrl()).getHost();
    }

    @Override
    public String cloneUrl(String org, Project project) {
        return "https://" + host() + "/" + org + "/" + project.getProject() + ".git";
    }

    @Override
    public String fileContent(String orgOrWorkspace, String repo, String branch, String filePath) {
        return getDescriptor(orgOrWorkspace, repo, branch, filePath);
    }

    String getDescriptor(String workspace, String repo, String branch, String filePath) {
        try {
            return this.httpClient.newCall(new Request.Builder()
                .url(rootUrl() + "repositories/" + workspace + "/" + repo + "/src/" + branch + "/" + filePath)
                .get()
                .build()).execute().body().string();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
