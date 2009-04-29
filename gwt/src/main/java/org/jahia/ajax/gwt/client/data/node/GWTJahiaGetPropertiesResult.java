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

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 12, 2008
 * Time: 11:01:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaGetPropertiesResult implements Serializable {
    private List<GWTJahiaNodeType> nodeTypes;
    private Map<String, GWTJahiaNodeProperty> properties;

    public GWTJahiaGetPropertiesResult() {
    }

    public GWTJahiaGetPropertiesResult(List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties) {
        this.nodeTypes = nodeTypes;
        this.properties = properties;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<GWTJahiaNodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, GWTJahiaNodeProperty> properties) {
        this.properties = properties;
    }
}
