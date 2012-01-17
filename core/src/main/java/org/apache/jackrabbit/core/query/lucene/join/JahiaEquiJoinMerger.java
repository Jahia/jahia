/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.query.lucene.join;

import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModelFactory;

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
    public List<Constraint> getRightJoinConstraints(List<Row> leftRows)
            throws RepositoryException {
        return isIncludeTranslationNode() ? getRightJoinConstraintsWithTranslation(leftRows)
                : super.getRightJoinConstraints(leftRows);
    }
    
    public List<Constraint> getRightJoinConstraintsWithTranslation(List<Row> leftRows)
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
