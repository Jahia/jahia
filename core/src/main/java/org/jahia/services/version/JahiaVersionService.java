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
