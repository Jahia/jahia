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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.jahia.hibernate.dao.JahiaAuditLogDAO;
import org.jahia.hibernate.dao.JahiaContainerDAO;
import org.jahia.hibernate.dao.JahiaContainerListDAO;
import org.jahia.hibernate.dao.JahiaFieldsDataDAO;
import org.jahia.hibernate.model.JahiaAuditLog;
import org.jahia.params.ProcessingContext;
import org.jahia.services.audit.LoggingEventListener;
import org.jahia.utils.JahiaObjectTool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.text.DateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 15 avr. 2005
 * Time: 11:53:22
 * To change this template use File | Settings | File Templates.
 */
public class JahiaAuditLogManager {
    private Log log = LogFactory.getLog(getClass());
    
    private JahiaAuditLogDAO dao = null;
    private JahiaContainerDAO containerDAO = null;
    private JahiaFieldsDataDAO fieldsDAO = null;
    private JahiaContainerListDAO listDAO = null;
    private static final String START_TIME = "starttime";

    public void setJahiaAuditLogDAO(JahiaAuditLogDAO dao) {
        this.dao = dao;
    }

    public void setJahiaContainerDAO(JahiaContainerDAO containerDAO) {
        this.containerDAO = containerDAO;
    }

    public void setJahiaFieldsDataDAO(JahiaFieldsDataDAO fieldsDAO) {
        this.fieldsDAO = fieldsDAO;
    }

    public void setJahiaContainerListDAO(JahiaContainerListDAO listDAO) {
        this.listDAO = listDAO;
    }

    public boolean insertAuditLog(int entryID, Long time, String userNameStr, String objTypeStr, String objIDStr,
                                  String parentObjIDStr, String parentObjTypeStr, String siteKey, String operationStr,
                                  String contentStr, long startTime) {
        JahiaAuditLog auditLog = new JahiaAuditLog(null, time, userNameStr, new Integer(objTypeStr),
                                              new Integer(objIDStr), new Integer(parentObjTypeStr),
                                              new Integer(parentObjIDStr), operationStr, siteKey, contentStr);
        try {
            auditLog.setEventType(START_TIME);
            Long in = new Long(startTime);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(in);
            objectOutputStream.close();
            auditLog.setEventInformation(Hibernate.createBlob(baos.toByteArray()));
        } catch (IOException e) {
            log.error("Error serializing object", e);
        }
        dao.save(auditLog);
        return false;
    }
 
    public List<Integer[]> getAllChildren(int objectType, int objectID, List<Integer[]> parents) {
        List<Integer[]> fullChildrenList = parents == null ? new FastArrayList(103)
                : parents;
        List<Integer[]> tempChildrenList = getChildrenList(objectType, objectID);
        for (Iterator<Integer[]> it = tempChildrenList.iterator(); it.hasNext();) {
            Integer[] newChild = (Integer[]) it.next();
            fullChildrenList.add(newChild);
            Integer newObjType = newChild[0];
            Integer newObjID = newChild[1];
            getAllChildren(newObjType.intValue(), newObjID.intValue(),
                    fullChildrenList);
        }
        if (fullChildrenList instanceof FastArrayList) {
            ((FastArrayList)fullChildrenList).setFast(true);
        }
        return fullChildrenList;
    }

    private List<Integer[]> getChildrenList(int objectType, int objectID) {
        List list = null;
        List<Integer[]> retList = new FastArrayList(103);
        Integer integer;
        switch (objectType) {

            // no Children...
            case LoggingEventListener.FIELD_TYPE:
                break;

                // Children can be CONTAINER...
            case LoggingEventListener.CONTAINERLIST_TYPE:
                list = containerDAO.getAllContainerIdsFromList(new Integer(objectID));
                integer = new Integer(JahiaObjectTool.CONTAINER_TYPE);
                fillListWithArrayOfIntegers(list, integer, retList);
                break;

                // Children can be FIELD or CONTAINERLIST...
            case LoggingEventListener.CONTAINER_TYPE:
                list = fieldsDAO.findNonDeletedFieldsIdInContainer(new Integer(objectID));
                // child found in fields...
                if (!list.isEmpty()) {
                    integer = new Integer(JahiaObjectTool.FIELD_TYPE);
                    fillListWithArrayOfIntegers(list, integer, retList);
                    // no child found in fields... look in container lists...
                } else {
                    list = listDAO.getNonDeletedContainerListIdsInContainer(new Integer(objectID));
                    integer = new Integer(JahiaObjectTool.CONTAINERLIST_TYPE);
                    fillListWithArrayOfIntegers(list, integer, retList);
                }
                break;

                // Children can be PAGE, FIELD or CONTAINERLIST...
            case LoggingEventListener.PAGE_TYPE:
                list = fieldsDAO.findNonDeletedFieldsIdInPage(new Integer(objectID));
                // child found in fields...

                integer = new Integer(JahiaObjectTool.FIELD_TYPE);
                fillListWithArrayOfIntegers(list, integer, retList);

                list = listDAO.getNonDeletedContainerListIdsInPage(new Integer(objectID));
                integer = new Integer(JahiaObjectTool.CONTAINERLIST_TYPE);
                fillListWithArrayOfIntegers(list, integer, retList);
                break;
        }
        if (retList instanceof FastArrayList) {        
            ((FastArrayList)retList).setFast(true);
        }
        return retList;
    }

