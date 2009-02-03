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

package org.jahia.services.containers;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

class ContentContainerTools {

    /** this class unique instance */
    private static ContentContainerTools instance;
    private JahiaContainerManager containerManager = null;
    private JahiaFieldsDataManager fieldsDataManager = null;
    // the Container cache name.
    public static final String CONTENT_CONTAINER_CACHE = "ContentContainerCache";
    // field IDs by container cache.
    public static final String FIELD_IDS_BY_CONTAINER_CACHE = "FieldIDsByContainerCache";

    /**
     * Default constructor, creates a new <code>ContentContainerTools</code> instance.
     */
    private ContentContainerTools () {
        final ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        containerManager = (JahiaContainerManager) context.getBean(JahiaContainerManager.class.getName());
        fieldsDataManager = (JahiaFieldsDataManager) context.getBean(JahiaFieldsDataManager.class.getName());
    }

    /**
     * Return the unique instance of this class
     *
     * @return the unique instance of this class
     */
    public static synchronized ContentContainerTools getInstance () {
        if (instance == null) {
            instance = new ContentContainerTools ();
        }

        return instance;
    }

    /**
     * Get a JahiaContainer from its ID
     *
     * @param containerID
     *
     * @throws JahiaException if the container doesn't exist, or there's a DB error
     */
    public ContentContainer getContainer (int containerID)
            throws JahiaException {
        return getContainer (containerID, false);
    }

    /**
     * Get a JahiaContainer from its ID
     *
     * @param containerID
     * @param forceLoadFromDB if true, force loading from db
     *
     * @throws JahiaException If the container doesn't exist, or there's a DB error
     */
    public ContentContainer getContainer (int containerID, boolean forceLoadFromDB)
            throws JahiaException {
        return containerManager.getContainer(containerID);
    }

    /**
     * Get the list of field IDs for a given container ID
     *
     * @param containerID
     * @param loadRequest
     * @return
     * @throws JahiaException
     */
    public List<Integer> getFieldIDsByContainer (int containerID,
                                          EntryLoadRequest loadRequest)
    throws JahiaException{
        return getFieldIDsByContainer (containerID, loadRequest, false);
    }

    /**
     * Get a JahiaContainer from its ID
     *
     * @param containerID
     * @param forceLoadFromDB if true, force loading from db
     * @param loadRequest
     * @return
     * @throws JahiaException If the container doesn't exist, or there's a DB error
     */
    public List<Integer> getFieldIDsByContainer (int containerID,
                                                       EntryLoadRequest loadRequest,
                                                       boolean forceLoadFromDB)
            throws JahiaException {
        if ( containerID <=0 ){
            return new ArrayList<Integer>();
        }
        return new ArrayList<Integer>(fieldsDataManager.getFieldsIdsInContainer(containerID, loadRequest));
    }

    /**
     * Removes a content container from the cache if it was present in the cache. If not,
     * this does nothing.
     *
     * @param containerID the identifier for the container to try to remove from the
     *                cache.
     */
    public void invalidateContainerFromCache (int containerID) {
    }

    /**
     * Invalidate field IDs By container cache for the given containerID
     *
     * @param containerID
     */
    public void invalidateFieldIDsByContainerFromCache (int containerID) {
    }

    public List<ContentObjectEntryState> getVersionedEntryStates(int containerId, boolean withActive) {
        return containerManager.getVersionedEntryStates(containerId, withActive);
    }

    public void deleteEntry(int id, EntryStateable deleteEntryState) {
        containerManager.deleteContainerEntry(id, deleteEntryState);
    }

    public void copyEntry(int id, EntryStateable fromE, EntryStateable toE) {
        containerManager.copyContainerEntry(id, fromE, toE);
    }
}
