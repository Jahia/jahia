/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
