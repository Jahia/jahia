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
package org.jahia.taglibs.query;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.qom.ConstraintImpl;
import org.jahia.query.qom.QueryObjectModelFactoryImpl;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class AndTag extends ConstraintTag  {

    private Constraint andConstraint;
    private List<Constraint> constraints;

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        andConstraint = null;
        constraints = null;
        return eval;
    }

    public Constraint getConstraint() throws Exception {
        if ( andConstraint != null ){
            return andConstraint;
        }
        if ( this.constraints == null || this.constraints.isEmpty() ){
            return null;
        }
        if ( this.constraints.size()==1 ){
            andConstraint = (ConstraintImpl)this.constraints.get(0);
        } else {
            andConstraint = ((QueryObjectModelFactoryImpl)this.getQueryFactory()).and(this.constraints);
        }
        return andConstraint;
    }

    public void addChildConstraint(Constraint constraint){
        if ( constraint == null ){
            return;
        }
        if ( this.constraints == null ){
            this.constraints = new ArrayList<Constraint>();
        }
        this.constraints.add(constraint);
    }

}
