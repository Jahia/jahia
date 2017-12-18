/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;
import java.util.Date;

/**
 * Serializable bean to wrap a JCR node property value.
 *
 * @see GWTJahiaNodePropertyType
 */
public class GWTJahiaNodePropertyValue extends BaseTreeModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String value;
    private int type;
    private GWTJahiaNode reference;

    // case of a link
    private GWTJahiaNode linkNode;

    public GWTJahiaNodePropertyValue() {
    	super();
    }

    public GWTJahiaNodePropertyValue(String value) {
    	this(value, GWTJahiaNodePropertyType.STRING);
    }

    public GWTJahiaNodePropertyValue(String value, int type) {
    	this();
        this.type = type;
        this.value = value;
    }
    
    public GWTJahiaNodePropertyValue(String value, GWTJahiaNode node, int type) {
        this();
        this.type = type;
        this.value = value;
        this.reference = node;
    }

    public GWTJahiaNodePropertyValue(GWTJahiaNode node, int type) {
        this.type = type;
        if (type == GWTJahiaNodePropertyType.REFERENCE || type == GWTJahiaNodePropertyType.WEAKREFERENCE) {
            this.reference = node;
            this.value = reference.getUUID();
        } else if (type == GWTJahiaNodePropertyType.PAGE_LINK) {
            this.linkNode = node;
            this.value = node.get("jnt:url"); 
        } else {
            this.reference = node;
            this.value = node.getPath();
        }
    }

    public boolean equals(Object obj) {
        if (obj != null && this.getClass() == obj.getClass()) {
            GWTJahiaNodePropertyValue val = (GWTJahiaNodePropertyValue) obj;
            return val.getType() == getType() && val.getString() != null && val.getString().equals(getString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + type;
        return result;
    }

    public int getType() {
        return type;
    }


    public byte[] getBinary() {
        return null;
    }

    public byte[] getStream() {
        return null;
    }

    public Boolean getBoolean() {
        if (type == GWTJahiaNodePropertyType.BOOLEAN && value != null) {
            return Boolean.valueOf(value);
        } else {
            return null;
        }
    }

    public Date getDate() {
        if (type == GWTJahiaNodePropertyType.DATE && value != null) {
            return new Date(Long.valueOf(value));
        } else {
            return null;
        }
    }

    public Float getDecimal() {
        if (type == GWTJahiaNodePropertyType.DECIMAL && value != null) {
            return Float.valueOf(value);
        } else {
            return null;
        }
    }

    public Double getDouble() {
        if (type == GWTJahiaNodePropertyType.DOUBLE && value != null) {
            return Double.valueOf(value);
        } else {
            return null;
        }
    }

    public Long getLong() {
        if (type == GWTJahiaNodePropertyType.LONG && value != null) {
            return Double.valueOf(value).longValue();
        } else {
            return null;
        }
    }

    public String getString() {
        return value;
    }

    public GWTJahiaNode getNode() {
        return reference;
    }

    public GWTJahiaNode getLinkNode() {
        return linkNode;
    }


    @Override
    public String toString() {
        return getString();
    }

}
