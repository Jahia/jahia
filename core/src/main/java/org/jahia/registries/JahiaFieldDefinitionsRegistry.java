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
//  EV      25.11.2000
//

package org.jahia.registries;

import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.CacheService;
import org.jahia.services.fields.JahiaFieldService;

import java.util.ArrayList;
import java.util.List;

public class JahiaFieldDefinitionsRegistry implements CacheListener {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaFieldDefinitionsRegistry.class);

    private static JahiaFieldDefinitionsRegistry instance = null;

    private static final String KEY_SEPARATOR = "###";

    public static final String FIELD_DEFINITION_BY_ID_CACHE = "FieldDefinitionsByID";
    public static final String FIELD_DEFINITION_BY_SITE_AND_NAME_CACHE =
            "FieldDefinitionsBySiteAndName";
    private Cache<Integer, JahiaFieldDefinition> fieldDefTable;
    private Cache<String, JahiaFieldDefinition> fieldDefSiteAndNameTable;
    private CacheService cacheService;
    private JahiaFieldService fieldService;

    private boolean initialized = false;


    private JahiaFieldDefinitionsRegistry () {
    }

    public static synchronized JahiaFieldDefinitionsRegistry getInstance () {
        if (instance == null) {
            instance = new JahiaFieldDefinitionsRegistry();
        }
        return instance;
    }

    public void init () throws JahiaException {
        if (!initialized) {
            logger.debug("Starting FieldDefinitions Registry");
            try {
                fieldDefTable = cacheService.createCacheInstance(FIELD_DEFINITION_BY_ID_CACHE);
                fieldDefTable.registerListener(this);

                fieldDefSiteAndNameTable = cacheService.createCacheInstance(FIELD_DEFINITION_BY_SITE_AND_NAME_CACHE);
                fieldDefSiteAndNameTable.registerListener(this);

            } catch (JahiaException je) {
                logger.error(
                        "Error while creating caches for JahiaFieldDefinition registry.", je);
            }
//            loadAllDefinitions();
            initialized = true;
        }
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setFieldService(JahiaFieldService fieldService) {
        this.fieldService = fieldService;
    }

    private void loadAllDefinitions ()
        throws JahiaException {
        List<Integer> ids = fieldService.getAllFieldDefinitionIDs();
        for (Integer currentID : ids) {
            JahiaFieldDefinition curDefinition = fieldService.loadFieldDefinition(currentID.
                intValue());
            if (curDefinition != null) {            
                addToCache(curDefinition);
            }    
        }
    }

    private JahiaFieldDefinition loadDefinitionByID (int defID)
        throws JahiaException {
        JahiaFieldDefinition curDefinition = fieldService.loadFieldDefinition(defID);
        if (curDefinition != null) {
            addToCache(curDefinition);
        }
        return curDefinition;
    }

    private JahiaFieldDefinition loadDefinitionBySiteIDAndName (int siteID, String definitionName)
        throws JahiaException {
        JahiaFieldDefinition curDefinition = fieldService.loadFieldDefinition(siteID, definitionName);
        if (curDefinition != null) {
            addToCache(curDefinition);
        }
        return curDefinition;
    }

    private synchronized void addToCache(JahiaFieldDefinition curDefinition) {
        fieldDefTable.put(new Integer(curDefinition.getID()), curDefinition);
        fieldDefSiteAndNameTable.put(buildCacheKey(curDefinition.getName(),
            curDefinition.getJahiaID()), curDefinition);
    }     
    
    public JahiaFieldDefinition getDefinition (int defID)
        throws JahiaException {
        JahiaFieldDefinition result = fieldDefTable.get(new
            Integer(defID));
        if (result == null) {
            result = loadDefinitionByID(defID);
        }

        if (result != null) {
            return result;
        }
        String errorMsg = "Could not find the definition ID " + defID;
        logger.error(errorMsg + " -> BAILING OUT");
        throw new JahiaException("Database synchronisation error",
                                 errorMsg, JahiaException.REGISTRY_ERROR,
                                 JahiaException.CRITICAL_SEVERITY);
    } // end getDefinition

    public List<JahiaFieldDefinition> getAllDefinitions () throws JahiaException {
        /** @todo FIXME this will not work if the cache is limited */
        List<Integer> ids = fieldService.getAllFieldDefinitionIDs();
        List<JahiaFieldDefinition> definitions = new ArrayList<JahiaFieldDefinition>();
        JahiaFieldDefinition fieldDef = null;
        for ( int i=0 ; i<ids.size() ; i++ ){
            fieldDef = this.fieldDefTable.get(ids.get(i));
            definitions.add(fieldDef);
        }
        return definitions;
    }


    // 11.07.2001 update to multi site by adding siteID
    public JahiaFieldDefinition getDefinition (int siteID, String fieldName)
        throws JahiaException {

        // UNIQUE DEFINITION CONCERN ( FOR METADATA )
        // @todo : load prefix from config file!!!!
        if ( fieldName == null ){
            return null;
        }
        if ( fieldName.startsWith(JahiaFieldDefinition.GLOBAL_FIELD) ){
            siteID = 0;
        }

        JahiaFieldDefinition result = fieldDefSiteAndNameTable.get(
         buildCacheKey(fieldName, siteID));
        if (result == null) {
            result = loadDefinitionBySiteIDAndName(siteID, fieldName);
        }
        if (result == null) {
            logger.debug("Couldn't find field definition for siteID=" + siteID + " and name=" + fieldName);
        }
        return result;
    } // end getDefinition

    public void setDefinition (JahiaFieldDefinition theFieldDef)
        throws JahiaException {

        JahiaFieldDefinition aFieldDef = getDefinition(theFieldDef.getJahiaID(),
            theFieldDef.getName());
        if (aFieldDef != null) {
            // field definition already exists, just have to update
            // We ensure to perform an update
            theFieldDef.setID(aFieldDef.getID());
        } else {
            // field definition doesn't exist, need to add to registry
            //We ensure to create a new one
            theFieldDef.setID(0);
        }
        fieldService.saveFieldDefinition(theFieldDef);
        addToCache(theFieldDef);        
    } // end setDefinition

    public void removeFieldDefinition (int fieldDefID)
        throws JahiaException {
        JahiaFieldDefinition fieldDef = getDefinition(fieldDefID);
        if (fieldDef != null) {
            synchronized (this){        	
                fieldDefTable.remove(new Integer(fieldDefID));
                fieldDefSiteAndNameTable.remove(buildCacheKey(fieldDef.getName(),
                    fieldDef.getJahiaID()));
            }    
        }

    } // end setDefinition

    private String buildCacheKey (String fieldDefinitionName, int siteID) {
        StringBuffer result = new StringBuffer();
        result.append(fieldDefinitionName);
        result.append(KEY_SEPARATOR);
        result.append(siteID);
        return result.toString();
    }

    /**
     * This method is called each time the cache flushes its items.
     *
     * @param cacheName the name of the cache which flushed its items.
     */
    public void onCacheFlush(String cacheName) {
        if (FIELD_DEFINITION_BY_ID_CACHE.equals(cacheName)) {
            fieldDefSiteAndNameTable.flush (false);

        } else if (FIELD_DEFINITION_BY_SITE_AND_NAME_CACHE.equals(cacheName)) {
             fieldDefTable.flush (false);
        }

        try {
            loadAllDefinitions();
        } catch (JahiaException e) {
            logger.warn("Could not reload the Field Definitions.", e);
        }
    }

}
