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
