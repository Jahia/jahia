/**
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
 */
package org.jahia.engines;

import java.util.*;

/**
 * @author Xavier Lawrence
 */
public class LangLinksFactory {
    private String url;
    private List<Locale> locales;
    private String prevLanguageCode;
    private final List<LangLink> links = new ArrayList<LangLink>();
    private String selectedLanguageCode;

    public LangLinksFactory(final String url,
                            final List<Locale> locales,
                            final String prevLanguageCode,
                            final String selectedLanguageCode) {
        this.url = url;
        this.locales = locales;
        this.prevLanguageCode = prevLanguageCode;
        this.selectedLanguageCode = selectedLanguageCode;
        createLinks();
    }

    public Iterator<LangLink> getLinks() {
        return links.iterator();
    }

    private void createLinks() {

        links.clear();
        if (locales == null) {
            return;
        }

        final Iterator<Locale> iterator = locales.iterator();
        while (iterator.hasNext()) {
            final Locale locale = (Locale) iterator.next();
            final String languageCode = locale.toString();
            final LangLink link = new LangLink(url + "&engine_lang=" + languageCode +
                    "&prev_engine_lang=" + prevLanguageCode,
                    locale.getDisplayName(),
                    locale,
                    (selectedLanguageCode.equals(languageCode)));
            links.add(link);
        }
    }
}
