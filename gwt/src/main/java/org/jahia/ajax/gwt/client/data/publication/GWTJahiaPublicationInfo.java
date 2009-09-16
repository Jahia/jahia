package org.jahia.ajax.gwt.client.data.publication;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 12:00:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPublicationInfo implements Serializable {

    public static final int UNPUBLISHED = 0;
    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 2;

    public static final int LIVE_ONLY = 4;
    public static final int LIVE_MODIFIED = 5;

    public static final int UNPUBLISHABLE = 3;

    private int status;

    private boolean canPublish;

    private Map<String, GWTJahiaPublicationInfo> references;

    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(int status, boolean canPublish, Map<String, GWTJahiaPublicationInfo> references) {
        this.status = status;
        this.canPublish = canPublish;
        this.references = references;
    }

    public int getStatus() {
        return status;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public Map<String, GWTJahiaPublicationInfo> getReferences() {
        return references;
    }
}
