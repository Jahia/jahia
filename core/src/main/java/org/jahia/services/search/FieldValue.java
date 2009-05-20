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
 package org.jahia.services.search;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 16:35:44
 * To change this template use File | Settings | File Templates.
 */
public class FieldValue implements Serializable {

    private static final long serialVersionUID = -8934143355357429354L;
    private Object value;

    public FieldValue(){
    }

    public FieldValue(Object value){
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getValueAsString(){
        return getValueAsString(null);
    }

    public String getValueAsString(String defaultValue){
        if ( this.value != null ){
            return this.value.toString();
        }
        return defaultValue;
    }

    public static String toString(Object value){
        if ( value != null ){
            return value.toString();
        }
        return null;
    }

    public static String toString(Object value, String defaultValue){
        if ( value != null ){
            return value.toString();
        }
        return defaultValue;
    }

}
