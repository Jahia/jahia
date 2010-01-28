package org.jahia.services.content;

import java.util.Map;
import java.util.HashMap;

/**
 * Object to return publication status info for a node and if requested 
 * also contains the infos for the referenced nodes or the subnodes. 
 *
 * @author toto
 */
public class PublicationInfo {
    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 2;
    public static final int NOT_PUBLISHED = 3;
    public static final int UNPUBLISHED = 4;
    public static final int UNPUBLISHABLE = 5;
    public static final int LIVE_MODIFIED = 6;
    public static final int CONFLICT = 9;

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
