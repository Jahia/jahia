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
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Not;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 10:26:32
 * To change this template use File | Settings | File Templates.
 */
public class NotImpl extends ConstraintImpl implements Not, NoTerminalConstraint {

    private ConstraintImpl constraint;

    public NotImpl(ConstraintImpl constraint) {
        this.constraint = constraint;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(ConstraintImpl constraint) {
        this.constraint = constraint;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer(NotImpl.class.getName());
        buffer.append(" NOT (").append(this.constraint).append(") ");
        return buffer.toString();
    }

}
