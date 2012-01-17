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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Jahia password policy object, holding a list of rules to be enforced e.g. on
 * changing password and on login.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicy implements Serializable, Cloneable {

	private static final long serialVersionUID = 7340082798180832549L;

	private String id;

	private String name;

	private List<JahiaPasswordPolicyRule> rules = new LinkedList<JahiaPasswordPolicyRule>();

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
		this();
		this.id = id;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		JahiaPasswordPolicy policy = null;
		try {
			policy = (JahiaPasswordPolicy) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}

		policy.setRules(new LinkedList<JahiaPasswordPolicyRule>());
		for (JahiaPasswordPolicyRule rule : rules) {
			policy.getRules().add((JahiaPasswordPolicyRule) rule.clone());
		}

		return policy;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj != null && this.getClass() == obj.getClass()) {
			JahiaPasswordPolicyRuleParam castOther = (JahiaPasswordPolicyRuleParam) obj;
			return new EqualsBuilder().append(this.getId(), castOther.getId())
			        .isEquals();
		}
		return false;
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
		return (JahiaPasswordPolicyRule) rules.get(position);
	}

	/**
	 * Returns list of rules for this policy.
	 * 
	 * @return list of rules for this policy
	 */
	public List<JahiaPasswordPolicyRule> getRules() {
		return rules;
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name)
		        .append("rules", rules).toString();
	}

}
