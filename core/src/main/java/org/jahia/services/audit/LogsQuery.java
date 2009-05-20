/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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