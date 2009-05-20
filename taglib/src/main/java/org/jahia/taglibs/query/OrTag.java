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
import java.util.Iterator;
import java.util.List;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import javax.servlet.jsp.JspException;

import org.jahia.query.qom.ConstraintImpl;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class OrTag extends ConstraintTag  {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(OrTag.class);

    private Constraint orConstraint;
    private List<Constraint> constraints;

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        orConstraint = null;
        constraints = null;
        return eval;
    }

    public Constraint getConstraint(){
        if ( orConstraint != null ){
            return orConstraint;
        }
        if ( this.constraints == null || this.constraints.isEmpty() ){
            return null;
        }
        if ( this.constraints.size()==1 ){
            orConstraint = (ConstraintImpl)this.constraints.get(0);
        } else {
            Iterator<Constraint> it = constraints.iterator();
            Constraint c1 = it.next();
            Constraint c2 = null;
            try {
                while (it.hasNext()){
                    c2 = it.next();
                    c1 = this.getQueryFactory().or(c1,c2);
                }
                orConstraint = c1;
            } catch ( Exception t ){
                logger.debug("Exception occured",t);
                orConstraint = null;
            }
        }
        return orConstraint;
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