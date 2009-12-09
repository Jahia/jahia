package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.bean.Item;
import org.jahia.services.toolbar.bean.Selected;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.comparator.LanguageSettingsComparator;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet; /**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 **/

/**
 * User: ktlili
 * Date: Dec 7, 2009
 * Time: 9:03:31 PM
 */
public class LanguageItemsResolver extends DefaultItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SitesItemsResolver.class);

    public List<Item> getItems(JahiaData jahiaData) {
        List<Item> items = new ArrayList<Item>();

        try {
            final JahiaSite currentSite = jahiaData.getProcessingContext().getSite();
            final List<SiteLanguageSettings> languageSettings = currentSite.getLanguageSettings(true);
            final Locale selectedLang = jahiaData.getProcessingContext().getLocale();
            if (languageSettings != null && languageSettings.size() > 0) {
                final TreeSet<SiteLanguageSettings> orderedLangs = new TreeSet<SiteLanguageSettings>(new LanguageSettingsComparator());
                orderedLangs.addAll(languageSettings);
                for (SiteLanguageSettings lang : orderedLangs) {
                    Item item = createJsRedirectItem(getDisplayName(lang.getCode()), lang.getCode());
                    // add to itemsgroup
                    if (item != null) {
                        String minIconStyle = getLangIconStyle(lang.getCode());
                        String maxIconStyle = getLangIconStyle(lang.getCode());
                        item.setMediumIconStyle(maxIconStyle);
                        item.setMinIconStyle(minIconStyle);

                        if (selectedLang != null) {
                            if (selectedLang.getLanguage().equals(LanguageCodeConverters.languageCodeToLocale(lang.getCode()))) {
                                Selected s = new Selected();
                                s.setValue(true);
                                item.setSelected(s);
                            }
                        }
                        // add to group lis
                        items.add(item);
                    }

                }
            }
        } catch (JahiaException e) {
            logger.error("JahiaException: Error while creating change site link", e);
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
        }
        return items;
    }

    /**
     * Get icon style depending and the selected language
     *
     * @param langCode
     * @return
     */
    public static String getLangIconStyle(String langCode) {
        return "gwt-toolbar-icon-lang-" + langCode;
    }

    /**
     * Get displa name
     * @param langCode
     * @return
     */
    public static String getDisplayName(String langCode){
        if(langCode == null){
            return "";
        }
        langCode = langCode.replace("-","_"); 
        Locale currentLocale = LanguageCodeConverters.getLocaleFromCode(langCode);
        return currentLocale.getDisplayName(currentLocale);

    }
}
