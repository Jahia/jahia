package org.jahia.services.search.lucene;

import java.io.Serializable;

public class IndexUpdatedMessage implements Serializable {
    private static final long serialVersionUID = -8293759033981575917L;
    private String searchHandlerName;
    private String serverId;

    public IndexUpdatedMessage(String serverId, String searchHandlerName) {
        super();
        this.serverId = serverId;
        this.searchHandlerName = searchHandlerName;
    }

    public String getSearchHandlerName() {
        return searchHandlerName;
    }

    public void setSearchHandlerName(String searchHandlerName) {
        this.searchHandlerName = searchHandlerName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

}
