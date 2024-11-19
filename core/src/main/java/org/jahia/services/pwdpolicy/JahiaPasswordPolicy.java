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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Jahia password policy object, holding a list of rules to be enforced e.g. on
 * changing password and on login.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicy implements Serializable {

	private static final long serialVersionUID = 7340082798180832549L;

	private String id;
	private String name;
	private List<JahiaPasswordPolicyRule> rules = new LinkedList<>();

	/**
	 * Initializes an instance of this class.
	 */
	public JahiaPasswordPolicy() {
		super();
	}

	/**
	 * Initializes an instance of this class.
	 *
	 * @param id
	 * @param name
	 */
	public JahiaPasswordPolicy(String id, String name,
	        boolean userAllowedToChangePassword) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Copy constructor
	 *
	 * @param policy the policy to create the copy from
	 */
	public JahiaPasswordPolicy(JahiaPasswordPolicy policy) {
		this.id = policy.id;
		this.name = policy.name;
		this.rules = (policy.rules == null) ? null : policy.rules.stream()
				.map(rule -> new JahiaPasswordPolicyRule(rule))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Returns the policy id.
	 *
	 * @return the policy id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the policy name.
	 *
	 * @return the policy name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the requested policy rule.
	 *
	 * @param position
	 *            the rule position in the list (zero-based)
	 * @return the requested policy rule
	 */
	public JahiaPasswordPolicyRule getRule(int position) {
		return rules.get(position);
	}

	/**
	 * Returns list of rules for this policy.
	 *
	 * @return list of rules for this policy
	 */
	public List<JahiaPasswordPolicyRule> getRules() {
		return rules;
	}

	/**
	 * Sets the value of policy id.
	 *
	 * @param id
	 *            the policy id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the value of policy name.
	 *
	 * @param name
	 *            the policy name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the list of rules for this policy.
	 *
	 * @param rules
	 *            the list of rules for this policy
	 */
	public void setRules(List<JahiaPasswordPolicyRule> rules) {
		this.rules = rules;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj != null && this.getClass() == obj.getClass()) {
			JahiaPasswordPolicyRuleParam castOther = (JahiaPasswordPolicyRuleParam) obj;
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
		        .append("rules", rules).toString();
	}

}
