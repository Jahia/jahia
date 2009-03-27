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

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.containers.JahiaContainerSubDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaContainerDefinitionDAO;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.model.*;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 janv. 2005
 * Time: 16:58:42
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerDefinitionManager {
// ------------------------------ FIELDS ------------------------------
    public static final String CONTAINERDEFINITIONMANAGER_CACHENAME = "ContainerDefinitionManagerCache";
    public static final String CONTAINERDEFINITIONMANAGERLIST_CACHENAME = "ContainerDefinitionManagerListCache";
    private static final String CACHEBYID_PREFIX = "ContainerDefinitionID";
    private static final String CACHEBYSITEANDNAME_PREFIX = "ContainerDefinitionSiteAndName";
    private static final String CACHEBYTEMPLATEID_PREFIX = "ContainerDefinitionTemplateId";
    private JahiaContainerDefinitionDAO dao = null;
    private JahiaSiteDAO siteDAO = null;
    private Log log = LogFactory.getLog(JahiaContainerDefinitionManager.class);
    private CacheService cacheService = null;
    private Cache<String, JahiaContainerDefinition> definitionCache = null;
    private Cache<GroupCacheKey, List<JahiaContainerDefinition>> listCache = null;
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Integer> getAllContainerDefinitionIds() {
        return dao.findAllContainerDefinitionId();
    }

    public void setJahiaContainerDefinitionDAO(JahiaContainerDefinitionDAO dao) {
        this.dao = dao;
    }

    public void setJahiaSiteDAO(JahiaSiteDAO dao) {
        this.siteDAO = dao;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
// -------------------------- OTHER METHODS --------------------------

    public void createContainerDefinition(JahiaContainerDefinition theDefinition) {
        JahiaCtnDef ctnDef = new JahiaCtnDef();
        Set<JahiaCtnDefProperty> properties = convertToJahiaCtnDef(theDefinition, ctnDef);
        JahiaSite site = null;
        try {
            site = siteDAO.findById(new Integer(theDefinition.getJahiaID()));
        } catch (ObjectRetrievalFailureException e) {
            log.warn("JahiaSite not found " + theDefinition.getJahiaID(), e);
        }
        ctnDef.setJahiaSiteId(site.getId());
        ctnDef.setName(theDefinition.getName());
        ctnDef.setSubDefinitions(properties);
        ctnDef.setProperties(theDefinition.getProperties());
        ctnDef.setContainerType(theDefinition.getContainerType());
        if (theDefinition.getParentCtnType() != null) {
            ctnDef.setParentCtnName(theDefinition.getParentCtnType());
        }
        ctnDef = dao.save(ctnDef);
        theDefinition.setID(ctnDef.getId().intValue());
        theDefinition = convertToJahiaContainerDefinition(ctnDef);
        if (definitionCache != null) {
            definitionCache.put(CACHEBYID_PREFIX + theDefinition.getID(), theDefinition);
            definitionCache.put(CACHEBYSITEANDNAME_PREFIX + theDefinition.getJahiaID() + theDefinition.getName(), theDefinition);
        }
        if(listCache!= null) {
            listCache.flushGroup(CACHEBYID_PREFIX + theDefinition.getID());
            if( theDefinition.getSubDefs() != null && !theDefinition.getSubDefs().isEmpty() ){
                JahiaContainerSubDefinition subDef = (JahiaContainerSubDefinition)
                        theDefinition.getSubDefs().values().iterator().next();
                this.invalidateContainerDefinitionInTemplate(subDef.getPageDefID());
            }
        }
    }

    public void deleteContainerDefinition(int definitionID) {
        if (definitionCache != null) {
            JahiaContainerDefinition definition = loadContainerDefinition(definitionID);
            if (definition != null) {
                definitionCache.remove(CACHEBYID_PREFIX + definitionID);
                definitionCache.remove(CACHEBYSITEANDNAME_PREFIX + definition.getJahiaID() + definition.getName());
            }
        }
        dao.delete(new Integer(definitionID));
    }

    /**
     * Load a container definition with it's properties and subdefinition
     *
     * @param definitionID
     * @return JahiaContainerDefinition
     */
    public JahiaContainerDefinition loadContainerDefinition(int definitionID) {
        log.debug("load container definition for id " + definitionID);
        JahiaContainerDefinition definition = null;
        if(definitionCache==null) {
            try {
                definitionCache = cacheService.createCacheInstance(CONTAINERDEFINITIONMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot initialize cache", e);
            }
        }
        if (definitionCache != null) {
            definition = definitionCache.get(CACHEBYID_PREFIX + definitionID);
        }
        if (definition == null) {
            JahiaCtnDef ctnDef = null;
            try {
                ctnDef = dao.fullyLoadContainerDefinition(new Integer(definitionID));
            } catch (ObjectRetrievalFailureException e) {
                log.warn("Error during retireval of container definition " + definitionID, e);
                return null;
            }
            definition = convertToJahiaContainerDefinition(ctnDef);
            if (definition != null) {
                if(definitionCache!=null) {
                definitionCache.put(CACHEBYID_PREFIX + definitionID, definition);
                definitionCache.put(CACHEBYSITEANDNAME_PREFIX + definition.getJahiaID() + definition.getName(), definition);
                }
                if(listCache!=null)
                listCache.flushGroup(CACHEBYID_PREFIX + definition.getID());
            }
        }
        return definition;
    }

    public JahiaContainerDefinition loadContainerDefinition(int siteID, String definitionName) {
        JahiaContainerDefinition definition = null;
        if(definitionCache==null) {
            try {
                definitionCache = cacheService.createCacheInstance(CONTAINERDEFINITIONMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot create cache", e);
            }
        }
        if (definitionCache != null) {
            definition = definitionCache.get(CACHEBYSITEANDNAME_PREFIX + siteID + definitionName);
        }
        if (definition == null) {
            JahiaCtnDef ctnDef = dao.fullyLoadContainerDefinition(new Integer(siteID), definitionName);
            definition = convertToJahiaContainerDefinition(ctnDef);
            if (definition != null) {
                if(definitionCache !=null) {
                definitionCache.put(CACHEBYID_PREFIX + definition.getID(), definition);
                definitionCache.put(CACHEBYSITEANDNAME_PREFIX + definition.getJahiaID() + definition.getName(), definition);
                }
                if(listCache!=null) {
                listCache.flushGroup(CACHEBYID_PREFIX + definition.getID());
                }
            }
        }
        return definition;
    }

    public List<JahiaContainerDefinition> loadContainerDefinitionInTemplate(int templateId) {
        List<JahiaContainerDefinition> retList = null;
        if(listCache==null) {
            try {
                listCache = cacheService.createCacheInstance(CONTAINERDEFINITIONMANAGERLIST_CACHENAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot create cache", e);
            }
        }
        if (listCache != null) {
            retList = listCache.get(new GroupCacheKey(CACHEBYTEMPLATEID_PREFIX + templateId, new HashSet<String>()));
        }
        if (retList == null) {
            List<JahiaCtnDef> list = dao.fullyLoadContainerDefinitionInTemplate(new Integer(templateId));
            FastArrayList tempList = new FastArrayList(list.size());
            Set<String> groups = new HashSet<String>(list.size());
            for (JahiaCtnDef jahiaCtnDef : list) {
                JahiaContainerDefinition definition = convertToJahiaContainerDefinition(jahiaCtnDef);
                if (definition != null) {
                    tempList.add(definition);
                    groups.add(CACHEBYID_PREFIX + definition.getID());
                }
            }
            tempList.setFast(true);
            retList = tempList;
            if (listCache != null) {
                listCache.put(new GroupCacheKey(CACHEBYTEMPLATEID_PREFIX + templateId, groups), retList);
            }
        }
        return retList;
    }

    public void invalidateContainerDefinitionInTemplate(int definitionId){
        if (listCache != null) {
            listCache.remove(new GroupCacheKey(CACHEBYTEMPLATEID_PREFIX + definitionId, new HashSet<String>()));
            listCache.flushGroup(CACHEBYTEMPLATEID_PREFIX + definitionId);
        }
    }

    public void updateContainerDefinition(JahiaContainerDefinition theDefinition) {
        log.debug("update container definition " + theDefinition.getName() + " with id " + theDefinition.getID());
        JahiaCtnDef ctnDef = dao.fullyLoadContainerDefinition(new Integer(theDefinition.getID()));
        JahiaSite site = null;
        try {
            site = siteDAO.findById(new Integer(theDefinition.getJahiaID()));
        } catch (ObjectRetrievalFailureException e) {
            log.warn("JahiaSite not found " + theDefinition.getJahiaID(), e);
        }
        Set<JahiaCtnDefProperty> properties = convertToJahiaCtnDef(theDefinition, ctnDef);
        ctnDef.setJahiaSiteId(site.getId());
        ctnDef.setName(theDefinition.getName());
        ctnDef.setContainerType(theDefinition.getContainerType());
        if (theDefinition.getParentCtnType() != null) {
            ctnDef.setParentCtnName(theDefinition.getParentCtnType());
        }
        if (!properties.equals(ctnDef.getSubDefinitions())) {
            ctnDef.setSubDefinitions(properties);
        }
        ctnDef.setId(new Integer(theDefinition.getID()));
        if (!theDefinition.getProperties().equals(ctnDef.getProperties())) {
            ctnDef.getProperties().putAll(theDefinition.getProperties());
            ctnDef.getProperties().keySet().retainAll(theDefinition.getProperties().keySet());
        }
        dao.update(ctnDef);
        theDefinition.setID(ctnDef.getId().intValue());
        log.debug("updated container definition " + theDefinition.getName() + " with id " + theDefinition.getID());
        theDefinition = convertToJahiaContainerDefinition(ctnDef);
        if (definitionCache != null) {
            definitionCache.put(CACHEBYID_PREFIX + theDefinition.getID(), theDefinition);
            definitionCache.put(CACHEBYSITEANDNAME_PREFIX + theDefinition.getJahiaID() + theDefinition.getName(), theDefinition);
        }
            if(listCache!=null){
            listCache.flushGroup(CACHEBYID_PREFIX + theDefinition.getID());
        }
    }

    private JahiaContainerDefinition convertToJahiaContainerDefinition(org.jahia.hibernate.model.JahiaCtnDef ctnDef) {
        JahiaContainerDefinition containerDefinition = null;
        if (ctnDef != null) {
            try {
                FastHashMap subDefinition = new FastHashMap(11);
                if (ctnDef.getSubDefinitions() != null) {
                    for (JahiaCtnDefProperty property : ctnDef.getSubDefinitions()) {
                        List<JahiaContainerStructure> subdefs = null;
                        List<JahiaCtnStruct> jahiaCtnStructs = new ArrayList<JahiaCtnStruct>(property.getJahiaCtnStructs());
                        Collections.sort(jahiaCtnStructs, new Comparator<JahiaCtnStruct>() {
                            public int compare(JahiaCtnStruct struct1, JahiaCtnStruct struct2) {
                                return struct1.getRankJahiaCtnStruct().compareTo(struct2.getRankJahiaCtnStruct());
                            }
                        });
                        if (jahiaCtnStructs != null) {
                            subdefs = new ArrayList<JahiaContainerStructure>(jahiaCtnStructs.size());
                            Iterator<JahiaCtnStruct> iterator2 = jahiaCtnStructs.iterator();
                            while (iterator2.hasNext()) {
                                JahiaCtnStruct struct = iterator2.next();
                                subdefs.add(new JahiaContainerStructure(property.getIdJahiaCtnDefProperties().intValue(),
                                                                        struct.getComp_id().getObjType().intValue(),
                                                                        struct.getComp_id().getObjDefId().intValue(),
                                                                        struct.getRankJahiaCtnStruct().intValue(), ctnDef.getJahiaSiteId()));
                            }
                        }
                        subDefinition.put(property.getPageDefinitionId(),
                                          new JahiaContainerSubDefinition(property.getIdJahiaCtnDefProperties().intValue(),
                                                                          property.getPageDefinitionId().intValue(),
                                                 ctnDef.getJahiaSiteId(), subdefs));
                    }
                }
                subDefinition.setFast(true);
                containerDefinition = new JahiaContainerDefinition(ctnDef.getId().intValue(),
                                                                   ctnDef.getJahiaSiteId().intValue(),
                                                                   ctnDef.getName(), subDefinition, ctnDef.getContainerType());
                if (ctnDef.getParentCtnName() != null) {
                    containerDefinition.setParentCtnType(ctnDef.getParentCtnName());
                }

                Properties properties = new Properties();
                if (ctnDef.getProperties() != null) {
                    for (Map.Entry<Object, Object> entry : ctnDef.getProperties().entrySet()) {
                        properties.put(entry.getKey(), entry.getValue());
                    }
                    containerDefinition.setProperties(properties);
                }
            } catch (JahiaException e) {
                log.warn("Could not add object to list", e);
            }
        }
        return containerDefinition;
    }

    private Set<JahiaCtnDefProperty> convertToJahiaCtnDef(JahiaContainerDefinition theDefinition, JahiaCtnDef ctnDef) {
        // get the stored subdefs
        Set<JahiaCtnDefProperty> properties = ctnDef.getSubDefinitions();
        // iterate over the new subdefs
        List<JahiaCtnDefProperty> found = new ArrayList<JahiaCtnDefProperty>(53);
        for (final Map.Entry<Integer, JahiaContainerSubDefinition> entry : theDefinition.getSubDefs().entrySet()) {
            Integer pageDefinitionId = entry.getKey();
            JahiaContainerSubDefinition subDefinition = entry.getValue();
            JahiaCtnDefProperty property = new JahiaCtnDefProperty();
            if (subDefinition.getID() > 0) {
                property.setIdJahiaCtnDefProperties(new Integer(subDefinition.getID()));
                if (properties.contains(property)) {
                    Iterator<JahiaCtnDefProperty> propertiesIterator = properties.iterator();
                    while (propertiesIterator.hasNext()) {
                        JahiaCtnDefProperty defProperty = propertiesIterator.next();
                        if (defProperty.equals(property)) {
                            property = defProperty;
                            break;
                        }
                    }
                }
            }
            property.setJahiaCtnDef(ctnDef);
            property.setPageDefinitionId(pageDefinitionId);
            if (subDefinition.getStructure() != null) {
                List<JahiaCtnStruct> foundS = new ArrayList<JahiaCtnStruct>(53);
                Set<JahiaCtnStruct> subdefs = property.getJahiaCtnStructs();
                for (JahiaContainerStructure structure : subDefinition.getStructure()) {
                    JahiaCtnStruct struct = new JahiaCtnStruct(new JahiaCtnStructPK(property,
                                                                                    new Integer(structure.getObjectType()),
                                                                                    new Integer(structure.getObjectDefID())));
                    if (!subdefs.contains(struct))
                        subdefs.add(struct);
                    else {
                        Iterator<JahiaCtnStruct> iterator2 = subdefs.iterator();
                        while (iterator2.hasNext()) {
                            JahiaCtnStruct ctnStruct = iterator2.next();
                            if(ctnStruct.equals(struct)) {
                                struct = ctnStruct;
                                break;
                            }
                        }
                    }
                    struct.setRankJahiaCtnStruct(new Integer(structure.getRank()));
                    foundS.add(struct);
                }
                List<JahiaCtnStruct> removed = new ArrayList<JahiaCtnStruct>(53);
                Iterator<JahiaCtnStruct> subIterator = subdefs.iterator();
                while (subIterator.hasNext()) {
                    JahiaCtnStruct containerStructure = subIterator.next();
                    if (!foundS.contains(containerStructure)) {
                        removed.add(containerStructure);
                    }
                }
                subdefs.removeAll(removed);

                property.setJahiaCtnStructs(subdefs);
            }
            if (!properties.contains(property))
                properties.add(property);
            found.add(property);
        }
        List<JahiaCtnDefProperty> removed = new ArrayList<JahiaCtnDefProperty>(53);
        Iterator<JahiaCtnDefProperty> pIterator = properties.iterator();
        while (pIterator.hasNext()) {
            JahiaCtnDefProperty defProperty = pIterator.next();
            if (!found.contains(defProperty))
                removed.add(defProperty);
        }
        properties.removeAll(removed);

        return properties;
    }

    public List<String> getContainerDefinitionNamesWithType(Set<String> types) {
        List<Integer> ids = dao.getContainerDefinitionsWithType(types);
        if (ids == null || ids.isEmpty()){
            return new ArrayList<String>();
        }
        List<String> result = new ArrayList<String>();
        for (Integer id : ids){
            JahiaContainerDefinition def = loadContainerDefinition(id.intValue());
            if ( def != null && !result.contains(def.getName())){
                result.add(def.getName());
            }
        }
        return result;
    }
}

