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
