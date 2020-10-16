package io.deliverances.repocrawler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitLabRepositoryManagementBuilder implements RepositoryManagementBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GitLabRepositoryManagementBuilder.class);

    @Override
    public RepositoryManagement build(Options options) {
        boolean applicable = isApplicable(options.getRootUrl());
        if (applicable) {
            return createNewRepositoryManagement(options);
        }
        if (options.getRepositoryProvider() != RepositoryProvider.GITLAB) {
            return null;
        }
        return createNewRepositoryManagement(options);
    }

    @Override
    public RepositoryManagement createNewRepositoryManagement(Options options) {
        return new GitLabRepositoryManagement(options);
    }

    @Override
    public boolean isApplicable(String url) {
        boolean applicable = StringUtils.isNoneBlank(url) && url.contains("gitlab");
        if (LOG.isDebugEnabled()) {
            LOG.debug("URL [{}] is applicable [{}]", url, applicable);
        }
        return applicable;
    }

}
