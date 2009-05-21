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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class EngineLanguageHelper implements Serializable {

    private static final long serialVersionUID = -2812778018861341683L;
    
    public static final String ENGINE_LANG_PARAM = "engine_lang";
    public static final String PREV_ENGINE_LANG_PARAM = "prev_engine_lang";

    // the new requested locale ( when the user request another language )
    private Locale currentLocale;

    // the previous locale ( the previously active locale )
    private Locale previousLocale;

    private EntryLoadRequest currentEntryLoadRequest;
    private EntryLoadRequest previousEntryLoadRequest;

    /**
     * Constructor with default locale
     *
     * @param locale Locale
     */
    public EngineLanguageHelper(Locale locale) {

        this.setCurrentLocale(locale);
        this.setPreviousLocale(locale);

        List<Locale> locales = new ArrayList<Locale>();
        locales.add(locale);
        EntryLoadRequest entryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0,
                        locales);
        this.setCurrentEntryLoadRequest(entryLoadRequest);

        List<Locale> prevLocales = new ArrayList<Locale>();
        prevLocales.add(locale);
        EntryLoadRequest prevEntryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0,
                        prevLocales);
        this.setPreviousEntryLoadRequest(prevEntryLoadRequest);
    }

    public EngineLanguageHelper() {
    }

    public Locale getCurrentLocale() {
        return this.currentLocale;
    }

    public void setCurrentLocale(Locale locale) {
        this.currentLocale = locale;
    }

    public Locale getPreviousLocale() {
        return this.previousLocale;
    }

    public void setPreviousLocale(Locale locale) {
        this.previousLocale = locale;
    }

    public String getCurrentLanguageCode() {
        if (this.currentLocale != null) {
            return this.currentLocale.toString();
        }
        return null;
    }

    public String getPreviousLanguageCode() {
        if (this.previousLocale != null) {
            return this.previousLocale.toString();
        }
        return null;
    }

    public EntryLoadRequest getCurrentEntryLoadRequest() {
        return this.currentEntryLoadRequest;
    }

    public void setCurrentEntryLoadRequest(EntryLoadRequest loadRequest) {
        this.currentEntryLoadRequest = loadRequest;
    }

    public EntryLoadRequest getPreviousEntryLoadRequest() {
        return this.previousEntryLoadRequest;
    }

    public void setPreviousEntryLoadRequest(EntryLoadRequest loadRequest) {
        this.previousEntryLoadRequest = loadRequest;
    }

    /**
     * Update internal Locales.
     * Set internal language with request paramater values if any.
     *
     * @param processingContext processingContext
     */
    public void update(ProcessingContext processingContext) {
        if (processingContext == null) {
            return;
        }

        SessionState session = processingContext.getSessionState();

        // Resolve language code
        String languageCode = processingContext.getParameter(ENGINE_LANG_PARAM);
        if (languageCode == null) {
            if (this.currentLocale == null) {
                Locale navLocale = (Locale) session.getAttribute(ProcessingContext.
                        SESSION_LOCALE);
                languageCode = navLocale.toString();
            } else {
                languageCode = this.currentLocale.toString();
            }
        }

        if (languageCode == null) {
            languageCode = org.jahia.settings.SettingsBean.getInstance().getDefaultLanguageCode();
        }
        String prevLanguageCode = processingContext.getParameter(PREV_ENGINE_LANG_PARAM);
        if (prevLanguageCode == null) {
            prevLanguageCode = languageCode;
        }

        this.setCurrentLocale(LanguageCodeConverters.languageCodeToLocale(languageCode));

        this.setPreviousLocale(LanguageCodeConverters.languageCodeToLocale(prevLanguageCode));

        List<Locale> locales = new ArrayList<Locale>();
        locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));
        EntryLoadRequest entryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0,
                        locales);
        this.setCurrentEntryLoadRequest(entryLoadRequest);

        List<Locale> prevLocales = new ArrayList<Locale>();
        prevLocales.add(LanguageCodeConverters.languageCodeToLocale(prevLanguageCode));
        EntryLoadRequest prevEntryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0,
                        prevLocales);
        this.setPreviousEntryLoadRequest(prevEntryLoadRequest);
    }
}
