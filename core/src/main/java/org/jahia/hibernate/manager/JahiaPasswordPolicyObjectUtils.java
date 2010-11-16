/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.hibernate.manager;

import org.jahia.hibernate.model.JahiaPwdPolicy;
import org.jahia.hibernate.model.JahiaPwdPolicyRule;
import org.jahia.hibernate.model.JahiaPwdPolicyRuleParam;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicy;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyRule;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyRuleParam;

/**
 * Helper class for converting password policy related data objects into service
 * objects and vice versa.
 * 
 * @author Sergiy Shyrkov
 */
final class JahiaPasswordPolicyObjectUtils {

	static void dereferenceObjects(JahiaPwdPolicy policy) {
		for (JahiaPwdPolicyRule rule : policy.getRules()) {
			rule.setPolicy(null);
			for (JahiaPwdPolicyRuleParam param : rule.getParameters()) {
				param.setRule(null);
			}
			rule.getParameters().clear();
			rule.setParameters(null);
		}
		policy.getRules().clear();
		policy.setRules(null);
	}

	private static JahiaPwdPolicyRuleParam toDataObjectParam(
	        JahiaPasswordPolicyRuleParam paramSrvObj, char paramType) {
		return new JahiaPwdPolicyRuleParam(paramSrvObj.getId(), paramSrvObj
		        .getName(), paramType, paramSrvObj.getValue());
	}

	static JahiaPwdPolicy toDataObjectPolicy(JahiaPasswordPolicy policySrvObj) {
		JahiaPwdPolicy policy = new JahiaPwdPolicy(policySrvObj.getId(),
		        policySrvObj.getName());

		int position = 0;
		for (JahiaPasswordPolicyRule ruleBusinessObj : policySrvObj.getRules()) {
			JahiaPwdPolicyRule ruleDataObj = toDataObjectRule(ruleBusinessObj);

			ruleDataObj.setPolicy(policy);
			ruleDataObj.setPosition(position++);
			policy.getRules().add(ruleDataObj);
		}

		return policy;
	}

	private static JahiaPwdPolicyRule toDataObjectRule(
	        JahiaPasswordPolicyRule ruleSrvObj) {

		JahiaPwdPolicyRule rule = new JahiaPwdPolicyRule(ruleSrvObj.getId(),
		        ruleSrvObj.getName(), ruleSrvObj.isActive(), ruleSrvObj
		                .isPeriodical(), ruleSrvObj.isLastRule(), ruleSrvObj
		                .getEvaluator(), ruleSrvObj.getCondition(), ruleSrvObj
		                .getAction());

		int position = 0;
		for (JahiaPasswordPolicyRuleParam param : ruleSrvObj.getConditionParameters()) {
			JahiaPwdPolicyRuleParam paramDataObj = toDataObjectParam(
			        param,
			        JahiaPwdPolicyRuleParam.TYPE_CONDITION_PARAM);
			paramDataObj.setRule(rule);
			paramDataObj.setPosition(position++);
			rule.getParameters().add(paramDataObj);
		}
		for (JahiaPasswordPolicyRuleParam param : ruleSrvObj.getActionParameters()) {
			JahiaPwdPolicyRuleParam paramDataObj = toDataObjectParam(
			        param,
			        JahiaPwdPolicyRuleParam.TYPE_ACTION_PARAM);
			paramDataObj.setRule(rule);
			paramDataObj.setPosition(position++);
			rule.getParameters().add(paramDataObj);
		}

		return rule;
	}

	/**
	 * Initializes an instance of the rule parameter service object using the
	 * data stored in the data object.
	 * 
	 * @param paramDataObj
	 *            the persistent rule parameter data object
	 * @return an instance of the rule parameter service object using the data
	 *         stored in the data object
	 */
	private static JahiaPasswordPolicyRuleParam toServiceObjectParam(
	        JahiaPwdPolicyRuleParam paramDataObj) {

		return new JahiaPasswordPolicyRuleParam(paramDataObj.getId(),
		        paramDataObj.getName(), paramDataObj.getValue());
	}

	/**
	 * Initializes an instance of the policy service object using the data
	 * stored in the data object.
	 * 
	 * @param policyDataObj
	 *            the persistent policy data object
	 * @return an instance of the policy service object using the data stored in
	 *         the data object
	 */
	static JahiaPasswordPolicy toServiceObjectPolicy(
	        JahiaPwdPolicy policyDataObj) {

		return toServiceObjectPolicy(policyDataObj, null);
	}

	static JahiaPasswordPolicy toServiceObjectPolicy(
	        JahiaPwdPolicy policyDataObj, JahiaPasswordPolicy policy) {

		if (policy == null) {
			policy = new JahiaPasswordPolicy();
		} else {
			policy.getRules().clear();
		}
		policy.setId(policyDataObj.getId());
		policy.setName(policyDataObj.getName());

		for (JahiaPwdPolicyRule rule : policyDataObj.getRules()) {
			policy.getRules().add(toServiceObjectRule(rule));
		}

		return policy;
	}

	/**
	 * Initializes an instance of the policy rule service object using the data
	 * stored in the data object.
	 * 
	 * @param ruleDataObj
	 *            the persistent policy rule data object
	 * @return an instance of the policy rule service object using the data
	 *         stored in the data object
	 */
	private static JahiaPasswordPolicyRule toServiceObjectRule(
	        JahiaPwdPolicyRule ruleDataObj) {

		JahiaPasswordPolicyRule rule = new JahiaPasswordPolicyRule(ruleDataObj
		        .getId(), ruleDataObj.getName(), ruleDataObj.isActive(),
		        ruleDataObj.isPeriodical(), ruleDataObj.isLastRule(),
		        ruleDataObj.getEvaluator(), ruleDataObj.getCondition(),
		        ruleDataObj.getAction());

		for (JahiaPwdPolicyRuleParam ruleParamDataObj : ruleDataObj.getParameters()) {
			JahiaPasswordPolicyRuleParam paramSrvObj = toServiceObjectParam(ruleParamDataObj);
			if (JahiaPwdPolicyRuleParam.TYPE_ACTION_PARAM == ruleParamDataObj
			        .getType()) {
				rule.getActionParameters().add(paramSrvObj);
			} else if (JahiaPwdPolicyRuleParam.TYPE_CONDITION_PARAM == ruleParamDataObj
			        .getType()) {
				rule.getConditionParameters().add(paramSrvObj);
			} else {
				throw new IllegalArgumentException("Unknown parameter type: "
				        + ruleParamDataObj.getType());
			}
		}

		return rule;
	}

	/**
	 * Initializes an instance of this class.
	 */
	private JahiaPasswordPolicyObjectUtils() {
		super();
	}

}
