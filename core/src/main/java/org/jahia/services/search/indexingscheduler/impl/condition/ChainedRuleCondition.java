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

package org.jahia.services.search.indexingscheduler.impl.condition;

import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.exceptions.JahiaException;

/**
 * A Rule that is in fact a chain of RuleCondition instances, allowing
 * to build advanced AND and OR logics.
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 14:08:32
 * To change this template use File | Settings | File Templates.
 */
public class ChainedRuleCondition implements RuleCondition {

    public static final int OR = 0;

    public static final int AND = 1;

    public static final int ANDNOT = 2;

    public static int DEFAULT = OR;

    /** The evaluator chain */
    private RuleCondition[] chain = null;

    private int[] logicArray;

    private int logic = -1;

    /**
     *
     * @param chain The chain of evaluators
     */
    public ChainedRuleCondition(RuleCondition[] chain)
    {
        this.chain = chain;
    }

    /**
     *
     * @param chain The chain of evaluators
     * @param logicArray Logical operations to apply between evaluators
     */
    public ChainedRuleCondition(RuleCondition[] chain, int[] logicArray)
    {
        this.chain = chain;
        this.logicArray = logicArray;
    }

    /**
     *
     * @param chain The chain of evaluators
     * @param logic Logicial operation to apply to ALL evaluators
     */
    public ChainedRuleCondition(RuleCondition[] chain, int logic)
    {
        this.chain = chain;
        this.logic = logic;
    }


    public boolean evaluate(RuleEvaluationContext ctx) throws JahiaException {

        if (logic != -1)
            return evaluate(logic,ctx);
        else if (logicArray != null)
            return evaluate(logicArray,ctx);
        else
            return evaluate(DEFAULT,ctx);
    }

    /**
     * Delegates to each evaluator in the chain.
     * @param logic Logical operation
     * @param ctx
     * @return
     * @throws JahiaException
     */
    private boolean evaluate(int logic, RuleEvaluationContext ctx) throws JahiaException
    {
        int i = 0;
        boolean result = false;
        if (logic == AND)
        {
            result = chain[i].evaluate(ctx);
            ++i;
        }
        else
        {
            result = false;
        }

        for (; i < chain.length; i++)
        {
            doChain(result, logic, chain[i], ctx);
        }
        return result;
    }

    /**
     * Delegates to each evaluator in the chain.
     * @param logic Logical operation
     * @param ctx
     * @return BitSet
     */
    private boolean evaluate(int[] logic, RuleEvaluationContext ctx) throws JahiaException
    {
        if (logic.length < chain.length-1)
            throw new IllegalArgumentException("Invalid number of elements in logic array");

        boolean result = chain[0].evaluate(ctx);

        for (int i=1; i < chain.length; i++)
        {
            doChain(result, logic[i-1], chain[i], ctx);
        }
        return result;
    }

    private void doChain(boolean result, int logic, RuleCondition condition,
                         RuleEvaluationContext ctx)
    throws JahiaException
    {
        switch (logic)
        {
            case OR:
                result = result || condition.evaluate(ctx);
                break;
            case AND:
                if (!result) // --> no need to execute next condition
                    break;
                result = condition.evaluate(ctx);
                break;
            case ANDNOT:
                if (!result) // --> no need to execute next condition
                    break;
                result = !condition.evaluate(ctx);
                break;
            default:
                doChain(result, DEFAULT, condition, ctx);
                break;
        }
    }

}
