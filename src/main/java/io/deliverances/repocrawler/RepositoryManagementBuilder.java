package io.deliverances.repocrawler;

/**
 * Builder for {@link RepositoryManagement}.
 */
public interface RepositoryManagementBuilder {

    default RepositoryManagement build(Options options) {
        return null;
    }

    default RepositoryManagement createNewRepositoryManagement(Options options) {
        return null;
    }

    default boolean isApplicable(String url) {
        return false;
    }

}
