package org.jahia.ajax.gwt.client.data.publication;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BaseTreeModel;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 12:00:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPublicationInfo extends BaseModel implements Serializable {

    public static final int PUBLISHED = 1;
    public static final int LOCKED = 2;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;

    private Set<Integer> subnodesStatus = new HashSet<Integer>();

    private List<GWTJahiaPublicationInfo> subnodes = new ArrayList<GWTJahiaPublicationInfo>();

    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(String path, int status, boolean canPublish) {
        setPath(path);
        setStatus(status);
        setCanPublish(canPublish);
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String path) {
        set("title", path);
    }

    public String getNodetype() {
        return get("nodetype");
    }

    public void setNodetype(String nodetype) {
        set("nodetype", nodetype);
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String path) {
        set("path", path);
    }

    public Integer getStatus() {
        return get("status");
    }

    public void setStatus(Integer status) {
        set("status", status);
    }

    public Set<Integer> getSubnodesStatus() {
        return subnodesStatus;
    }

    public void setSubnodesStatus(Set<Integer> subnodesStatus) {
        this.subnodesStatus = subnodesStatus;
    }

    public Boolean isCanPublish() {
        return get("canPublish");
    }

    public void setCanPublish(Boolean canPublish) {
        set("canPublish", canPublish);
    }

    public void addSubnode(GWTJahiaPublicationInfo subnode) {
        subnodes.add(subnode);
    }

    public List<GWTJahiaPublicationInfo> getSubnodes() {
        return subnodes;
    }


}
