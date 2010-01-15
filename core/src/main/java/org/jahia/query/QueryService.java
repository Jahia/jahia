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
package org.jahia.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.axis.utils.StringUtils;
import org.apache.jackrabbit.spi.commons.query.qom.AbstractQOMNode;
import org.apache.jackrabbit.spi.commons.query.qom.AndImpl;
import org.apache.jackrabbit.spi.commons.query.qom.BindVariableValueImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ChildNodeImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ColumnImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ComparisonImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ConstraintImpl;
import org.apache.jackrabbit.spi.commons.query.qom.DefaultQOMTreeVisitor;
import org.apache.jackrabbit.spi.commons.query.qom.DefaultTraversingQOMTreeVisitor;
import org.apache.jackrabbit.spi.commons.query.qom.DescendantNodeImpl;
import org.apache.jackrabbit.spi.commons.query.qom.DynamicOperandImpl;
import org.apache.jackrabbit.spi.commons.query.qom.FullTextSearchImpl;
import org.apache.jackrabbit.spi.commons.query.qom.FullTextSearchScoreImpl;
import org.apache.jackrabbit.spi.commons.query.qom.JoinConditionImpl;
import org.apache.jackrabbit.spi.commons.query.qom.JoinImpl;
import org.apache.jackrabbit.spi.commons.query.qom.LengthImpl;
import org.apache.jackrabbit.spi.commons.query.qom.LiteralImpl;
import org.apache.jackrabbit.spi.commons.query.qom.LowerCaseImpl;
import org.apache.jackrabbit.spi.commons.query.qom.NodeLocalNameImpl;
import org.apache.jackrabbit.spi.commons.query.qom.NodeNameImpl;
import org.apache.jackrabbit.spi.commons.query.qom.NotImpl;
import org.apache.jackrabbit.spi.commons.query.qom.OrImpl;
import org.apache.jackrabbit.spi.commons.query.qom.OrderingImpl;
import org.apache.jackrabbit.spi.commons.query.qom.PropertyExistenceImpl;
import org.apache.jackrabbit.spi.commons.query.qom.PropertyValueImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QOMTreeVisitor;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.apache.jackrabbit.spi.commons.query.qom.SameNodeImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SourceImpl;
import org.apache.jackrabbit.spi.commons.query.qom.StaticOperandImpl;
import org.apache.jackrabbit.spi.commons.query.qom.UpperCaseImpl;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.qom.ChildNodeJoinCondition;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;

/**
 * Created by IntelliJ IDEA. User: hollis Date: 20 nov. 2007 Time: 10:45:14 To change this template use File | Settings | File Templates.
 */
public class QueryService extends JahiaService {
    private static transient Logger logger = Logger
            .getLogger(QueryService.class);
    private static QueryService singletonInstance = null;
    private static int INITIALIZE_MODE = 1;
    private static int CHECK_FOR_MODIFICATION_MODE = 2;
    private static int MODIFY_MODE = 3;

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    private transient JCRSessionFactory sessionFactory;

    protected QueryService() {
    }

