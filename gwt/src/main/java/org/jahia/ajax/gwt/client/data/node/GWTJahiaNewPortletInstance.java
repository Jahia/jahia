/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
