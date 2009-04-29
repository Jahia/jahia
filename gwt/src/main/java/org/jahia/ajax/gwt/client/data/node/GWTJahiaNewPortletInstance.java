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
package org.jahia.ajax.gwt.client.data.node;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * User: ktlili
 * Date: 4 déc. 2008
 * Time: 11:12:59
 */
public class GWTJahiaNewPortletInstance implements Serializable {
    private GWTJahiaPortletDefinition gwtJahiaPortletDefinition;
    private List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
    private GWTJahiaNodeACL roles;
    private GWTJahiaNodeACL modes;
    private String instanceName;

    public GWTJahiaNewPortletInstance() {
    }

    public GWTJahiaPortletDefinition getGwtJahiaPortletDefinition() {
        return gwtJahiaPortletDefinition;
    }

    public void setGwtJahiaPortletDefinition(GWTJahiaPortletDefinition gwtJahiaPortletDefinition) {
        this.gwtJahiaPortletDefinition = gwtJahiaPortletDefinition;
    }

    public List<GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<GWTJahiaNodeProperty> properties) {
        this.properties = properties;
    }

    public GWTJahiaNodeACL getRoles() {
        return roles;
    }

    public void setRoles(GWTJahiaNodeACL roles) {
        this.roles = roles;
    }

    public GWTJahiaNodeACL getModes() {
        return modes;
    }

    public void setModes(GWTJahiaNodeACL modes) {
        this.modes = modes;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
