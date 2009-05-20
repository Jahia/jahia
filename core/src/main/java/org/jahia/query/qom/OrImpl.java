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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Or;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 10:25:01
 * To change this template use File | Settings | File Templates.
 */
public class OrImpl extends ConstraintImpl implements Or {

    private ConstraintImpl constraint1;
    private ConstraintImpl constraint2;

    public OrImpl(ConstraintImpl constraint1, ConstraintImpl constraint2) {
        this.constraint1 = constraint1;
        this.constraint2 = constraint2;
    }

    public Constraint getConstraint1() {
        return constraint1;
    }

    public void setConstraint1(ConstraintImpl constraint1) {
        this.constraint1 = constraint1;
    }

    public Constraint getConstraint2() {
        return constraint2;
    }

    public void setConstraint2(ConstraintImpl constraint2) {
        this.constraint2 = constraint2;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(OrImpl.class.getName());
        buffer.append(" ( constraint1=");
        if ( this.constraint1 != null ){
            buffer.append(this.constraint1.toString());
        } else {
            buffer.append("null");
        }
        buffer.append(" OR ");
        buffer.append("operand2=");
        if ( this.constraint2 != null ){
            buffer.append(this.constraint2.toString());
        } else {
            buffer.append("null");
        }
        buffer.append(" ) ");
        return buffer.toString();
    }

}
