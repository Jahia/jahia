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
package org.jahia.services.version;

import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;


/**
 * Defines some methods related to versinoning and content staging
 *
 * @author  <a href="mailto:djilli@jahia.com">David Jilli</a>
 */
public abstract class JahiaVersionService extends JahiaService
{

    /**
     * Is staging enabled for this site?
     * @param siteID, the site identifier
     */
    public abstract boolean isStagingEnabled (int siteID);

    /**
     * Is versioning enabled for this site?
     * @param siteID, the site identifier
     */
    public abstract boolean isVersioningEnabled (int siteID);

    /**
     * @return the current versionID, which is the number of secondes since 1970
     */
    public abstract int getCurrentVersionID();

    /**
     * @return the SaveVersion for a specified site
     */
    public abstract JahiaSaveVersion getSiteSaveVersion(int siteID);

    /**
     * This method should be called if we have a list of "Versionable"s that
     * represent every version that the DB contains, and a ProcessingContext that
     * contains the version we would like to load
     * SHARED language has the highest priority!
     * @param entryStateables a List of object implementing the EntryStateable interface
     * @param jParams the ProcessingContext that contains the version we want to load
     * @param ignoreLanguage, if true, resolve entry state without checking specific language
     * @return an element, or null if field doesn't exist in this version!
     */
    public abstract EntryStateable resolveEntry( List<EntryStateable> entryStateables, EntryLoadRequest loadRequest , boolean ignoreLanguage);

    /**
     * This method should be called if we have a list of "EntryState-able"s that
     * represent every entry that the DB contains, and a ProcessingContext that
     * contains the entry request we would like to load
     * @param entryStateables a List of object implementing the EntryStateable interface
     * @param loadRequest the EntryLoadRequest that contains information about
     * the entry we want to load, specifically the version, the workflow state
     * and the list of locales
     * @return an element of the list (or null if none found)
     */
    public abstract EntryStateable resolveEntry( List<EntryStateable> entryStateables, EntryLoadRequest loadRequest );

   /**
    * Validate all the staged content from a page to which the user has WRITE+ADMIN access to
    */
    public abstract void activateStagedPage ( int pageID, JahiaUser user, ProcessingContext jParams, StateModificationContext stateModifContext )
    throws JahiaException;

}
