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
package org.jahia.query.qom;

import org.jahia.exceptions.JahiaException;

import javax.jcr.Value;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DynamicOperand;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.StaticOperand;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 10:28:39
 * To change this template use File | Settings | File Templates.
 */
public class ComparisonImpl extends ConstraintImpl implements Comparison, Constraint {

    private PropertyValueImpl operand1;
    private StaticOperandImpl operand2;
    private int operator;

    public ComparisonImpl( PropertyValueImpl operand1, int operator, StaticOperandImpl operand2 ) {
        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;
    }

    public DynamicOperand getOperand1() {
        return operand1;
    }

    public void setOperand1(PropertyValueImpl operand1) {
        this.operand1 = operand1;
    }

    public StaticOperand getOperand2() {
        return operand2;
    }

    public void setOperand2(StaticOperandImpl operand2) {
        this.operand2 = operand2;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer(ComparisonImpl.class.getName());
        buffer.append("operand1=");
        if ( this.operand1 != null ){
            buffer.append(this.operand1.getPropertyName());
            if ( this.operand1.getAliasNames() != null && this.operand1.getAliasNames().length > 0){
                buffer.append(" (alias=").append(this.operand1.getAliasNames().toString()).append(")");
            }            
        } else {
            buffer.append("unknownProperty");
        }
        buffer.append(" operator=\'")
                .append(JahiaQueryObjectModelConstants.OPERATOR_LABELS.get(new Integer(this.operator)))
                .append("\'");
        buffer.append(" operand2=");
        if ( this.operand2 != null ){
            Value val = ((LiteralImpl)this.operand2).getValue();
            if ( val != null ){
                buffer.append(val.toString());
            } else {
                buffer.append("null");
            }
        } else {
            buffer.append("null");
        }
        return buffer.toString();
    }

}
