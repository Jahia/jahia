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

package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * User: toto
 * Date: Jun 14, 2010
 * Time: 7:17:44 PM
 * 
 */
public class GWTJahiaEditEngineInitBean extends GWTJahiaGetPropertiesResult {
    /** The serialVersionUID. */
    private static final long serialVersionUID = 8831509358274880097L;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTJahiaFieldInitializer> initializersValues;
    private GWTJahiaNodeACL acl;
    private Map<String, Set<String>> referencesWarnings;
    private String defaultLanguageCode;

    public GWTJahiaEditEngineInitBean() {
    }

    public GWTJahiaEditEngineInitBean(List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties) {
        super(nodeTypes, properties);
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public void setMixin(List<GWTJahiaNodeType> mixin) {
        this.mixin = mixin;
    }

    public Map<String, GWTJahiaFieldInitializer> getInitializersValues() {
        return initializersValues;
    }

    public void setInitializersValues(Map<String, GWTJahiaFieldInitializer> initializersValues) {
        this.initializersValues = initializersValues;
    }

    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    public void setAcl(GWTJahiaNodeACL acl) {
        this.acl = acl;
    }

    public Map<String, Set<String>> getReferencesWarnings() {
        return referencesWarnings;
    }

    public void setReferencesWarnings(Map<String, Set<String>> referencesWarnings) {
        this.referencesWarnings = referencesWarnings;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

}
