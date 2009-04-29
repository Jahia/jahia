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