    private void fillListWithArrayOfIntegers(List list, Integer integer, List<Integer[]> retList) {
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object object = it.next();
            Integer id = (object instanceof Integer ? (Integer) object
                    : (Integer) ((Object[]) object)[0]);

            Integer[] child = new Integer[2];
            child[0] = integer;
            child[1] = id;
            if (!retList.contains(child)) {
                retList.add(child);
            }
        }
    }

    public List<Map<String, Object>>  getLogs(int objectType, int objectID, List<Integer[]> childrenObjectList, ProcessingContext processingContext) {
        List<JahiaAuditLog> list = dao.getLogs(new Integer(objectType), new Integer(objectID), childrenObjectList);
        return fillList(list, processingContext);
    }

    private List<Map<String, Object>>  fillList(List<JahiaAuditLog> list, ProcessingContext processingContext) {
        List<Map<String, Object>> retList = new FastArrayList(list.size());
        DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(3, 3, processingContext.getCurrentLocale());
        for (Iterator<JahiaAuditLog> it = list.iterator(); it.hasNext();) {
            JahiaAuditLog auditLog = (JahiaAuditLog) it.next();
            String time;
            Long myTime = auditLog.getTime();
            java.util.Date myDate = new java.util.Date(myTime.longValue());
            time = dateTimeInstance.format(myDate);
            Map<String, Object> map = new FastHashMap(7);
            map.put("timeStr", time);
            map.put("time",myTime);
            map.put("username", auditLog.getUsername());
            map.put("operation", auditLog.getOperation());
            map.put("objecttype", auditLog.getObjecttype().toString());
            map.put("objectid", auditLog.getObjectid().toString());
            map.put("sitekey", auditLog.getSite());
            map.put("objectname", auditLog.getContent());
            map.put("parentid", auditLog.getParentid().toString());
            try {
                map.put("parenttype", JahiaObjectTool.getInstance().getObjectTypeName(auditLog.getParenttype().intValue()));
                // deactivated the parentname resolution, as it is *really* slow !
                //map.put("parentname", JahiaObjectTool.getInstance().getObjectName(log.getParenttype().intValue(), log.getParentid().intValue(), processingContext));
                map.put("parentname", auditLog.getParentid());
            } catch (Exception e) {
                map.put("parentname", auditLog.getParentid());
            }
            try {
                Blob eventInformation = auditLog.getEventInformation();
                if (eventInformation != null) {
                    ObjectInputStream is = new ObjectInputStream(eventInformation.getBinaryStream());
                    Object o = is.readObject();
                    if (auditLog.getEventType().equals(START_TIME))
                        map.put(START_TIME, o);
                }
            } catch (Exception e) {
                log.error("Error getting serialized object", e);
            }
            map.put("id", auditLog.getId());
            if (map instanceof FastHashMap) {
                ((FastHashMap)map).setFast(true);
            }
            retList.add(map);
        }
        Collections.sort(retList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                //first order by site
                /*
                String site1 = (String) map1.get("sitekey");
                String site2 = (String) map2.get("sitekey");
                int result = (site1.compareTo(site2));
                if(result!=0) return result;
                */
                Long start1 = (Long) map1.get(START_TIME);
                Long start2 = (Long) map2.get(START_TIME);
                if (start1 != null && start2 != null && !start1.equals(start2)) {
                    return -(start1.compareTo(start2));
                } else {
                    Long id1 = (Long) map1.get("time");
                    Long id2 = (Long) map2.get("time");
                    return -(id1.compareTo(id2));
                }
            }
        });
        if (retList instanceof FastArrayList){
            ((FastArrayList)retList).setFast(true);
        }
//        Collections.sort(retList, new Comparator() {
//            public int compare(Object o1, Object o2) {
//                Map map1 = (Map) o1;
//                Map map2 = (Map) o2;
//                //first order by site
//                String site1 = (String) map1.get("sitekey");
//                String site2 = (String) map1.get("sitekey");
//                return (site1.compareTo(site2));
//            }
//        });
        return retList;
    }

    public List<Map<String, Object>> getLogs(long fromDate, ProcessingContext processingContext) {
        List<JahiaAuditLog> list = dao.getLogs(fromDate);
        return fillList(list, processingContext);
    }

    public int flushLogs(int objectType, int objectID, List<Integer[]> childrenObjectList) {
        return dao.flushLogs(new Integer(objectType), new Integer(objectID), childrenObjectList);
    }

    public void flushLogs(String oldestEntryTime) {
        dao.flushLogs(new Long(oldestEntryTime));
    }

    public void flushSiteLogs(String siteKey) {
        dao.flushSiteLogs(siteKey);
    }

    public int enforceMaxLogs(int maxLogs) {
        return dao.enforceMaxLogs(maxLogs);
    }

    public int deleteAllLogs(){
        return dao.deleteAllLogs();
    }

    public void deleteOldestRow() {
        dao.deleteOldestRow();
    }

    public List<Object[]> executeCriteria(DetachedCriteria criteria, int maxResultSet){
        return dao.executeCriteria(criteria, maxResultSet);
    }

    public <E> List<E> executeNamedQuery(String queryName, Map<String, Object> parameters) {
        return dao.executeNamedQuery(queryName, parameters);
    }
}
