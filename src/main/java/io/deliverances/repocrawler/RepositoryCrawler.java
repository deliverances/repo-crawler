package io.deliverances.repocrawler;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry class that wraps around all available implementations of repository managers.
 */
public final class RepositoryCrawler implements RepositoryManagement {

    private static final List<RepositoryManagementBuilder> DEFAULT_BUILDERS = Arrays.asList(
        new GitLabRepositoryManagementBuilder(),
        new GitHubRepositoryManagementBuilder()
    );

    private final Options options;

    private final ServiceLoader<RepositoryManagementBuilder> loadedService = ServiceLoader.load(RepositoryManagementBuilder.class);

    public RepositoryCrawler(Options options) {
        this.options = options;
    }

    @Override
    public List<Repository> repositories(String orgOrWorkspace) {
        return firstMatching().repositories(orgOrWorkspace);
    }

    private RepositoryManagement firstMatching() {
        RepositoryManagement management = firstMatching(loadedService);
        if (management != null) {
            return management;
        }
        management = firstMatching(DEFAULT_BUILDERS);
        if (management == null) {
            throw new IllegalStateException("Nothing is matching the root url [" + this.options.getRootUrl() + "]");
        }
        return management;
    }

    private RepositoryManagement firstMatching(Iterable<RepositoryManagementBuilder> builders) {
        for (RepositoryManagementBuilder builder : builders) {
            RepositoryManagement management = builder.build(this.options);
            if (management != null) {
                return management;
            }
        }
        return null;
    }

    @Override
    public String fileContent(String orgOrWorkspace, String repo, String branch, String filePath) {
        return firstMatching().fileContent(orgOrWorkspace, repo, branch, filePath);
    }

}
