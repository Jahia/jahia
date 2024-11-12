/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jahia.engines.EngineMessages;

/**
 * The password policy evaluation result.
 *
 * @author Sergiy Shyrkov
 */
public class PolicyEnforcementResult {

	static final PolicyEnforcementResult SUCCESS = new PolicyEnforcementResult();

	private List<JahiaPasswordPolicyRule> invalidationRules = Collections.emptyList();

	private List<JahiaPasswordPolicyRule> violatedRules = Collections.emptyList();

	private List<JahiaPasswordPolicyRule> warningRules = Collections.emptyList();

	/**
	 * Initializes an instance of this class.
	 */
	private PolicyEnforcementResult() {
		super();
	}

	/**
	 * Initializes an instance of this class.
	 *
	 * @param violatedRules
	 */
	public PolicyEnforcementResult(List<JahiaPasswordPolicyRule> violatedRules) {
		this();
		this.violatedRules = violatedRules;
		buildResult();
	}

	private void buildResult() {
		if (violatedRules.size() > 0) {
			invalidationRules = new LinkedList<JahiaPasswordPolicyRule>();
			warningRules = new LinkedList<JahiaPasswordPolicyRule>();
			for (JahiaPasswordPolicyRule rule : violatedRules) {
				if (JahiaPasswordPolicyRule.ACTION_INVALIDATE_PASSWORD == rule
				        .getAction()) {
					invalidationRules.add(rule);
				} else if (JahiaPasswordPolicyRule.ACTION_WARN == rule
				        .getAction()) {
					warningRules.add(rule);
				} else {
					throw new IllegalArgumentException("Unknown action type: "
					        + rule.getAction());
				}

			}
		}

	}

	/**
	 * Returns the result of the evaluation as the {@link EngineMessages}
	 * object, that can be displayed in the front-end. Each message is composed
	 * using an I18N key and a list of arguments to be substituted.
	 *
	 * @return the result of the evaluation as the {@link EngineMessages}
	 *         object, that can be displayed in the front-end. Each message is
	 *         composed using an I18N key and a list of arguments to be
	 *         substituted
	 */
	public EngineMessages getEngineMessages() {
		return PolicyEnforcementResultConvertor.toEngineMessages(this);
	}

	/**
	 * Returns the result of the evaluation as the list of plain English messages.
	 *
	 * @return the result of the evaluation as the list of plain English messages
	 */
	public List<String> getTextMessages() {
		return PolicyEnforcementResultConvertor.toText(this);
	}
	/**
	 * Returns the invalidationRules.
	 *
	 * @return the invalidationRules
	 */
	public List<JahiaPasswordPolicyRule> getInvalidationRules() {
		return invalidationRules;
	}

	/**
	 * Returns the violatedRules.
	 *
	 * @return the violatedRules
	 */
	public List<JahiaPasswordPolicyRule> getViolatedRules() {
		return violatedRules;
	}

	/**
	 * Returns the warningRules.
	 *
	 * @return the warningRules
	 */
	public List<JahiaPasswordPolicyRule> getWarningRules() {
		return warningRules;
	}

	/**
	 * Returns <code>true</code> if at least one of the violated rules
	 * requires password invalidation as a target action.
	 *
	 * @return <code>true</code> if at least one of the violated rules
	 *         requires password invalidation as a target action
	 */
	public boolean isPasswordInvalidationRequired() {
		return invalidationRules.size() > 0;
	}

	/**
	 * Returns <code>true</code> if none of the applied rules is violated.
	 *
	 * @return <code>true</code> if none of the applied rules is violated
	 */
	public boolean isSuccess() {
		return violatedRules.size() == 0;
	}

	/**
	 * Returns <code>true</code> if at least one of the violated rules
	 * requires displaying a notification as a target action.
	 *
	 * @return <code>true</code> if at least one of the violated rules
	 *         requires displaying a notification as a target action
	 */
	public boolean isWarningRequired() {
		return warningRules.size() > 0;
	}

}
