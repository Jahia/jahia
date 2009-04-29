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
