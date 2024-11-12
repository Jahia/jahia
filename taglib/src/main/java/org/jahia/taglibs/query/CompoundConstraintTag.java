/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Constraint;
import javax.servlet.jsp.JspTagException;

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
