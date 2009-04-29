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
import java.util.Date;

/**
 * Serializable bean to wrap a JCR node property value.
 * @see GWTJahiaNodePropertyType
 */
public class GWTJahiaNodePropertyValue implements Serializable {

    private String value ;
    private int type ;

     public GWTJahiaNodePropertyValue() {}

    public GWTJahiaNodePropertyValue(String value, int type) {
        this.type = type ;
        this.value = value ;
    }

    public boolean equals(Object obj) {
        if (obj instanceof GWTJahiaNodePropertyValue) {
            GWTJahiaNodePropertyValue val = (GWTJahiaNodePropertyValue) obj ;
            return val.getString().equals(getString()) &&
                    val.getType() == val.getType() ;
        } else {
            return false ;
        }
    }

    public int getType() {
        return type;
    }

    public byte[] getBinary() {
        return null ;
    }

    public byte[] getStream() {
        return null ;
    }

    public Boolean getBoolean() {
        if (type == GWTJahiaNodePropertyType.BOOLEAN) {
            return Boolean.valueOf(value) ;
        } else {
            return null ;
        }
    }

    public Date getDate() {
        if (type == GWTJahiaNodePropertyType.DATE) {
            return new Date(Long.valueOf(value)) ;
        } else {
            return null ;
        }
    }

    public Float getDecimal() {
        if (type == GWTJahiaNodePropertyType.DECIMAL) {
            return Float.valueOf(value) ;
        } else {
            return null ;
        }
    }

    public Double getDouble() {
        if (type == GWTJahiaNodePropertyType.DOUBLE) {
            return Double.valueOf(value) ;
        } else {
            return null ;
        }
    }

    public Long getLong() {
        if (type == GWTJahiaNodePropertyType.LONG) {
            return Double.valueOf(value).longValue() ;
        } else {
            return null ;
        }
    }

    public String getString() {
        return value ;
    }

}
