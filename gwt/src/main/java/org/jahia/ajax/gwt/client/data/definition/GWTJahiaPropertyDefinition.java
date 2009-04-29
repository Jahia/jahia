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
package org.jahia.ajax.gwt.client.data.definition;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * User: toto
 * Date: Aug 26, 2008 - 7:37:53 PM
 */
public class GWTJahiaPropertyDefinition extends GWTJahiaItemDefinition implements Serializable {

    private int requiredType = 0;

    private boolean internationalized;

//    private Value[] valueConstraints = new Value[0];
//    private Value[] defaultValues = new Value[0];


    private boolean multiple;
    private boolean constrained ;
    private List<String> valueConstraints ;
    private List<GWTJahiaNodePropertyValue> defaultValues;

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
}
