package io.deliverances.repocrawler;

public class Repository {

    public String name, sshUrl, cloneUrl, requestedBranch;

    public Repository(String name, String sshUrl, String cloneUrl, String requestedBranch) {
        this.name = name;
        this.sshUrl = sshUrl;
        this.cloneUrl = cloneUrl;
        this.requestedBranch = requestedBranch;
    }

    public String getName() {
        return name;
    }

    public String getSshUrl() {
        return sshUrl;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public String getRequestedBranch() {
        return requestedBranch;
    }

}
