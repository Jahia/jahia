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
package org.jahia.services.search.indexingscheduler;

import java.util.List;

import org.jahia.exceptions.JahiaException;

/**
 * Indexation Rule interface
 * Each Indexation Rule defines the indexation policy ( like delaying indexation )
 * based on criterias like ContentType, Action being performed,...
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 09:49:31
 */
public interface IndexationRuleInterface {

    /**
     * this mode is used to skip indexing matched content
     */
    public static final int DONT_INDEX = 0;

    /**
     * this mode is used to queue the indexation of matched content as soon as possible
     */
    public static final int INDEX_IMMEDIATELY = 1;

    /**
     * this mode is used when the indexation is scheduled at specified times.
     */
    public static final int SCHEDULED_INDEXATION = 2;

    /**
     * Implementing classe should return itsef if it matches the given ContentObject
     * and action performed. If it doesn't match, then it must return the last matched
     * rule.
     *
     * @param ctx
     * @param lastMatchedRule
     * @return
     * @throws JahiaException
     */
    public IndexationRuleInterface evaluate(RuleEvaluationContext ctx,
                                           IndexationRuleInterface lastMatchedRule)
    throws JahiaException;


    /**
     * Returns the rule identifier
     * @return
     */
    public int getId();

    public void setId(int id);

    /**
     * Returns the rule name.
     * @return
     */
    public String getName();

    public void setName(String name);

    /**
     * Returns the namedID.
     * A Rule can be uniquely identified by a namedID
     * @return
     */
    public String getNamedID();

    public void setNamedID(String namedID);

    /**
     * Returns the rule order
     * @return
     */
    public int getOrder();

    public void setOrder(int order);

    /**
     * Returns a list of daily indexation range of time defined for this rule.
     * @return return a list of TimeRange instance
     */
    public List<TimeRange> getDailyIndexationTimes();

    public void setDailyIndexationTimes(List<TimeRange> dailyIndexationTimes);

    /**
     * Returns true if this rule is enabled or false if it's disabled
     * @return
     */
    public boolean enabled();

    public void setEnabled(boolean enabled);

    /**
     * A rule ca be set to one of these 3 modes, don't index,
     * index immediately or schedule indexation.
     *
     * Return
     * @return
     */
    public int getIndexationMode();

    public void setIndexationMode(int mode);
}
