/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
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
     * Get available languages for the current site
     *
     *
     * @param site
     * @param currentLocale
     * @return
     */
    public List<GWTJahiaLanguage> getLanguages(@NotNull JCRSiteNode site, Locale currentLocale) {
        List<GWTJahiaLanguage> items = new ArrayList<GWTJahiaLanguage>();

        try {
            if (!site.isNodeType("jnt:module") && site.getLanguages() != null && site.getLanguages().size()>0)  {
                final Set<String> languageSettings = site.getLanguages();
                final Set<String> mandatoryLanguages = site.getMandatoryLanguages();
                final Set<String> activeLanguages = site.getActiveLiveLanguages();
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
                        JahiaSitesService.SYSTEM_SITE_KEY);
                final Set<String>languages  = siteByKey.getLanguages();
                final Set<String> activeLanguages = siteByKey.getActiveLiveLanguages();
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
