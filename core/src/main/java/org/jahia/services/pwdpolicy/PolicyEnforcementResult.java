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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.struts.action.ActionMessages;
import org.jahia.engines.EngineMessages;

/**
 * The password policy evaluation result.
 * 
 * @author Sergiy Shyrkov
 */
public class PolicyEnforcementResult {

	static final PolicyEnforcementResult SUCCESS = new PolicyEnforcementResult();

	private List invalidationRules = Collections.emptyList();

	private List violatedRules = Collections.emptyList();

	private List warningRules = Collections.emptyList();

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
	public PolicyEnforcementResult(List violatedRules) {
		this();
		this.violatedRules = violatedRules;
		buildResult();
	}

	private void buildResult() {
		if (violatedRules.size() > 0) {
			invalidationRules = new LinkedList();
			warningRules = new LinkedList();
			for (Iterator iterator = violatedRules.iterator(); iterator
			        .hasNext();) {
				JahiaPasswordPolicyRule rule = (JahiaPasswordPolicyRule) iterator
				        .next();
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
	 * Returns the result of the evaluation as the {@link ActionMessages}
	 * object, that can be displayed in the front-end. Each message is composed
	 * using an I18N key and a list of arguments to be substituted.
	 * 
	 * @return the result of the evaluation as the {@link ActionMessages}
	 *         object, that can be displayed in the front-end. Each message is
	 *         composed using an I18N key and a list of arguments to be
	 *         substituted
	 */
	public ActionMessages getActionMessages() {
		return PolicyEnforcementResultConvertor.toActionMessages(this);
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
	 * Returns the invalidationRules.
	 * 
	 * @return the invalidationRules
	 */
	public List getInvalidationRules() {
		return invalidationRules;
	}

	/**
	 * Returns the violatedRules.
	 * 
	 * @return the violatedRules
	 */
	public List getViolatedRules() {
		return violatedRules;
	}

	/**
	 * Returns the warningRules.
	 * 
	 * @return the warningRules
	 */
	public List getWarningRules() {
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
	 * Reurns <code>true</code> if none of the applied rules is violated.
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
