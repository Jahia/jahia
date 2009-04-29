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
package org.jahia.query.qom;

import org.jahia.exceptions.JahiaException;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyValue;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 11:10:55
 * To change this template use File | Settings | File Templates.
 */
public class PropertyValueImpl extends DynamicOperandImpl implements PropertyValue {

    private String propertyName;
    private String selectorName;
    private boolean multiValue;
    private boolean numberValue;
    private String numberFormat;
    private String valueProviderClass;
    private boolean isMetadata;
    private String[] aliasNames;    

    public PropertyValueImpl(String selectorName,String propertyName) {
        this.selectorName = selectorName;
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getSelectorName() {
        return selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

    public boolean isMetadata() {
        return isMetadata;
    }

    public void setMetadata(boolean metadata) {
        isMetadata = metadata;
    }

    public boolean getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
    }

    public boolean getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(boolean numberValue) {
        this.numberValue = numberValue;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String getValueProviderClass() {
        return valueProviderClass;
    }

    public void setValueProviderClass(String valueProviderClass) {
        this.valueProviderClass = valueProviderClass;
    }

    public String[] getAliasNames() {
        return aliasNames;
    }

    public void setAliasNames(String[] aliasNames) {
        this.aliasNames = aliasNames;
    }
        
    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

}
