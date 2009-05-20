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
package org.jahia.hibernate.manager;

import java.util.Iterator;

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
		for (Iterator iterator = policy.getRules().iterator(); iterator
		        .hasNext();) {
			JahiaPwdPolicyRule rule = (JahiaPwdPolicyRule) iterator.next();
			rule.setPolicy(null);
			for (Iterator paramInerator = rule.getParameters().iterator(); paramInerator
			        .hasNext();) {
				((JahiaPwdPolicyRuleParam) paramInerator.next()).setRule(null);
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
		for (Iterator iterator = policySrvObj.getRules().iterator(); iterator
		        .hasNext();) {
			JahiaPwdPolicyRule ruleDataObj = toDataObjectRule((JahiaPasswordPolicyRule) iterator
			        .next());

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
		for (Iterator iterator = ruleSrvObj.getConditionParameters().iterator(); iterator
		        .hasNext();) {
			JahiaPwdPolicyRuleParam paramDataObj = toDataObjectParam(
			        (JahiaPasswordPolicyRuleParam) iterator.next(),
			        JahiaPwdPolicyRuleParam.TYPE_CONDITION_PARAM);
			paramDataObj.setRule(rule);
			paramDataObj.setPosition(position++);
			rule.getParameters().add(paramDataObj);
		}
		for (Iterator iterator = ruleSrvObj.getActionParameters().iterator(); iterator
		        .hasNext();) {
			JahiaPwdPolicyRuleParam paramDataObj = toDataObjectParam(
			        (JahiaPasswordPolicyRuleParam) iterator.next(),
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

		for (Iterator iterator = policyDataObj.getRules().iterator(); iterator
		        .hasNext();) {
			policy.getRules().add(
			        toServiceObjectRule((JahiaPwdPolicyRule) iterator.next()));
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

		for (Iterator iterator = ruleDataObj.getParameters().iterator(); iterator
		        .hasNext();) {
			JahiaPwdPolicyRuleParam ruleParamDataObj = (JahiaPwdPolicyRuleParam) iterator
			        .next();
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
