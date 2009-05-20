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

import java.io.Serializable;
import java.util.Iterator;
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

	private int id;

	private String name;

	private List rules = new LinkedList();

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
	public JahiaPasswordPolicy(int id, String name,
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

		policy.setRules(new LinkedList());
		for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
			JahiaPasswordPolicyRule rule = (JahiaPasswordPolicyRule) iterator
			        .next();
			policy.getRules().add(rule.clone());
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
	public int getId() {
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
	public List getRules() {
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
	public void setId(int id) {
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
	public void setRules(List rules) {
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