    /**
     * Return the unique service instance. If the instance does not exist, a new instance is created.
     * 
     * @return The unique service instance.
     */
    public synchronized static QueryService getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new QueryService();
        }
        return singletonInstance;
    }

    /**
     * Initializes the servlet dispatching service with parameters loaded from the Jahia configuration file.
     * 
     * @throws org.jahia.exceptions.JahiaInitializationException
     *             thrown in the case of an error during this initialization, that will be treated as a critical error in Jahia and probably
     *             stop execution of Jahia once and for all.
     */
    public void start() throws JahiaInitializationException {
    }

    public void stop() {
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Entry point to request a QueryObjectModelFactory
     * 
     * @return
     * @throws RepositoryException
     */
    public QueryObjectModelFactory getQueryObjectModelFactory()
            throws RepositoryException {
        return sessionFactory.getCurrentUserSession().getWorkspace()
                .getQueryManager().getQOMFactory();
    }

    public QueryObjectModel modifyAndOptimizeQuery(QueryObjectModel qom,
            Locale locale) throws RepositoryException {
        ModificationInfo info = getModificationInfo(qom.getSource(), qom
                .getConstraint(), qom.getOrderings(), qom.getColumns(), locale);
        return info.getNewQueryObjectModel() != null ? info
                .getNewQueryObjectModel() : qom;
    }

    public QueryObjectModel modifyAndOptimizeQuery(Source source,
            Constraint constraint, Ordering[] orderings, Column[] columns,
            Locale locale) throws RepositoryException {
        ModificationInfo info = getModificationInfo(source, constraint,
                orderings, columns, locale);
        return info.getNewQueryObjectModel() != null ? info
                .getNewQueryObjectModel() : getQueryObjectModelFactory()
                .createQuery(source, constraint, orderings, columns);
    }

    protected Source getModifiedSource(Source source, ModificationInfo info) {
        return info.getNewJoin() != null ? info.getNewJoin() : source;
    }

    protected ModificationInfo getModificationInfo(Source source,
            Constraint constraint, Ordering[] orderings, Column[] columns,
            Locale locale) throws RepositoryException {
        ModificationInfo info = new ModificationInfo(
                getQueryObjectModelFactory());
        Map<String, Selector> selectorsJoinedWithTranslation = checkSelectorForTranslationNodes(source);

        QOMTreeVisitor visitor = new QueryModifierAndOptimizerVisitor(info,
                source, selectorsJoinedWithTranslation, locale);

        try {
            info.setMode(INITIALIZE_MODE);
            ((ConstraintImpl) constraint).accept(visitor, null);

            for (Ordering ordering : orderings) {
                ((OrderingImpl) ordering).accept(visitor, null);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        try {
            info.setMode(CHECK_FOR_MODIFICATION_MODE);
            ((ConstraintImpl) constraint).accept(visitor, null);

            for (Ordering ordering : orderings) {
                ((OrderingImpl) ordering).accept(visitor, null);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        if (info.isModificationNecessary()) {
            makeModifications(source, constraint, orderings, columns, info,
                    visitor, locale);
        }

        return info;
    }

    private void makeModifications(Source source, Constraint constraint,
            Ordering[] orderings, Column[] columns, ModificationInfo info,
            QOMTreeVisitor visitor, Locale locale) throws RepositoryException {
        info.setMode(MODIFY_MODE);

        try {
            Constraint newConstraint = (Constraint) ((ConstraintImpl) constraint)
                    .accept(visitor, null);
            for (Constraint constraintToAdd : info.getNewConstraints()) {
                newConstraint = info.getQueryObjectModelFactory().and(
                        newConstraint, constraintToAdd);
            }

            Ordering[] newOrderings = new Ordering[orderings.length];
            int i = 0;
            for (Ordering ordering : orderings) {
                Ordering newOrdering = ordering;

                newOrdering = (Ordering) ((OrderingImpl) ordering).accept(
                        visitor, null);
                newOrderings[i++] = newOrdering;
            }
            info.setNewQueryObjectModel(info.getQueryObjectModelFactory()
                    .createQuery(getModifiedSource(source, info),
                            newConstraint, newOrderings, columns));
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

    }

    private Map<String, Selector> checkSelectorForTranslationNodes(
            final Source source) {
        Map<String, Selector> selectorsJoinedWithTranslation = new HashMap<String, Selector>();
        try {
            ((SourceImpl) source).accept(new DefaultTraversingQOMTreeVisitor() {
                public Object visit(JoinImpl node, Object data)
                        throws Exception {
                    if (node.getJoinCondition() instanceof ChildNodeJoinCondition) {
                        ChildNodeJoinCondition childNodeJoin = (ChildNodeJoinCondition) node;
                        String childSelectorName = childNodeJoin
                                .getChildSelectorName();
                        Selector childSelector = getSelector(source,
                                childSelectorName);
                        if (Constants.JAHIANT_TRANSLATION.equals(childSelector
                                .getNodeTypeName())) {
                            ((HashMap<String, Selector>) data).put(
                                    childNodeJoin.getParentSelectorName(),
                                    childSelector);
                        }
                    }
                    return super.visit(node, data);
                }

                public Object visit(SelectorImpl node, Object data)
                        throws Exception {
                    if (node.equals(source)
                            && Constants.JAHIANT_TRANSLATION.equals(node
                                    .getNodeTypeName())) {
                        ((HashMap<String, Selector>) data).put(node
                                .getSelectorName(), node);
                    }

                    return super.visit(node, data);
                }
            }, selectorsJoinedWithTranslation);
        } catch (Exception e) {
            logger.warn("Error checking selector for translation node", e);
        }
        return selectorsJoinedWithTranslation;
    }

    private Selector getSelector(Source source, String name) {
        Selector foundSelector = null;
        for (SelectorImpl selector : ((SourceImpl) source).getSelectors()) {
            if (StringUtils.isEmpty(name)
                    || name.equals(selector.getSelectorName())) {
                foundSelector = selector;
                break;
            }
        }
        return foundSelector;
    }

    /**
     * Entry point to request a ValueFactory instance
     * 
     * @return
     */
    public ValueFactory getValueFactory() {
        return this.valueFactory;
    }

    class QueryModifierAndOptimizerVisitor extends DefaultQOMTreeVisitor {
        private ModificationInfo modificationInfo;
        private Source originalSource;
        private Map<String, Selector> selectorsJoinedWithTranslation;
        private Map<String, String> languagesPerSelector = new HashMap<String, String>();
        private Map<String, Set<String>> nodeTypesPerSelector = new HashMap<String, Set<String>>();
        private Locale currentLocale;

        public QueryModifierAndOptimizerVisitor(
                ModificationInfo modificationInfo, Source originalSource,
                Map<String, Selector> selectorsJoinedWithTranslation,
                Locale currentLocale) {
            super();
            this.modificationInfo = modificationInfo;
            this.originalSource = originalSource;
            this.selectorsJoinedWithTranslation = selectorsJoinedWithTranslation;
            this.currentLocale = currentLocale;
        }

        public Object visit(ChildNodeImpl node, Object data) throws Exception {
            if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                if (Constants.NT_BASE.equals(getSelector(originalSource,
                        node.getSelectorName()).getNodeTypeName())) {
                    Set<String> commonChildNodeTypes = new HashSet<String>();
                    String primaryChildNodeType = getCommonChildNodeTypes(node
                            .getParentPath(), commonChildNodeTypes);
                    if (primaryChildNodeType != null) {
                        commonChildNodeTypes = new HashSet<String>();
                        commonChildNodeTypes.add(primaryChildNodeType);
                    }
                    nodeTypesPerSelector.put(node.getSelectorName(),
                            commonChildNodeTypes);
                }
            }
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = node;
            }
            return data;
        }

        public Object visit(PropertyValueImpl node, Object data)
                throws Exception {
            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData;
            }
            return data;
        }

        /**
         * Does nothing and returns <code>data</code>.
         */
        public Object visit(ColumnImpl node, Object data) throws Exception {

            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData;
            }

            return data;
        }

        /**
         * Does nothing and returns <code>data</code>.
         */
        public Object visit(FullTextSearchImpl node, Object data)
                throws Exception {

            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData;
            }

            return data;
        }

        /**
         * Does nothing and returns <code>data</code>.
         */
        public Object visit(PropertyExistenceImpl node, Object data)
                throws Exception {

            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData;
            }

            return data;
        }

        /**
         * Calls accept on each of the attached constraints of the AND node.
         */
        public final Object visit(AndImpl node, Object data) throws Exception {
            Constraint constraint1 = node.getConstraint1();
            Constraint constraint2 = node.getConstraint2();
            boolean modified = false;
            Object returnedData = ((ConstraintImpl) node.getConstraint1())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE
                    && returnedData != null
                    && !returnedData.equals(node.getConstraint1())) {
                constraint1 = (Constraint) returnedData;
                modified = true;
            }
            returnedData = ((ConstraintImpl) node.getConstraint2()).accept(
                    this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE
                    && returnedData != null
                    && !returnedData.equals(node.getConstraint2())) {
                constraint2 = (Constraint) returnedData;
                modified = true;
            }
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = modified ? getQueryObjectModelFactory().and(constraint1,
                        constraint2) : node;
            }
            return data;
        }

        /**
         * Calls accept on the two operands in the comparison node.
         */
        public Object visit(ComparisonImpl node, Object data) throws Exception {
            DynamicOperand operand1 = node.getOperand1();
            boolean modified = false;
            Object returnedData = ((DynamicOperandImpl) node.getOperand1())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE
                    && returnedData != null
                    && !returnedData.equals(node.getOperand1())) {
                operand1 = (DynamicOperand) returnedData;
                modified = true;
            }
            ((StaticOperandImpl) node.getOperand2()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = modified ? getQueryObjectModelFactory().comparison(
                        operand1, node.getOperator(), node.getOperand2())
                        : node;
            }

            return data;
        }

        /**
         * Calls accept on the two sources and the join condition in the join node.
         */
        public Object visit(JoinImpl node, Object data) throws Exception {
            ((SourceImpl) node.getRight()).accept(this, data);
            ((SourceImpl) node.getLeft()).accept(this, data);
            ((JoinConditionImpl) node.getJoinCondition()).accept(this, data);
            return data;
        }

        /**
         * Calls accept on the property value in the length node.
         */
        public Object visit(LengthImpl node, Object data) throws Exception {
            Object returnedData = ((PropertyValueImpl) node.getPropertyValue())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null
                        && !returnedData.equals(node.getPropertyValue()) ? getQueryObjectModelFactory()
                        .length((PropertyValue) returnedData)
                        : node;
            }
            return data;
        }

        /**
         * Calls accept on the dynamic operand in the lower-case node.
         */
        public Object visit(LowerCaseImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null
                        && !returnedData.equals(node.getOperand()) ? getQueryObjectModelFactory()
                        .lowerCase((DynamicOperand) returnedData)
                        : node;
            }
            return data;
        }

        /**
         * Calls accept on the constraint in the NOT node.
         */
        public Object visit(NotImpl node, Object data) throws Exception {
            Object returnedData = ((ConstraintImpl) node.getConstraint())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null
                        && !returnedData.equals(node.getConstraint()) ? getQueryObjectModelFactory()
                        .not((Constraint) data)
                        : node;
            }
            return data;
        }

        /**
         * Calls accept on the dynamic operand in the ordering node.
         */
        public Object visit(OrderingImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand())
                    .accept(this, data);

            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null
                        && !returnedData.equals(node.getOperand()) ? (node
                        .isAscending() ? getQueryObjectModelFactory()
                        .ascending((DynamicOperand) returnedData)
                        : getQueryObjectModelFactory().descending(
                                (DynamicOperand) returnedData)) : node;
            }
            return data;
        }

        /**
         * Calls accept on each of the attached constraints of the OR node.
         */
        public final Object visit(OrImpl node, Object data) throws Exception {
            Constraint constraint1 = node.getConstraint1();
            Constraint constraint2 = node.getConstraint2();
            boolean modified = false;
            Object returnedData = ((ConstraintImpl) node.getConstraint1())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE
                    && returnedData != null
                    && !returnedData.equals(node.getConstraint1())) {
                constraint1 = (Constraint) returnedData;
                modified = true;
            }
            returnedData = ((ConstraintImpl) node.getConstraint2()).accept(
                    this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE
                    && returnedData != null
                    && !returnedData.equals(node.getConstraint2())) {
                constraint2 = (Constraint) returnedData;
                modified = true;
            }
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = modified ? getQueryObjectModelFactory().or(constraint1,
                        constraint2) : node;
            }
            return data;
        }

        /**
         * Calls accept on the following contained QOM nodes:
         * <ul>
         * <li>Source</li>
         * <li>Constraints</li>
         * <li>Orderings</li>
         * <li>Columns</li>
         * </ul>
         */
        public Object visit(QueryObjectModelTree node, Object data)
                throws Exception {
            node.getSource().accept(this, data);

            ConstraintImpl constraint = node.getConstraint();
            Object newConstraint = null;
            if (constraint != null) {
                newConstraint = constraint.accept(this, data);
            }
            OrderingImpl[] orderings = node.getOrderings();
            Object[] newOrderingObjects = new Object[orderings.length];
            for (int i = 0; i < orderings.length; i++) {
                newOrderingObjects[i] = orderings[i].accept(this, data);
            }
            ColumnImpl[] columns = node.getColumns();
            Object[] newColumnObjects = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                newColumnObjects[i] = columns[i].accept(this, data);
            }
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = newConstraint != null
                        && !newConstraint.equals(constraint)
                        || newOrderingObjects != null
                        && !newOrderingObjects.equals(orderings)
                        || newColumnObjects != null
                        && !newColumnObjects.equals(columns) ? getQueryObjectModelFactory()
                        .createQuery(
                                getModifiedSource(node.getSource(),
                                        getModificationInfo()),
                                (Constraint) newConstraint,
                                (Ordering[]) newOrderingObjects,
                                (Column[]) newColumnObjects)
                        : node;
            }
            return data;
        }

        @Override
        public Object visit(UpperCaseImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand())
                    .accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null
                        && !returnedData.equals(node.getOperand()) ? getQueryObjectModelFactory()
                        .upperCase((DynamicOperand) data)
                        : node;
            }
            return data;
        }

        @Override
        public Object visit(BindVariableValueImpl node, Object data)
                throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        @Override
        public Object visit(DescendantNodeImpl node, Object data)
                throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        @Override
        public Object visit(FullTextSearchScoreImpl node, Object data)
                throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        @Override
        public Object visit(LiteralImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        @Override
        public Object visit(NodeLocalNameImpl node, Object data)
                throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        @Override
        public Object visit(NodeNameImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        @Override
        public Object visit(SameNodeImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node
                    : data);
        }

        private AbstractQOMNode getNewPropertyBasedNodeIfRequired(
                AbstractQOMNode node) throws RepositoryException {
            AbstractQOMNode newNode = node;
            String selectorName = null;
            String propertyName = null;
            if (node instanceof PropertyValue) {
                selectorName = ((PropertyValue) node).getSelectorName();
                propertyName = ((PropertyValue) node).getPropertyName();
            } else if (node instanceof FullTextSearch) {
                selectorName = ((FullTextSearch) node).getSelectorName();
                propertyName = ((FullTextSearch) node).getPropertyName();
            } else if (node instanceof PropertyExistence) {
                selectorName = ((PropertyExistence) node).getSelectorName();
                propertyName = ((PropertyExistence) node).getPropertyName();
            } else if (node instanceof Column) {
                selectorName = ((Column) node).getSelectorName();
                propertyName = ((Column) node).getPropertyName();
            }
            Selector selector = getSelector(getOriginalSource(), selectorName);

            try {
                String languageCode = getLanguagesPerSelector().get(
                        selector.getSelectorName());
                if (languageCode == null && currentLocale != null) {
                    languageCode = currentLocale.toString();
                }
                if (languageCode != null) {
                    ExtendedNodeType nodeType = NodeTypeRegistry.getInstance()
                            .getNodeType(selector.getNodeTypeName());

                    ExtendedPropertyDefinition propDef = getPropertyDefinition(
                            nodeType, selector, propertyName);
                    if (propDef != null && propDef.isInternationalized()) {
                        if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                            QueryObjectModelFactory qomFactory = getModificationInfo()
                                    .getQueryObjectModelFactory();
                            Selector translationSelector = getSelectorsJoinedWithTranslation()
                                    .get(selectorName);
                            String translationSelectorName = null;
                            if (translationSelector == null) {
                                translationSelectorName = "translationAdded"
                                        + selectorName;
                                translationSelector = qomFactory.selector(
                                        Constants.JAHIANT_TRANSLATION,
                                        translationSelectorName);
                                getSelectorsJoinedWithTranslation().put(
                                        selectorName, translationSelector);

                                getModificationInfo()
                                        .getNewConstraints()
                                        .add(
                                                qomFactory
                                                        .comparison(
                                                                qomFactory
                                                                        .propertyValue(
                                                                                translationSelectorName,
                                                                                Constants.JCR_LANGUAGE),
                                                                QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                                                                qomFactory
                                                                        .literal(getValueFactory()
                                                                                .createValue(
                                                                                        languageCode))));

                                Join newJoin = getModificationInfo()
                                        .getQueryObjectModelFactory()
                                        .join(
                                                getModificationInfo()
                                                        .getNewJoin() != null ? getModificationInfo()
                                                        .getNewJoin()
                                                        : getOriginalSource(),
                                                translationSelector,
                                                QueryObjectModelConstants.JCR_JOIN_TYPE_INNER,
                                                qomFactory
                                                        .childNodeJoinCondition(
                                                                translationSelectorName,
                                                                selectorName));
                                getModificationInfo().setNewJoin(newJoin);
                            }

                        } else if (getModificationInfo().getMode() == CHECK_FOR_MODIFICATION_MODE) {
                            getModificationInfo()
                                    .setModificationNecessary(true);
                        } else if (getModificationInfo().getMode() == MODIFY_MODE) {
                            QueryObjectModelFactory qomFactory = getModificationInfo()
                                    .getQueryObjectModelFactory();
                            Selector translationSelector = getSelectorsJoinedWithTranslation()
                                    .get(selectorName);
                            String translationSelectorName = translationSelector
                                    .getSelectorName();

                            String i18nPropertyName = propertyName + "_"
                                    + languageCode;
                            if (node instanceof PropertyValue) {
                                newNode = (AbstractQOMNode) qomFactory
                                        .propertyValue(translationSelectorName,
                                                i18nPropertyName);
                            } else if (node instanceof FullTextSearch) {
                                newNode = (AbstractQOMNode) qomFactory
                                        .fullTextSearch(
                                                translationSelectorName,
                                                propertyName,
                                                ((FullTextSearch) node)
                                                        .getFullTextSearchExpression());
                            } else if (node instanceof PropertyExistence) {
                                newNode = (AbstractQOMNode) qomFactory
                                        .propertyExistence(
                                                translationSelectorName,
                                                i18nPropertyName);
                            } else if (node instanceof Column) {
                                newNode = (AbstractQOMNode) qomFactory.column(
                                        translationSelectorName,
                                        i18nPropertyName, ((Column) node)
                                                .getColumnName());
                            }
                        }

                    }
                }
            } catch (NoSuchNodeTypeException e) {
                logger.debug("Type " + selector.getNodeTypeName()
                        + " not found in registry", e);
            }

            return newNode;
        }

        private ExtendedPropertyDefinition getPropertyDefinition(
                ExtendedNodeType nodeType, Selector selector,
                String propertyName) throws RepositoryException {
            ExtendedPropertyDefinition propDef = null;

            if (!Constants.JAHIANT_TRANSLATION.equals(nodeType.getName())) {
                if (Constants.NT_BASE.equals(nodeType.getName())) {
                    for (String commonNodeType : getNodeTypesPerSelector().get(
                            selector.getSelectorName())) {
                        nodeType = NodeTypeRegistry.getInstance().getNodeType(
                                commonNodeType);
                        propDef = nodeType.getPropertyDefinitionsAsMap().get(
                                propertyName);
                        if (propDef != null) {
                            break;
                        }
                    }
                } else {
                    propDef = nodeType.getPropertyDefinitionsAsMap().get(
                            propertyName);
                }
            }
            return propDef;
        }

        public ModificationInfo getModificationInfo() {
            return modificationInfo;
        }

        public Source getOriginalSource() {
            return originalSource;
        }

        public Map<String, Selector> getSelectorsJoinedWithTranslation() {
            return selectorsJoinedWithTranslation;
        }

        private String getCommonChildNodeTypes(String parentPath,
                Set<String> commonNodeTypes) throws RepositoryException {
            String commonPrimaryType = null;
            JCRNodeWrapper node = sessionFactory.getCurrentUserSession()
                    .getNode(parentPath);
            Set<String> checkedPrimaryTypes = new HashSet<String>();
            if (node.hasNodes()) {
                NodeIterator children = node.getNodes();
                if (children.getSize() < 100) {
                    while (children.hasNext()) {

                        JCRNodeWrapper child = (JCRNodeWrapper) children
                                .nextNode();
                        if (commonPrimaryType == null
                                && commonNodeTypes.isEmpty()) {
                            commonPrimaryType = child.getPrimaryNodeType()
                                    .getName();
                            commonNodeTypes.addAll(child.getNodeTypes());
                        } else if (commonPrimaryType != null
                                && child.getPrimaryNodeType().getName().equals(
                                        commonPrimaryType)) {
                            commonPrimaryType = null;
                        }
                        if (!checkedPrimaryTypes.contains(child
                                .getPrimaryNodeType().getName())) {
                            checkedPrimaryTypes.add(child.getPrimaryNodeType()
                                    .getName());
                            commonNodeTypes.retainAll(child.getNodeTypes());
                        }
                    }
                }
            }
            return commonPrimaryType;
        }

        public Map<String, String> getLanguagesPerSelector() {
            return languagesPerSelector;
        }

        public Map<String, Set<String>> getNodeTypesPerSelector() {
            return nodeTypesPerSelector;
        }
    };

    class ModificationInfo {
        private int mode = INITIALIZE_MODE;
        private boolean modificationNecessary = false;
        private Join newJoin = null;
        private QueryObjectModelFactory queryObjectModelFactory = null;
        private QueryObjectModel newQueryObjectModel = null;
        private List<Constraint> newConstraints = new ArrayList<Constraint>();

        public ModificationInfo(QueryObjectModelFactory queryObjectModelFactory) {
            super();
            this.queryObjectModelFactory = queryObjectModelFactory;
        }

        public boolean isModificationNecessary() {
            return modificationNecessary;
        }

        public void setModificationNecessary(boolean modificationNecessary) {
            this.modificationNecessary = modificationNecessary;
        }

        public QueryObjectModelFactory getQueryObjectModelFactory() {
            return queryObjectModelFactory;
        }

        public Join getNewJoin() {
            return newJoin;
        }

        public void setNewJoin(Join newJoin) {
            setModificationNecessary(true);
            this.newJoin = newJoin;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public QueryObjectModel getNewQueryObjectModel() {
            return newQueryObjectModel;
        }

        public void setNewQueryObjectModel(QueryObjectModel newQueryObjectModel) {
            this.newQueryObjectModel = newQueryObjectModel;
        }

        public List<Constraint> getNewConstraints() {
            return newConstraints;
        }

    }
}
