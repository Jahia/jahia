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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
