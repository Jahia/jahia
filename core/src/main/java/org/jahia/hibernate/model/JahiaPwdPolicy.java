/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.hibernate.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

/**
 * Password policy data object.
 * 
 * @hibernate.class table="jahia_pwd_policies"
 * @hibernate.cache usage="nonstrict-read-write"
 * @author Sergiy Shyrkov
 */
public class JahiaPwdPolicy implements Serializable {

	private int id;

	private String name;

	private List rules = new LinkedList();

	/**
	 * Initializes an instance of this class.
	 */
	public JahiaPwdPolicy() {
		super();
	}

	/**
	 * Initializes an instance of this class.
	 * 
	 * @param id
	 * @param name
	 */
	public JahiaPwdPolicy(int id, String name) {
		this();
		this.id = id;
		this.name = name;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj != null && this.getClass() == obj.getClass()) {
			JahiaPwdPolicyRuleParam castOther = (JahiaPwdPolicyRuleParam) obj;
			return new EqualsBuilder().append(this.getId(), castOther.getId())
			        .isEquals();
		}
		return false;
	}

	/**
	 * Returns the policy id.
	 * 
	 * @hibernate.id column="jahia_pwd_policy_id" type="int"
	 *               generator-class="org.jahia.hibernate.dao.JahiaIdentifierGenerator"
	 *               unsaved-value="0"
	 * @return the policy id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the policy name.
	 * 
	 * @hibernate.property column="name" not-null="true" type="string"
	 *                     length="255"
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
	public JahiaPwdPolicyRule getRule(int position) {
		return (JahiaPwdPolicyRule) rules.get(position);
	}

	/**
	 * Returns tehlist of rules for this policy.
	 * 
	 * @hibernate.list lazy="true" inverse="true" cascade="all-delete-orphan"
	 * @hibernate.collection-one-to-many class="org.jahia.hibernate.model.JahiaPwdPolicyRule"
	 * @hibernate.collection-key column="jahia_pwd_policy_id"
	 * @hibernate.collection-index column="position_index"
	 * @return the list of rules for this policy
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
		        .append("rules", Hibernate.isInitialized(rules) ? rules : null)
		        .toString();
	}

}
