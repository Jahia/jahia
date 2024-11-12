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
package org.jahia.ajax.gwt.client.data.definition;

import java.io.Serializable;
import java.util.List;

/**
 * Property definition GWT bean.
 *
 * User: toto
 * Date: Aug 26, 2008 - 7:37:53 PM
 */
public class GWTJahiaPropertyDefinition extends GWTJahiaItemDefinition implements Serializable {

    private static final long serialVersionUID = -7766550549947629084L;

    private int requiredType = 0;

    private boolean internationalized;

    private boolean multiple;
    private boolean constrained ;
    private List<String> valueConstraints ;
    private List<GWTJahiaNodePropertyValue> defaultValues;
    private String constraintErrorMessage;

    private String minValue;
    private String maxValue;

    public GWTJahiaPropertyDefinition() {
    }

    public int getRequiredType() {
        return requiredType;
    }

    public void setRequiredType(int requiredType) {
        this.requiredType = requiredType;
    }

    public boolean isInternationalized() {
        return internationalized;
    }

    public void setInternationalized(boolean internationalized) {
        this.internationalized = internationalized;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }


    public boolean isConstrained() {
        return constrained;
    }

    public void setConstrained(boolean constrained) {
        this.constrained = constrained;
    }

    public List<String> getValueConstraints() {
        return valueConstraints;
    }

    public void setValueConstraints(List<String> valueConstraints) {
        this.valueConstraints = valueConstraints;
    }

    public List<GWTJahiaNodePropertyValue> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(List<GWTJahiaNodePropertyValue> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getConstraintErrorMessage() {
        return constraintErrorMessage;
    }

    public void setConstraintErrorMessage(String constraintErrorMessage) {
        this.constraintErrorMessage = constraintErrorMessage;
    }
}
