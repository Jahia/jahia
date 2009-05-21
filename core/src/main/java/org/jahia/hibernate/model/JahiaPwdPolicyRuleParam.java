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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Rule parameter data object.
 * 
 * @hibernate.class table="jahia_pwd_policy_rule_params"
 * @hibernate.cache usage="nonstrict-read-write"
 * @author Sergiy Shyrkov
 */
public class JahiaPwdPolicyRuleParam implements Serializable {

	public static final char TYPE_ACTION_PARAM = 'A';

	public static final char TYPE_CONDITION_PARAM = 'C';

	private int id;

	private String name;

	private int position;

	private JahiaPwdPolicyRule rule;

	private char type;

	private String value;

	/**
	 * Initializes an instance of this class.
	 */
	public JahiaPwdPolicyRuleParam() {
		super();
	}

	/**
	 * Initializes an instance of this class.
	 * 
	 * @param id
	 * @param name
	 * @param type
	 * @param value
	 */
	public JahiaPwdPolicyRuleParam(int id, String name, char type, String value) {
		this();
		this.id = id;
		this.name = name;
		this.type = type;
		this.value = value;
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
	 * Returns the parameter id.
	 * 
	 * @hibernate.id column="jahia_pwd_policy_rule_param_id" type="int"
	 *               generator-class="org.jahia.hibernate.dao.JahiaIdentifierGenerator"
	 *               unsaved-value="0"
	 * @return the rule id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the parameter name.
	 * 
	 * @hibernate.property column="name" type="string" length="50"
	 *                     not-null="true"
	 * @return the parameter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the position (zero-based) of this parameter in the list of rule
	 * parameters.
	 * 
	 * @hibernate.property column="position_index" type="int" not-null="true"
	 * @return the position of this parameter in the list of rule parameters
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Returns the corresponding rule.
	 * 
	 * @hibernate.many-to-one column="jahia_pwd_policy_rule_id" not-null="true"
	 *                        outer-join="false"
	 * @return the corresponding rule
	 */
	public JahiaPwdPolicyRule getRule() {
		return rule;
	}

	/**
	 * Returns the type.
	 * 
	 * @hibernate.property column="type" type="char" not-null="true"
	 * @return the type
	 */
	public char getType() {
		return type;
	}

	/**
	 * Returns the value.
	 * 
	 * @hibernate.property column="value" type="string" length="255"
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	/**
	 * Sets the value of id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the value of parameter name.
	 * 
	 * @param name
	 *            the parameter name to set
	 */
	public void setName(String name) {
		this.name = name;
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

	/**
	 * Sets the value of corresponding rule.
	 * 
	 * @param rule
	 *            the corresponding rule to set
	 */
	public void setRule(JahiaPwdPolicyRule rule) {
		this.rule = rule;
	}

	/**
	 * Sets the value of type.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(char type) {
		this.type = type;
	}

	/**
	 * Sets the value for this parameter.
	 * 
	 * @param value
	 *            the value for this parameter
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("ruleId",
		        rule.getId()).append("position", position).append("name", name)
		        .append("type", type).append("value", value).toString();
	}

}
