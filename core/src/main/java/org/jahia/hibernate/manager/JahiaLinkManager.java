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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.content.*;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaLinkDAO;
import org.jahia.hibernate.model.JahiaLink;
import org.jahia.hibernate.model.JahiaXRef;
import org.jahia.hibernate.model.JahiaXRefPK;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.spring.advice.CacheAdvice;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 14 mars 2005
 * Time: 11:41:22
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLinkManager {
// ------------------------------ FIELDS ------------------------------

    private JahiaLinkDAO dao = null;
    private Log log = LogFactory.getLog(JahiaLinkManager.class);
    private static final String CACHE_NAME = "JahiaLinkManagerCache";
    private static final String CACHE_KEY_LEFTPREFIX = "JahiaLinkLeft_";
    private static final String CACHE_KEY_RIGHTPREFIX = "JahiaLinkRight_";
    private static final String CACHE_KEY_TYPEPREFIX = "JahiaLinkType_";
    private CacheService cacheService = null;
// --------------------- GETTER / SETTER METHODS ---------------------

    private List<ObjectLink> emptyList = Collections.unmodifiableList(new ArrayList<ObjectLink>());

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJahiaLinkDAO(JahiaLinkDAO dao) {
        this.dao = dao;
    }

// -------------------------- OTHER METHODS --------------------------

    public void createObjectLink(ObjectLink link) {
        if (link.getLeftObjectKey().getIdInType() > 0 && link.getRightObjectKey().getIdInType() > 0) {
            if (CrossReferenceManager.REFERENCE_TYPE.equals(link.getType())) {
                JahiaXRef jahiaLink = new JahiaXRef(new JahiaXRefPK(new Integer(link.getLeftObjectKey().getIdInType()),
                        new Integer(link.getRightObjectKey().getIdInType()),
                        new Integer(getType(link.getRightObjectKey().getType()))));
                dao.save(jahiaLink);
            } else {
                JahiaLink jahiaLink = fillJahiaLink(link);
                dao.save(jahiaLink);
                link.setID(jahiaLink.getId().intValue());
            }
            flushCache(link.getLeftObjectKey().toString(), link.getRightObjectKey().toString());
            createType(link.getType());
        }
    }
    // none
    public List<ObjectLink> findByLeftAndRightObjectKeys(ObjectKey leftObjectKey, ObjectKey rightObjectKey) {
        List<ObjectLink> objectLinks = null;
        Cache<GroupCacheKey, List<ObjectLink>> cache = cacheService.getCache(CACHE_NAME+"findByLeftAndRightObjectKeys");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"findByLeftAndRightObjectKeys");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME, e);
            }
        }
        final GroupCacheKey entryKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_LEFTPREFIX + leftObjectKey.toString(),
                CACHE_KEY_RIGHTPREFIX + rightObjectKey.toString()});
        if (cache != null) {
            objectLinks = cache.get(entryKey);
        }
        if (objectLinks == null) {
            List<JahiaLink> jahiaLinks = dao.findByLeftAndRightObjectKeys(leftObjectKey.toString(), rightObjectKey.toString());
            List<ObjectLink> tempList = new FastArrayList(jahiaLinks.size());
            for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
                JahiaLink jahiaLink = it.next();
                ObjectLink objectLink = fillObjectLink(jahiaLink);
                tempList.add(objectLink);
            }
            ((FastArrayList)tempList).setFast(true);
            objectLinks = tempList;
            if (cache != null) {
                cache.put(entryKey, objectLinks);
            }
        }
        return objectLinks;
    }
    // none
    public List<ObjectLink> findByLeftObjectKey(ObjectKey objectXRef) {
        List<ObjectLink> objectLinks = null;
        Cache<GroupCacheKey, List<ObjectLink>> cache = cacheService.getCache(CACHE_NAME+"findByLeftObjectKey");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"findByLeftObjectKey");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME, e);
            }
        }
        final GroupCacheKey entryKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_LEFTPREFIX + objectXRef.toString()});
        if (cache != null) {
            objectLinks = cache.get(entryKey);
        }
        if (objectLinks == null) {
            List<JahiaLink> jahiaLinks = dao.findByLeftObjectKey(objectXRef.toString());
            List<ObjectLink> tempList = new FastArrayList(jahiaLinks.size());
            for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
                JahiaLink jahiaLink = it.next();
                ObjectLink objectLink = fillObjectLink(jahiaLink);
                tempList.add(objectLink);
            }
            ((FastArrayList)tempList).setFast(true);
            objectLinks = tempList;
            if (cache != null) {
                cache.put(entryKey, objectLinks);
            }
        }
        return objectLinks;
    }
    // export
    public List<ObjectLink> findByRightObjectKey(ObjectKey objectKey) {
        List<ObjectLink> objectLinks = null;
        Cache<String, List<ObjectLink>> cache = cacheService.getCache(CACHE_NAME+"findByRightObjectKey");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"findByRightObjectKey");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME, e);
            }
        }
        final String entryKey = CACHE_KEY_RIGHTPREFIX + objectKey.toString();
        if (cache != null) {
            objectLinks = cache.get(entryKey);
        }
        if (objectLinks == null) {
            List<JahiaLink> jahiaLinks = dao.findByRightObjectKey(objectKey.toString());
            FastArrayList tempList = new FastArrayList(jahiaLinks.size());
            for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
                JahiaLink jahiaLink = it.next();
                ObjectLink objectLink = fillObjectLink(jahiaLink);
                tempList.add(objectLink);
            }
            tempList.setFast(true);
            objectLinks = tempList;
            if (cache != null) {
                cache.put(entryKey, objectLinks);
            }
        }
        return objectLinks;
    }
    // none
    public List<ObjectLink> findByRightAndLikeLeftObjectKey(ObjectKey objectKey, String likeLeftObjectKey) {
        List<JahiaLink> jahiaLinks = dao.findByRightAndLikeLeftObjectKey(objectKey
                .toString(), likeLeftObjectKey);
        FastArrayList objectLinks = new FastArrayList(jahiaLinks.size());
        for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
            JahiaLink jahiaLink = it.next();
            ObjectLink objectLink = fillObjectLink(jahiaLink);
            objectLinks.add(objectLink);
        }
        objectLinks.setFast(true);
        return objectLinks;
    }
    // catgeories
    public List<ObjectLink> findByTypeAndLeftAndLikeRightObjectKeys(String type, ObjectKey leftObjectKey, String rightObjectKey) {
        if (!typeExists(type)) {
            return emptyList;
        }

        List<JahiaLink> jahiaLinks = dao.findByTypeAndLeftAndLikeRightObjectKeys(
                leftObjectKey.toString(), rightObjectKey, type);
        List<ObjectLink> objectLinks = new FastArrayList(jahiaLinks.size());
        for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
            JahiaLink jahiaLink = it.next();
            ObjectLink objectLink = fillObjectLink(jahiaLink);
            objectLinks.add(objectLink);
        }
        ((FastArrayList)objectLinks).setFast(true);
        return objectLinks;
    }    
    // xref and categories
    public List<ObjectLink> findByTypeAndLeftAndRightObjectKeys(String type, ObjectKey leftObjectKey, ObjectKey rightObjectKey) {
        if (!typeExists(type)) {
            return emptyList;
        }
        List<ObjectLink> objectLinks = null;
        Cache<GroupCacheKey, List<ObjectLink>> cache = cacheService.getCache(CACHE_NAME+"findByTypeAndLeftAndRightObjectKeys");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"findByTypeAndLeftAndRightObjectKeys");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME, e);
            }
        }
        final GroupCacheKey entryKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_LEFTPREFIX + leftObjectKey.toString(),
                CACHE_KEY_RIGHTPREFIX + rightObjectKey.toString(), CACHE_KEY_TYPEPREFIX + type});
        if (cache != null) {
            objectLinks = cache.get(entryKey);
        }
        if (objectLinks == null) {
            if (CrossReferenceManager.REFERENCE_TYPE.equals(type)) {
                JahiaXRef jahiaLink = dao.getJahiaXRef(new Integer(leftObjectKey.getIdInType()), new Integer(rightObjectKey.getIdInType()), new Integer(getType(rightObjectKey.getType())));
                FastArrayList tempList = new FastArrayList(1);
                if (jahiaLink != null) {
                    tempList.add(fillObjectLink(jahiaLink));
                }
                tempList.setFast(true);
                objectLinks = tempList;
            } else {
                List<JahiaLink> jahiaLinks = dao.findByTypeAndLeftAndRightObjectKeys(leftObjectKey.toString(), rightObjectKey.toString(),
                        type);
                FastArrayList tempList = new FastArrayList(jahiaLinks.size());
                for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
                    JahiaLink jahiaLink = it.next();
                    ObjectLink objectLink = fillObjectLink(jahiaLink);
                    tempList.add(objectLink);
                }
                tempList.setFast(true);
                objectLinks = tempList;
            }
            if (cache != null) {
                cache.put(entryKey, objectLinks);
            }
        }
        return objectLinks;
    }
    // xref, pickes, metadata, bigtext hardcoded, categories
    public List<ObjectLink> findByTypeAndLeftObjectKey(String type, ObjectKey leftObjectKey) {
        if (!typeExists(type)) {
            return emptyList;
        }
        List<ObjectLink> objectLinks = null;
        Cache<GroupCacheKey, List<ObjectLink>> cache = cacheService.getCache(CACHE_NAME+"findByTypeAndLeftObjectKey");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"findByTypeAndLeftObjectKey");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME, e);
            }
        }
        final GroupCacheKey entryKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_LEFTPREFIX + leftObjectKey.toString(),
                CACHE_KEY_TYPEPREFIX + type});
        if (cache != null) {
            objectLinks = cache.get(entryKey);
        }
        if (objectLinks == null) {
            if (CrossReferenceManager.REFERENCE_TYPE.equals(type)) {
                List<JahiaXRef> jahiaLinks = dao.findJahiaXRefForPage(new Integer(leftObjectKey.getIdInType()));
                FastArrayList tempList = new FastArrayList(jahiaLinks.size());
                for (Iterator<JahiaXRef> it = jahiaLinks.iterator(); it.hasNext();) {
                    JahiaXRef jahiaLink = (JahiaXRef) it.next();
                    ObjectLink objectLink = fillObjectLink(jahiaLink);
                    tempList.add(objectLink);
                }
                tempList.setFast(true);
                objectLinks = tempList;
            } else {
                List<JahiaLink> jahiaLinks = dao.findByTypeAndLeftObjectKey(leftObjectKey.toString(), type);
                FastArrayList tempList = new FastArrayList(jahiaLinks.size());
                for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
                    JahiaLink jahiaLink = it.next();
                    ObjectLink objectLink = fillObjectLink(jahiaLink);
                    tempList.add(objectLink);
                }
                tempList.setFast(true);
                objectLinks = tempList;
            }
            if (cache != null) {
                cache.put(entryKey, objectLinks);
            }
        }
        return objectLinks;
    }
    // categories
    public List<ObjectLink> findByTypeAndRightAndLikeLeftObjectKey(String type, ObjectKey rightObjectKey, String leftObjectKey) {
        if (!typeExists(type)) {
            return emptyList;
        }

        List<JahiaLink> jahiaLinks = dao.findByTypeAndRightAndLikeLeftObjectKey(rightObjectKey.toString(), leftObjectKey, type);
        FastArrayList objectLinks = new FastArrayList(jahiaLinks.size());
        for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
            JahiaLink jahiaLink = it.next();
            ObjectLink objectLink = fillObjectLink(jahiaLink);
            objectLinks.add(objectLink);
        }
        objectLinks.setFast(true);
        return objectLinks;
    }    
    // metadata, pickers, xref, ..
    public List<ObjectLink> findByTypeAndRightObjectKey(String type, ObjectKey rightObjectKey) {
        if (!typeExists(type)) {
            return emptyList;
        }
        List<ObjectLink> objectLinks = null;
        Cache<GroupCacheKey, List<ObjectLink>> cache = cacheService.getCache(CACHE_NAME+"findByTypeAndRightObjectKey");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"findByTypeAndRightObjectKey");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME, e);
            }
        }
        final GroupCacheKey entryKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_RIGHTPREFIX + rightObjectKey.toString(),
                CACHE_KEY_TYPEPREFIX + type});
        if (cache != null) {
            objectLinks = cache.get(entryKey);
        }
        if (objectLinks == null || type.equals(StructuralRelationship.ACTIVATION_PICKER_LINK)) {
            if (CrossReferenceManager.REFERENCE_TYPE.equals(type)) {
                List<JahiaXRef> jahiaLinks = dao.findJahiaXRefForObject(new Integer(rightObjectKey.getIdInType()), new Integer(getType(rightObjectKey.getType())));
                FastArrayList tempList = new FastArrayList(jahiaLinks.size());
                for (Iterator<JahiaXRef> it = jahiaLinks.iterator(); it.hasNext();) {
                    JahiaXRef jahiaLink = it.next();
                    ObjectLink objectLink = fillObjectLink(jahiaLink);
                    tempList.add(objectLink);
                }
                tempList.setFast(true);
                objectLinks = tempList;
            } else {
                List<JahiaLink> jahiaLinks = dao.findByTypeAndRightObjectKey(rightObjectKey.toString(), type);
                FastArrayList tempList = new FastArrayList(jahiaLinks.size());
                for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
                    JahiaLink jahiaLink = it.next();
                    ObjectLink objectLink = fillObjectLink(jahiaLink);
                    tempList.add(objectLink);
                }
                tempList.setFast(true);
                objectLinks = tempList;
            }
            if (cache != null) {
                cache.put(entryKey, objectLinks);
            }
        }
        return objectLinks;
    }
    // none
    public List<ObjectLink> findByTypeAndRightObjectKey(String type, ObjectKey[] rightObjectKeys) {
        if (!typeExists(type)) {
            return emptyList;
        }

        String[] keys = new String[rightObjectKeys.length];
        for ( int i=0; i<rightObjectKeys.length; i++ ){
            keys[i] = rightObjectKeys[i].toString();
        }
        List<JahiaLink> jahiaLinks = dao.findByTypeAndRightObjectKeys(keys, type);
        FastArrayList objectLinks = new FastArrayList(jahiaLinks.size());
        for (Iterator<JahiaLink> it = jahiaLinks.iterator(); it.hasNext();) {
            JahiaLink jahiaLink = it.next();
            ObjectLink objectLink = fillObjectLink(jahiaLink);
            objectLinks.add(objectLink);
        }
        objectLinks.setFast(true);
        return objectLinks;
    }

    // none
    public ObjectLink getObjectLink(int linkID) {
        JahiaLink jahiaLink = dao.getJahiaLink(new Integer(linkID));
        return fillObjectLink(jahiaLink);
    }
    // none
    public void preloadObjectXRefsByRefObjectKey(ObjectKey childInstance) {
        dao.preloadObjectXRefsByRefObjectKey(childInstance.toString());
    }

    public void removeObjectLink(int linkId) {
        JahiaLink jahiaLink = dao.getJahiaLink(new Integer(linkId));
        flushCache(jahiaLink.getLeftOid(), jahiaLink.getRightOid());
        dao.delete(jahiaLink);
    }

    public void removeObjectLink(String type, ObjectKey leftObjectKey, ObjectKey rightObjectKey, int linkId) {
        if (CrossReferenceManager.REFERENCE_TYPE.equals(type)) {
            JahiaXRef jahiaLink = dao.getJahiaXRef(new Integer(leftObjectKey.getIdInType()), new Integer(rightObjectKey.getIdInType()), new Integer(getType(rightObjectKey.getType())));
            dao.delete(jahiaLink);
        } else {
            JahiaLink jahiaLink = dao.getJahiaLink(new Integer(linkId));
            dao.delete(jahiaLink);
        }
        flushCache(leftObjectKey.toString(), rightObjectKey.toString());
    }

    public boolean typeExists(String type) {
        if (CrossReferenceManager.REFERENCE_TYPE.equals(type)) {
            return true;
        }
        Cache<String, Boolean> cache = cacheService.getCache(CACHE_NAME+"Count");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"Count");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME+"Count", e);
            }
        }
        Boolean b = cache.get(type);
        if (b == null) {
            Integer i = dao.countType(type);
            b = Boolean.valueOf(i.intValue() > 0);
            cache.put(type, b);
        }
        return b.booleanValue();
    }

    public void createType(String type) {
        Cache<String, Boolean> cache = cacheService.getCache(CACHE_NAME+"Count");
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME+"Count");
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache " + CACHE_NAME+"Count", e);
            }
        }
        cache.put(type, Boolean.TRUE);
    }

    private void flushCache(String leftOid, String rightOid) {
        final String leftKey = CACHE_KEY_LEFTPREFIX + leftOid;
        final String rightKey = CACHE_KEY_RIGHTPREFIX + rightOid;
        Cache<String, Object> cache = cacheService.getCache(CACHE_NAME+"findByLeftAndRightObjectKeys");
        if (cache != null) {
            cache.flushGroup(leftKey);
            cache.flushGroup(rightKey);
        }
        cache = cacheService.getCache(CACHE_NAME+"findByLeftObjectKey");
        if (cache != null) {
            cache.flushGroup(leftKey);
        }
        cache = cacheService.getCache(CACHE_NAME+"findByRightObjectKey");
        if (cache != null) {
            cache.remove(rightKey);
        }
        cache = cacheService.getCache(CACHE_NAME+"findByTypeAndLeftAndRightObjectKeys");
        if (cache != null) {
            cache.flushGroup(leftKey);
            cache.flushGroup(rightKey);
        }
        cache = cacheService.getCache(CACHE_NAME+"findByTypeAndLeftObjectKey");
        if (cache != null) {
            cache.flushGroup(leftKey);
        }
        cache = cacheService.getCache(CACHE_NAME+"findByTypeAndRightObjectKey");
        if (cache != null) {
            cache.flushGroup(rightKey);
        }
    }
    // mnetadata
    public void updateObjectLink(ObjectLink objectLink) {
        int fieldId = objectLink.getID();
        if(objectLink.getLeftObjectKey().getIdInType()>0 && objectLink.getRightObjectKey().getIdInType()>0) {
            final JahiaLink jahiaLink = fillJahiaLink(objectLink);
            if (fieldId > 0) {
                jahiaLink.setId(new Integer(fieldId));
            }
            dao.save(jahiaLink);
            createType(objectLink.getType());
            objectLink.setID(jahiaLink.getId().intValue());
            flushCache(jahiaLink.getLeftOid(), jahiaLink.getRightOid());
        } else {
            if(fieldId >0)
                removeObjectLink(fieldId);
        }
    }

    private JahiaLink fillJahiaLink(ObjectLink link) {
        JahiaLink jahiaLink = new JahiaLink();
        jahiaLink.setLeftOid(link.getLeftObjectKey().toString());
        jahiaLink.setRightOid(link.getRightObjectKey().toString());
        jahiaLink.setType(link.getType());
        return jahiaLink;
    }

    private ObjectLink fillObjectLink(JahiaLink jahiaLink) {
        ObjectLink objectLink = null;
        try {
            objectLink = new ObjectLink(jahiaLink.getId().intValue(),
                                        ObjectKey.getInstance(jahiaLink.getLeftOid()),
                                        ObjectKey.getInstance(jahiaLink.getRightOid()),
                                        jahiaLink.getType(),
                    new HashMap<String, String> (2));
        } catch (ClassNotFoundException e) {
            log.error("Class Not found when converting ObjectLink", e);
        }
        return objectLink;
    }

    private ObjectLink fillObjectLink(JahiaXRef jahiaLink) {
        ObjectLink objectLink = null;
        try {
            objectLink = new ObjectLink(0,
                                        new ContentPageKey(jahiaLink.getComp_id().getPageId().intValue()),
                                        ObjectKey.getInstance(getType(jahiaLink.getComp_id().getRefType().intValue())+"_"+jahiaLink.getComp_id().getRefId()),
                                        CrossReferenceManager.REFERENCE_TYPE,
                    new HashMap<String, String> (2));
        } catch (ClassNotFoundException e) {
            log.error("Class Not found when converting ObjectLink", e);
        }
        return objectLink;
    }

    private int getType(String type )  {
        if ("ContentPage".equals(type)) {
            return 1;
        } else if ("ContentContainerList".equals(type)) {
            return 2;
        } else if ("ContentContainer".equals(type)) {
            return 3;
        } else if ("ContentField".equals(type)) {
            return 4;
        } else {
            return 0;
        }
    }

    private String getType(int type )  {
        switch (type) {
            case 1: return "ContentPage";
            case 2: return "ContentContainerList";
            case 3: return "ContentContainer";
            case 4: return "ContentField";
            default: return "";
        }
    }

}

