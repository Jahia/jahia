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
 package org.jahia.resourcebundle;

/**
 * <p>Title: I18N Resource bundle message object</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ResourceMessage {

    private Object[] parameters;
    private String resourceKey;

    public ResourceMessage () {
    }

    public ResourceMessage (String resourceKey) {
        this.resourceKey = resourceKey;
        this.parameters = null;
    }

    public ResourceMessage (String resourceKey, Object[] parameters) {
        this.resourceKey = resourceKey;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[1];
        parameters[0] = parameter;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter1,
                            Object parameter2) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[2];
        parameters[0] = parameter1;
        parameters[1] = parameter2;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter1,
                            Object parameter2, Object parameter3) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[3];
        parameters[0] = parameter1;
        parameters[1] = parameter2;
        parameters[2] = parameter3;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter1,
                            Object parameter2, Object parameter3,
                            Object parameter4) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[4];
        parameters[0] = parameter1;
        parameters[1] = parameter2;
        parameters[2] = parameter3;
        parameters[3] = parameter4;
        this.parameters = parameters;
    }

    public Object[] getParameters () {
        return parameters;
    }

    public void setParameters (Object[] parameters) {
        this.parameters = parameters;
    }

    public String getResourceKey () {
        return resourceKey;
    }

    public void setResourceKey (String resourceKey) {
        this.resourceKey = resourceKey;
    }

}