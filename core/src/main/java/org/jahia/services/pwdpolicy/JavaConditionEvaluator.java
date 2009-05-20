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

	private static Map evaluatorsCache = new HashMap();

	public boolean evaluate(JahiaPasswordPolicyRule rule, EvaluationContext ctx) {
		return getConditionClazz(rule.getCondition()).evaluate(
		        rule.getConditionParameters(), ctx);
	}

	private PasswordPolicyRuleCondition getConditionClazz(String condition) {

		PasswordPolicyRuleCondition condClazz = null;
		if (evaluatorsCache.containsKey(condition)) {
			condClazz = (PasswordPolicyRuleCondition) evaluatorsCache
			        .get(condition);
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
					condClazz = (PasswordPolicyRuleCondition) evaluatorsCache
					        .get(condition);
				}
			}
		}

		return condClazz;
	}
}