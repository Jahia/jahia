/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.pwdpolicy;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.utils.i18n.Messages;

import com.ibm.icu.text.MessageFormat;

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

    static final EngineMessages toEngineMessages(
            PolicyEnforcementResult evalResult) {
        EngineMessages msgs = new EngineMessages();

        List<JahiaPasswordPolicyRule> rules = evalResult.getViolatedRules();
        for (JahiaPasswordPolicyRule theRule : rules) {
        	List<JahiaPasswordPolicyRuleParam> params = theRule.getActionParameters();
            if (params.size() > 0) {
                Map<String, String> paramMap = theRule.getActionParametersValues();
                String msgKey = (String) paramMap.get("message");
                if (msgKey == null) {
                    // get the first one in the list
                    msgKey = ((JahiaPasswordPolicyRuleParam) params.get(0))
                            .getValue();
                }
                List<String> arguments = new LinkedList<String>();
                for (int i = 0; i < params.size() - 1; i++) {
                    arguments.add(paramMap.get("arg" + i));
                }
                while (arguments.size() > 0
                        && arguments.get(arguments.size() - 1) == null) {
                    arguments.remove(arguments.size() - 1);
                }
                if (arguments.size() > 0) {
                    // evaluate arguments first
                    List<String> argValues = new LinkedList<String>();
                    Map<String, String> condParams = theRule.getConditionParametersValues();
                    for (String argParam : arguments) {
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

    static final List<String> toText(PolicyEnforcementResult evalResult) {
    	List<String> texts = new LinkedList<String>();
        for (EngineMessage msg : toEngineMessages(evalResult).getMessages()) {
        	texts.add(getMsg(msg.getKey(), msg.getValues()));
        }

        return texts;
    }
    
    private static String getMsg(String key, Object[] args) {
    	String msg = Messages.getInternal(key, Locale.ENGLISH);
    	if (args != null && args.length > 0 && msg != null && msg.contains("{0}")) {
    		msg = MessageFormat.format(msg, args);
    	}
    	
    	return msg;
    }
}
