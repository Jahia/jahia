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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;


/**
 * Container Factory Proxy
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerFactoryProxy {

    public static final int LOAD_FIELDS = 1;
    public static final int LOAD_SUBCONTAINER_LISTS = 2;
    public static final int LOAD_FIELD_AND_SUBCONTAINER_LISTS = LOAD_FIELDS | LOAD_SUBCONTAINER_LISTS;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContainerFactoryProxy.class);

    private boolean containerListsBeingLoaded = false;
    private boolean fieldsBeingLoaded = false;
    private ProcessingContext jParams;
    private EntryLoadRequest loadRequest;
    private Map<Integer, List<Integer>> cachedFieldsFromContainers;
    private Map<Integer, List<Integer>> cachedContainersFromContainerLists;
    private Map<Integer, List<Integer>> cachedContainerListsFromContainers;

    //--------------------------------------------------------------------------
    /**
     *
     * @param loadFlag
     * @param jParams
     * @param loadRequest
     * @param cachedFieldsFromContainers
     * @param cachedContainersFromContainerLists
     * @param cachedContainerListsFromContainers
     */
    public ContainerFactoryProxy (int loadFlag,
                                       ProcessingContext jParams,
                                       EntryLoadRequest loadRequest,
                                       Map<Integer, List<Integer>> cachedFieldsFromContainers,
                                       Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                       Map<Integer, List<Integer>> cachedContainerListsFromContainers)
    {
        this.jParams = jParams;
        this.loadRequest = loadRequest;
        this.cachedFieldsFromContainers = cachedFieldsFromContainers;
        if ( this.cachedFieldsFromContainers == null ){
            this.cachedFieldsFromContainers = new HashMap<Integer, List<Integer>>();
        }
        this.cachedContainersFromContainerLists = cachedContainersFromContainerLists;
        if ( this.cachedContainersFromContainerLists == null ){
            this.cachedContainersFromContainerLists = new HashMap<Integer, List<Integer>>();
        }
        this.cachedContainerListsFromContainers = cachedContainerListsFromContainers;
        if ( this.cachedContainerListsFromContainers == null ){
            this.cachedContainerListsFromContainers = new HashMap<Integer, List<Integer>>();
        }
    }

    /**
     * Load this container's sub containers list if not already loaded
     *
     * @param container
     * @param loadFlag, LOAD_FIELDS, LOAD_SUBCONTAINER_LISTS or LOAD_FIELD_AND_SUBCONTAINER_LISTS
     */
    public void load(JahiaContainer container, int loadFlag){
        if ( container == null ){
            return;
        }
        try {
            if ( loadFlag == LOAD_FIELD_AND_SUBCONTAINER_LISTS && !isFieldsBeingLoaded() && !isContainerListsBeingLoaded()){
                setContainerListsBeingLoaded(true);
                setFieldsBeingLoaded(true);
                ContainerFactory.getInstance()
                    .fullyLoadContainer(container,
                                        loadFlag, jParams, loadRequest,
                                        cachedFieldsFromContainers,
                                        cachedContainersFromContainerLists,
                                        cachedContainerListsFromContainers);
                setContainerListsBeingLoaded(false);
                setFieldsBeingLoaded(false);               
            } else if ( loadFlag == LOAD_FIELDS && !isFieldsBeingLoaded()){
                setFieldsBeingLoaded(true);                
                boolean isContainerListsLoadedState =
                    container.isContainerListsLoaded();
                container.setContainerListsLoaded(true);
                ContainerFactory.getInstance()
                    .fullyLoadContainer(container,
                                        loadFlag, jParams, loadRequest,
                                        cachedFieldsFromContainers,
                                        cachedContainersFromContainerLists,
                                        cachedContainerListsFromContainers);
                container.setContainerListsLoaded(isContainerListsLoadedState);
                setFieldsBeingLoaded(false);                
            } else if ( loadFlag == LOAD_SUBCONTAINER_LISTS && !isContainerListsBeingLoaded()){
                setContainerListsBeingLoaded(true);                
                boolean isFieldsLoadedState =
                    container.isFieldsLoaded();
                container.setFieldsLoaded(true);
                ContainerFactory.getInstance()
                    .fullyLoadContainer(container,
                                        loadFlag, jParams, loadRequest,
                                        cachedFieldsFromContainers,
                                        cachedContainersFromContainerLists,
                                        cachedContainerListsFromContainers);
                container.setFieldsLoaded(isFieldsLoadedState);
                setContainerListsBeingLoaded(false);                
            }
        } catch ( JahiaException je){
            logger.debug("Exception occured when loading Container ["
            + container.getID() + "]", je);
        }
    }

    /**
     * @return the fieldsBeingLoaded
     */
    private boolean isFieldsBeingLoaded() {
        return fieldsBeingLoaded;
    }

    /**
     * @param fieldsBeingLoaded the fieldsBeingLoaded to set
     */
    private void setFieldsBeingLoaded(boolean fieldsBeingLoaded_) {
        this.fieldsBeingLoaded = fieldsBeingLoaded_;
    }

    /**
     * @return the containerListsBeingLoaded
     */
    private boolean isContainerListsBeingLoaded() {
        return containerListsBeingLoaded;
    }

    /**
     * @param containerListsBeingLoaded the containerListsBeingLoaded to set
     */
    private void setContainerListsBeingLoaded(boolean containerListsBeingLoaded_) {
        this.containerListsBeingLoaded = containerListsBeingLoaded_;
    }
}
