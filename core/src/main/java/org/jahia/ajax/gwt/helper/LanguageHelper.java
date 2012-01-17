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

package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;

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
     * Get available languages for the current site
     *
     * @param site
     * @param user
     * @param currentLocale
     * @return
     */
    public List<GWTJahiaLanguage> getLanguages(JCRSiteNode site, JahiaUser user, Locale currentLocale) {
        List<GWTJahiaLanguage> items = new ArrayList<GWTJahiaLanguage>();

        try {
            if (site != null && site.getLanguages() != null && site.getLanguages().size()>0)  {
                final Set<String> languageSettings = site.getLanguages();
                final Set<String> mandatoryLanguages = site.getMandatoryLanguages();
                final Set<String> activeLanguages = site.getActiveLanguages();
                if (languageSettings != null && languageSettings.size() > 0) {
                    final TreeSet<String> orderedLangs = new TreeSet<String>();
                    orderedLangs.addAll(languageSettings);
                    for (String langCode : orderedLangs) {
                        GWTJahiaLanguage item = new GWTJahiaLanguage();
                        item.setLanguage(langCode);
                        item.setDisplayName(getDisplayName(langCode));
                        item.setImage(getLangIcon(Jahia.getContextPath(), LanguageCodeConverters.languageCodeToLocale(langCode)));
                        item.setCurrent(langCode.equalsIgnoreCase(currentLocale.toString()));
                        item.setActive(activeLanguages.contains(langCode));
                        item.setMandatory(mandatoryLanguages.contains(langCode));
                        items.add(item);
                    }
                }
            } else {
                JCRSiteNode siteByKey = (JCRSiteNode) ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(
                        JahiaSitesBaseService.SYSTEM_SITE_KEY).getNode();
                final Set<String>languages  = siteByKey.getLanguages();
                final Set<String> activeLanguages = siteByKey.getActiveLanguages();
                final Set<String> mandatoryLanguages = site.getMandatoryLanguages();
                final TreeSet<String> orderedLangs = new TreeSet<String>();
                orderedLangs.addAll(languages);
                for (String langCode : orderedLangs) {
                    GWTJahiaLanguage item = new GWTJahiaLanguage();
                    item.setLanguage(langCode);
                    item.setDisplayName(getDisplayName(langCode));
                    item.setImage(getLangIcon(Jahia.getContextPath(), LanguageCodeConverters.languageCodeToLocale(langCode)));
                    item.setCurrent(langCode.equalsIgnoreCase(currentLocale.toString()));
                    item.setActive(activeLanguages.contains(langCode));
                    item.setMandatory(mandatoryLanguages.contains(langCode));
                    items.add(item);
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
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
            return contextPath + "/css/images/flags/plain/flag_" + locale.getDisplayCountry(Locale.ENGLISH).toLowerCase().replaceAll(" ","_") + ".png";
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
        langCode = langCode.replace("-", "_");
        Locale currentLocale = LanguageCodeConverters.getLocaleFromCode(langCode);
        return StringUtils.capitalize(currentLocale.getDisplayName(currentLocale));

    }
}
