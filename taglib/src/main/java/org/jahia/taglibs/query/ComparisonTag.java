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

package org.jahia.taglibs.query;

import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.LiteralImpl;
import org.jahia.query.qom.PropertyValueImpl;
import org.jahia.query.qom.QueryModelTools;
import org.jahia.utils.JahiaTools;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ComparisonTag extends ConstraintTag  {

    private Comparison comparison;

    private String propertyName;
    private String aliasNames;        
    private String numberFormat;
    private String numberValue;
    private String isMetadata;
    private String multiValue;
    private String multiValueANDLogic = "false";
    private String valueProviderClass;

    private int operator = JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO;
    private List<String> value;

    public ComparisonTag(){        
    }

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        comparison = null;
        operator = JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO;
        value = null;
        propertyName = null;
        aliasNames = null;
        numberFormat = null;
        numberValue = null;
        isMetadata= null;
        multiValue = null;
        multiValueANDLogic = "false";
        valueProviderClass = null;
        return eval;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public void setAliasNames(String aliasNames) {
        this.aliasNames = aliasNames;
    }

    public String getAliasNames() {
        return aliasNames;
    }    

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public String getValue() {
        if ( value == null ){
            value = new ArrayList<String>();
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        Iterator<String> it = value.iterator();
        while (it.hasNext()){
            buffer.append(it.next());
            if ( it.hasNext() ){
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    public void setValue(String value) {
        this.value = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(value,",");
        while (tokenizer.hasMoreElements()){
            this.value.add(tokenizer.nextToken().trim());
        }
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(String numberValue) {
        this.numberValue = numberValue;
    }

    public String getMetadata() {
        return isMetadata;
    }

    public void setMetadata(String metadata) {
        isMetadata = metadata;
    }

    public String getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(String multiValue) {
        this.multiValue = multiValue;
    }

    public String getMultiValueANDLogic() {
        return multiValueANDLogic;
    }

    public void setMultiValueANDLogic(String multiValueANDLogic) {
        this.multiValueANDLogic = multiValueANDLogic;
    }

    public String getValueProviderClass() {
        return valueProviderClass;
    }

    public void setValueProviderClass(String valueProviderClass) {
        this.valueProviderClass = valueProviderClass;
    }

    public Constraint getConstraint() throws Exception {
        if ( comparison != null ){
            return comparison;
        }
        if ( !QueryModelTools.isNotEmptyStringOrNull(this.getPropertyName()) ){
          throw new Exception("propertyName is empty or null");
        }
        PropertyValueImpl propValue = (PropertyValueImpl)this.getQueryFactory()
                .propertyValue(this.getPropertyName().trim());
        propValue.setMultiValue("true".equals(this.getMultiValue()));
        propValue.setNumberValue("true".equals(this.getNumberValue()));
        propValue.setMetadata("true".equals(this.getMetadata()));
        propValue.setNumberFormat(this.getNumberFormat());
        propValue.setValueProviderClass(this.getValueProviderClass());
        String[] aliasNames = JahiaTools.getTokens(this.getAliasNames(),",");
        if (aliasNames != null){
            propValue.setAliasNames(aliasNames);
        }         
        LiteralImpl literal = (LiteralImpl)this.getQueryFactory()
                .literal(this.getValueFactory().createValue(this.getValue()));
        literal.setMultiValueANDLogic("true".equals(this.getMultiValueANDLogic()));
        comparison = this.getQueryFactory().comparison( propValue, this.getOperator(), literal );
        return comparison;
    }

}
