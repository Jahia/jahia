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

package org.jahia.hibernate.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

/**
 * Password policy rule data object.
 * 
 * @hibernate.class table="jahia_pwd_policy_rules"
 * @hibernate.cache usage="nonstrict-read-write"
 * @author Sergiy Shyrkov
 */
public class JahiaPwdPolicyRule implements Serializable {

	private char action;

	private boolean active;

	private String condition;

	private char evaluator;

	private int id;

	private boolean lastRule;

	private String name;

	private List parameters = new LinkedList();

	private boolean periodical;

	private JahiaPwdPolicy policy;

	private int position;

	/**
	 * Initializes an instance of this class.
	 */
	public JahiaPwdPolicyRule() {
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
	public JahiaPwdPolicyRule(int id, String name, boolean active,
	        boolean periodical, boolean lastRule, char evaluator,
	        String condition, char action) {
		super();
		this.id = id;
		this.name = name;
		this.active = active;
		this.periodical = periodical;
		this.lastRule = lastRule;
		this.evaluator = evaluator;
		this.condition = condition;
		this.action = action;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj != null && this.getClass() == obj.getClass()) {
			JahiaPwdPolicyRule castOther = (JahiaPwdPolicyRule) obj;
			return new EqualsBuilder().append(this.getId(), castOther.getId())
			        .isEquals();
		}
		return false;
	}

	/**
	 * Returns the action type.
	 * 
	 * @hibernate.property column="action" type="char" not-null="true"
	 * @return the action type
	 */
	public char getAction() {
		return action;
	}

	/**
	 * Returns the condition.
	 * 
	 * @hibernate.property column="rule_condition" type="text" length="1048576"
	 *                     not-null="true"
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Returns the evaluator type.
	 * 
	 * @hibernate.property column="evaluator" type="char" not-null="true"
	 * @return the evaluator type
	 */
	public char getEvaluator() {
		return evaluator;
	}

	/**
	 * Returns the rule id.
	 * 
	 * @hibernate.id column="jahia_pwd_policy_rule_id" type="int"
	 *               generator-class="org.jahia.hibernate.dao.JahiaIdentifierGenerator"
	 *               unsaved-value="0"
	 * @return the rule id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the rule name.
	 * 
	 * @hibernate.property column="name" type="string" length="255"
	 *                     not-null="true"
	 * @return the rule name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the list of parameters.
	 * 
	 * @hibernate.list lazy="true" inverse="true" cascade="all-delete-orphan"
	 * @hibernate.collection-one-to-many class="org.jahia.hibernate.model.JahiaPwdPolicyRuleParam"
	 * @hibernate.collection-key column="jahia_pwd_policy_rule_id"
	 * @hibernate.collection-index column="position_index"
	 * @return the list of parameters
	 */
	public List getParameters() {
		return parameters;
	}

	/**
	 * Returns the corresponding policy.
	 * 
	 * @hibernate.many-to-one column="jahia_pwd_policy_id" not-null="true"
	 *                        outer-join="false"
	 * @return the corresponding policy
	 */
	public JahiaPwdPolicy getPolicy() {
		return policy;
	}

	/**
	 * Returns the position (zero-based) of this rule in the list of policy
	 * rules.
	 * 
	 * @hibernate.property column="position_index" type="int" not-null="true"
	 * @return the position of this rule in the list of policy rules
	 */
	public int getPosition() {
		return position;
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	/**
	 * Returns <code>true</code> if this rule is active.
	 * 
	 * @hibernate.property column="active" type="boolean" not-null="true"
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
	 * @hibernate.property column="last_rule" type="boolean" not-null="true"
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
	 * @hibernate.property column="periodical" type="boolean" not-null="true"
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
	 * Sets the value of lastRule.
	 * 
	 * @param lastRule
	 *            the lastRule to set
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
		this.parameters = parameters;
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

	/**
	 * Sets the value of corresponding policy.
	 * 
	 * @param policy
	 *            the corresponding policy to set
	 */
	public void setPolicy(JahiaPwdPolicy policy) {
		this.policy = policy;
	}

	/**
	 * Sets the value of position.
	 * 
	 * @param position
	 *            the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	public String toString() {
		return new ToStringBuilder(this)
		        .append("id", id)
		        .append("policyId", policy.getId())
		        .append("active", active)
		        .append("periodical", periodical)
		        .append("lastRule", lastRule)
		        .append("position", position)
		        .append("name", name)
		        .append("evaluator", evaluator)
		        .append("action", action)
		        .append(
		                "condition",
		                condition.length() > 255 ? condition.substring(0, 255)
		                        : condition)
		        .append("parameters",
		                Hibernate.isInitialized(parameters) ? parameters : null)
		        .toString();
	}

}
