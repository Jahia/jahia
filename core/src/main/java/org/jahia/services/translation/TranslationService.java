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

package org.jahia.services.translation;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.slf4j.Logger;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to translate text using an online translation service.
 * Several {@link TranslationProvider}s can be contributed, each one representing an online service.
 */
public class TranslationService {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TranslationService.class);

    private Map<String, TranslationProvider> providers = new HashMap<String, TranslationProvider>();

    /**
     * Calls the translate method from the first enabled {@link TranslationProvider} with a single text to translate.
     * If the text is blank, it's just returned without calling the {@link TranslationProvider}.
     *
     * @param text a text to translate
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param isHtml is the text html or plain text
     * @param site the site
     * @return the translated text
     * @throws TranslationException
     */
    public String translate(String text, String srcLanguage, String destLanguage, boolean isHtml, JCRSiteNode site) throws TranslationException {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        TranslationProvider provider = getFirstEnabledProvider(site);
        if (provider == null) {
            return text;
        }
        return provider.translate(text, srcLanguage, destLanguage, isHtml, site);
    }

    /**
     * Calls the translate method from the first enabled {@link TranslationProvider} with a list of texts to translate.
     * If the list contains only one text, the single text method is called.
     * Blank texts are not passed to the {@link TranslationProvider}.
     *
     * @param texts a list of texts to translate
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param isHtml are the texts html or plain texts
     * @param site the site
     * @return the translated texts
     * @throws TranslationException
     */
    public List<String> translate(List<String> texts, String srcLanguage, String destLanguage, boolean isHtml, JCRSiteNode site) throws TranslationException {
        TranslationProvider provider = getFirstEnabledProvider(site);
        if (provider == null) {
            return texts;
        }
        List<StringWithIndex> blankTexts = new ArrayList<StringWithIndex>();
        for (int i = texts.size() - 1; i >= 0 ; i--) {
            String text = texts.get(i);
            if (StringUtils.isBlank(text)) {
                blankTexts.add(0, new StringWithIndex(texts.remove(i), i));
            }
        }
        List<String> translatedTexts;
        if (!texts.isEmpty()) {
            if (texts.size() == 1) {
                translatedTexts = new ArrayList<String>();
                translatedTexts.add(provider.translate(texts.get(0), srcLanguage, destLanguage, isHtml, site));
            } else {
                translatedTexts = provider.translate(texts, srcLanguage, destLanguage, isHtml, site);
            }
        } else {
            translatedTexts = new ArrayList<String>();
        }
        for (StringWithIndex blankText : blankTexts) {
            translatedTexts.add(blankText.index, blankText.text);
        }
        return translatedTexts;
    }

    private TranslationProvider getFirstEnabledProvider(JCRSiteNode site) {
        for (TranslationProvider p : providers.values()) {
            if (p.isEnabled(site)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Checks if one provider is enabled for a given site.
     *
     * @param site a site
     * @return a boolean
     */
    public boolean isEnabled(JCRSiteNode site) {
        for (TranslationProvider p : providers.values()) {
            if(p.isEnabled(site)) {
                return true;
            }
        }
        return false;
    }

    public void addProvider(TranslationProvider provider) {
        providers.put(provider.getName(), provider);
    }

    private class StringWithIndex {
        String text;
        int index;

        StringWithIndex(String text, int index) {
            this.text = text;
            this.index = index;
        }
    }
}
