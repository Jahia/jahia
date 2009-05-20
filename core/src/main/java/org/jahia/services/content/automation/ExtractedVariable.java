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
package org.jahia.services.content.automation;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 8 janv. 2008
 * Time: 15:57:55
 * To change this template use File | Settings | File Templates.
 */
public class ExtractedVariable {
    private NodeWrapper node;
    private String name;
    private Object value;

    public ExtractedVariable(NodeWrapper node, String name, Object value) {
        this.node = node;
        this.name = name;
        this.value = value;
    }

    public NodeWrapper getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
