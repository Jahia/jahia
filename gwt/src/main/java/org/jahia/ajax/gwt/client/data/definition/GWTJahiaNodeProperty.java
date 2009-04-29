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
import java.util.ArrayList;

/**
 * This is a bean to wrap a JCR node property, made from JCR 2.0 specs.
 */
public class GWTJahiaNodeProperty implements Serializable {

    private boolean multiple ;
    private String name;
    private List<GWTJahiaNodePropertyValue> values ;

    public GWTJahiaNodeProperty() {}

    public GWTJahiaNodeProperty(String name, GWTJahiaNodePropertyValue v) {
        setName(name);
        setValue(v);
    }

    public GWTJahiaNodeProperty cloneObject() {
        GWTJahiaNodeProperty prop = new GWTJahiaNodeProperty() ;
        prop.setName(getName());
        List<GWTJahiaNodePropertyValue> vals = new ArrayList<GWTJahiaNodePropertyValue>(values.size()) ;
        for (GWTJahiaNodePropertyValue aVal: values) {
            vals.add(new GWTJahiaNodePropertyValue(aVal.getString(), aVal.getType())) ;
        }
        prop.setValues(vals);
        return prop ;
    }

    public boolean equals(Object obj) {
        if (obj instanceof GWTJahiaNodeProperty) {
            GWTJahiaNodeProperty prop = (GWTJahiaNodeProperty) obj ;
            return prop.getName().equals(getName())&&
                    prop.getValues().containsAll(getValues()) &&
                    getValues().containsAll(prop.getValues()) ;
        } else {
            return false ;
        }
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
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
        this.values = values;
    }

    public void setValue(GWTJahiaNodePropertyValue value) {
        List<GWTJahiaNodePropertyValue> vals = new ArrayList<GWTJahiaNodePropertyValue>(1) ;
        vals.add(value) ;
        setValues(vals);
    }

    public String toString() {
        if (multiple) {
            StringBuilder buf = new StringBuilder(values.get(0).toString()) ;
            for (int i=1; i<values.size(); i++) {
                buf.append(", ").append(values.get(i).toString()) ;
            }
            return buf.toString() ;
        }
        else {
            return values.toString();
        }
    }

}
