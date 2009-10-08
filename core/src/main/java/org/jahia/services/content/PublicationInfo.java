package org.jahia.services.content;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 16, 2009
 * Time: 10:57:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationInfo {
    public static final int NOT_PUBLISHED = 0;
    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 2;
    public static final int UNPUBLISHABLE = 3;



    private int status;

    private boolean canPublish;

    private boolean isVersioned;
    private String liveVersion;
    private String stagingVersion;

    private Map<String, PublicationInfo> references;
    private Map<String, PublicationInfo> subnodes;

    public PublicationInfo() {
        this.references = new HashMap<String, PublicationInfo>();
        this.subnodes = new HashMap<String, PublicationInfo>();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public void setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
    }

    public boolean isVersioned() {
        return isVersioned;
    }

    public void setVersioned(boolean versioned) {
        isVersioned = versioned;
    }

    public String getLiveVersion() {
        return liveVersion;
    }

    public void setLiveVersion(String liveVersion) {
        this.liveVersion = liveVersion;
    }

    public String getStagingVersion() {
        return stagingVersion;
    }

    public void setStagingVersion(String stagingVersion) {
        this.stagingVersion = stagingVersion;
    }

    public Map<String, PublicationInfo> getReferences() {
        return references;
    }

    public void addReference(String path, PublicationInfo info) {
        this.references.put(path, info);
    }

    public Map<String, PublicationInfo> getSubnodes() {
        return subnodes;
    }

    public void addSubnode(String path, PublicationInfo info) {
        this.subnodes.put(path, info);
    }
}
