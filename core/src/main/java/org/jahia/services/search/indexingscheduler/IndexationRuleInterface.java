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
