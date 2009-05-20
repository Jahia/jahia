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
package org.jahia.ajax.gwt.client.data.versioning;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 15:21:41
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaField extends GWTJahiaRawField {

    private int fieldType;

    public GWTJahiaField() {
        super();
    }

    public GWTJahiaField(int fieldType, String name, String title, String icon, boolean bigText, String originalValue,
                      String newValue,String mergedDiffValue)
    {
        super(name,title,icon,bigText,originalValue,newValue,mergedDiffValue);
        this.fieldType = fieldType;
    }

    public int getFieldType() {
        return fieldType;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

}