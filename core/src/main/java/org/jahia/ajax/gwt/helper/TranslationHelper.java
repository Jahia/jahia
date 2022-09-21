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
package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeSelectorType;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.translation.TranslationException;
import org.jahia.services.translation.TranslationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class used to delegate calls to the TranslationService
 */
public class TranslationHelper {

    private TranslationService translationService;

    /**
     * Calls {@link TranslationService} to translate values of a list of properties.
     * The number of calls is minimized : maximum two, one for html values, one for plain texts.
     * Properties definitions are used to determine if the values are html or plain texts.
     *
     *
     * @param properties a list of properties
     * @param definitions the corresponding list of property definitions
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param site the site
     * @param uiLocale
     * @return the properties with their values translated
     * @throws TranslationException
     */
    public List<GWTJahiaNodeProperty> translate(List<GWTJahiaNodeProperty> properties, List<GWTJahiaItemDefinition> definitions, String srcLanguage, String destLanguage, JCRSiteNode site, Locale uiLocale) throws TranslationException {
        List<String> plainTextValues = new ArrayList<String>();
        List<String> htmlValues = new ArrayList<String>();
        for (int i = 0; i < properties.size(); i++) {
            GWTJahiaNodeProperty property = properties.get(i);
            List<String> stringValues =  definitions.get(i).getSelector() == GWTJahiaNodeSelectorType.RICHTEXT ? htmlValues : plainTextValues;
            for (GWTJahiaNodePropertyValue value : property.getValues()) {
                stringValues.add(value.getString());
            }
        }
        if (!plainTextValues.isEmpty()) {
            plainTextValues = translationService.translate(plainTextValues, srcLanguage, destLanguage, false , site, uiLocale);
        }
        if (!htmlValues.isEmpty()) {
            htmlValues = translationService.translate(htmlValues, srcLanguage, destLanguage, true , site, uiLocale);
        }
        List<GWTJahiaNodeProperty> translatedProperties = new ArrayList<GWTJahiaNodeProperty>();
        for (int i = 0; i < properties.size(); i++) {
            GWTJahiaNodeProperty property = properties.get(i);
            GWTJahiaNodeProperty translatedProperty = property.cloneObject();
            List<GWTJahiaNodePropertyValue> translatedValues = new ArrayList<GWTJahiaNodePropertyValue>();
            List<String> stringValues =  definitions.get(i).getSelector() == GWTJahiaNodeSelectorType.RICHTEXT ? htmlValues : plainTextValues;
            for (GWTJahiaNodePropertyValue value : property.getValues()) {
                translatedValues.add(new GWTJahiaNodePropertyValue(stringValues.remove(0)));
            }
            translatedProperty.setValues(translatedValues);
            translatedProperties.add(translatedProperty);
        }
        return translatedProperties;
    }

    /**
     * Calls {@link TranslationService} to translate values of a property.
     * Property definition is used to determine if the values are html or plain texts.
     *
     *
     * @param property a property
     * @param definition the corresponding property definition
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param site the site
     * @param uiLocale
     * @return the property with its values translated
     * @throws TranslationException
     */
    public GWTJahiaNodeProperty translate(GWTJahiaNodeProperty property, GWTJahiaItemDefinition definition, String srcLanguage, String destLanguage, JCRSiteNode site, Locale uiLocale) throws TranslationException {
        List<String> stringValues = new ArrayList<String>();
        for (GWTJahiaNodePropertyValue value : property.getValues()) {
            stringValues.add(value.getString());
        }
        stringValues = translationService.translate(stringValues, srcLanguage, destLanguage, definition.getSelector() == GWTJahiaNodeSelectorType.RICHTEXT, site, uiLocale);
        List<GWTJahiaNodePropertyValue> translatedValues = new ArrayList<GWTJahiaNodePropertyValue>();
        for (String stringValue : stringValues) {
            translatedValues.add(new GWTJahiaNodePropertyValue(stringValue));
        }
        GWTJahiaNodeProperty translatedProperty = property.cloneObject();
        translatedProperty.setValues(translatedValues);
        return translatedProperty;
    }

    /**
     * Calls {@link TranslationService} to check if a site is enabled for online translations
     *
     * @param site
     * @return a boolean
     */
    public boolean isTranslationEnabled(JCRSiteNode site) {
        return translationService.isEnabled(site);
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }
}
