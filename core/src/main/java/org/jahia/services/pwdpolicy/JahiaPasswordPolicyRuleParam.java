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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Rule parameter service object.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyRuleParam implements Serializable {

    private static final long serialVersionUID = -7015454747220376620L;

	private String id;
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
    public JahiaPasswordPolicyRuleParam(String id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    /**
     * Copy constructor
     *
     * @param param the parameter to create a copy from
     */
    public JahiaPasswordPolicyRuleParam(JahiaPasswordPolicyRuleParam param) {
        this(param.id, param.name, param.value);
    }

    /**
     * Returns the parameter id.
     *
     * @return the rule id
     */
    public String getId() {
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

    /**
     * Sets the value of id.
     *
     * @param id
     *            the id to set
     */
    public void setId(String id) {
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
                .append("value", value).toString();
    }

}
