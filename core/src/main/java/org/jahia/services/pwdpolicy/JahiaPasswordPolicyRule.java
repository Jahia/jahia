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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Password policy rule data object.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyRule implements Serializable, Cloneable {

	/**
	 * Indicates that the password will be invalidated and the user will be
	 * forced to change it.
	 */
	public static final char ACTION_INVALIDATE_PASSWORD = 'P';

	/**
	 * Indicates that the warning (validation or notification) message will be
	 * shown to the user.
	 */
	public static final char ACTION_WARN = 'W';

	/** The rule condition represents a Groovy-based implementation to be called. */
	public static final char EVALUATOR_GROOVY = 'G';

	/** The rule condition represents a Java-based implementation to be called. */
	public static final char EVALUATOR_JAVA = 'J';

	private char action = ACTION_WARN;

	private List actionParameters = new LinkedList();

	private boolean active = true;

	private String condition;

	private List conditionParameters = new LinkedList();

	private char evaluator = EVALUATOR_JAVA;

	private int id;

	private boolean lastRule;

	private String name;

	private boolean periodical;

	/**
	 * Initializes an instance of this class.
	 */
	public JahiaPasswordPolicyRule() {
		super();
	}

	/**
	 * Initializes an instance of this class.
	 * 
	 * @param id
	 * @param name
	 * @param active
	 * @param periodical
	 * @param lastRule
	 * @param evaluator
	 * @param condition
	 * @param action
	 */
	public JahiaPasswordPolicyRule(int id, String name, boolean active,
	        boolean periodical, boolean lastRule, char evaluator,
	        String condition, char action) {
		this();
		this.id = id;
		this.name = name;
		this.active = active;
		this.periodical = periodical;
		this.lastRule = lastRule;
		this.evaluator = evaluator;
		this.condition = condition;
		this.action = action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		JahiaPasswordPolicyRule rule = null;
		try {
			rule = (JahiaPasswordPolicyRule) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}

		rule.setConditionParameters(new LinkedList());
		rule.setActionParameters(new LinkedList());

		for (Iterator iterator = actionParameters.iterator(); iterator
		        .hasNext();) {
			JahiaPasswordPolicyRuleParam param = (JahiaPasswordPolicyRuleParam) iterator
			        .next();
			rule.getActionParameters().add(param.clone());
		}
		for (Iterator iterator = conditionParameters.iterator(); iterator
		        .hasNext();) {
			JahiaPasswordPolicyRuleParam param = (JahiaPasswordPolicyRuleParam) iterator
			        .next();
			rule.getConditionParameters().add(param.clone());
		}
		return rule;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj != null && this.getClass() == obj.getClass()) {
			JahiaPasswordPolicyRule castOther = (JahiaPasswordPolicyRule) obj;
			return new EqualsBuilder().append(this.getId(), castOther.getId())
			        .isEquals();
		}
		return false;
	}

	/**
	 * Returns the action type.
	 * 
	 * @return the action type
	 */
	public char getAction() {
		return action;
	}

	/**
	 * Returns the actionParameters.
	 * 
	 * @return the actionParameters
	 */
	public List getActionParameters() {
		return actionParameters;
	}

	/**
	 * Returns the map with all action parameters, keyed by their names. The new
	 * map is created each time this method is called.
	 * 
	 * @return the map with all action parameters, keyed by their names
	 */
	public Map getActionParametersValues() {
		if (actionParameters.size() == 0)
			return Collections.emptyMap();

		Map params = new HashMap();
		for (Iterator iterator = actionParameters.iterator(); iterator
		        .hasNext();) {
			JahiaPasswordPolicyRuleParam theParam = (JahiaPasswordPolicyRuleParam) iterator
			        .next();
			params.put(theParam.getName(), theParam.getValue());
		}

		return params;
	}

	/**
	 * Returns the condition.
	 * 
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Returns the conditionParameters.
	 * 
	 * @return the conditionParameters
	 */
	public List getConditionParameters() {
		return conditionParameters;
	}

	/**
	 * Returns the map with all condition parameters, keyed by their names. The
	 * new map is created each time this method is called.
	 * 
	 * @return the map with all condition parameters, keyed by their names
	 */
	public Map getConditionParametersValues() {
		if (conditionParameters.size() == 0)
			return Collections.emptyMap();

		Map params = new HashMap();
		for (Iterator iterator = conditionParameters.iterator(); iterator
		        .hasNext();) {
			JahiaPasswordPolicyRuleParam theParam = (JahiaPasswordPolicyRuleParam) iterator
			        .next();
			params.put(theParam.getName(), theParam.getValue());
		}

		return params;
	}

	/**
	 * Returns the evaluator type.
	 * 
	 * @return the evaluator type
	 */
	public char getEvaluator() {
		return evaluator;
	}

	/**
	 * Returns the rule id.
	 * 
	 * @return the rule id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the rule name.
	 * 
	 * @return the rule name
	 */
	public String getName() {
		return name;
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	/**
	 * Returns <code>true</code> if this rule is active.
	 * 
	 * @return <code>true</code> if this rule is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Returns the <code>true</code> if the processing of further rules in the
	 * policy is stopped if this one is violated, thus it is possible to define
	 * exclusive rules.
	 * 
	 * @return the <code>true</code> if the processing of further rules in the
	 *         policy is stopped if this one is violated, thus it is possible to
	 *         define exclusive rules
	 */
	public boolean isLastRule() {
		return lastRule;
	}

	/**
	 * Returns <code>true</code> if this is a reoccurring rule (e.g. on each
	 * user login).
	 * 
	 * @return <code>true</code> if this is a reoccurring rule (e.g. on each
	 *         user login)
	 */
	public boolean isPeriodical() {
		return periodical;
	}

	/**
	 * Sets the value of action.
	 * 
	 * @param action
	 *            the action to set
	 */
	public void setAction(char action) {
		this.action = action;
	}

	/**
	 * Sets the value of actionParameters.
	 * 
	 * @param actionParameters
	 *            the actionParameters to set
	 */
	public void setActionParameters(List actionParameters) {
		this.actionParameters = actionParameters;
	}

	/**
	 * Sets the value of active flag.
	 * 
	 * @param active
	 *            the active flag value to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets the value of condition.
	 * 
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * Sets the value of conditionParameters.
	 * 
	 * @param conditionParameters
	 *            the conditionParameters to set
	 */
	public void setConditionParameters(List conditionParameters) {
		this.conditionParameters = conditionParameters;
	}

	/**
	 * Sets the value of evaluator.
	 * 
	 * @param evaluator
	 *            the evaluator to set
	 */
	public void setEvaluator(char evaluator) {
		this.evaluator = evaluator;
	}

	/**
	 * Sets the value of rule id.
	 * 
	 * @param id
	 *            the rule id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Set to <code>true</code> if the processing of further rules in the
	 * policy is stopped if this one is violated, thus it is possible to define
	 * exclusive rules.
	 * 
	 * @param lastRule
	 *            <code>true</code> if the processing of further rules in the
	 *            policy is stopped if this one is violated, thus it is possible
	 *            to define exclusive rules
	 */
	public void setLastRule(boolean lastRule) {
		this.lastRule = lastRule;
	}

	/**
	 * Sets the value of rule name.
	 * 
	 * @param name
	 *            the rule name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the value of parameters.
	 * 
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(List parameters) {
		this.conditionParameters = parameters;
	}

	/**
	 * Sets the value of periodical flag.
	 * 
	 * @param periodical
	 *            the periodical flag to set
	 */
	public void setPeriodical(boolean periodical) {
		this.periodical = periodical;
	}

	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name)
		        .append("active", active).append("periodical", periodical)
		        .append("lastRule", lastRule).append("evaluator", evaluator)
		        .append("action", action).append(
		                "condition",
		                condition.length() > 255 ? condition.substring(0, 255)
		                        : condition).append("parameters",
		                conditionParameters).toString();
	}

}
