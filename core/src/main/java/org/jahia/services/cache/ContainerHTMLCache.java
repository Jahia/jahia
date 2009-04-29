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
package org.jahia.services.cache;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.ajax.usersession.userSettings;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ObjectKey;
import org.jahia.settings.SettingsBean;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
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
        String wfVisuParam = (String) jParams.getSessionState().getAttribute(userSettings.WF_VISU_ENABLED) ;
        boolean wfVisu = wfVisuParam != null && wfVisuParam.equals("true") ;
        boolean wfVisuAes = wfVisuParam != null ;

        String aclVisuParam = (String) jParams.getSessionState().getAttribute(userSettings.ACL_VISU_ENABLED) ;
        boolean aclVisu = aclVisuParam != null && aclVisuParam.equals("true") ;
        boolean aclVisuAes = aclVisuParam != null ;

        String tbpVisuParam = (String) jParams.getSessionState().getAttribute(userSettings.TBP_VISU_ENABLED) ;
        boolean tbpVisu = tbpVisuParam != null && tbpVisuParam.equals("true") ;
        boolean tbpVisuAes = tbpVisuParam != null ;

        String chatVisuParam = (String) jParams.getSessionState().getAttribute(userSettings.CHAT_VISU_ENABLED) ;
        boolean chatVisu = chatVisuParam != null && chatVisuParam.equals("true") ;
        boolean chatVisuAes = chatVisuParam != null ;

        String pdispVisuParam = (String) jParams.getSessionState().getAttribute(userSettings.MONITOR_VISU_ENABLED) ;
        boolean pdispVisu = pdispVisuParam != null && pdispVisuParam.equals("true") ;
        boolean pdispVisuAes = pdispVisuParam != null ;

        short settingsMode = 0 ;
        if (org.jahia.settings.SettingsBean.getInstance().isWflowDisp()) {
            settingsMode += 1 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isAclDisp()) {
            settingsMode += 2 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isTbpDisp()) {
            settingsMode += 4 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isChatDisp()) {
            settingsMode += 8 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isPdispDisp()) {
            settingsMode += 16 ;
        }

        if ((wfVisuAes && wfVisu) || (!wfVisuAes && org.jahia.settings.SettingsBean.getInstance().isWflowDisp())) {
            mode += 1 ;
        }
        if ((aclVisuAes && aclVisu) || (!aclVisuAes && org.jahia.settings.SettingsBean.getInstance().isAclDisp())) {
            mode += 2 ;
        }
        if ((tbpVisuAes && tbpVisu) || (!tbpVisuAes && org.jahia.settings.SettingsBean.getInstance().isTbpDisp())) {
            mode += 4 ;
        }
        if ((chatVisuAes && chatVisu) || (!chatVisuAes && org.jahia.settings.SettingsBean.getInstance().isChatDisp())) {
            mode += 8 ;
        }
        if ((pdispVisuAes && pdispVisu) || (!pdispVisuAes && org.jahia.settings.SettingsBean.getInstance().isPdispDisp())) {
            mode += 16 ;
        }

        if (settingsMode == mode) { // don't append anything if mode corresponds to settings
            return new StringBuilder(cacheKey != null ? cacheKey : "").toString() ;
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
