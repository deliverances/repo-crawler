package io.deliverances.repocrawler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubRepositoryManagementBuilder implements RepositoryManagementBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubRepositoryManagementBuilder.class);

    @Override
    public RepositoryManagement build(Options options) {
        boolean applicable = isApplicable(options.getRootUrl());
        if (applicable) {
            return createNewRepositoryManagement(options);
        }
        if (options.getRepositoryProvider() != RepositoryProvider.GITHUB) {
            return null;
        }
        return createNewRepositoryManagement(options);
    }

    @Override
    public RepositoryManagement createNewRepositoryManagement(Options options) {
        return new GitHubRepositoryManagement(options);
    }

    @Override
    public boolean isApplicable(String url) {
        boolean applicable = StringUtils.isNotBlank(url) && url.contains("github");
        if (LOG.isDebugEnabled()) {
            LOG.debug("URL [{}] is applicable [{}]", url, applicable);
        }
        return applicable;
    }

}
