package org.jahia.services.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Aug 26, 2010
* Time: 5:31:55 PM
* 
*/
public class PublicationInfoNode implements Serializable {
    private String uuid;
    private String path;
    private int status;
    private boolean locked;
    private boolean canPublish;
    private List<PublicationInfoNode> child = new ArrayList<PublicationInfoNode>();
    private List<PublicationInfo> references = new ArrayList<PublicationInfo>();
    private List<String> pruned;

    public PublicationInfoNode() {
    }

    public PublicationInfoNode(String uuid, String path) {
        this.uuid = uuid;
        this.path = path;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPath() {
        return path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public void setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
    }

    public List<PublicationInfoNode> getChildren() {
        return child;
    }

    public List<PublicationInfo> getReferences() {
        return references;
    }

    public List<String> getPruned() {
        return pruned;
    }

    public void addChild(PublicationInfoNode node) {
        child.add(node);
    }

    public PublicationInfoNode addChild(String uuid, String path) {
        final PublicationInfoNode node = new PublicationInfoNode(uuid, path);
        child.add(node);
        return node;
    }

    public void addReference(PublicationInfo ref) {
        references.add(ref);
    }

    public PublicationInfo addReference(String uuid, String path) {
        final PublicationInfo ref = new PublicationInfo(uuid, path);
        references.add(ref);
        return ref;
    }
}
