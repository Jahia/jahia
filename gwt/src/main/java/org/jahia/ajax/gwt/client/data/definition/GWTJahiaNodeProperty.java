/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
import java.util.ArrayList;

/**
 * This is a bean to wrap a JCR node property, made from JCR 2.0 specs.
 */
public class GWTJahiaNodeProperty implements Serializable {

    /** The serialVersionUID. */
    private static final long serialVersionUID = 462346339035404507L;
    private boolean multiple;
    private boolean dirty;
    private String name;
    private List<GWTJahiaNodePropertyValue> values;

    public GWTJahiaNodeProperty() {
    	super();
    }

    public GWTJahiaNodeProperty(String name, String value) {
        this(name, new GWTJahiaNodePropertyValue(value));
    }

    public GWTJahiaNodeProperty(String name, String value, int valueType) {
        this(name, new GWTJahiaNodePropertyValue(value, valueType));
    }

    public GWTJahiaNodeProperty(String name, GWTJahiaNodePropertyValue v) {
    	this();
        setName(name);
        setValue(v);
    }

    public GWTJahiaNodeProperty cloneObject() {
        GWTJahiaNodeProperty prop = new GWTJahiaNodeProperty();
        prop.setName(getName());
        prop.setMultiple(isMultiple());
        List<GWTJahiaNodePropertyValue> vals = new ArrayList<GWTJahiaNodePropertyValue>(values.size());
        for (GWTJahiaNodePropertyValue aVal : values) {
            if (aVal.getNode() != null) {
                vals.add(new GWTJahiaNodePropertyValue(aVal.getNode(), aVal.getType()));
            } else if (aVal.getLinkNode() != null) {
                 vals.add(new GWTJahiaNodePropertyValue(aVal.getLinkNode(),aVal.getType()));
            } else {
                vals.add(new GWTJahiaNodePropertyValue(aVal.getString(), aVal.getType()));
            }
        }
        prop.setValues(vals);
        return prop;
    }

    public boolean equals(Object obj) {
        if (obj != null && this.getClass() == obj.getClass()) {
            GWTJahiaNodeProperty prop = (GWTJahiaNodeProperty) obj;
            return prop.getName().equals(getName()) &&
                    prop.getValues().containsAll(getValues()) &&
                    getValues().containsAll(prop.getValues());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GWTJahiaNodePropertyValue> getValues() {
        return values;
    }

    public void setValues(List<GWTJahiaNodePropertyValue> values) {
        this.dirty = true;
        this.values = values;
    }

    public void setValue(GWTJahiaNodePropertyValue value) {
        List<GWTJahiaNodePropertyValue> vals = new ArrayList<GWTJahiaNodePropertyValue>(1);
        vals.add(value);
        setValues(vals);
    }

    public String toString() {
        if (multiple) {
            StringBuilder buf = new StringBuilder(name);
            buf.append(" = ");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    buf.append(",");
                }
                buf.append(values.get(i).toString());
            }
            return buf.toString();
        } else {
            return name + " = " + values.toString();
        }
    }

}
