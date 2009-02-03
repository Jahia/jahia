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

package org.jahia.services.audit;

import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 d�c. 2007
 * Time: 15:16:22
 * To change this template use File | Settings | File Templates.
 */
public interface LogsQuery {

    /**
     * Subclass should implements and provide specific initialization
     *
     * @param context
     * @throws JahiaException
     */
    public void init(ProcessingContext context) throws JahiaException;

    /**
     * performs the query and returns the result.
     *
     * @param context
     * @return
     * @throws JahiaException
     */
    public LogsResultList<LogEntryItem> executeQuery(ProcessingContext context) throws JahiaException;

    /**
     * Returns a previously cached result
     * @return
     */
    public List<LogEntryItem> getResult();

    /**
     * define the db max limit to use for querying the logs.
     *
     * @param dbMaxLimit
     */
    public void setDBMaxLimit(int dbMaxLimit);

    /**
     * return the db max limit to use for querying the logs.
     *
     */
    public int getDBMaxLimit();

    /**
     * define the max LogEntryItem to keep from the returned resultset
     *
     * @param maxSize
     */
    public void setMaxSize(int maxSize);

    /**
     * return the max LogsEntryItem
     *
     * @return
     */
    public int getMaxSize();

    /**
     * Set the time based publishing load flags used to filter out content based on their time based
     * publishing state.
     * @see org.jahia.content.TimeBasedPublishingState
     * @param loadFlag
     */
    public void setTimeBasedPublishingLoadFlag(int loadFlag);

    public int getTimeBasedPublishingLoadFlag();

    public void disableTimeBasedPublishingCheck();

    /**
     * if true, only the first entrylog for each ContentObject will be returned
     * It is used to filter multiple log entries with same content object but with differents users
     *
     * @param uniqueContentObject
     */
    public void setUniqueContentObject(boolean uniqueContentObject);

    public boolean getUniqueContentObject();

    /**
     * enable or disable acl check
     *
     * @param checkACL
     */
    public void setCheckACL(boolean checkACL);

    public boolean getCheckACL();

    /**
     * Set limitation to the given definition names
     *
     * @param definitionNames
     */
    public void setContentDefinitionNames(List<String> definitionNames);

    public List<String> getContentDefinitionNames();

    /**
     * Set limitation to the given username
     *
     * @param usernames
     */
    public void setUsernames(List<String> usernames);

    public List<String> getUsernames();

}