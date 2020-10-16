package io.deliverances.repocrawler;

import java.util.Collections;
import java.util.List;

/**
 * Informs whether the given password management is applicable.
 */
public interface RepositoryManagement {

    default List<Repository> repositories(String orgOrWorkspace) {
        return Collections.emptyList();
    }

    default String fileContent(String orgOrWorkspace, String repo, String branch, String filePath) {
        return "";
    }

    default String sshKey(String orgOrWorkspace, Project project) {
        return "";
    }

    default String host() {
        return "";
    }

    default String cloneUrl(String orgOrWorkspace, Project project) {
        return "";
    }

}
