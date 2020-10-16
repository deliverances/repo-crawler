package io.deliverances.repocrawler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitbucketRepositoryManagementBuilder implements RepositoryManagementBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BitbucketRepositoryManagementBuilder.class);

    @Override
    public RepositoryManagement build(Options options) {
        boolean applicable = isApplicable(options.getRootUrl());
        if (applicable) {
            return createNewRepositoryManagement(options);
        }
        if (options.getRepositoryProvider() != RepositoryProvider.BITBUCKET) {
            return null;
        }
        return createNewRepositoryManagement(options);
    }

    @Override
    public RepositoryManagement createNewRepositoryManagement(Options options) {
        return new BitbucketRepositoryManagement(options);
    }

    @Override
    public boolean isApplicable(String url) {
        boolean applicable = StringUtils.isNotBlank(url) && url.contains("bitbucket");
        if (LOG.isDebugEnabled()) {
            LOG.debug("URL [{}] is applicable [{}]", url, applicable);
        }
        return applicable;
    }

}
