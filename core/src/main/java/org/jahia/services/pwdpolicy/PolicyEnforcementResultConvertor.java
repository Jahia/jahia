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
