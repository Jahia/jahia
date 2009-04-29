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
package org.jahia.ajax.gwt.client.data.definition;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 26, 2008
 * Time: 7:38:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaNodeDefinition extends GWTJahiaItemDefinition implements Serializable {
    private String[] requiredPrimaryTypes;
    private String defaultPrimaryType;
    private boolean allowsSameNameSiblings;
    private String workflow;

    public boolean isNode() {
        return true;
    }

    public String[] getRequiredPrimaryTypes() {
        return requiredPrimaryTypes;
    }

    public void setRequiredPrimaryTypes(String[] requiredPrimaryTypes) {
        this.requiredPrimaryTypes = requiredPrimaryTypes;
    }

    public String getDefaultPrimaryType() {
        return defaultPrimaryType;
    }

    public void setDefaultPrimaryType(String defaultPrimaryType) {
        this.defaultPrimaryType = defaultPrimaryType;
    }

    public boolean isAllowsSameNameSiblings() {
        return allowsSameNameSiblings;
    }

    public void setAllowsSameNameSiblings(boolean allowsSameNameSiblings) {
        this.allowsSameNameSiblings = allowsSameNameSiblings;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

}
