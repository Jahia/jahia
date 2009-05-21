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
package org.jahia.services.audit;

import org.apache.commons.lang.StringUtils;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaAuditLogManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaObjectTool;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class JahiaDBAuditLogManagerService extends JahiaAuditLogManagerService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaDBAuditLogManagerService.class);

    public static final String TABLE_NAME = "jahia_audit_log";
    public static final int FIELD_TYPE = JahiaObjectTool.FIELD_TYPE;
    public static final int CONTAINER_TYPE = JahiaObjectTool.CONTAINER_TYPE;
    public static final int CONTAINERLIST_TYPE = JahiaObjectTool.CONTAINERLIST_TYPE;
    public static final int PAGE_TYPE = JahiaObjectTool.PAGE_TYPE;
    public static final int TEMPLATE_TYPE = JahiaObjectTool.TEMPLATE_TYPE;
    public static final int ACL_TYPE = JahiaObjectTool.ACL_TYPE;

    public static final String ENTRY_ID = "ENTRY_ID";
    public static final String TIME = "TIME";
    public static final String USER_ID = "USER_ID";
    public static final String OBJECT_TYPE = "CONTENT_TYPE";
    public static final String OBJECT_ID = "OBJECT_ID";
    public static final String CONTENT = "CONTENT";

    private static final String MSG_INTERNAL_ERROR = new String ("Audit Log Manager internal error");


    private static JahiaDBAuditLogManagerService instance = null;


    private JahiaAuditLogManager logManager = null;

    public void setLogManager(JahiaAuditLogManager logManager) {
        this.logManager = logManager;
    }

    public JahiaAuditLogManager getLogManager() {
        return logManager;
    }

    /**
     * constructor
     * initializes database connection pool and incrementor service.
     *
     */
    protected JahiaDBAuditLogManagerService () {
    } // end constructor


    /**
     * returns a single instance of the object
     */
    public static synchronized JahiaDBAuditLogManagerService getInstance () {
        if (instance == null) {
            instance = new JahiaDBAuditLogManagerService ();
        }
        return instance;
    }

    public void start() {}

    public void stop() {}

    /**
     * standard access method to log an Event
     *
     * @param   je              a reference to the JahiaEvent object to log
     * @param   objectType      an <code>int</code> representing the type of the of the logged event
     * @param   operationStr    a <code>String</code> containing the message logged with the event
     */
    public boolean logEvent (JahiaEvent je, int objectType, String operationStr) {
        try {
            ProcessingContext jParams = je.getProcessingContext();
            if (jParams != null) {                                       // jParams is null in Event if generated by Jahia
                String userNameStr = jParams.getUser ().getUsername ();
                String siteKey = jParams.getSiteKey();
                if (null == siteKey) {
                    JahiaSite site = (JahiaSite)jParams.getSessionState ().getAttribute (ProcessingContext.SESSION_SITE);
                    if (site != null) {
                        siteKey = site.getSiteKey ();
                    }
                }
                String objTypeStr = Integer.toString(objectType);
                int objId = getObjectID(je, objectType);
                String[] parent = getParent(je, objectType);
                String contentStr = JahiaObjectTool.getInstance ().getObjectName (objectType, objId, jParams);
                if (contentStr.length() > 250) {
                    contentStr =  contentStr.substring(0, 247) + "...";
                }
                return logManager.insertAuditLog(0,
                        new Long(je.getEventTime()), userNameStr, objTypeStr,
                        Integer.toString(objId), parent[0], parent[1], siteKey,
                        operationStr, contentStr, jParams.getStartTime());
            }
            return false;
        } catch (Exception jex) {
            logger.warn("Error creating audit log entry", jex);
            return false;
        }
    } // end logEvent


    /**
     * utility method to retrieve log entries for a specific object type and ID
     *
     * @param   objectType  an <code>int</code> representing the object type to retrieve
     * @param   objectID    an <code>int</code> representing the ID of the object to retrieve logs for
     * @return              a <code>List</code> of HashMaps containing keys for time, username, operation, and their value for each logged event
     */
    public List<Map<String, Object>> getLog (int objectType, int objectID, ProcessingContext jParams) {
        // Have the recursive method descend down the tree
        List<Integer[]> childrenObjectList = logManager.getAllChildren (objectType, objectID, null);
        return logManager.getLogs(objectType,objectID,childrenObjectList,jParams);

    } // end getLog


    /**
     * utility method to retrieve all log entries
     *
     * @param fromDate the date from which to retrieve the logs. Set to 0 if you
     * want to retrieve all the log entries, but be aware that it might be *really*
     * long !
     * @return  a <code>List</code> of HashMaps containing keys for time, username,
     *          operation, and their value for each logged event
     */
    public List<Map<String, Object>> getLog (long fromDate, ProcessingContext jParams) {
        return logManager.getLogs(fromDate, jParams);
    } // end getLog

    /**
     * utility method to flush log entries for a specific object type and  ID
     *
     * @return              the number of rows deleted, as an <code>int</code>
     */
    public int flushLogs (int objectType, int objectID, ProcessingContext jParams) {
        List<Integer[]> childrenObjectList = logManager.getAllChildren (objectType, objectID, null);
        int result = logManager.flushLogs(objectType,objectID,childrenObjectList);
        if (result > 0) {

            // if flush succeeded, log the flush itself ;o)
            // Get the next available entry ID
            int entryID = 0;
            Long time = new Long(System.currentTimeMillis());
            String userNameStr = jParams.getUser().getUsername();
            String objectTypeStr = Integer.toString(objectType);
            String objectIDStr = Integer.toString(objectID);
            String siteKey = "";
            String operationStr = "flushed logs ";

            // looks nicer with the name of the objecttype the logs are flushed for
            try {
                operationStr += JahiaObjectTool.getInstance().getObjectTypeName(objectType);
                siteKey = jParams.getSiteKey();
                if (null == siteKey) {
                    JahiaSite site = (JahiaSite) jParams.getSessionState().getAttribute(ProcessingContext.SESSION_SITE);
                    if (site != null) {
                        siteKey = site.getSiteKey();
                    }
                }
            } catch (JahiaException je) {
                ;// do nothing... keep existing operationStr
            }
//                try {
//                    siteKey       = ((JahiaSite)jParams.getSession().getAttribute(ProcessingContext.SESSION_SITE)).getSiteKey();
//                } catch (JahiaSessionExpirationException jsee ) {
//                    ;
//                }
            logManager.insertAuditLog(entryID, time, userNameStr, objectTypeStr, objectIDStr, "0", "0", siteKey,
                                      operationStr, "",jParams.getStartTime());
        }
        return result;
    } // end flushLogs


    /**
     * overloaded method to flush all log entries older than a given number of days
     *
     * @param   theUser     a reference to the JahiaUser object representing the user requesting the flush
     * @param   maxlogsdays the number of days to keep existing log entries for
     * @return              true on success, false otherwise
     */
    public boolean flushLogs (JahiaUser theUser, Integer maxlogsdays) {

        String oldestEntryTime = "999999999999999";

        if (maxlogsdays != null) {
            oldestEntryTime = Long.toString (System.currentTimeMillis () - (maxlogsdays.intValue () * 86400000L));
            oldestEntryTime = padTimeString (oldestEntryTime);
        }
        logManager.flushLogs(oldestEntryTime);
                // Get the next available entry ID
        int entryID = 0;
        Long time = new Long((new java.util.Date()).getTime());
        String userNameStr = theUser.getUsername();
        String objectTypeStr = Integer.toString(JahiaObjectTool.SERVER_TYPE);
        String objectIDStr = "0";
        String operationStr = "";
        if (maxlogsdays != null) {
            operationStr = "flushed logs > " + maxlogsdays + " days";
        } else {
            operationStr = "flushed all logs ";
        }

        logManager.insertAuditLog(entryID, time, userNameStr, objectTypeStr,
                                  objectIDStr, "0", "0", "", operationStr, "",System.currentTimeMillis());

        return true;
    } // end flushLogs ( user, maxlogsdays )


    /**
     * utility method to flush all log entries of a site
     *
     * @param   theUser     a reference to the JahiaUser object representing the administrator user requesting the flush
     * @return  true on success, false on any error
     */
    public boolean flushSiteLogs (JahiaUser theUser, String siteKey) {

        logManager.flushSiteLogs(siteKey);
        return true;
    } // end flushSiteLogs ( user, siteID )


    /**
     * get the object ID  for an Event to be logged, according to the object type
     *
     * @param   je              a reference to the JahiaEvent object to log
     * @param   objectType      an <code>int</code> representing the type of the of the logged event
     * @return  the objectID, as an <code>int</code>
     */
    private int getObjectID(JahiaEvent je, int objectType)
            throws JahiaException {
        int id = 0;
        try {
            if (je.getObject() instanceof ContentObjectKey) {
                id = ((ContentObjectKey) je.getObject()).getIdInType();
            } else {
                switch (objectType) {
                case FIELD_TYPE:
                    id = ((JahiaField) je.getObject()).getID();
                    break;
                case CONTAINER_TYPE:
                    id = ((JahiaContainer) je.getObject()).getID();
                    break;
                case CONTAINERLIST_TYPE:
                    id = ((JahiaContainerList) je.getObject()).getID();
                    break;
                case PAGE_TYPE:
                    id = ((JahiaPage) je.getObject()).getID();
                    break;
                case TEMPLATE_TYPE:
                    id = ((JahiaPageDefinition) je.getObject()).getID();
                    break;
                default:
                    throw new JahiaException(
                            MSG_INTERNAL_ERROR,
                            "Incompatible Object Type passed to JahiaAuditLogManager",
                            JahiaException.SERVICE_ERROR,
                            JahiaException.CRITICAL_SEVERITY);
                }
            }
        } catch (Exception e) {
            logger.debug(
                    "Exception occurred while retrieving Event Object ID for object: "
                            + je.getObject(), e);
            throw new JahiaException(MSG_INTERNAL_ERROR,
                    "Exception occurred while retrieving Event Object ID",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.CRITICAL_SEVERITY, e);
        }
        return id;
    } // end getObjectID


    /**
     * get object ID Type for the parent object, by the objectID of a child
     *
     * @param   je              a reference to the JahiaEvent object to log
     * @param   objectType      an <code>int</code> representing the type of the of the logged event
     * @return  the objectID, as a <code>String</code>
     */
    private String[] getParent(JahiaEvent je, int objectType)
            throws JahiaException {
        int[] parent = null;
        try {
            if (je.getObject() instanceof ContentObjectKey) {
                int idInType = ((ContentObjectKey) je.getObject())
                        .getIdInType();
                switch (objectType) {
                case FIELD_TYPE:
                    ContentField cf = ContentField.getField(idInType);
                    if (cf.getContainerID() == 0) {
                        parent = new int[] { cf.getPageID(), PAGE_TYPE };
                    } else {
                        parent = new int[] { cf.getContainerID(),
                                CONTAINER_TYPE };
                    }
                    break;
                case CONTAINER_TYPE:
                    parent = new int[] {
                            ContentContainer.getContainer(idInType)
                                    .getParentContainerListID(),
                            CONTAINERLIST_TYPE };
                    break;
                case CONTAINERLIST_TYPE:
                    ContentContainerList containerList = ContentContainerList
                            .getContainerList(idInType);
                    if (containerList.getParentContainerID() == 0) {
                        parent = new int[] { containerList.getPageID(),
                                PAGE_TYPE };
                    } else {
                        parent = new int[] {
                                containerList.getParentContainerID(),
                                CONTAINER_TYPE };
                    }
                    break;
                case PAGE_TYPE:
                    parent = new int[] { ContentPage.getPage(idInType).getID(),
                            PAGE_TYPE };
                    break;
                default:
                    throw new JahiaException(
                            MSG_INTERNAL_ERROR,
                            "Incompatible Object Type passed to JahiaAuditLogManager",
                            JahiaException.SERVICE_ERROR,
                            JahiaException.CRITICAL_SEVERITY);
                }

            } else {
                switch (objectType) {
                case FIELD_TYPE:
                    JahiaField fld = (JahiaField) je.getObject();
                    if (fld.getctnid() == 0) {
                        parent = new int[] { fld.getPageID(), PAGE_TYPE };
                    } else {
                        parent = new int[] { fld.getctnid(), CONTAINER_TYPE };
                    }
                    break;

                case CONTAINER_TYPE:
                    parent = new int[] {
                            ((JahiaContainer) je.getObject()).getListID(),
                            CONTAINERLIST_TYPE };
                    break;

                case CONTAINERLIST_TYPE:
                    JahiaContainerList ctnList = (JahiaContainerList) je
                            .getObject();
                    if (ctnList.getParentEntryID() == 0) {
                        parent = new int[] { ctnList.getPageID(), PAGE_TYPE };
                    } else {
                        parent = new int[] { ctnList.getParentEntryID(),
                                CONTAINER_TYPE };
                    }
                    break;

                case PAGE_TYPE:
                    parent = new int[] {
                            ((JahiaPage) je.getObject()).getParentID(),
                            PAGE_TYPE };
                    break;

                case TEMPLATE_TYPE:
                    parent = new int[] { -1, TEMPLATE_TYPE };
                    break;

                default:
                    throw new JahiaException(
                            MSG_INTERNAL_ERROR,
                            "Incompatible Object Type passed to JahiaAuditLogManager",
                            JahiaException.SERVICE_ERROR,
                            JahiaException.CRITICAL_SEVERITY);
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Exception occurred while retrieving Event Object Parent",
                    e);
            throw new JahiaException(MSG_INTERNAL_ERROR,
                    "Exception occurred while retrieving Event Object Parent",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.CRITICAL_SEVERITY, e);
        }
        
        return parent != null ? new String[] { String.valueOf(parent[0]),
                String.valueOf(parent[1]) } : new String[2];
    }

    /**
     * load and perform a named query
     *
     * @param queryName
     * @param parameters
     * @return
     * @throws JahiaException
     */
    public <E> List<E> executeNamedQuery(String queryName, Map<String, Object> parameters) throws JahiaException{
        return new LinkedList<E>();
    }

    /**
     * delete the n oldest rows in the db
     *
     * @param   maxLogs     the number of rows to delete
     * @return  the number of rows deleted
     */
    public int enforceMaxLogs (int maxLogs) {
        return maxLogs != 0 ? logManager.enforceMaxLogs(maxLogs) : 0;
    }


    /**
     * delete all rows in the db
     *
     */
    public int deleteAllLogs(){
        return logManager.deleteAllLogs();
    }

    /**
     * delete the oldest row in the table
     *
     */
    public boolean deleteOldestRow () {

        logManager.deleteOldestRow();
        return true;
    } // end deleteOldestRow


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of all logs of a site
     *
     * @param siteKey the site key
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     */
    public JahiaDOMObject getLogsAsDOM (String siteKey)
            throws JahiaException {
        return null;
    }

    /**
     * Return a LogsQuery instance given it's name
     *
     * @param name
     * @return
     * @throws JahiaException
     */
    public LogsQuery getLogsQuery(String name) throws JahiaException {
        ApplicationContext springContext = SpringContextSingleton.getInstance().getContext();
        return (LogsQuery)springContext.getBean(name);
    }

    //--------------------------------------------------------------------------
    /**
     * Pad a string with initial "0"s to obtain a length of 15 positions
     *
     * @param   timeStr  the String to pad
     *
     * @return  String  the padded String
     *
     */

    private String padTimeString (String timeStr) {
        return StringUtils.leftPad(timeStr, 15, '0');
    }
}
