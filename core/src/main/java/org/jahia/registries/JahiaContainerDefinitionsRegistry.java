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

//
//  JahiaContainerDefinitionsRegistry

package org.jahia.registries;

import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.CacheService;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.pages.JahiaPageDefinition;

import java.util.List;
import java.util.ArrayList;

public class JahiaContainerDefinitionsRegistry implements CacheListener {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaContainerDefinitionsRegistry.class);

    private static JahiaContainerDefinitionsRegistry theObject = null;

    public static final String CONTAINER_DEFINITION_BY_ID_CACHE = "ContainerDefinitionByID";
    public static final String CONTAINER_DEFINITION_BY_SITE_AND_NAME_CACHE =
            "ContainerDefinitionsBySiteAndName";
//    private Cache containerDefByID;
//    private Cache containerDefBySiteIDAndName;
    @SuppressWarnings("unused")
    private CacheService cacheService;
    private JahiaContainersService containersService;

    private boolean initialized = false;

    private JahiaContainerDefinitionsRegistry () {
    } // end constructor

    /***
     * registry accessor
     * @return       the registry object
     *
     */
    public static synchronized JahiaContainerDefinitionsRegistry getInstance () {
        if (theObject == null) {
            theObject = new JahiaContainerDefinitionsRegistry();
        }
        return theObject;
    } // end getInstance

    /***
     * calls the loadAllDefinitions method
     *
     */
    public void init ()
        throws JahiaException {
        if (!initialized) {
            logger.debug("Starting ContainerDefinitions Registry");
            /*try {
            containerDefByID = ServicesRegistry.getInstance().getCacheService().createCacheInstance(CONTAINER_DEFINITION_BY_ID_CACHE);
            containerDefByID.registerListener(this);


            containerDefBySiteIDAndName = ServicesRegistry.getInstance().getCacheService().createCacheInstance(CONTAINER_DEFINITION_BY_SITE_AND_NAME_CACHE);
            containerDefBySiteIDAndName.registerListener(this);

        } catch (JahiaException je) {
            logger.error(
                "Error while creating caches for JahiaContainerDefinition registry.", je);
        }*/
            loadAllDefinitions();
            initialized = true;
        }
    } // end init

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setContainersService(JahiaContainersService containersService) {
        this.containersService = containersService;
    }

    /***
     * loads all container definitions from the database
     *
     * calls load_container_definitions in JahaiContainersDBService
     *
     */
    private void loadAllDefinitions ()
        throws JahiaException {
        containersService.getAllContainerDefinitionIDs();

//        for (int i = 0; i < defIDs.size(); i++) {
//            JahiaContainerDefinition currentDefinition =
//                    containersService.loadContainerDefinition (((Integer)defIDs.elementAt(i)).intValue());

            /*containerDefByID.put(new Integer(currentDefinition.getID()),
                                 currentDefinition);
            containerDefBySiteIDAndName.put(buildCacheKey(currentDefinition.
                getName(), currentDefinition.getJahiaID()), currentDefinition);*/
//        }
    }

    private JahiaContainerDefinition loadDefinitionByID (int defID)
        throws JahiaException {
        JahiaContainerDefinition currentDefinition = containersService.loadContainerDefinition(defID);
        if (currentDefinition != null) {
            /*containerDefByID.put(new Integer(currentDefinition.getID()),
                                 currentDefinition);
            containerDefBySiteIDAndName.put(buildCacheKey(currentDefinition.
                getName(), currentDefinition.getJahiaID()), currentDefinition);*/
        }
        return currentDefinition;
    }

    private JahiaContainerDefinition loadDefinitionBySiteIDAndName (int siteID,
        String definitionName)
        throws JahiaException {
        JahiaContainerDefinition currentDefinition = containersService.loadContainerDefinition(siteID, definitionName);
        if (currentDefinition != null) {
            /*containerDefByID.put(new Integer(currentDefinition.getID()),
                                 currentDefinition);
            containerDefBySiteIDAndName.put(buildCacheKey(currentDefinition.
                getName(), currentDefinition.getJahiaID()), currentDefinition);*/
        }
        return currentDefinition;
    }

    /***
     * gets a definition in the registry through its definition ID
     *
     * @param        defID           the definition ID
     * @return       a JahiaContainerDefinition object; null if not found
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     * @exception JahiaException   raises a critical JahiaException if no definition found
     *
     */
    public JahiaContainerDefinition getDefinition (int defID)
        throws JahiaException {
//        synchronized (containerDefByID) {
            JahiaContainerDefinition currentDef =null;
            currentDef = loadDefinitionByID(defID);
            if (currentDef == null) {
                logger.debug("Couldn't find container definition for ID=" +defID);
            }
            return currentDef;
//        }
    } // end getDefinition

    /***
     * gets a definition in the registry through its page definition id and container name
     *
     * @param		siteID			the site id
     * @param        containerName   the container name
     * @return       a JahiaContainerDefinition object; null if not found
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     *
     */
    public synchronized JahiaContainerDefinition getDefinition (int siteID,
        String containerName)
        throws JahiaException {
//        synchronized (containerDefBySiteIDAndName) {
            JahiaContainerDefinition currentDef = null;
            currentDef = loadDefinitionBySiteIDAndName(siteID, containerName);
            if (currentDef == null) {
                logger.debug("Couldn't find container definition for siteID=" +
                             siteID + " and name=" + containerName);
            }
            return currentDef;
//        }
    } // end getDefinition

    /***
     * gets all definitions of a page template in the registry
     *
     * @param        pageDefID       the page definition id
     * @return       a JahiaContainerDefinition object; null if not found
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     *
     */
    public List<JahiaContainerDefinition> getDefinitionsInTemplate (JahiaPageDefinition pageDefinition) {
        List<JahiaContainerDefinition> containerDefinitions = containersService.loadContainerDefinitionInTemplate(0);
        List<JahiaContainerDefinition> theDefs = new ArrayList<JahiaContainerDefinition>();
        for (JahiaContainerDefinition def : containerDefinitions) {
            String pageDefName = pageDefinition.getPageType().replace(':', '_');
            if (def.getName().startsWith(pageDefName)) {
                theDefs.add(def);
            }
        }
        return theDefs;
    } // end getDefinitionsInTemplate

    /***
     * sets a definition in the registry, and synchronizes it with the database
     *
     * @param        theContainerDef the JahiaContainerDefinition object to set
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     */
    public void setDefinition (JahiaContainerDefinition
                                            theContainerDef)
        throws JahiaException {
        containersService.saveContainerDefinition(theContainerDef);
    } // end setDefinition

    /***
     * remove a Container Definition
     */
    public void removeContainerDefinition (int ctnDefID)
        throws JahiaException {
    } // end removeContainerDefinition

    /**
     * This method is called each time the cache flushes its items.
     *
     * @param cacheName the name of the cache which flushed its items.
     */
    public void onCacheFlush(String cacheName) {
       /* if (CONTAINER_DEFINITION_BY_ID_CACHE.equals(cacheName)) {
            containerDefBySiteIDAndName.flush(false);

        } else if (CONTAINER_DEFINITION_BY_SITE_AND_NAME_CACHE.equals(cacheName)) {
            containerDefByID.flush(false);
        }*/

        try {
            loadAllDefinitions();

        } catch (JahiaException e) {
            logger.warn("Could not reload the Field Definitions.", e);
        }
    }

}