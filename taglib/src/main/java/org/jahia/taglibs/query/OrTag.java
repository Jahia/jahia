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