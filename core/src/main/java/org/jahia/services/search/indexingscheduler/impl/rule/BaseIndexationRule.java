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
import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.search.indexingscheduler.TimeRange;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.Iterator;

/**
 * Default base implementation of IndexationRuleInterface.
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 10:27:59
 * To change this template use File | Settings | File Templates.
 */
public class BaseIndexationRule extends AbstractIndexationRule {

    private List<RuleCondition> conditions;

    /**
     *
     */
    public BaseIndexationRule() {
        super();
    }

    /**
     *
     * @param id
     * @param namedID
     * @param name
     */
    public BaseIndexationRule(int id, String namedID, String name) {
        super(id,namedID,name);
    }

    /**
     *
     * @param id
     * @param namedID
     * @param name
     * @param order
     * @param indexationMode
     * @param dailyIndexationTimes
     * @param enabled
     * @param conditions
     */
    public BaseIndexationRule(int id, String namedID, String name, int order,
                              int indexationMode, List<TimeRange> dailyIndexationTimes, boolean enabled,
                              List<RuleCondition> conditions) {
        super(id,namedID,name,order,indexationMode,dailyIndexationTimes,enabled);
        this.conditions = conditions;
    }

    /**
     * Implementing classe should return itsef if it matches the given ContentObject
     * and action performed. If it doesn't match, then it must return the last matched
     * rule.
     *
     * @param ctx
     * @param lastMatchedRule
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    public IndexationRuleInterface evaluate(RuleEvaluationContext ctx,
                                           IndexationRuleInterface lastMatchedRule)
    throws JahiaException {
        if (conditions == null || conditions.isEmpty()){
            return lastMatchedRule;
        }
        boolean result = true;
        for (Iterator<RuleCondition> it=conditions.iterator(); result && it.hasNext();){
             result = it.next().evaluate(ctx);
        }
        if (result){
            return this;
        }
        return lastMatchedRule;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }
}
