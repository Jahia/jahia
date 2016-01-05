/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.collections.FastHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Policy rules evaluator.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("unchecked")
final class PolicyEvaluator {

	private static final Map<Character, ConditionEvaluator> EVALUATORS;

	static {
		FastHashMap evals = new FastHashMap(1);
		// currently only a single Java evaluator is supported
		evals.put(new Character(JahiaPasswordPolicyRule.EVALUATOR_JAVA),
		        new JavaConditionEvaluator());
		evals.setFast(true);
		EVALUATORS = evals;
	}

	static PolicyEnforcementResult evaluate(JahiaPasswordPolicy policy,
	        EvaluationContext ctx, boolean periodicalRulesOnly) {

		PolicyEnforcementResult result = PolicyEnforcementResult.SUCCESS;
		List<JahiaPasswordPolicyRule> violatedRules = new LinkedList<JahiaPasswordPolicyRule>();

		for (JahiaPasswordPolicyRule rule : policy.getRules()) {
			if (rule.isActive() && rule.isPeriodical() == periodicalRulesOnly) {
				if (!evaluateRule(rule, ctx)) {
					violatedRules.add(rule);
					// stop processing and skip other rules?
					if (rule.isLastRule()) {
						break;
					}
				}
			}
		}

		if (violatedRules.size() > 0) {
			result = new PolicyEnforcementResult(violatedRules);
		}

		return result;
	}

	private static boolean evaluateRule(JahiaPasswordPolicyRule rule,
	        EvaluationContext ctx) {

		return getEvaluator(rule.getEvaluator()).evaluate(rule, ctx);
	}

	private static ConditionEvaluator getEvaluator(char type) {
		ConditionEvaluator evaluator = (ConditionEvaluator) EVALUATORS
		        .get(new Character(type));

		if (evaluator == null) {
			throw new UnsupportedOperationException(
			        "Not supported evaluator type '" + type + "'");
		}

		return evaluator;
	}

}
