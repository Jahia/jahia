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
