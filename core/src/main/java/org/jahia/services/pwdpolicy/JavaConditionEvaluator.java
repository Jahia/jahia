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