/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
