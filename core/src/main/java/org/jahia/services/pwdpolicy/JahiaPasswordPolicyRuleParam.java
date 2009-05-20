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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Rule parameter service object.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyRuleParam implements Serializable, Cloneable {

    private int id;

    private String name;

    private String value;

    /**
     * Initializes an instance of this class.
     */
    public JahiaPasswordPolicyRuleParam() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     * @param name
     * @param value
     */
    public JahiaPasswordPolicyRuleParam(int id, String name, String value) {

        this();
        this.id = id;
        this.name = name;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        JahiaPasswordPolicyRuleParam param = null;
        try {
            param = (JahiaPasswordPolicyRuleParam) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        return param;
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
     * Returns the parameter id.
     * 
     * @return the rule id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the parameter name.
     * 
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * 
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
        return new ToStringBuilder(this).append("id", id).append("name", name)
                .append("value", value).toString();
    }

}
