/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
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
    private Map<String, GWTChoiceListInitializer> initializersValues;
    private Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> defaultValues;
    private GWTJahiaNodeACL acl;
    private Map<String, Set<String>> referencesWarnings;
    private String defaultLanguageCode;
    private boolean translationEnabled;
    private boolean hasOrderableChildNodes;

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

    public Map<String, GWTChoiceListInitializer> getInitializersValues() {
        return initializersValues;
    }

    public void setInitializersValues(Map<String, GWTChoiceListInitializer> initializersValues) {
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

    public boolean isTranslationEnabled() {
        return translationEnabled;
    }

    public void setTranslationEnabled(boolean translationEnabled) {
        this.translationEnabled = translationEnabled;
    }

    public Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public boolean hasOrderableChildNodes() {
        return hasOrderableChildNodes;
    }

    public void hasOrderableChildNodes(boolean hasOrderableChildNodes) {
        this.hasOrderableChildNodes = hasOrderableChildNodes;
    }
}
