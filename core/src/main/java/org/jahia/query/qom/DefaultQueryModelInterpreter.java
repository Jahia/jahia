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

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 f�vr. 2008
 * Time: 14:53:31
 * To change this template use File | Settings | File Templates.
 */
public class DefaultQueryModelInterpreter implements QueryObjectModelInterpreter {

    public void accept(AndImpl node) throws JahiaException {
        ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
        c1.accept(this);
        ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
        c2.accept(this);
    }

    public void accept(ComparisonImpl node) throws JahiaException {

    }

    public void accept(FullTextSearchImpl node) throws JahiaException {

    }

    public void accept(NotImpl node) throws JahiaException {
        ConstraintImpl c = (ConstraintImpl)node.getConstraint();
        c.accept(this);
    }

    public void accept(OrImpl node) throws JahiaException {
        ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
        c1.accept(this);
        ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
        c2.accept(this);
    }

    public void accept(ChildNodeImpl node) throws JahiaException {

    }

    public void accept(DescendantNodeImpl node) throws JahiaException {

    }

    public void accept(BindVariableValueImpl node) throws JahiaException {

    }

    public void accept(ColumnImpl node) throws JahiaException {

    }

    public void accept(LiteralImpl node) throws JahiaException {

    }

    public void accept(PropertyValueImpl node) throws JahiaException {

    }

    public void accept(OrderingImpl node) throws JahiaException {

    }

    public void accept(SelectorImpl node) throws JahiaException {

    }

    public void accept(QueryObjectModelImpl queryObjectModel) throws JahiaException {

    }

}
