/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.apache.jackrabbit.core.query.lucene.join;

import org.apache.jackrabbit.commons.query.qom.OperandEvaluator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;
import javax.jcr.query.qom.*;
import javax.jcr.query.qom.Join;
import java.util.*;

import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;

public class JahiaEquiJoinMerger extends EquiJoinMerger {

    private final PropertyValue leftProperty;

    private final PropertyValue rightProperty;    
    private boolean includeTranslationNode = false;

    public JahiaEquiJoinMerger(Join join, Map<String, PropertyValue> columns,
            OperandEvaluator evaluator, QueryObjectModelFactory factory,
            EquiJoinCondition condition) throws RepositoryException {
        super(join, columns, evaluator, factory, condition);
        
        PropertyValue property1 = factory.propertyValue(
                condition.getSelector1Name(), condition.getProperty1Name());
        PropertyValue property2 = factory.propertyValue(
                condition.getSelector2Name(), condition.getProperty2Name());

        if (leftSelectors.contains(property1.getSelectorName())
                && rightSelectors.contains(property2.getSelectorName())) {
            leftProperty = property1;
            rightProperty = property2;
        } else if (leftSelectors.contains(property2.getSelectorName())
                && rightSelectors.contains(property1.getSelectorName())) {
            leftProperty = property2;
            rightProperty = property1;
        } else {
            throw new RepositoryException("Invalid equi-join");
        }        
    }
    
    @Override
    public List<Constraint> getRightJoinConstraints(Collection<Row> leftRows)
            throws RepositoryException {
        return isIncludeTranslationNode() ? getRightJoinConstraintsWithTranslation(leftRows)
                : super.getRightJoinConstraints(leftRows);
    }
    
    public List<Constraint> getRightJoinConstraintsWithTranslation(Collection<Row> leftRows)
            throws RepositoryException {
        Map<String, Literal> literals = new HashMap<String, Literal>();
        for (Row leftRow : leftRows) {
            for (Value value : evaluator.getValues(leftProperty, leftRow)) {
                literals.put(value.getString(), factory.literal(value));
            }
        }

        List<Constraint> constraints =
            new ArrayList<Constraint>(literals.size());
        for (Literal literal : literals.values()) {
            constraints.add(factory.comparison(rightProperty,
                    JCR_OPERATOR_EQUAL_TO, literal));
            constraints.add(factory.comparison(factory.propertyValue(
                    rightProperty.getSelectorName(), "_PARENT"),
                    JCR_OPERATOR_EQUAL_TO, literal));
        }
        return constraints;
    }    

    public boolean isIncludeTranslationNode() {
        return includeTranslationNode;
    }

    public void setIncludeTranslationNode(boolean includeTranslationNode) {
        this.includeTranslationNode = includeTranslationNode;
    }
    
}
