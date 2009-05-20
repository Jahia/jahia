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
package org.jahia.services.pwdpolicy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;

/**
 * Convenient utility class for converting the password policy enforcement
 * result into a {@link EngineMessages} or {@link ActionMessages} object to be
 * displayed in the front-end layer.
 * 
 * @author Sergiy Shyrkov
 */
final class PolicyEnforcementResultConvertor {

    private static final Pattern POLICY_PARAM_PATTERN = Pattern
            .compile("params\\['(.+)'\\]");

    static final ActionMessages toActionMessages(
            PolicyEnforcementResult evalResult) {
        ActionMessages msgs = new ActionMessages();

        for (Iterator msgIterator = toEngineMessages(evalResult).getMessages()
                .iterator(); msgIterator.hasNext();) {
            EngineMessage engineMsg = (EngineMessage) msgIterator.next();
            msgs.add("password", new ActionMessage(engineMsg.getKey(),
                    engineMsg.getValues()));
        }

        return msgs;
    }

    static final EngineMessages toEngineMessages(
            PolicyEnforcementResult evalResult) {
        EngineMessages msgs = new EngineMessages();

        List rules = evalResult.getViolatedRules();
        for (Iterator ruleIterator = rules.iterator(); ruleIterator.hasNext();) {
            JahiaPasswordPolicyRule theRule = (JahiaPasswordPolicyRule) ruleIterator
                    .next();
            List params = theRule.getActionParameters();
            if (params.size() > 0) {
                Map paramMap = theRule.getActionParametersValues();
                String msgKey = (String) paramMap.get("message");
                if (msgKey == null) {
                    // get the first one in the list
                    msgKey = ((JahiaPasswordPolicyRuleParam) params.get(0))
                            .getValue();
                }
                List arguments = new LinkedList();
                for (int i = 0; i < params.size() - 1; i++) {
                    arguments.add(paramMap.get("arg" + i));
                }
                while (arguments.size() > 0
                        && arguments.get(arguments.size() - 1) == null) {
                    arguments.remove(arguments.size() - 1);
                }
                if (arguments.size() > 0) {
                    // evaluate arguments first
                    List argValues = new LinkedList();
                    Map condParams = theRule.getConditionParametersValues();
                    for (Iterator iterator = arguments.iterator(); iterator
                            .hasNext();) {

                        String argParam = (String) iterator.next();
                        String value = argParam;

                        if (argParam != null) {
                            Matcher argMatcher = POLICY_PARAM_PATTERN
                                    .matcher(argParam);
                            if (argMatcher.find()
                                    && argMatcher.groupCount() > 0) {
                                String condParamName = argMatcher.group(1);
                                if (condParams.containsKey(condParamName)) {
                                    value = (String) condParams
                                            .get(condParamName);
                                }
                            }
                        }
                        argValues.add(value);
                    }

                    msgs.add(new EngineMessage(msgKey, argValues.toArray()));
                } else {
                    msgs.add(new EngineMessage(msgKey));
                }
            }
        }

        return msgs;
    }

}
