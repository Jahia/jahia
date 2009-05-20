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
package org.jahia.ajax.gwt.client.data.actionmenu.acldiff;

import java.util.Map;
import java.io.Serializable;

/**
 * User: rfelden
 * Date: 21 janv. 2009 - 15:27:19
 */
public class GWTJahiaAclDiffDetails implements Serializable {

    private String url ;
    private Map<String, String> rights ;
    private Map<String, String> inheritedRights ;

    public GWTJahiaAclDiffDetails() {}

    public GWTJahiaAclDiffDetails(String url, Map<String, String> rights, Map<String, String> inheritedRights) {
        this.url = url ;
        this.rights = rights ;
        this.inheritedRights = inheritedRights ;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getRights() {
        return rights ;
    }

    public Map<String, String> getInheritedRights() {
        return inheritedRights;
    }
    
}
