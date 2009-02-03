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

package org.jahia.services.search.indexingscheduler.impl.rule;

import org.jahia.services.search.indexingscheduler.IndexationRuleInterface;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.search.indexingscheduler.TimeRange;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.ArrayList;

/**
 * Abstract base implementation of IndexationRuleInterface.
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 10:27:59
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractIndexationRule implements IndexationRuleInterface {

    private int id = 0;
    private String namedID = "";
    private String name = "";
    private int order = 0;
    private int indexationMode = IndexationRuleInterface.SCHEDULED_INDEXATION;
    private List<TimeRange> dailyIndexationTimes = new ArrayList<TimeRange>();
    private boolean enabled = true;

    /**
     *
     */
    public AbstractIndexationRule() {
    }

    /**
     *
     * @param id
     * @param namedID
     * @param name
     */
    public AbstractIndexationRule(int id, String namedID, String name) {
        this();
        this.id = id;
        this.namedID = namedID;
        this.name = name;
    }

    /**
     *
     * @param id
     * @param namedID optional. A rule can be assigned a unique namedID
     * @param name
     * @param order
     * @param indexationMode
     * @param dailyIndexationTimes
     * @param enabled
     */
    public AbstractIndexationRule(int id, String namedID, String name, int order,
                                  int indexationMode, List<TimeRange> dailyIndexationTimes,
                                  boolean enabled) {
        this(id,namedID,name);
        this.order = order;
        this.indexationMode = indexationMode;
        this.dailyIndexationTimes = dailyIndexationTimes;
        this.enabled = enabled;
    }

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
    public abstract IndexationRuleInterface evaluate(RuleEvaluationContext ctx,
                                           IndexationRuleInterface lastMatchedRule)
    throws JahiaException;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNamedID() {
        return namedID;
    }

    public void setNamedID(String namedID) {
        this.namedID = namedID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<TimeRange> getDailyIndexationTimes() {
        return dailyIndexationTimes;
    }

    public void setDailyIndexationTimes(List<TimeRange> dailyIndexationTimes) {
        this.dailyIndexationTimes = dailyIndexationTimes;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIndexationMode() {
        return indexationMode;
    }

    public void setIndexationMode(int indexationMode) {
        this.indexationMode = indexationMode;
    }

}
