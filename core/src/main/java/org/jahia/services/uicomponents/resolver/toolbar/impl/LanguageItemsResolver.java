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

package org.jahia.services.uicomponents.resolver.toolbar.impl;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.uicomponents.bean.toolbar.Item;
import org.jahia.services.uicomponents.bean.toolbar.Selected;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * User: ktlili
 * Date: Dec 7, 2009
 * Time: 9:03:31 PM
 */
public class LanguageItemsResolver extends DefaultItemsResolver {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LanguageItemsResolver.class);

    public List<Item> getItems(JCRSiteNode currentSite, JahiaUser user, Locale selectedLang) {
        List<Item> items = new ArrayList<Item>();

        try {
            final Set<String> languageSettings = currentSite != null ? currentSite.getLanguages() : null;
            if (languageSettings != null && languageSettings.size() > 0) {
                final TreeSet<String> orderedLangs = new TreeSet<String>();
                orderedLangs.addAll(languageSettings);
                for (String lang : orderedLangs) {
                    Item item = createJsRedirectItem(getDisplayName(lang), lang);
                    // add to itemsgroup
                    if (item != null) {
                        String minIconStyle = getLangIconStyle(lang);
                        item.setIcon(minIconStyle);

                        if (selectedLang != null) {
                            if (selectedLang.getLanguage().equals(LanguageCodeConverters.languageCodeToLocale(lang))) {
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
