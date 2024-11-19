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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Helper class for language-related actions.
 * User: ktlili
 * Date: Jan 18, 2010
 * Time: 2:16:33 PM
 */
public class LanguageHelper {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LanguageHelper.class);

    /**
     * Get available languages for the current site.
     *
     * When the site object is a jnt:module (mainly in studio mode) or if the passed site has no languages configured,
     * then we retrieve the languages of the system site.
     *
     * If the currentLocale parameter is empty or does not match any of the language of the site, none of the languages
     * will be flagged as current.
     *
     * @param site
     * @param currentLocale
     * @return
     */
    public List<GWTJahiaLanguage> getLanguages(@NotNull JCRSiteNode site, Locale currentLocale) {
        List<GWTJahiaLanguage> items = new ArrayList<GWTJahiaLanguage>();
        try {
            JCRSiteNode siteToCheck = site;
            if (site.isNodeType("jnt:module") || CollectionUtils.isEmpty(site.getLanguages())) {
                siteToCheck = (JCRSiteNode) ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY);
            }
            final Set<String> mandatoryLanguages = siteToCheck.getMandatoryLanguages();
            final Set<String> activeLanguages = siteToCheck.getActiveLiveLanguages();
            final TreeSet<String> orderedLanguages = new TreeSet<String>(siteToCheck.getLanguages());
            for (String langCode : orderedLanguages) {
                GWTJahiaLanguage item = new GWTJahiaLanguage();
                item.setLanguage(langCode);
                item.setDisplayName(getDisplayName(langCode));
                item.setImage(getLangIcon(Jahia.getContextPath(), LanguageCodeConverters.languageCodeToLocale(langCode)));
                item.setCurrent(currentLocale != null && langCode.equalsIgnoreCase(currentLocale.toString()));
                item.setActive(activeLanguages.contains(langCode));
                item.setMandatory(mandatoryLanguages.contains(langCode));
                items.add(item);
            }
        } catch (Exception e) {
            logger.error("Error while retrieving languages for site/module: " + site.getPath(), e);
        }
        return items;
    }

    /**
     * Get current lang
     *
     * @return
     * @param locale
     */
    public GWTJahiaLanguage getCurrentLang(Locale locale) {
        String langCode = locale.toString();
        GWTJahiaLanguage item = new GWTJahiaLanguage();
        item.setLanguage(langCode);
        item.setDisplayName(getDisplayName(langCode));
        item.setImage(getLangIcon(Jahia.getContextPath(), locale));
        return item;
    }

    /**
     * Get icon style depending and the selected language
     *
     *
     * @param locale
     * @return
     */
    public static String getLangIcon(String contextPath, Locale locale) {
        if("".equals(locale.getCountry()))
            return contextPath + "/css/images/flags/" + locale.getLanguage().toLowerCase() + "_on.png";
        else
            return contextPath + "/css/images/flags/plain/flag_" + Patterns.SPACE.matcher(locale.getDisplayCountry(Locale.ENGLISH).toLowerCase()).replaceAll("_") + ".png";
    }

    /**
     * Get display name
     *
     * @param langCode
     * @return
     */
    public static String getDisplayName(String langCode) {
        if (langCode == null) {
            return "";
        }
        langCode = Patterns.DASH.matcher(langCode).replaceAll("_");
        Locale currentLocale = LanguageCodeConverters.getLocaleFromCode(langCode);
        return StringUtils.capitalize(currentLocale.getDisplayName(currentLocale));

    }
}
