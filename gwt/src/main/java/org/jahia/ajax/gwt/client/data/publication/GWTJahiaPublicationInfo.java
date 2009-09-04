package org.jahia.ajax.gwt.client.data.publication;

import java.io.Serializable;

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

    public static final int UNPUBLISHABLE = 3;


    private int status;
    private boolean isVersioned;
    private String liveVersion;
    private String stagingVersion;

    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "GWTJahiaPublicationInfo{" +
                "status=" + status +
                ", isVersioned=" + isVersioned +
                ", liveVersion='" + liveVersion + '\'' +
                ", stagingVersion='" + stagingVersion + '\'' +
                '}';
    }
}
