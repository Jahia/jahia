/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.query;

import org.jahia.query.qom.QueryModelTools;

import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModelConstants;
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

    private String operator = QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
    private List<String> value;

    public ComparisonTag(){        
    }

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        comparison = null;
        operator = QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
        value = null;
        propertyName = null;
        return eval;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
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

    public Constraint getConstraint() throws Exception {
        if ( comparison != null ){
            return comparison;
        }
        if ( !QueryModelTools.isNotEmptyStringOrNull(this.getPropertyName()) ){
          throw new Exception("propertyName is empty or null");
        }
        PropertyValue propValue = this.getQueryFactory()
                .propertyValue(null,this.getPropertyName().trim());
        Literal literal = (Literal)this.getQueryFactory()
                .literal(this.getValueFactory().createValue(this.getValue()));
        comparison = this.getQueryFactory().comparison( propValue, this.getOperator(), literal );
        return comparison;
    }
}
