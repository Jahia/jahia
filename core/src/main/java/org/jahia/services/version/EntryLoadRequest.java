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
//
//  DJ 12/04/02
//

package org.jahia.services.version;

import org.jahia.services.fields.ContentField;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.content.ContentObject;
import org.jahia.settings.SettingsBean;

import java.io.Serializable;
import java.util.*;

/**
 * Class describing which entry of a field/container/containerlist to
 * load
 */
public class EntryLoadRequest implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private int workflowState;  // -1=deleted, 0=versioned, 1 = active, >= 2 = staging
    private int versionID;
    private List<Locale> locales;

    private boolean compareMode = false;

    final static public int ACTIVE_WORKFLOW_STATE = 1;
    final static public int STAGING_WORKFLOW_STATE = 2;
    final static public int WAITING_WORKFLOW_STATE = 3;
    final static public int VERSIONED_WORKFLOW_STATE = 0;
    final static public int DELETED_WORKFLOW_STATE = -1;

    final static private List<Locale> defaultLocaleList;
    
    public static final Locale SHARED_LANG_LOCALE = new Locale(
            ContentObject.SHARED_LANGUAGE, "");    

    static {
        List<Locale> localeList = new ArrayList<Locale>();
        localeList.add(SHARED_LANG_LOCALE);
        localeList.add(Locale.ENGLISH);
        defaultLocaleList = Collections.unmodifiableList(localeList);
    }

    final static public StaticEntryLoadRequest CURRENT = new StaticEntryLoadRequest(ACTIVE_WORKFLOW_STATE, 0, defaultLocaleList);
    final static public StaticEntryLoadRequest STAGED = new StaticEntryLoadRequest(STAGING_WORKFLOW_STATE, 0, defaultLocaleList, SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
    final static public StaticEntryLoadRequest VERSIONED = new StaticEntryLoadRequest(VERSIONED_WORKFLOW_STATE, 0, defaultLocaleList);
    final static public StaticEntryLoadRequest WAITING = new StaticEntryLoadRequest(WAITING_WORKFLOW_STATE, 0, defaultLocaleList);
    final static public StaticEntryLoadRequest DELETED = new StaticEntryLoadRequest(DELETED_WORKFLOW_STATE, 0, defaultLocaleList);

    private boolean withMarkedForDeletion;
    private boolean withDeleted;
    private String cacheToString = null;

    public EntryLoadRequest(int workflowState, int versionID, List<Locale> locales) {
        this.workflowState = workflowState;
        this.versionID = versionID;
        this.locales = locales;
        this.withMarkedForDeletion = false;
        this.withDeleted = true;
        cacheToString = null;
    }

    /**
     * Constructor with added parameter to specify whether we also want to load
     * the content objects that have been marked for deletion when loading
     * staged entries.
     *
     * @param workflowState
     * @param versionID
     * @param locales
     * @param withMarkedForDeletion
     */
    public EntryLoadRequest(int workflowState, int versionID, List<Locale> locales, boolean withMarkedForDeletion) {
        this.workflowState = workflowState;
        this.versionID = versionID;
        if (locales != null) {
            this.locales = new ArrayList<Locale>(locales);
        } else {
            this.locales = null;
        }
        this.withMarkedForDeletion = withMarkedForDeletion;
        this.withDeleted = true;
        cacheToString = null;
    }

    /**
     * Copy constructor for EntryLoadRequest
     *
     * @param sourceRequest the EntryLoadRequest to copy from.
     */
    public EntryLoadRequest(EntryLoadRequest sourceRequest) {
        this(sourceRequest.getWorkflowState(), sourceRequest.getVersionID(),
                sourceRequest.getLocales(), sourceRequest.isWithMarkedForDeletion());
    }

    /**
     * Contructor for any entrystateable.
     *
     * @param entryState
     */
    public EntryLoadRequest(EntryStateable entryState) {
        this.workflowState = entryState.getWorkflowState();
        this.versionID = entryState.getVersionID();
        this.withMarkedForDeletion = false;
        this.withDeleted = true;
        this.locales = new ArrayList<Locale>();
        this.locales.add(LanguageCodeConverters
                .languageCodeToLocale(entryState.getLanguageCode()));
        cacheToString = null;
    }

    /**
     * Contructor for any entrystateable.
     *
     * @param entryState
     * @param withMarkedForDeletion
     */
    public EntryLoadRequest(EntryStateable entryState,
                            boolean withMarkedForDeletion) {
        this.workflowState = entryState.getWorkflowState();
        this.versionID = entryState.getVersionID();
        this.withMarkedForDeletion = withMarkedForDeletion;
        this.withDeleted = true;
        this.locales = new ArrayList<Locale>();
        this.locales.add(LanguageCodeConverters
                .languageCodeToLocale(entryState.getLanguageCode()));
        cacheToString = null;
    }

    public int getWorkflowState() {
        return workflowState;
    }

    public int getVersionID() {
        return versionID;
    }

    public boolean isCurrent() {
        return (workflowState == ACTIVE_WORKFLOW_STATE);
    }

    public boolean isStaging() {
        return (workflowState == STAGING_WORKFLOW_STATE);
    }

    public boolean isWaiting() {
        return (workflowState == WAITING_WORKFLOW_STATE);
    }

    public boolean isVersioned() {
        return (workflowState <= VERSIONED_WORKFLOW_STATE);
    }

    public boolean isDeleted() {
        return (workflowState == DELETED_WORKFLOW_STATE);
    }

    public void setWorkflowState(int ws) {
        this.workflowState = ws;
        cacheToString = null;
    }

    public void setVersionID(int versionID) {
        this.versionID = versionID;
        cacheToString = null;
    }

    public String toString() {
        if (cacheToString == null) {
            StringBuffer result = new StringBuffer();
            result.append("[workflowState=");
            result.append(workflowState);
            result.append(" versionID=");
            result.append(versionID);

            result.append(" languages=");
            int i = 0;
            for (Locale curLocale : getLocales()) {
                if (i > 0) {
                    result.append(",");
                }                
                if (curLocale != null) {
                    result.append(curLocale.toString());
                } else {
                    result.append("null");
                }
                i++;
            }

            result.append("]");
            cacheToString = result.toString();
        }
        return cacheToString;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    /**
     * Return the first available locale
     *
     * @param withoutShared if true, return the first Locale that is different than "shared"
     * @return return the first available locale
     */
    public Locale getFirstLocale(boolean withoutShared) {

        if (locales == null) {
            return null;
        }
        Locale resultLocale = null;
        if (!withoutShared) {
            resultLocale = (Locale) locales.get(0);
            return resultLocale;
        }

        Iterator<Locale> localeIter = locales.iterator();
        while (localeIter.hasNext() && (resultLocale == null)) {
            Locale curLocale = localeIter.next();
            if (!ContentField.SHARED_LANGUAGE.equals(curLocale.toString())) {
                resultLocale = curLocale;
                break;
            }
        }

        return resultLocale;
    }

    /**
     * Set the first locale locales.set(1,locale). 0 = shared
     *
     * @param languageCode
     */
    public void setFirstLocale(String languageCode) {

        if (locales == null || languageCode == null) {
            return;
        }
        Locale locale = (Locale) locales.get(0);
        if (!locale.toString().equals(ContentField.SHARED_LANGUAGE)) {
            locales.set(0, LanguageCodeConverters
                    .languageCodeToLocale(languageCode));
        } else {
            locales.set(1, LanguageCodeConverters
                    .languageCodeToLocale(languageCode));
        }
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
        cacheToString = null;
    }

    public boolean isWithMarkedForDeletion() {
        return withMarkedForDeletion;
    }

    public void setWithMarkedForDeletion(boolean withMarkedForDeletion) {
        this.withMarkedForDeletion = withMarkedForDeletion;
        cacheToString = null;
    }

    /**
     * Flag that can be used to return or not deleted content.
     *
     * @return
     */
    public boolean isWithDeleted() {
        return withDeleted;
    }

    /**
     * Flag that can be used to return or not deleted content.
     *
     * @return
     */
    public void setWithDeleted(boolean withDeleted) {
        this.withDeleted = withDeleted;
        cacheToString = null;
    }

    public Object clone() {
        List<Locale> locales = new ArrayList<Locale>();
        for (Locale loc : this.locales) {
            locales.add((Locale)loc.clone());
        }
        EntryLoadRequest clone = new EntryLoadRequest(this.workflowState, this.versionID, locales);
        clone.setWithDeleted(this.withDeleted);
        clone.setWithMarkedForDeletion(this.withMarkedForDeletion);
        clone.setCompareMode(this.compareMode);
        return clone;
    }

    /**
     * Returns the list of different language codes present in this EntryLoadRequest instance that are not SHARED language
     * @return
     */
    public List<String> getNotSharedLanguageCodes(){
        List<String> languageCodes = new ArrayList<String>();
        for (Locale loc : locales) {
            if (!loc.getLanguage().equals(ContentField.SHARED_LANGUAGE) && !languageCodes.contains(loc.toString())){
                languageCodes.add(loc.toString());
            }
        }
        return languageCodes;
    }

    /**
     * Returns a same load request but in Staging if versionID == 2 or
     * Versioned for any other value
     *
     * @param versionID
     * @return
     */
    public static EntryLoadRequest getEntryLoadRequest(int versionID, String languageCode) {
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));
        return getEntryLoadRequest(versionID, locales);
    }

    /**
     * Returns a same load request but in Staging if versionID == 0 or
     * Versioned for any other value
     *
     * @param versionID
     * @return
     */
    public static EntryLoadRequest getEntryLoadRequest(int versionID, List<Locale> locales) {
        final EntryLoadRequest loadRequest;
        if (versionID == STAGING_WORKFLOW_STATE) {
            loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGED);
            loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
        } else if (versionID == ACTIVE_WORKFLOW_STATE) {
            loadRequest = new EntryLoadRequest(EntryLoadRequest.CURRENT);
            loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
        } else {
            loadRequest = new EntryLoadRequest(EntryLoadRequest.VERSIONED);
            loadRequest.setVersionID(versionID);
        }
        loadRequest.getLocales().clear();
        loadRequest.getLocales().addAll(locales);
        return loadRequest;
    }

    public boolean isCompareMode() {
        return compareMode;
    }

    public void setCompareMode(boolean compareMode) {
        this.compareMode = compareMode;
    }

} // end EntryLoadVersion
