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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.translation;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.slf4j.Logger;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import java.util.*;

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
     *
     * @param text a text to translate
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param isHtml is the text html or plain text
     * @param site the site
     * @param uiLocale
     * @return the translated text
     * @throws TranslationException
     */
    public String translate(String text, String srcLanguage, String destLanguage, boolean isHtml, JCRSiteNode site, Locale uiLocale) throws TranslationException {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        TranslationProvider provider = getFirstEnabledProvider(site);
        if (provider == null) {
            return text;
        }
        return provider.translate(text, srcLanguage, destLanguage, isHtml, site, uiLocale);
    }

    /**
     * Calls the translate method from the first enabled {@link TranslationProvider} with a list of texts to translate.
     * If the list contains only one text, the single text method is called.
     * Blank texts are not passed to the {@link TranslationProvider}.
     *
     *
     * @param texts a list of texts to translate
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param isHtml are the texts html or plain texts
     * @param site the site
     * @param uiLocale
     * @return the translated texts
     * @throws TranslationException
     */
    public List<String> translate(List<String> texts, String srcLanguage, String destLanguage, boolean isHtml, JCRSiteNode site, Locale uiLocale) throws TranslationException {
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
                translatedTexts.add(provider.translate(texts.get(0), srcLanguage, destLanguage, isHtml, site, uiLocale));
            } else {
                translatedTexts = provider.translate(texts, srcLanguage, destLanguage, isHtml, site, uiLocale);
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
