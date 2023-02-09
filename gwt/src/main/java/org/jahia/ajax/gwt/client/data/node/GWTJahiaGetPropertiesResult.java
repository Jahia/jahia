/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * GWT bean, representing result of the node properties request.
 * User: toto
 * Date: Sep 12, 2008
 * Time: 11:01:51 AM
 */
public class GWTJahiaGetPropertiesResult implements Serializable {
	private GWTJahiaNode node;
    private List<GWTJahiaNodeType> nodeTypes;
    private Map<String, GWTJahiaNodeProperty> properties;
    private List<GWTJahiaLanguage> availabledLanguages;
    private GWTJahiaLanguage currentLocale;

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

	public GWTJahiaNode getNode() {
    	return node;
    }

	public void setNode(GWTJahiaNode node) {
    	this.node = node;
    }

    public List<GWTJahiaLanguage> getAvailabledLanguages() {
        return availabledLanguages;
    }

    public void setAvailabledLanguages(List<GWTJahiaLanguage> availabledLanguages) {
        this.availabledLanguages = availabledLanguages;
    }

    public GWTJahiaLanguage getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(GWTJahiaLanguage currentLocale) {
        this.currentLocale = currentLocale;
    }
}
