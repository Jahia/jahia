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
        if (obj instanceof GWTJahiaNodePropertyValue) {
            GWTJahiaNodePropertyValue val = (GWTJahiaNodePropertyValue) obj;
            return val.getType() == val.getType() && val.getString().equals(getString());
        } else {
            return false;
        }
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
