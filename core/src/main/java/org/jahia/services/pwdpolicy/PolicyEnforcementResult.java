/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
