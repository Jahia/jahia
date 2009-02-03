/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.cache.CacheService;
import org.jahia.content.ContentObject;

/**
 * DB implementation of the Versinoning service
 * 
 * @author <a href="mailto:djilli@jahia.com">David Jilli</a>
 */
public class JahiaVersionDBService extends JahiaVersionService {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(JahiaVersionDBService.class);

    private static JahiaVersionDBService mInstance;
    private CacheService cacheService;
    public static final String CACHE_NAME = "EntryStateVersionCache";

    // --------------------------------------------------------------------------
    /**
     * Default constructor.
     * 
     * @exception JahiaException
     *                    Raise a JahiaException when during initialization one of the needed services could not be instanciated.
     */
    protected JahiaVersionDBService() throws JahiaException {
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    // -------------------------------------------------------------------------
    /**
     * Create an new instance of the Version Service if the instance do not exist, or return the existing instance.
     * 
     * @return Return the instance of the Version Service.
     */
    public static synchronized JahiaVersionDBService getInstance() {
        if (mInstance == null) {
            try {
                mInstance = new JahiaVersionDBService();
            } catch (JahiaException ex) {
                logger
                        .debug(
                                "Could not create an instance of the JahiaVersionDBService class",
                                ex);
            }
        }
        return mInstance;
    }

    public void start() {
    }

    public void stop() {
    }

    /**
     * Is staging enabled for this site?
     * 
     * @param siteID,
     *                the site identifier
     */
    public boolean isStagingEnabled(int siteID) {

        try {
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteID);
            if (site == null) {
                logger.debug("Requested site [" + siteID + "] is null...");
                return false;
            }
            return site.isStagingEnabled();
        } catch (JahiaException je) {
            logger.debug("Couldn't get staging status...", je);
            return false;
        }
    }

