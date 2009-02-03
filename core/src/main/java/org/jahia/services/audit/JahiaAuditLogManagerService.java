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

// MJ 19.02.2001
//
//
//


package org.jahia.services.audit;

import org.jahia.data.JahiaDOMObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

import java.util.List;
import java.util.Map;


/**
 * The audit log manager takes care of writing audit log entries for any events that should
 * be logged and to retrieve those entries for display or sending as a file.
 *
 *
 * @author Mikha�l Janson
 * @version 1.0
 */

public abstract class JahiaAuditLogManagerService extends JahiaService {

    /**
     * standard access method to log an Event
     *
     * @param   je              a reference to the JahiaEvent object to log
     * @param   objectType      an <code>int</code> representing the type of the of the logged event
     * @param   operationStr    a <code>String</code> containing the message logged with the event
     */
    public abstract boolean logEvent (JahiaEvent je, int objectType, String operationStr);


    /**
     * utility method to retrieve log entries for a specific object type and ID
     *
     * @param   objectType  an <code>int</code> representing the object type to retrieve
     * @param   objectID    an <code>int</code> representing the ID of the object to retrieve logs for
     * @return              a <code>List</code> of HashMaps containing keys for time, username, operation, and their value for each logged event
     */
    public abstract List getLog (int objectType, int objectID, ProcessingContext jParams);


    /**
     * utility method to retrieve all log entries
     *
     * @param fromDate the date from which to retrieve the logs. Set to 0 if you
     * want to retrieve all the log entries, but be aware that it might be *really*
     * long !
     * @return              a <code>List</code> of HashMaps containing keys for time, username, operation, and their value for each logged event
     */
    public abstract List getLog (long fromDate, ProcessingContext jParams);

    /**
     * utility method to flush log entries for a specific object type and  ID
     *
     * @param   objectType  an <code>int</code> representing the object type to retrieve
     * @param   objectID    an <code>int</code> representing the ID of the object to retrieve logs for
     * @param   jParams     a <code>ProcessingContext</code> object
     * @return              a <code>List</code> of HashMaps containing keys for time, username, operation, and their value for each logged event
     */
    public abstract int flushLogs (int objectType, int objectID, ProcessingContext jParams);

    /**
     * utility method to flush all log entries
     *
     * @param   theUser     a reference to the JahiaUser object representing the administrator user requesting the flush
     * @return  true on success, false on any error
     */
    //public abstract boolean flushLogs ( JahiaUser theUser );

    /**
     * utility method to flush all log entries older than a given number of days
     *
     * @param   theUser     a reference to the JahiaUser object representing the administrator user requesting the flush
     * @return  true on success, false on any error
     */
    public abstract boolean flushLogs (JahiaUser theUser, Integer maxlogsdays);

    /**
     * utility method to flush all log entries of a site
     *
     * @param   theUser     a reference to the JahiaUser object representing the administrator user requesting the flush
     * @return  true on success, false on any error
     */
    public abstract boolean flushSiteLogs (JahiaUser theUser, String siteKey);


    /**
     * utility method to delete superfluous log entries when the maxLogs property has been set to a lower
     * value than the current number of entries
     *
     * @param   maxLogs     the new maximal value for the number of log entries
     * @return  the number of rows deleted
     */
    public abstract int enforceMaxLogs (int maxLogs);


     /**
     * delete all rows in the db
     *
     */
    public abstract int deleteAllLogs();

    /**
     * load and perform a named query
     *
     * @param queryName
     * @param parameters
     * @return
     * @throws JahiaException
     */
    public abstract List executeNamedQuery(String queryName, Map parameters) throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * return a DOM document of all logs of a site
     *
     * @param siteKey the site key
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     * @author NK
     */
    public abstract JahiaDOMObject getLogsAsDOM (String siteKey)
            throws JahiaException;

    /**
     * Return a LogsQuery instance given it's name
     *
     * @param name
     * @return
     * @throws JahiaException
     */
    public abstract LogsQuery getLogsQuery(String name) throws JahiaException;
    
} // end JahiaDBAuditLogManager



