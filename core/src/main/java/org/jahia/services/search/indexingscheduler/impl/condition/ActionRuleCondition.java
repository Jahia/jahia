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
package org.jahia.services.search.indexingscheduler.impl.condition;

import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.ArrayList;

/**
 * A Rule that matches agains the action being performed
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 15:05:48
 * To change this template use File | Settings | File Templates.
 */
public class ActionRuleCondition implements RuleCondition {

    public static final String ADD_ENGINE       = "ADD_ENGINE";
    public static final String UPDATE_ENGINE    = "UPDATE_ENGINE";
    public static final String DELETE_ENGINE    = "DELETE_ENGINE";
    public static final String STORE_FORM_IN_TEMPLATE = "STORE_FORM_IN_TEMPLATE";    
    public static final String COPY_JOB         = "COPY_JOB";    
    public static final String MANUAL_ACTION    = "MANUAL_ACTION";

    public static final List<String> ALL_ACTIONS_LIST;

    static {
        ALL_ACTIONS_LIST = new ArrayList<String>();
        ALL_ACTIONS_LIST.add(ADD_ENGINE);
        ALL_ACTIONS_LIST.add(UPDATE_ENGINE);
        ALL_ACTIONS_LIST.add(DELETE_ENGINE);
        ALL_ACTIONS_LIST.add(MANUAL_ACTION);
        ALL_ACTIONS_LIST.add(STORE_FORM_IN_TEMPLATE);        
    }
    /**
     * If true, match all action
     */
    private boolean allowAll = false;

    private List<String> allowedActions = new ArrayList<String>();

    public ActionRuleCondition() {
    }

    public ActionRuleCondition(List<String> matchedActions) {
        this();
        this.allowedActions = matchedActions;
    }

    public boolean evaluate(RuleEvaluationContext ctx) throws JahiaException {
        if ( allowAll && ctx.getActionPerformed() != null && 
            ALL_ACTIONS_LIST.contains(ctx.getActionPerformed())){
            return true;
        }
        if (allowedActions == null || allowedActions.isEmpty()){
            return false;
        }
        String actionPerformed = ctx.getActionPerformed();
        boolean isManualActionPerformed = ctx.isManualActionPerformed();
        for (String actionPattern : allowedActions){
            if (actionPattern.equals(actionPerformed) ||
                    (isManualActionPerformed && actionPattern.equals(MANUAL_ACTION))){
                return true;
            }
        }
        return false;
    }

    public List<String> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<String> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public boolean isAllowAll() {
        return allowAll;
    }

    public void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }

}
