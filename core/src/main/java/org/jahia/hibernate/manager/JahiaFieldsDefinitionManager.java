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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaFieldSubDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaFieldsDefinitionDAO;
import org.jahia.hibernate.model.JahiaFieldsDef;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 8 févr. 2005
 * Time: 16:43:16
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldsDefinitionManager {
// ------------------------------ FIELDS ------------------------------
    public static final String CACHE_NAME = "FieldDefinitionCache";
    public static final String CACHE_ID_KEY_PREFIX = "FieldDefinitionId_";
    public static final String CACHE_SITE_NAME_KEY_PREFIX = "FieldDefinitionSiteName_";
    private JahiaFieldsDefinitionDAO dao = null;
    private Log log = LogFactory.getLog(getClass());
    private CacheService cacheService = null;
// --------------------- GETTER / SETTER METHODS ---------------------

    public void setJahiaFieldsDefinitionDAO(JahiaFieldsDefinitionDAO dao) {
        this.dao = dao;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
// -------------------------- OTHER METHODS --------------------------

    public void saveFieldDefinition(JahiaFieldDefinition theDef) {
        try {
            JahiaFieldsDef jahiaFieldsDef = new JahiaFieldsDef();
            if (theDef.getID()>0) {
                jahiaFieldsDef = dao.loadDefinition(new Integer(theDef.getID()));
            }
            convertToJahiaDefs(jahiaFieldsDef, theDef);
            dao.save(jahiaFieldsDef);
            theDef.setID(jahiaFieldsDef.getId().intValue());
            flushCache(theDef.getID(),theDef.getJahiaID(),theDef.getName());
        } catch (IOException e) {
            log.warn("error during creation of field definition " + theDef, e);
        } catch (JahiaException e) {
            log.warn("error during creation of field definition " + theDef, e);
        }
    }

    public JahiaFieldDefinition loadFieldDefinition(int defID) {
        try {
            if (defID > 0) {
                JahiaFieldDefinition fieldDefinition = null;
                Cache cache = getCache();
                if(cache!=null) {
                    fieldDefinition = (JahiaFieldDefinition) cache.get(CACHE_ID_KEY_PREFIX+defID);
                }
                if(fieldDefinition==null) {
                    JahiaFieldsDef fieldsDef = dao.loadDefinition(new Integer(defID));
                    fieldDefinition = convertToJahiaFieldDefinition(fieldsDef);
                    if(cache!=null) {
                        cache.put(CACHE_ID_KEY_PREFIX+defID,fieldDefinition);
                        cache.put(CACHE_SITE_NAME_KEY_PREFIX+fieldDefinition.getJahiaID()+fieldDefinition.getName(),fieldDefinition);
                    }
                }
                return fieldDefinition;
            }
        } catch (JahiaException e) {
            log.warn("error during load of field definition " + defID, e);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("error during load of field definition " + defID, e);
        }
        return null;
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionName
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getDefinitionIds(String definitionName, boolean isMetadata) {
        return dao.getDefinitionIds(definitionName,isMetadata);
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionNames
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getDefinitionIds(String[] definitionNames, boolean isMetadata) {
        return dao.getDefinitionIds(definitionNames,isMetadata);
    }

    public JahiaFieldDefinition loadFieldDefinition(int siteID, String definitionName) {
        try {
            JahiaFieldDefinition fieldDefinition = null;
            Cache cache = getCache();
            Object o = null;
            if (cache != null) {
                o = cache.get(CACHE_SITE_NAME_KEY_PREFIX + siteID + definitionName);
                if (o != null) {
                    if (o.getClass().getName().equals(String.class.getName())) {
                        return null;
                    } else {
                        fieldDefinition = (JahiaFieldDefinition) o;
                    }
                }
            }
            if (fieldDefinition == null) {
                JahiaFieldsDef fieldsDef;
                if (siteID > 0) {
                    fieldsDef = dao.loadDefinition(new Integer(siteID), definitionName);
                } else {
                    fieldsDef = dao.loadDefinition(definitionName);
                }
                if (fieldsDef != null) {
                    fieldDefinition = convertToJahiaFieldDefinition(fieldsDef);
                    if (cache != null) {
                        cache.put(CACHE_ID_KEY_PREFIX + fieldDefinition.getID(), fieldDefinition);
                        cache.put(CACHE_SITE_NAME_KEY_PREFIX + siteID + definitionName, fieldDefinition);
                    }
                } else {
                    if (cache != null) {
                        cache.put(CACHE_SITE_NAME_KEY_PREFIX + siteID + definitionName, "");
                    }
                }

            }
            return fieldDefinition;
        } catch (JahiaException e) {
            log.warn("error during load of field definition for site " + siteID + " named " + definitionName, e);
        }
        return null;
    }

    /**
     * Returns all field def id of the given ctnName ( JahiaField Ctn Name, not Container Def name).
     *
     * @param ctnType
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getFieldDefinitionNameFromCtnType(String ctnType, boolean isMetadata) {
        return dao.getFieldDefinitionNameFromCtnType(ctnType,isMetadata);
    }

    /**
     * Returns all field def id of the given ctnName ( JahiaField Ctn Name, not Container Def name).
     *
     * @param ctnTypes
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getFieldDefinitionNameFromCtnType(String[] ctnTypes, boolean isMetadata) {
        return dao.getFieldDefinitionNameFromCtnType(ctnTypes,isMetadata);
    }

    private void convertToJahiaDefs(JahiaFieldsDef jahiaFieldsDef, JahiaFieldDefinition theDef) throws IOException,                                                                                                       JahiaException {
        if (theDef.getID()>0) {
            jahiaFieldsDef.setId(new Integer(theDef.getID()));
        }
        if (jahiaFieldsDef.getJahiaSiteId() == null && theDef.getJahiaID() != 0) {
            jahiaFieldsDef.setJahiaSiteId(new Integer(theDef.getJahiaID()));
        }
        jahiaFieldsDef.setName(theDef.getName());
        if ( theDef.getIsMetadata() ){
            jahiaFieldsDef.setIsMetadata(new Integer(1));
        } else {
            jahiaFieldsDef.setIsMetadata(new Integer(0));
        }
        if (theDef.getCtnType() != null) {
            jahiaFieldsDef.setCtnName(theDef.getCtnType());
        }
        Map<String, String> props = new HashMap<String, String>();
        props.putAll(theDef.getProperties());
        theDef.setProperties(props);
        if(jahiaFieldsDef.getProperties()!=null) {
            if (!props.equals(jahiaFieldsDef.getProperties())) {
                jahiaFieldsDef.getProperties().putAll(props);
                jahiaFieldsDef.getProperties().keySet().retainAll(props.keySet());
            }
        } else {
            jahiaFieldsDef.setProperties(theDef.getProperties());
        }

//        Set set = new HashSet(theDef.getSubDefs().size());
//        Iterator iterator = theDef.getSubDefs().entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            int pageDefID = ((Integer) entry.getKey()).intValue();
//            JahiaFieldSubDefinition subDefinition = (JahiaFieldSubDefinition) entry.getValue();
//            String defaultVal = ""; // theDef.getDefaultValue(pageDefID);
//            if (defaultVal == null || "".equals(defaultVal)) {
//                defaultVal = "<empty>";      // FIXME : Small text size should not be hardcoded
//            } else if (defaultVal.length() > 250) {
//                String filePath = ServicesRegistry.getInstance().getJahiaFieldService().composeFieldDefDefaultValueFilePath(
//                        theDef.getJahiaID(), pageDefID, theDef.getName());
//                File f = new File(filePath);
//                f.createNewFile();
//                FileUtils.writeFile(f.getAbsolutePath(), defaultVal);
//                defaultVal = "<defvalue_in_flat_file>";
//            }
//            Integer idJahiaFieldsDefProp = null;
//            if (subDefinition.getID() > 0) {
//                idJahiaFieldsDefProp = new Integer(subDefinition.getID());
//            }
//            set.add(new JahiaFieldsDefProp(new JahiaFieldsDefPropPK(new Integer(subDefinition.getPageDefID()), jahiaFieldsDef),
//                    idJahiaFieldsDefProp,
//                    new Integer(subDefinition.getType())));
//        }
//        Set existingDefs = jahiaFieldsDef.getSubDefinitions();
//        if(existingDefs!= null && !existingDefs.isEmpty()) {
//            iterator = set.iterator();
//            while (iterator.hasNext()) {
//                JahiaFieldsDefProp defProp = (JahiaFieldsDefProp) iterator.next();
//                if(!existingDefs.contains(defProp))
//                    existingDefs.add(defProp);
//                else {
//                    //find the existing prop and update it
//                    Iterator iterator2 = existingDefs.iterator();
//                    while (iterator2.hasNext()) {
//                        JahiaFieldsDefProp prop = (JahiaFieldsDefProp) iterator2.next();
//                        if(prop.equals(defProp)) {
//                            prop.setType(defProp.getType());
//                            break;
//                        }
//                    }
//                }
//            }
//        }else{
//            jahiaFieldsDef.setSubDefinitions(set);
//        }
    }

    private JahiaFieldDefinition convertToJahiaFieldDefinition(JahiaFieldsDef fieldsDef) throws JahiaException {
//        FastHashMap map = new FastHashMap(fieldsDef.getSubDefinitions().size());
        int siteID = 0;
        if (fieldsDef.getJahiaSiteId() != null) {
            siteID = fieldsDef.getJahiaSiteId().intValue();
        }
//        for (Iterator iterator = fieldsDef.getSubDefinitions().iterator(); iterator.hasNext();) {
//            JahiaFieldsDefProp defProp = (JahiaFieldsDefProp) iterator.next();
//            final JahiaFieldsDef jahiaFieldsDef = defProp.getComp_id().getJahiaFieldsDef();
//            int fieldDefID = 0;
//            if (jahiaFieldsDef != null) {
//                fieldDefID = jahiaFieldsDef.getId().intValue();
//            }
//            map.put(defProp.getComp_id().getTemplateId(),
//                    new JahiaFieldSubDefinition(defProp.getId().intValue(),
//                                                fieldDefID,
//                                                defProp.getComp_id().getTemplateId().intValue(),
//                            (fieldsDef.getIsMetadata().intValue()==1)));
//        }
//        map.setFast(true);
        JahiaFieldDefinition fieldDefinition = new JahiaFieldDefinition(fieldsDef.getId().intValue(),
                                                                        siteID,
                                                                        fieldsDef.getName(), new HashMap<Integer, JahiaFieldSubDefinition>());
        fieldDefinition.setIsMetadata(fieldsDef.getIsMetadata().intValue() == 1);
        fieldDefinition.setProperties(fieldsDef.getProperties());
        if (fieldsDef.getCtnName() != null) {
            fieldDefinition.setCtnType(fieldsDef.getCtnName());
        }

        return fieldDefinition;
    }

    public void deleteFieldDefinition(int fieldDefID) {
        JahiaFieldDefinition def = loadFieldDefinition(fieldDefID);
        if(def != null) {
            flushCache(def.getID(), def.getJahiaID(), def.getName());
            dao.delete(new Integer(fieldDefID));
        }
    }

    public List<Integer>getAllFieldDefinitionIds() {
        return dao.findAllFieldDefinitions();
    }

    private void flushCache(int definitionID,int siteID, String definitionName) {
        Cache cache =getCache();
        if(cache!=null) {
            cache.remove(CACHE_ID_KEY_PREFIX+definitionID);
            cache.remove(CACHE_SITE_NAME_KEY_PREFIX+siteID+definitionName);
        }
    }

    private Cache getCache() {
        Cache cache = cacheService.getCache(CACHE_NAME);
        if(cache==null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot create cache", e);
            }
        }
        return cache;
    }
}

