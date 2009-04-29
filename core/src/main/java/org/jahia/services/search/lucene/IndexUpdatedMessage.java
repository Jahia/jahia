/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
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
