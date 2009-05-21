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
