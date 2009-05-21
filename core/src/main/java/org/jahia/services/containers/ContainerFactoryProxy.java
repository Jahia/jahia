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
