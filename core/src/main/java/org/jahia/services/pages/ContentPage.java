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
package org.jahia.services.pages;


import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;

import java.util.*;


/**
 * <p>Title: ContentPage - all the content for a page</p>
 * <p>Description: This class contains all the content in multiple languages
 * and multiple active versions for a given page. For a single language version
 * this is represented by JahiaPage.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 *          todo FIXME no support for old version restoring exists in this class as
 *          of yet.
 */
public class ContentPage extends ContentObject  {
    public static final int ACTIVE_PAGE_INFOS = 0x01;
    public static final int STAGING_PAGE_INFOS = 0x02;
    public static final int ARCHIVED_PAGE_INFOS = 0x04;

    private static final long serialVersionUID = 60613213284223604L;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentPage.class);

    private Set<JahiaPageInfo> mPageInfos;
    private int aclID = -1;

    private Byte[] statusMapLock = new Byte[0];

    static {
        JahiaObject.registerType(ContentPageKey.PAGE_TYPE,
                ContentPage.class.getName());
    }

    public static ContentObject getChildInstance(String IDInType) {
        return null;
    }

    public static ContentObject getChildInstance(String IDInType, boolean forceLoadFromDB) {
        return null;
    }


    public static ContentPage getPage(int pageID, boolean withTemplate, boolean forceLoadFromDB)
            throws JahiaException {
        return null;
    }

    public static ContentPage getPage(int pageID, boolean withTemplate)
            throws JahiaException {
        return null;
    }

    public static ContentPage getPage(int pageID) throws JahiaException {
        return null;
    }


    protected ContentPage() {
    }

    protected ContentPage(int pageID,
                          List<JahiaPageInfo> pageInfoList,
                          JahiaBaseACL acl)
            throws JahiaException {
    }

    public boolean checkGuestAccess(int siteID) {
        return false;  
    }

    public boolean checkAccess(JahiaUser user, int permission, boolean checkChilds) {
        return false;  
    }

    public boolean checkAccess(JahiaUser user, int permission, boolean checkChilds, boolean forceChildRights) {
        return false;  
    }

    public void invalidateHtmlCache() throws JahiaException {
        
    }

    public void commitChanges(boolean flushCaches, boolean fireEvent, JahiaUser user) throws JahiaException {
        
    }

    public void commitChanges(boolean flushCaches, ProcessingContext jParams) throws JahiaException {
        
    }

    public JahiaBaseACL getACL() {
        return null;  
    }

    public int getAclID() {
        return 0;  
    }

    public int getAclID(int pageInfosFlag) {
        return 0;  
    }

    public int getJahiaID() {
        return 0;  
    }

    public int getSiteID() {
        return 0;  
    }

    public JahiaPageDefinition getPageTemplate(EntryLoadRequest loadRequest) {
        return null;  
    }

    public JahiaPageDefinition getPageTemplate(ProcessingContext jParams) {
        return null;  
    }

    public int getPageTemplateID(ProcessingContext jParams) {
        return 0;  
    }

    public int getPageTemplateID(EntryLoadRequest loadRequest) {
        return 0;  
    }

    public int getDefinitionID(EntryLoadRequest loadRequest) {
        return 0;  
    }

    public ObjectKey getDefinitionKey(EntryLoadRequest loadRequest) {
        return null;  
    }

    public int getPageLinkID(EntryLoadRequest loadRequest) {
        return 0;  
    }

    public int getPageLinkID(ProcessingContext jParams) {
        return 0;  
    }

    public JahiaPage getPage(EntryLoadRequest loadRequest, String operationMode, JahiaUser user) throws JahiaException {
        return null;  
    }

    public JahiaPage getPage(ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public int getPageType(EntryLoadRequest entryLoadRequest) {
        return 0;  
    }

    public int getParentID(EntryLoadRequest loadRequest) {
        return 0;  
    }

    public int getParentID(ProcessingContext jParams) {
        return 0;  
    }

    public int hasSameParentID() {
        return 0;  
    }

    public String getRemoteURL(EntryLoadRequest loadRequest) {
        return null;  
    }

    public String getRemoteURL(ProcessingContext jParams) {
        return null;  
    }

    public Map<String, String> getRemoteURLs(boolean lastUpdated) {
        return null;  
    }

    public String getTitle(EntryLoadRequest loadRequest) {
        return null;  
    }

    public String getTitle(EntryLoadRequest loadRequest, boolean htmlCompliant) {
        return null;  
    }

    public String getTitle(ProcessingContext jParams) {
        return null;  
    }

    public void incrementCounter(EntryLoadRequest loadRequest) {
        
    }

    public void incrementCounter(ProcessingContext jParams) {
        
    }

    public boolean setPageTemplateID(int value, EntryLoadRequest loadRequest) throws JahiaException, JahiaTemplateNotFoundException {
        return false;  
    }

    public void setPageTemplateID(int value, ProcessingContext jParams) throws JahiaException, JahiaTemplateNotFoundException {
        
    }

    public void setPageTemplate(JahiaPageDefinition value, EntryLoadRequest loadRequest) throws JahiaException {
        
    }

    public void setPageTemplate(JahiaPageDefinition value, ProcessingContext jParams) throws JahiaException {
        
    }

    public void setPageLinkID(int value, EntryLoadRequest loadRequest) {
        
    }

    public void setPageLinkID(int value, ProcessingContext jParams) {
        
    }

    public void setPageType(int value, EntryLoadRequest loadRequest) {
        
    }

    public void setPageType(int value, ProcessingContext jParams) {
        
    }

    public void setParentID(int value, ProcessingContext jParams) throws JahiaException {
        
    }

    public void setParentID(int value, JahiaUser user, EntryLoadRequest loadRequest) throws JahiaException {
        
    }

    public void setAclID(int aclID) {
        
    }

    public void setAclID(int aclID, EntryLoadRequest loadRequest) {
        
    }

    public void setAclID(int aclID, ProcessingContext jParams) {
        
    }

    public void setRemoteURL(String value, EntryLoadRequest loadRequest) {
        
    }

    public void setRemoteURL(String value, ProcessingContext jParams) {
        
    }

    public void setRemoteURL(String languageCode, String title, EntryLoadRequest loadRequest) {
        
    }

    public boolean setTitle(String value, EntryLoadRequest loadRequest) {
        return false;  
    }

    public void setTitle(String value, ProcessingContext jParams) {
        
    }

    public boolean setTitles(Set<String> languagesSet, Map<String, String> titles, EntryLoadRequest loadRequest) {
        return false;  
    }

    public void setTitles(Set<String> languagesSet, Map<String, String> titles, ProcessingContext jParams) {
        
    }

    public void setTitles(Map<String, String> titles, EntryLoadRequest loadRequest) {
        
    }

    public void setTitles(Map<String, String> titles, ProcessingContext jParams) {
        
    }

    public boolean setTitle(String languageCode, String title, EntryLoadRequest loadRequest) {
        return false;  
    }

    public void setTitle(String languageCode, String title, ProcessingContext jParams) {
        
    }

    public Map<String, String> getTitles(boolean lastUpdatedTitles) {
        return null;  
    }

    public JahiaSite getSite() throws JahiaException {
        return null;  
    }

    public String getUrl(ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public String getURL(ProcessingContext jParams, String languageCode) throws JahiaException {
        return null;  
    }

    public String getURL(ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getContentPagePath(EntryLoadRequest loadRequest, String operationMode, JahiaUser user) throws JahiaException {
        return null;  
    }

    public List<ContentPage> getContentPagePathAsList(EntryLoadRequest loadRequest, String operationMode, JahiaUser user) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getContentPagePath(ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getContentPagePath(EntryLoadRequest loadRequest, String operationMode, JahiaUser user, int command) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getContentPagePath(int levels, ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getContentPagePath(int levels, EntryLoadRequest loadRequest, String operationMode, JahiaUser user, int command) throws JahiaException {
        return null;  
    }

    public Iterator<JahiaPage> getPagePath(EntryLoadRequest loadRequest, String operationMode, JahiaUser user) throws JahiaException {
        return null;  
    }

    public Iterator<JahiaPage> getPagePath(ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public Iterator<JahiaPage> getPagePath(int levels, ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public Iterator<JahiaPage> getPagePath(int levels, EntryLoadRequest loadRequest, String operationMode, JahiaUser user) throws JahiaException {
        return null;  
    }

    public List<JahiaPage> getChildPages(ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public List<JahiaPage> getChildPages(JahiaUser user) throws JahiaException {
        return null;  
    }

    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest) throws JahiaException {
        return null;  
    }

    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest, int loadFlag) throws JahiaException {
        return null;  
    }

    public ContentObject getParent(EntryLoadRequest loadRequest) throws JahiaException {
        return null;  
    }

    public ContentObject getParent(JahiaUser user, EntryLoadRequest loadRequest, String operationMode) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getDirectContentPageChilds(JahiaUser user, int pageInfosFlags, String languageCode) throws JahiaException {
        return null;  
    }

    public Iterator<ContentPage> getContentPageChilds(JahiaUser user, int pageInfosFlags, String languageCode, boolean directPageOnly) throws JahiaException {
        return null;  
    }

    public int compare(JahiaPage c1, JahiaPage c2) throws ClassCastException {
        return 0;  
    }

    public void purge(EntryLoadRequest loadRequest) throws JahiaException {
        
    }

    protected JahiaPageInfo getPageInfoVersion(EntryLoadRequest loadRequest, boolean isWrite, boolean ignoreLanguage) {
        return null;
      }

    public PageProperty getPageLocalProperty(String name) throws JahiaException {
        return null;  
    }

    public String getProperty(String name, EntryLoadRequest loadRequest) throws JahiaException {
        return null;  
    }

    public String getProperty(String name, ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public String getProperty(String name, String languageCode, ProcessingContext jParams) throws JahiaException {
        return null;  
    }

    public String getProperty(String name) throws JahiaException {
        return null;  
    }

    public void setProperty(Object name, Object value) throws JahiaException {
        
    }

    public void setProperty(Object name, String languageCode, Object value) throws JahiaException {
        
    }

    public void removeProperty(String name) throws JahiaException {
        
    }

    public int getPageID() {
        return 0;  
    }

    public ActivationTestResults activate(Set<String> languageCodes, boolean versioningActive, JahiaSaveVersion saveVersion, JahiaUser user, ProcessingContext jParams, StateModificationContext stateModifContext) throws JahiaException {
        return null;  
    }

    public ActivationTestResults isValidForActivation(Set<String> languageCodes, ProcessingContext jParams, StateModificationContext stateModifContext) throws JahiaException {
        return null;  
    }

    public void setWorkflowState(Set<String> languageCodes, int newWorkflowState, ProcessingContext jParams, StateModificationContext stateModifContext) throws JahiaException {
        
    }

    public void markLanguageForDeletion(JahiaUser user, String languageCode, StateModificationContext stateModifContext) throws JahiaException {
        
    }

    public boolean willAllChildsBeCompletelyDeleted(JahiaUser user, String markDeletedLanguageCode, Set<String> activationLanguageCodes) throws JahiaException {
        return false;  
    }

    public void undoStaging(ProcessingContext jParams) throws JahiaException {
        
    }

    public void copyEntry(EntryStateable fromEntryState, EntryStateable toEntryState) throws JahiaException {
        
    }

    public void deleteEntry(EntryStateable deleteEntryState) throws JahiaException {
        
    }

    public boolean hasActiveEntries() {
        return false;  
    }

    public boolean hasStagingEntries() {
        return false;  
    }

    public boolean hasEntries(int pageInfosFlag, String languageCode) {
        return false;  
    }

    public boolean hasEntries(int pageInfosFlag) {
        return false;  
    }

    public int getDeleteVersionID() throws JahiaException {
        return 0;  
    }

    public Map<String, Integer> getLanguagesStates(boolean withContent) {
        return null;  
    }

    public Map<String, Integer> getLanguagesStates() {
        return null;  
    }

    public SortedSet<ContentObjectEntryState> getEntryStates() throws JahiaException {
        return null;  
    }

    public SortedSet<ContentObjectEntryState> getActiveAndStagingEntryStates() {
        return null;  
    }

    public RestoreVersionTestResults isValidForRestore(JahiaUser user, String operationMode, ContentObjectEntryState entryState, boolean removeMoreRecentActive, StateModificationContext stateModificationContext) throws JahiaException {
        return null;  
    }

    public RestoreVersionTestResults restoreVersion(JahiaUser user, String operationMode, ContentObjectEntryState entryState, boolean removeMoreRecentActive, boolean restoreContent, RestoreVersionStateModificationContext stateModificationContext) throws JahiaException {
        return null;  
    }

    public RestoreVersionTestResults restoreVersion(JahiaUser user, String operationMode, ContentObjectEntryState entryState, boolean removeMoreRecentActive, RestoreVersionStateModificationContext stateModificationContext) throws JahiaException {
        return null;  
    }

    public boolean isShared() {
        return false;  
    }

    public String getDisplayName(ProcessingContext jParams) {
        return null;  
    }

    public void notifyStateChanged() {
        
    }

    public String getPagePathString(ProcessingContext context, boolean ignoreMetadata) {
        return null;  
    }

    public void updateContentPagePath(ProcessingContext context) throws JahiaException {
        
    }

    public String getURLKey() throws JahiaException {
        return null;  
    }

    public ObjectKey getObjectKey() {
        return null;  
    }
}
