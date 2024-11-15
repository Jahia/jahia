/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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

import java.util.HashMap;
import java.util.Map;

/**
 * Java-based condition evaluator that expects the fully qualified class name to
 * be called. The class must implement the {@link PasswordPolicyRuleCondition}
 * interface.
 *
 * @author Sergiy Shyrkov
 */
class JavaConditionEvaluator implements ConditionEvaluator {

	private static Map<String, PasswordPolicyRuleCondition> evaluatorsCache = new HashMap<String, PasswordPolicyRuleCondition>();

	public boolean evaluate(JahiaPasswordPolicyRule rule, EvaluationContext ctx) {
		return getConditionClazz(rule.getCondition()).evaluate(
		        rule.getConditionParameters(), ctx);
	}

	private PasswordPolicyRuleCondition getConditionClazz(String condition) {

		PasswordPolicyRuleCondition condClazz = null;
		if (evaluatorsCache.containsKey(condition)) {
			condClazz = evaluatorsCache.get(condition);
		} else {
			synchronized (JavaConditionEvaluator.class) {
				if (!evaluatorsCache.containsKey(condition)) {
					try {
						condClazz = (PasswordPolicyRuleCondition) Thread
						        .currentThread().getContextClassLoader()
						        .loadClass(condition).newInstance();
						evaluatorsCache.put(condition, condClazz);
					} catch (Exception ex) {
						throw new RuntimeException(
						        "Unable to instantiate condition class "
						                + condition, ex);
					}
				} else {
					condClazz = evaluatorsCache.get(condition);
				}
			}
		}

		return condClazz;
	}
}
