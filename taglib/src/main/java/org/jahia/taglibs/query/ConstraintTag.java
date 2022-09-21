/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import javax.jcr.query.qom.Constraint;
import javax.servlet.jsp.JspException;

/**
 * Base query constraint tag class.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:36:14
 */
public abstract class ConstraintTag extends QOMBuildingTag {

    private static final long serialVersionUID = 4590901723948727733L;

    @Override
    public final int doEndTag() throws JspException {
        try {
            CompoundConstraintTag compoundConstraintTag = (CompoundConstraintTag) findAncestorWithClass(this, CompoundConstraintTag.class);
            if (compoundConstraintTag != null) {
                // this constraint is nested within a compound constraint tag (AND or OR) 
                compoundConstraintTag.addConstraint(getConstraint());
            } else {
                getQOMBuilder().andConstraint(getConstraint());
            }
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
            resetState();
        }
        return EVAL_PAGE;
    }

    protected abstract Constraint getConstraint() throws Exception;
}