    /**
     * Is versioning enabled for this site?
     * 
     * @param siteID,
     *                the site identifier
     */
    public boolean isVersioningEnabled(int siteID) {
        try {
            if (siteID == 0) {
                return false;
            }
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteID);
            if (site == null) {
                logger.debug("Requested site [" + siteID + "] is null...");
                return false;
            }
            return site.isVersioningEnabled();

        } catch (JahiaException je) {
            logger.debug("Couldn't get versioning status...", je);
            return false;
        }
    }

    /**
     * @return the current versionID, which is the number of secondes since 1970
     */
    public int getCurrentVersionID() {
        java.util.Date d = new java.util.Date();
        return (int) (d.getTime() / 1000);
    }

    /**
     * @return the SaveVersion for a specified site
     */
    public JahiaSaveVersion getSiteSaveVersion(int siteID) {
        boolean staging = false;
        boolean versioning = false;
        // if (isStagingEnabled (siteID)) staging = true;
        staging = true;
        /** @todo staging is always activated */
        if (isVersioningEnabled(siteID))
            versioning = true;

        return new JahiaSaveVersion(staging, versioning);
    }

    /**
     * Validate all the staged content from a page to which the user has WRITE+ADMIN access to
     */
    public void activateStagedPage(int pageID, JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        ServicesRegistry sr = ServicesRegistry.getInstance();

        JahiaPage thePage = null;
        thePage = sr.getJahiaPageService().lookupPage(pageID, jParams);
        if (thePage != null) {
            int siteID = thePage.getJahiaID();
            JahiaSaveVersion saveVersion = getSiteSaveVersion(siteID);
            ActivationTestResults activationResults = thePage
                    .activeStagingVersion(stateModifContext.getLanguageCodes(),
                            saveVersion, user, jParams, stateModifContext);
            if (logger.isDebugEnabled()) {
                logger.debug("activation results : "
                        + activationResults.toString());
            }
        }
    }

    /**
     * This method should be called if we have a list of "Versionable"s that represent every version that the DB contains, and a
     * ProcessingContext that contains the version we would like to load SHARED language has the highest priority!
     * 
     * @param entryStateables
     *                a List of object implementing the EntryStateable interface
     * @param ignoreLanguage,
     *                if true, resolve entry state without checking specific language
     * @return an element, or null if field doesn't exist in this version!
     */
    public EntryStateable resolveEntry(List<EntryStateable> entryStateables,
            EntryLoadRequest loadRequest, boolean ignoreLanguage) {
        return resolveEntry(entryStateables, loadRequest, ignoreLanguage, false);
    }

    /**
     * This method should be called if we have a list of "Versionable"s that represent every version that the DB contains, and a
     * ProcessingContext that contains the version we would like to load SHARED language has the highest priority!
     * 
     * @param entryStateables
     *                a List of object implementing the EntryStateable interface
     * @param ignoreLanguage,
     *                if true, resolve entry state without checking specific language
     * @param recursiveCall,
     *                only allow one recursive call to avoid infinite loop
     * @return an element, or null if field doesn't exist in this version!
     */
    protected EntryStateable resolveEntry(List<EntryStateable> entryStateables,
            EntryLoadRequest loadRequest, boolean ignoreLanguage,
            boolean recursiveCall) {
        /**
         * @todo Multilanguage resolution implemented, must still do staging and active version checking... Also I'm not sure if we can
         *       access at this point the activeAndStagingVersionInfo table. Do we need to load the values first ?
         */

//        StopWatch stopWatch = new StopWatch("ResolveEntry ");
        EntryStateable result = null;
        // Object[] array = new Object[entryStateables.size()+2];
        if (result == null) {
//            stopWatch.start("getLocales");
            // we could probably optimize the code below by making sure it is called
            // only once per request and by storing the requested version info
            // once it has been resolved.
            List<Locale> clientLocales = loadRequest.getLocales();
            if (clientLocales.isEmpty()) {
                // clientLocales.add(0, new Locale(SHARED_LANGUAGE, ""));
                clientLocales.add(0, EntryLoadRequest.SHARED_LANG_LOCALE);
            }
            Locale sharedLocale = (Locale) clientLocales.get(0);
            if (!ContentObject.SHARED_LANGUAGE.equals(sharedLocale
                    .getLanguage())) {
                clientLocales.add(0, EntryLoadRequest.SHARED_LANG_LOCALE);
            }

            if (!ignoreLanguage && clientLocales.size() == 1) {
                Locale loc = (Locale) clientLocales.get(0);
                if (ContentObject.SHARED_LANGUAGE.equals(loc.getLanguage())) {
                    ignoreLanguage = true;
                }
            }
//            stopWatch.stop();
            Iterator<Locale> clientLocalesIter = clientLocales.iterator();
//            stopWatch.start("load the right version");
            // we want to load the active version
            if (loadRequest.isCurrent()) {
                while (clientLocalesIter.hasNext() && (result == null)) {
                    Locale currentClientLocale = (Locale) clientLocalesIter
                            .next();
                    String langCodeToFind = currentClientLocale.toString();
                    // now we must compare this value with the list of available
                    // languages to match them...
                    result = findActiveLanguageEntry(langCodeToFind,
                            entryStateables, ignoreLanguage, loadRequest);
                    if ((result == null)
                            && (!ignoreLanguage && currentClientLocale
                                    .getCountry().length() > 0)) {
                        // we have looked so far a country specific language, let's
                        // try now for the generic language.
                        langCodeToFind = currentClientLocale.getLanguage();
                        result = findActiveLanguageEntry(langCodeToFind,
                                entryStateables, ignoreLanguage, loadRequest);
                    }
                }
            } else if (loadRequest.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                // we want to load the staged version                
                while (clientLocalesIter.hasNext() && (result == null)) {
                    Locale currentClientLocale = (Locale) clientLocalesIter
                            .next();
                    String langCodeToFind = currentClientLocale.toString();
                    // now we must compare this value with the list of available
                    // languages to match them...
                    result = findActiveOrStagingLanguageEntry(langCodeToFind,
                            entryStateables, ignoreLanguage, loadRequest, false);
                    if ((result == null)
                            && (!ignoreLanguage && currentClientLocale
                                    .getCountry().length() > 0)) {
                        // we have looked so far a country specific language, let's
                        // try now for the generic language.
                        langCodeToFind = currentClientLocale.getLanguage();
                        result = findActiveOrStagingLanguageEntry(
                                langCodeToFind, entryStateables,
                                ignoreLanguage, loadRequest, false);
                    }

                    if (result != null
                            && (result.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE)) {
                        loadRequest = new EntryLoadRequest(loadRequest);
                        boolean withMarkedForDelete = loadRequest
                                .isWithMarkedForDeletion();
                        loadRequest.setWithMarkedForDeletion(true);
                        // check that it's not marked for delete
                        EntryStateable stagedEntry = findActiveOrStagingLanguageEntry(
                                langCodeToFind, entryStateables,
                                ignoreLanguage, loadRequest, true);
                        loadRequest
                                .setWithMarkedForDeletion(withMarkedForDelete);
                        if (stagedEntry != null
                                && stagedEntry.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                                && stagedEntry.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                            result = null;
                        }
                    }
                }
            } else if (loadRequest.isVersioned()) {
                // we want to load an old version                
                int wantedVersionID = loadRequest.getVersionID();
                // the following Map contains languages as keys, and the value is
                // the Versionable that has the closest versionID to the wanted version ID
                // for the specified language
                Map<String, EntryStateable> lastVersion = new HashMap<String, EntryStateable>();
                Map<String, EntryStateable> deletedVersion = new HashMap<String, EntryStateable>();
                for (EntryStateable thisVer : entryStateables) {
                    // remember deleted versions
                    if (loadRequest.isWithDeleted()
                            && thisVer.getWorkflowState() == EntryLoadRequest.VERSIONED_WORKFLOW_STATE
                            && isDeleted(entryStateables, thisVer
                                    .getVersionID())) {
                        EntryStateable deletedVer = (EntryStateable) deletedVersion
                                .get(thisVer.getLanguageCode());
                        if (deletedVer == null
                                || ((deletedVer.getLanguageCode()
                                        .equals(thisVer.getLanguageCode())) && (thisVer
                                        .getVersionID() > deletedVer
                                        .getVersionID()))) {
                            deletedVersion.put(thisVer.getLanguageCode(),
                                    thisVer);
                        }
                    }

                    if ((thisVer.getWorkflowState() <= 1)
                            && ((thisVer.getVersionID() <= wantedVersionID || wantedVersionID == 0))
                            || (thisVer.getWorkflowState() == 1 && loadRequest
                                    .getVersionID() == 0)) {
                        EntryStateable storedVer = (EntryStateable) lastVersion.get(thisVer
                                .getLanguageCode());
                        if ((storedVer == null)
                                || ((storedVer.getLanguageCode().equals(thisVer
                                        .getLanguageCode())) && (thisVer
                                        .getVersionID() > storedVer
                                        .getVersionID()))) {
                            lastVersion.put(thisVer.getLanguageCode(), thisVer);
                        }
                    }
                }

                // should we use deleted version in place of archived version if they don't exist
                if (loadRequest.isWithDeleted()) {
                    for (String lang : deletedVersion.keySet()) {
                        if (lastVersion.get(lang) == null) {
                            lastVersion.put(lang, deletedVersion.get(lang));
                        }
                    }
                }

                // ok now in lastVersion we have the closest versions for each language, we
                // can do the resolving work easily now...
                while (clientLocalesIter.hasNext() && (result == null)) {
                    Locale currentClientLocale = (Locale) clientLocalesIter
                            .next();
                    String langCodeToFind = currentClientLocale.toString();
                    EntryStateable thisVer = (EntryStateable) lastVersion
                            .get(langCodeToFind);
                    // we keep this version if not null & not deleted (status=-1 when deleted)
                    if ((thisVer != null) && (thisVer.getWorkflowState() <= 0)) {
                        result = thisVer;
                        break;
                    }
                    if ((result == null)
                            && (!ignoreLanguage && currentClientLocale
                                    .getCountry().length() > 0)) {
                        langCodeToFind = currentClientLocale.getLanguage();
                        // we have looked so far a country specific language, let's
                        // try now for the generic language.
                        thisVer = (EntryStateable) lastVersion
                                .get(langCodeToFind);
                        // we keep this version if not null & not deleted (status=-1 when deleted)
                        if ((thisVer != null)
                                && (thisVer.getWorkflowState() <= 0)) {
                            result = thisVer;
                            break;
                        }
                    }
                    if (!clientLocalesIter.hasNext() && result == null
                            && ignoreLanguage && !lastVersion.isEmpty()) {
                        // as we ignore the language, we return the first
                        result = (EntryStateable) lastVersion.values()
                                .iterator().next();
                    }
                }
                if (result == null) {
                    // we didn't find any versioned entry, let's use the active
                    // entry instead.
                    clientLocalesIter = clientLocales.listIterator();
                    while (clientLocalesIter.hasNext() && (result == null)) {
                        Locale currentClientLocale = (Locale) clientLocalesIter
                                .next();
                        String langCodeToFind = currentClientLocale.toString();
                        // now we must compare this value with the list of available
                        // languages to match them...
                        result = findActiveLanguageEntry(langCodeToFind,
                                entryStateables, ignoreLanguage, loadRequest);
                        if ((result == null)
                                && (!ignoreLanguage && currentClientLocale
                                        .getCountry().length() > 0)) {
                            // we have looked so far a country specific language, let's
                            // try now for the generic language.
                            langCodeToFind = currentClientLocale.getLanguage();
                            result = findActiveLanguageEntry(langCodeToFind,
                                    entryStateables, ignoreLanguage,
                                    loadRequest);
                        }
                    }

                    // now let's check the version ID of the active entry if it
                    // was found. We should use an active entry that is more
                    // recent that the version we are requesting...
                    if (result != null) {
                        if (result.getVersionID() > loadRequest.getVersionID()) {
                            // found version ID is bigger than the requested
                            // version, let's NOT return it.
                            result = null;
                        }
                    }

                    // IN COMPARE MODE, we returns the staging or live if versioning does not exist
                    if (!recursiveCall && result == null
                            && loadRequest.isCompareMode()) {
                        EntryLoadRequest stagingVersion = (EntryLoadRequest) EntryLoadRequest.STAGED
                                .clone();
                        stagingVersion.setWithDeleted(true);
                        stagingVersion.setWithMarkedForDeletion(true);
                        result = resolveEntry(entryStateables, stagingVersion,
                                ignoreLanguage, true);
                    }
                }

            }
//            stopWatch.stop();
        }
        // System.out.println(stopWatch.prettyPrint());
        return result;
    }

    /**
     * This method should be called if we have a list of "Versionable"s that represent every version that the DB contains, and a
     * ProcessingContext that contains the version we would like to load SHARED language has the highest priority!
     * 
     * @param entryStateables
     *                a List of object implementing the EntryStateable interface
     * @return an element, or null if field doesn't exist in this version!
     */
    public EntryStateable resolveEntry(List<EntryStateable> entryStateables,
            EntryLoadRequest loadRequest) {
        return resolveEntry(entryStateables, loadRequest, false);
    }

    private EntryStateable findActiveLanguageEntry(String langCodeToFind,
            List<EntryStateable> entryStateables, boolean ignoreLanguage,
            EntryLoadRequest loadRequest) {
        EntryStateable result = null;

        int availableLanguagesEnum = entryStateables.size();
        Map<String, EntryStateable> deletedVersion = new HashMap<String, EntryStateable>();

        for (int i = availableLanguagesEnum - 1; i >= 0; i--) {
            EntryStateable thisVer = entryStateables.get(i);
            if ((thisVer.getWorkflowState() == 1)
                    && (ignoreLanguage || thisVer.getLanguageCode().equals(
                            langCodeToFind))) {
                result = thisVer;
                break;
            }
            // remember deleted versions
            if (loadRequest.isWithDeleted() && thisVer.getWorkflowState() == -1
                    && isDeleted(entryStateables, thisVer.getVersionID())) {
                EntryStateable deletedVer = deletedVersion.get(thisVer.getLanguageCode());
                if (deletedVer == null
                        || ((deletedVer.getLanguageCode().equals(thisVer
                                .getLanguageCode())) && (thisVer.getVersionID() > deletedVer
                                .getVersionID()))) {
                    deletedVersion.put(thisVer.getLanguageCode(), thisVer);
                }
            }
        }

        // should we use the deleted version if archive version don't exist
        if (result == null && !deletedVersion.isEmpty()) {
            result = deletedVersion.get(langCodeToFind);
            if (result == null && ignoreLanguage) {
                result = deletedVersion.values().iterator().next();
            }
        }

        return result;
    }

    /**
     * 
     * 
     * @param langCodeToFind
     *                String
     * @param entryStateables
     *                List
     * @param ignoreLanguage
     *                boolean
     * @param loadRequest
     *                EntryLoadRequest
     * @param stagingOnly
     *                boolean
     * @return EntryStateable
     */
    private EntryStateable findActiveOrStagingLanguageEntry(
            String langCodeToFind, List<EntryStateable> entryStateables,
            boolean ignoreLanguage, EntryLoadRequest loadRequest,
            boolean stagingOnly) {

        int availableLanguagesEnum = entryStateables.size();
        EntryStateable activeVer = null;
        EntryStateable stagedVer = null;
        for (int i = availableLanguagesEnum - 1; i >= 0; i--) {
            // @todo : should we need to handle withDeleted !

            EntryStateable thisVer = entryStateables.get(i);
            if (((thisVer.getWorkflowState() >= EntryLoadRequest.ACTIVE_WORKFLOW_STATE) && (ignoreLanguage || thisVer
                    .getLanguageCode().equals(langCodeToFind)))
                    || ((thisVer.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE)
                            && (ignoreLanguage || thisVer.getLanguageCode()
                                    .equals(langCodeToFind))
                            && loadRequest.isWithDeleted() && isDeleted(
                            entryStateables, thisVer.getVersionID()))) {
                if (!stagingOnly
                        && thisVer.getWorkflowState() < EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    if (activeVer == null) {
                        activeVer = thisVer;
                    } else if (activeVer.getVersionID() < thisVer
                            .getVersionID()) {
                        activeVer = thisVer;
                    }
                } else if (thisVer.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    stagedVer = thisVer;
                }
            }
        }

        if (loadRequest.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            // we requested staging or active ( if staging doesn't exist )

            if (stagedVer != null) {
                if (loadRequest.isWithMarkedForDeletion()) {
                    return stagedVer;
                } else if (stagedVer.getVersionID() != EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                    return stagedVer;
                }
            }
        }

        if (activeVer != null) {
            // we request active
            if (loadRequest.isWithDeleted()) {
                return activeVer;
            } else if (activeVer.getWorkflowState() != EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                return activeVer;
            }
        }

        return null;
    }

    /**
     * Return true if the content object is deleted in all language at a given date.
     * 
     * @param versionID
     * @return
     */
    private boolean isDeleted(List<EntryStateable> entryStateables, int versionID) {
        List<EntryStateable> entryStates = getClosestVersionedEntryStates(entryStateables,
                versionID);
        for (EntryStateable entryState : entryStates) {
            if (entryState.getWorkflowState() != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find and returns the closest versioned entry state for the given version ID, in ALL languages that weren't deleted at the time.
     * 
     * @param versionID
     *                the identifier of the version we want to retrieve the list of languages that were actually active at the time (deleted
     *                languages are only taken into account if the version ID matches the entry state version ID).
     * 
     * @return an List containing ContentObjectEntryState objects that are the various language entry states that correspond to the
     *         closest versions for each language.
     * @return
     * @throws JahiaException
     */
    private List<EntryStateable> getClosestVersionedEntryStates(List<EntryStateable> entryStateables,
            int versionID) {

        // let's test if we have a version id that exists prior to or equal
        // to the one we've been asked for
        Map<String, EntryStateable> closestInLanguage = new HashMap<String, EntryStateable>();
        for (EntryStateable curEntryState : entryStateables) {
            if (((curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) && (curEntryState
                    .getVersionID() <= versionID))
                    || ((curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONED) && (curEntryState
                            .getVersionID() <= versionID))
                    || ((curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) && (curEntryState
                            .getVersionID() <= versionID))) {

                // we found an acceptable versioned entry state, let's check
                // if it's closer in the language.
                // first we retrieve the current closest version ID for the
                // language if it exists...
                EntryStateable resultEntryState = closestInLanguage
                        .get(curEntryState.getLanguageCode());
                if (resultEntryState != null) {
                    // now let's test if it's closer to our previous result.
                    if (resultEntryState.getVersionID() < curEntryState
                            .getVersionID()) {
                        closestInLanguage.put(curEntryState.getLanguageCode(),
                                curEntryState);
                    }
                } else {
                    // no version found for this language, let's add it.
                    closestInLanguage.put(curEntryState.getLanguageCode(),
                            curEntryState);
                }
            }
        }
        List<EntryStateable> resultEntryStates = new ArrayList<EntryStateable>(closestInLanguage.values());
        return resultEntryStates;
    }
}
