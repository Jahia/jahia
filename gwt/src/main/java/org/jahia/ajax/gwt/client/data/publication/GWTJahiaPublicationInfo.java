package org.jahia.ajax.gwt.client.data.publication;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BaseTreeModel;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 12:00:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPublicationInfo extends BaseTreeModel implements Serializable {

    public static final int NOT_PUBLISHED = 0;
    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 2;
    public static final int UNPUBLISHABLE = 3;



    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(String path, int status, boolean canPublish) {
        setPath(path);
        setStatus(status);
        setCanPublish(canPublish);
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

    public Integer getSubnodesStatus() {
        return get("subnodesStatus");
    }

    public void setSubnodesStatus(Integer status) {
        set("subnodesStatus", status);
    }

    public Boolean isCanPublish() {
        return get("canPublish");
    }

    public void setCanPublish(Boolean canPublish) {
        set("canPublish", canPublish);
    }

}
