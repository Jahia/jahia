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

package org.jahia.hibernate.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;

/**
 * Password policy data object.
 * 
 * @author Sergiy Shyrkov
 */
@Entity
@Table(name = "jahia_pwd_policies")
public class JahiaPwdPolicy implements Serializable {

	private static final long serialVersionUID = 6503157469310331233L;

	private int id;

	private String name;

	private List<JahiaPwdPolicyRule> rules = new LinkedList<JahiaPwdPolicyRule>();

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
	 * @return the policy id
	 */
	@Id
	@Column(name = "jahia_pwd_policy_id", nullable = false)
	@GeneratedValue(generator = "jahia-generator")
	@GenericGenerator(name = "jahia-generator", strategy = "org.jahia.hibernate.dao.JahiaIdentifierGenerator")
	public int getId() {
		return id;
	}

	/**
	 * Returns the policy name.
	 * 
	 * @return the policy name
	 */
	@Column(name = "name", nullable = false)
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
	 * Returns the list of rules for this policy.
	 * 
	 * @return the list of rules for this policy
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "policy")
	@JoinColumn(name = "jahia_pwd_policy_id")
	@Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
	@IndexColumn(name="position_index")
	public List<JahiaPwdPolicyRule> getRules() {
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
	public void setRules(List<JahiaPwdPolicyRule> rules) {
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
