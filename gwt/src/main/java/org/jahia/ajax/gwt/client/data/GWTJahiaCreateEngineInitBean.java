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
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Jun 14, 2010
 * Time: 7:17:52 PM
 *
 */
public class GWTJahiaCreateEngineInitBean implements Serializable {
    /** The serialVersionUID. */
    private static final long serialVersionUID = 2401975272536722966L;
    private List<GWTJahiaLanguage> languages;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTChoiceListInitializer> choiceListInitializersValues;
    private Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> defaultValues;
    private GWTJahiaLanguage currentLocale;
    private GWTJahiaNodeACL acl;
    private String defaultName;
    private String defaultLanguageCode;

    public GWTJahiaCreateEngineInitBean() {
    }

    public List<GWTJahiaLanguage> getLanguages() {
        return languages;
    }

    public void setLanguages(List<GWTJahiaLanguage> languages) {
        this.languages = languages;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public void setMixin(List<GWTJahiaNodeType> mixin) {
        this.mixin = mixin;
    }

    public Map<String, GWTChoiceListInitializer> getChoiceListInitializersValues() {
        return choiceListInitializersValues;
    }

    public void setChoiceListInitializersValues(Map<String, GWTChoiceListInitializer> initializers) {
        this.choiceListInitializersValues = initializers;
    }

    public GWTJahiaLanguage getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(GWTJahiaLanguage currentLocale) {
        this.currentLocale = currentLocale;
    }

    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    public void setAcl(GWTJahiaNodeACL acl) {
        this.acl = acl;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> defaultValues) {
        this.defaultValues = defaultValues;
    }
}
