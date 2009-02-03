/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.definitions.client.model;

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
