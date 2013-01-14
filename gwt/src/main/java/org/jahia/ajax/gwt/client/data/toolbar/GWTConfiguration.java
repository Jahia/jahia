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

package org.jahia.ajax.gwt.client.data.toolbar;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: toto
 * Date: Apr 20, 2010
 * Time: 6:27:19 PM
 */
public class GWTConfiguration implements IsSerializable, Serializable {
    protected String name;
    private Map<String, GWTEngineConfiguration> engineConfigurations;
    private List<String> permissions;
    private GWTJahiaNode siteNode;
    private String sitesLocation;
    private Map<String, GWTJahiaNode> sitesMap;
    private List<GWTJahiaChannel> channels;
    private List<String> componentsPaths;
    private Set<String> editableTypes;
    private Set<String> nonEditableTypes;
    private Set<String> visibleTypes;
    private Set<String> nonVisibleTypes;
    private Set<String> skipMainModuleTypesDomParsing;

    public GWTConfiguration() {
    	super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GWTEngineConfiguration getDefaultEngineConfiguration() {
        if (engineConfigurations == null) {
            engineConfigurations = new HashMap<String, GWTEngineConfiguration>();
        }
        if (!engineConfigurations.containsKey("nt:base")) {
            engineConfigurations.put("nt:base", new GWTEngineConfiguration());
        }
        return engineConfigurations.get("nt:base");
    }

    public GWTEngineConfiguration getEngineConfiguration(GWTJahiaNode node) {
        for (String t : node.getNodeTypes()) {
            if (engineConfigurations.containsKey(t)) {
                return engineConfigurations.get(t);
            }
        }
        for (String t : node.getInheritedNodeTypes()) {
            if (engineConfigurations.containsKey(t)) {
                return engineConfigurations.get(t);
            }
        }
        return getDefaultEngineConfiguration();
    }

    public GWTEngineConfiguration getEngineConfiguration(GWTJahiaNodeType type) {
        if (engineConfigurations.containsKey(type.getName())) {
            return engineConfigurations.get(type.getName());
        }
        for (String t : type.getSuperTypes()) {
            if (engineConfigurations.containsKey(t)) {
                return engineConfigurations.get(t);
            }
        }
        return getDefaultEngineConfiguration();
    }

    public Map<String, GWTEngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }

    public void setEngineConfigurations(Map<String, GWTEngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public GWTJahiaNode getSiteNode() {
        return siteNode;
    }

    public void setSiteNode(GWTJahiaNode siteNode) {
        this.siteNode = siteNode;
    }

    public String getSitesLocation() {
        return sitesLocation;
    }

    public void setSitesLocation(String sitesLocation) {
        this.sitesLocation = sitesLocation;
    }

    public Map<String, GWTJahiaNode> getSitesMap() {
        return sitesMap;
    }

    public void setSitesMap(Map<String, GWTJahiaNode> sitesMap) {
        this.sitesMap = sitesMap;
    }

    public List<GWTJahiaChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<GWTJahiaChannel> channels) {
        this.channels = channels;
    }

    public List<String> getComponentsPaths() {
        return componentsPaths;
    }

    public void setComponentsPaths(List<String> componentsPaths) {
        this.componentsPaths = componentsPaths;
    }

    public Set<String> getEditableTypes() {
        return editableTypes;
    }

    public void setEditableTypes(Set<String> editableTypes) {
        this.editableTypes = editableTypes;
    }

    public Set<String> getNonEditableTypes() {
        return nonEditableTypes;
    }

    public void setNonEditableTypes(Set<String> nonEditableTypes) {
        this.nonEditableTypes = nonEditableTypes;
    }

    public Set<String> getSkipMainModuleTypesDomParsing() {
        return skipMainModuleTypesDomParsing;
    }

    public void setSkipMainModuleTypesDomParsing(Set<String> skipMainModuleTypesDomParsing) {
        this.skipMainModuleTypesDomParsing = skipMainModuleTypesDomParsing;
    }

    public Set<String> getVisibleTypes() {
        return visibleTypes;
    }

    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
    }

    public Set<String> getNonVisibleTypes() {
        return nonVisibleTypes;
    }

    public void setNonVisibleTypes(Set<String> nonVisibleTypes) {
        this.nonVisibleTypes = nonVisibleTypes;
    }
}
