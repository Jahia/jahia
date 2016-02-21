/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.query;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Constraint;
import javax.servlet.jsp.JspTagException;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents constraint aggregation tag (AND or OR) that is used to conjunct
 * multiple constraints together.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class CompoundConstraintTag extends ConstraintTag {

    private static final long serialVersionUID = -2449018230515946004L;

    private List<Constraint> constraints = new LinkedList<Constraint>();

    public final void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    /**
     * Performs the conjunction/disjunction of the provided constraints.
     * 
     * @param constraint1 the first constraint to use in the logical operation
     * @param constraint2 the first constraint to use in the logical operation
     * @return the resulting constraint
     * @throws InvalidQueryException
     * @throws JspTagException
     * @throws RepositoryException
     */
    protected abstract Constraint doLogic(Constraint constraint1, Constraint constraint2) throws InvalidQueryException,
            JspTagException, RepositoryException;

    @Override
    protected Constraint getConstraint() throws Exception {
        Constraint compoundConstraint = null;
        if (!constraints.isEmpty()) {
            if (constraints.size() == 1) {
                compoundConstraint = constraints.get(0);
            } else {
                for (Constraint constraint : constraints) {
                    compoundConstraint = compoundConstraint != null ? doLogic(compoundConstraint,
                            constraint) : constraint;
                }
            }
        }
        return compoundConstraint;
    }

    @Override
    protected void resetState() {
        constraints = new LinkedList<Constraint>();
        super.resetState();
    }
}
