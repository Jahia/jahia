/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Choice list initializer to provide a list of available languages for a site.
 *
 * @author : rincevent
 * @since JAHIA 8.2
 * Created : 6 dec. 2023
 */
public class SiteLanguagesChoiceListInitializerImpl implements ChoiceListInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SiteLanguagesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (context == null) {
            return new ArrayList<>();
        }
        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        JCRNodeWrapper parentNode = (JCRNodeWrapper) context.get("contextParent");

        JCRSiteNode site = null;

        try {
            if (node != null) {
                site = node.getResolveSite();
            }
            if (site == null && parentNode != null) {
                site = parentNode.getResolveSite();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        if (site == null) {
            return new ArrayList<>();
        }
        List<ChoiceListValue> vs = new ArrayList<>();
        Set<String> activeLiveLanguages = site.getActiveLiveLanguages();
        for (String activeLiveLanguage : activeLiveLanguages) {
            ChoiceListValue value = new ChoiceListValue();
            value.setStringValue(activeLiveLanguage);
            value.setDisplayName(LanguageCodeConverters.languageCodeToLocale(activeLiveLanguage).getDisplayName(locale));
            vs.add(value);
        }
        Collections.sort(vs);
        return vs;
    }

}
