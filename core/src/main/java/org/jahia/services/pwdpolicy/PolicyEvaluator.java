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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;

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
