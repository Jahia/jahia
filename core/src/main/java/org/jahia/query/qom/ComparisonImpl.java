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
