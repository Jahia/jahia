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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Password policy rule data object.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyRule implements Serializable {

	private static final long serialVersionUID = -3688773450875340830L;

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

	private String id;
	private String name;
	private boolean lastRule;
	private char action = ACTION_WARN;
	private char evaluator = EVALUATOR_JAVA;
	private boolean active = true;
	private boolean periodical;
	private String condition;
	private List<JahiaPasswordPolicyRuleParam> conditionParameters = new LinkedList<>();
	private List<JahiaPasswordPolicyRuleParam> actionParameters = new LinkedList<>();

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
	public JahiaPasswordPolicyRule(String id, String name, boolean active,
	        boolean periodical, boolean lastRule, char evaluator,
	        String condition, char action) {
		this.id = id;
		this.name = name;
		this.active = active;
		this.periodical = periodical;
		this.lastRule = lastRule;
		this.evaluator = evaluator;
		this.condition = condition;
		this.action = action;
	}

	/**
	 * Copy constructor
	 *
	 * @param rule the rule to create a copy from
	 */
	public JahiaPasswordPolicyRule(JahiaPasswordPolicyRule rule) {
		this(rule.id, rule.name, rule.active, rule.periodical, rule.lastRule, rule.evaluator, rule.condition, rule.action);
		this.actionParameters = (rule.actionParameters == null) ? null : rule.actionParameters.stream()
				.map(p -> new JahiaPasswordPolicyRuleParam(p))
				.collect(Collectors.toCollection(LinkedList::new));
		this.conditionParameters = (rule.conditionParameters == null) ? null : rule.conditionParameters.stream()
				.map(p -> new JahiaPasswordPolicyRuleParam(p))
				.collect(Collectors.toCollection(LinkedList::new));
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
	public List<JahiaPasswordPolicyRuleParam> getActionParameters() {
		return actionParameters;
	}

	/**
	 * Returns the map with all action parameters, keyed by their names. The new
	 * map is created each time this method is called.
	 *
	 * @return the map with all action parameters, keyed by their names
	 */
	public Map<String, String> getActionParametersValues() {
		if (actionParameters.size() == 0) {
			return Collections.emptyMap();
		}

		return actionParameters.stream()
				.collect(Collectors.toMap(JahiaPasswordPolicyRuleParam::getName, JahiaPasswordPolicyRuleParam::getValue));
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
	public List<JahiaPasswordPolicyRuleParam> getConditionParameters() {
		return conditionParameters;
	}

	/**
	 * Returns the map with all condition parameters, keyed by their names. The
	 * new map is created each time this method is called.
	 *
	 * @return the map with all condition parameters, keyed by their names
	 */
	public Map<String, String> getConditionParametersValues() {
		if (conditionParameters.size() == 0) {
			return Collections.emptyMap();
		}

		return conditionParameters.stream()
				.collect(Collectors.toMap(JahiaPasswordPolicyRuleParam::getName, JahiaPasswordPolicyRuleParam::getValue));

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
	public String getId() {
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
	public void setActionParameters(List<JahiaPasswordPolicyRuleParam> actionParameters) {
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
	public void setConditionParameters(List<JahiaPasswordPolicyRuleParam> conditionParameters) {
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
	public void setId(String id) {
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
	public void setParameters(List<JahiaPasswordPolicyRuleParam> parameters) {
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj != null && this.getClass() == obj.getClass()) {
			JahiaPasswordPolicyRule castOther = (JahiaPasswordPolicyRule) obj;
			return new EqualsBuilder().append(this.getId(), castOther.getId())
					.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	@Override
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
