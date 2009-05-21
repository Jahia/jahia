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
package org.jahia.services.cache;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ObjectKey;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.settings.SettingsBean;

import java.util.*;

/**
 * The implementation of the container HTML cache service. 
 * User: Serge Huber
 * Date: 26 mars 2007
 * Time: 12:09:55
 * To change this template use File | Settings | File Templates.
 */
public class ContainerHTMLCache<K, V> extends Cache<GroupCacheKey, ContainerHTMLCacheEntry>  {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContainerHTMLCache.class);

    // the Container HTML cache name.
    public static final String CONTAINER_HTML_CACHE = "ContainerHTMLCache";

    private final CacheKeyGeneratorService cacheKeyGeneratorService;
    
    /**
     * <p>Creates a new <code>ContainerHTMLCache</code> instance.</p>
     *
     */
    protected ContainerHTMLCache(CacheImplementation<GroupCacheKey, CacheEntry<ContainerHTMLCacheEntry>> cacheImplementation) {
        super(CONTAINER_HTML_CACHE, cacheImplementation);
        cacheKeyGeneratorService = ServicesRegistry.getInstance().getCacheKeyGeneratorService();
    }

    /**
     * <p>Invalidates all the cache entries related to the specified container <code>containerID</code>,
     * and mode by removing the cache entries.</p>
     *
     * @param containerID  the container identification number
     * @param mode the mode for which to invalidate the containers.
     * @param languageCode the language code for which to invalidate the containers.
     */

    public synchronized void invalidateContainerEntries(String containerID, String mode, String languageCode) {

        if (containerID == null) {
            logger.debug ("Cannot remove a null container ID from the cache!");
            return;
        }

        logger.debug ("Removing cache entries for container/containerlist ["+ containerID + "], mode ["+mode+"], lang ["+languageCode+"]");

        String pageKey = cacheKeyGeneratorService.getPageKey(containerID,mode,languageCode);

        flushGroup(pageKey);
    }

    public void invalidateContainerEntriesInAllModes(String containerID, String languageCode) {
        invalidateContainerEntries(containerID, ProcessingContext.NORMAL,languageCode);
        invalidateContainerEntries(containerID, ProcessingContext.EDIT,languageCode);
        invalidateContainerEntries(containerID, ProcessingContext.COMPARE,languageCode);
        invalidateContainerEntries(containerID, ProcessingContext.PREVIEW, languageCode);
    }

    /**
     * Add an entry into the HTML container cache as an HTMLCacheEntry, which contains an HTML string and a Map of user-defined properties.
     *
     * @param jahiaContainer the container
     * @param processingContext
     *@param entry the HTML cache entry to cache
     * @param cacheKey the key to access this entry
     * @param dependencies the containers this entry depends on
     * @param expiration @throws org.jahia.exceptions.JahiaInitializationException
     */
    public void writeToContainerCache(JahiaContainer jahiaContainer, ProcessingContext processingContext, ContainerHTMLCacheEntry entry,
                                      String cacheKey, Set<ContentObjectKey> dependencies, long expiration) throws JahiaInitializationException {
        if(expiration==0) return;
        // test that the cache is activated
        if(!org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated()) return;
        if (processingContext.getEntryLoadRequest()!=null && processingContext.getEntryLoadRequest().isVersioned()){
            // don't cache versioned content
            return;
        }
        String mode = processingContext.getOperationMode();
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();
        GroupCacheKey containerKey = cacheKeyGeneratorService.computeContainerEntryKeyWithGroups(jahiaContainer, cacheKey, processingContext.getUser(), curLanguageCode, mode, processingContext.getScheme(), dependencies);
        this.put(containerKey, entry);
        if(expiration >= 0) {
            try {
                getCacheEntry(containerKey).setExpirationDate(new Date(System.currentTimeMillis()+(expiration*1000)));
            } catch (NumberFormatException e) {
                logger.error("The argument expiration of your tag is not a number",e);
            }
        }
        else {
            try {
                long expirI = org.jahia.settings.SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
                getCacheEntry(containerKey).setExpirationDate(new Date(System.currentTimeMillis()+(expirI*1000)));
            } catch (NumberFormatException e) {
                logger.error("The default expiration value for containerCacheDefaultExpirationDelay is not a number, see your jahia.properties file",e);
            }
        }
    }

    /**
     * Add an entry into the HTML container cache as an HTML string.
     *
     * @param jahiaContainer the container
     * @param processingContext
     *@param bodyContent the HTML content to cache
     * @param cacheKey the key to access this entry
     * @param dependencies the containers this entry depends on
     * @param expiration @throws org.jahia.exceptions.JahiaInitializationException
     */
    public void writeToContainerCache(JahiaContainer jahiaContainer, ProcessingContext processingContext, String bodyContent, String cacheKey,
                                      Set<ContentObjectKey> dependencies,
                                      long expiration) throws JahiaInitializationException {
        writeToContainerCache(jahiaContainer, processingContext, new ContainerHTMLCacheEntry(bodyContent),cacheKey, dependencies,
                              expiration);
    }

    /**
     * Retrieve an entry from the HTML container cache.
     *
     * @param jahiaContainer
     * @param processingContext
     *@param cacheKey
     * @param esi
     * @param requestedFragment
     * @param currentURL
     * @param aclGroupFinalKey @return
     * @throws JahiaInitializationException
     */
    public ContainerHTMLCacheEntry getFromContainerCache(JahiaContainer jahiaContainer, ProcessingContext processingContext,
                                                         String cacheKey, boolean esi, int requestedFragment, String currentURL, String aclGroupFinalKey) throws JahiaInitializationException {
        String mode = processingContext.getOperationMode();
        if (processingContext.getEntryLoadRequest() != null && processingContext.getEntryLoadRequest().isVersioned()){
            // we don't cache versioned content
            return null;
        }
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();
        GroupCacheKey containerKey = cacheKeyGeneratorService.computeContainerEntryKey(
                jahiaContainer, cacheKey, processingContext.getUser(),
                curLanguageCode,
                mode,
                processingContext.getScheme());
        return (ContainerHTMLCacheEntry) get(containerKey);
    }

    /**
     * Retrieve an entry from the HTML container cache.
     *
     * @param jahiaContainer
     * @param processingContext
     *@param cacheKey
     * @param esi
     * @param requestedFragment
     * @param currentURL
     * @param aclGroupFinalKey @return
     * @throws JahiaInitializationException
     */
    public CacheEntry<ContainerHTMLCacheEntry> getCacheEntryFromContainerCache(JahiaContainer jahiaContainer, ProcessingContext processingContext,
                                                         String cacheKey, boolean esi, int requestedFragment, String currentURL, String aclGroupFinalKey) throws JahiaInitializationException {
        String mode = processingContext.getOperationMode();
        if (processingContext.getEntryLoadRequest() != null && processingContext.getEntryLoadRequest().isVersioned()){
            // we don't cache versioned content
            return null;
        }
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();
        GroupCacheKey containerKey = cacheKeyGeneratorService.computeContainerEntryKey(
                jahiaContainer, cacheKey, processingContext.getUser(),
                curLanguageCode,
                mode,
                processingContext.getScheme());
        return getCacheEntry(containerKey);
    }

    /**
     * This is to append the Advanced Editing Settings to the container cache key in order to have separate entries
     * for each display option.
     * @param jParams the processing context
     * @param cacheKey the original cache key (can be null or empty if not specified)
     * @return the formatted cache key
     */
    public static String appendAESMode(ProcessingContext jParams, String cacheKey) {
        short mode = 0 ;
        if (ProcessingContext.EDIT.equals(jParams.getOperationMode())) {
            SettingsBean settings = SettingsBean.getInstance();
            
            if (settings.isWflowDisp() != UserPreferencesHelper.isDisplayWorkflowState(jParams.getUser())) {
                mode += 1;
            }
            if (settings.isAclDisp() != UserPreferencesHelper.isDisplayAclDiffState(jParams.getUser())) {
                mode += 2;
            }
            if (settings.isTbpDisp() != UserPreferencesHelper.isDisplayTbpState(jParams.getUser())) {
                mode += 4;
            }
            if (settings.isInlineEditingActivated() != UserPreferencesHelper.isEnableInlineEditing(jParams.getUser())) {
                mode += 8;
            }
        }
        
        if (mode == 0) { // don't append anything if mode corresponds to settings
            return cacheKey != null ? cacheKey : "" ;
        } else {
            return new StringBuilder(cacheKey != null ? cacheKey : "").append("_").append(mode).toString() ;
        }
    }

    public Date getExpirationFromContainerCache(JahiaContainer jahiaContainer, ProcessingContext processingContext, String cacheKey, boolean esi, int requestedFragment, String currentURL, String aclGroupFinalKey) throws JahiaInitializationException {
        if (processingContext.getEntryLoadRequest() != null && processingContext.getEntryLoadRequest().isVersioned()){
            // we don't cache versioned content
            return null;
        }
        String mode = processingContext.getOperationMode();
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();
        GroupCacheKey containerKey = cacheKeyGeneratorService.computeContainerEntryKey(
                jahiaContainer, cacheKey, processingContext.getUser(),
                curLanguageCode,
                mode,
                processingContext.getScheme());
        CacheEntry<ContainerHTMLCacheEntry> cacheEntry = getCacheEntry(containerKey);
        if(cacheEntry!= null) return cacheEntry.getExpirationDate();
        return null;
    }

    public void flushContainersForSite(int siteID) {
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            flushGroup(CacheKeyGeneratorService.SITE_PREFIX+siteID);
        }else {
            flushGroup(Integer.toString((CacheKeyGeneratorService.SITE_PREFIX+siteID).hashCode()));
        }
    }


    public void flushPage(ObjectKey objectKey, List<Locale> languageSettingsAsLocales) {
        for (int i = 0; i < languageSettingsAsLocales.size(); i++) {
            Locale locale = languageSettingsAsLocales.get(i);
            invalidateContainerEntriesInAllModes(objectKey.toString(),locale.toString());
        }
    }
}
