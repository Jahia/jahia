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
package org.jahia.services.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.qom.ChildNodeJoinCondition;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.JoinCondition;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;

import org.apache.axis.utils.StringUtils;
import org.apache.jackrabbit.spi.commons.query.qom.AbstractQOMNode;
import org.apache.jackrabbit.spi.commons.query.qom.AndImpl;
import org.apache.jackrabbit.spi.commons.query.qom.BindVariableValueImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ChildNodeImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ChildNodeJoinConditionImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ColumnImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ComparisonImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ConstraintImpl;
import org.apache.jackrabbit.spi.commons.query.qom.DefaultQOMTreeVisitor;
import org.apache.jackrabbit.spi.commons.query.qom.DescendantNodeImpl;
import org.apache.jackrabbit.spi.commons.query.qom.DescendantNodeJoinConditionImpl;
import org.apache.jackrabbit.spi.commons.query.qom.DynamicOperandImpl;
import org.apache.jackrabbit.spi.commons.query.qom.EquiJoinConditionImpl;
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
import org.apache.jackrabbit.spi.commons.query.qom.SameNodeJoinConditionImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SourceImpl;
import org.apache.jackrabbit.spi.commons.query.qom.StaticOperandImpl;
import org.apache.jackrabbit.spi.commons.query.qom.UpperCaseImpl;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

/**
 * The default implementation of Jahia's QueryService.
 * 
 * Jahia's query service is based on the JCR QueryObjectModelFactory and thus supports all kinds of
 * complex queries specified in JSR-283 (Content Repository for Java™ Technology API 2.0)
 * 
 * Queries can be created with the API by using the QueryObjectModel {@link org.jahia.query.qom.QOMBuilder}.
 * Jahia will also provide a query builder user interface.
 * It is also possible to use SQL-2 and the deprecated XPATH language. 
 * 
 * As Jahia can plug-in multiple repositories via the universal content hub (UCH), the queries can be
 * converted to other languages, like the EntropySoft connector query language.
 * 
 * The query service provides methods to modify and optimize the queries to support and make use of Jahia's 
 * internal data model implementation.
 * 
 * @author Benjamin Papez
 */
public class QueryServiceImpl extends QueryService {
    private static transient Logger logger = Logger.getLogger(QueryService.class);

    private static QueryService singletonInstance = null;

    /**
     * The initiatlization mode for the first QOM traversing iteration
     */    
    public static int INITIALIZE_MODE = 1;
    
    /**
     * The check for modification mode for the second QOM traversing iteration
     */
    public static int CHECK_FOR_MODIFICATION_MODE = 2;
    
    /**
     * The optional modification mode for the third QOM traversing iteration only called
     * if query modification is necessary.
     */
    public static int MODIFY_MODE = 3;

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    private transient JCRSessionFactory sessionFactory;

    protected QueryServiceImpl() {
    }

    /**
     * Return the unique service instance. If the instance does not exist, a new
     * instance is created.
     * 
     * @return The unique service instance.
     */
    public synchronized static QueryService getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new QueryServiceImpl();
        }
        return singletonInstance;
    }

    /**
     * Initializes the servlet dispatching service with parameters loaded from
     * the Jahia configuration file.
     * 
     * @throws org.jahia.exceptions.JahiaInitializationException
     *             thrown in the case of an error during this initialization,
     *             that will be treated as a critical error in Jahia and
     *             probably stop execution of Jahia once and for all.
     */
    public void start() throws JahiaInitializationException {
    }

    public void stop() {
    }

    /**
     * Sets the JCR session factory
     * @param sessionFactory JCR session factory
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /* (non-Javadoc)
     * @see org.jahia.services.query.QueryService#getQueryObjectModelFactory()
     */
    @Override
    public QueryObjectModelFactory getQueryObjectModelFactory() throws RepositoryException {
        return sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().getQOMFactory();
    }

    /* (non-Javadoc)
     * @see org.jahia.services.query.QueryService#modifyAndOptimizeQuery(javax.jcr.query.qom.QueryObjectModel, java.util.Locale, javax.jcr.query.qom.QueryObjectModelFactory)
     */
    public QueryObjectModel modifyAndOptimizeQuery(QueryObjectModel qom, Locale locale,
            QueryObjectModelFactory qomFactory) throws RepositoryException {
        ModificationInfo info = getModificationInfo(qom.getSource(), qom.getConstraint(), qom.getOrderings(), qom
                .getColumns(), locale, qomFactory);
        return info.getNewQueryObjectModel() != null ? info.getNewQueryObjectModel() : qom;
    }

    /* (non-Javadoc)
     * @see org.jahia.services.query.QueryService#modifyAndOptimizeQuery(javax.jcr.query.qom.Source, javax.jcr.query.qom.Constraint, javax.jcr.query.qom.Ordering[], javax.jcr.query.qom.Column[], java.util.Locale, javax.jcr.query.qom.QueryObjectModelFactory)
     */
    public QueryObjectModel modifyAndOptimizeQuery(Source source, Constraint constraint, Ordering[] orderings,
            Column[] columns, Locale locale, QueryObjectModelFactory qomFactory) throws RepositoryException {
        ModificationInfo info = getModificationInfo(source, constraint, orderings, columns, locale, qomFactory);
        return info.getNewQueryObjectModel() != null ? info.getNewQueryObjectModel() : qomFactory.createQuery(source,
                constraint, orderings, columns);
    }

    protected Source getModifiedSource(Source source, ModificationInfo info) {
        return info.getNewJoin() != null ? info.getNewJoin() : source;
    }

    /**
     * We use a QOMTreeVisitor implementation to traverse through the query object model three times.
     * 
     * The ModificationInfo.mode changes before each iteration from INITIALIZE_MODE to 
     * CHECK_FOR_MODIFICATION_MODE and at last MODIFY_MODE, which is only called if modification is necessary.  
     */
    protected ModificationInfo getModificationInfo(Source source, Constraint constraint, Ordering[] orderings,
            Column[] columns, Locale locale, QueryObjectModelFactory qomFactory) throws RepositoryException {
        ModificationInfo info = new ModificationInfo(qomFactory);

        QOMTreeVisitor visitor = new QueryModifierAndOptimizerVisitor(info, source, locale);

        try {
            info.setMode(INITIALIZE_MODE);
            ((SourceImpl)source).accept(visitor, null);
            
            if (constraint != null) {
                ((ConstraintImpl) constraint).accept(visitor, null);
            }

            for (Ordering ordering : orderings) {
                ((OrderingImpl) ordering).accept(visitor, null);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        try {
            info.setMode(CHECK_FOR_MODIFICATION_MODE);
            ((SourceImpl)source).accept(visitor, null);
            
            if (constraint != null) {
                ((ConstraintImpl) constraint).accept(visitor, null);
            }

            for (Ordering ordering : orderings) {
                ((OrderingImpl) ordering).accept(visitor, null);
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        if (info.isModificationNecessary()) {
            makeModifications(source, constraint, orderings, columns, info, visitor, locale, qomFactory);
        }

        return info;
    }

    protected void makeModifications(Source source, Constraint constraint, Ordering[] orderings, Column[] columns,
            ModificationInfo info, QOMTreeVisitor visitor, Locale locale, QueryObjectModelFactory qomFactory)
            throws RepositoryException {
        info.setMode(MODIFY_MODE);

        try {
            Source newSource = (Source)((SourceImpl)getModifiedSource(source, info)).accept(visitor, null);
            
            Constraint newConstraint = null;
            if (constraint != null) {
                newConstraint = (Constraint) ((ConstraintImpl) constraint).accept(visitor, null);
                for (Constraint constraintToAdd : info.getNewConstraints()) {
                    newConstraint = info.getQueryObjectModelFactory().and(newConstraint, constraintToAdd);
                }
            }

            Ordering[] newOrderings = new Ordering[orderings.length];
            int i = 0;
            for (Ordering ordering : orderings) {
                Ordering newOrdering = ordering;

                newOrdering = (Ordering) ((OrderingImpl) ordering).accept(visitor, null);
                newOrderings[i++] = newOrdering;
            }
            info.setNewQueryObjectModel(info.getQueryObjectModelFactory().createQuery(newSource,
                    newConstraint, newOrderings, columns));
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

    }

    private Selector getSelector(Source source, String name) {
        Selector foundSelector = null;
        for (SelectorImpl selector : ((SourceImpl) source).getSelectors()) {
            if (StringUtils.isEmpty(name) || name.equals(selector.getSelectorName())) {
                foundSelector = selector;
                break;
            }
        }
        return foundSelector;
    }

    /* (non-Javadoc)
     * @see org.jahia.services.query.QueryService#getValueFactory()
     */
    public ValueFactory getValueFactory() {
        return this.valueFactory;
    }

    /**
     * We use this QOMTreeVisitor implementation to traverse through the query object model three times.
     * 
     * The ModificationInfo.mode changes before each iteration from INITIALIZE_MODE to 
     * CHECK_FOR_MODIFICATION_MODE and at last MODIFY_MODE, which is only called if modification is necessary.
     * 
     * In INITIALIZE_MODE we analyze the query to check, whether language constraints are already set for
     * selectors and we check whether selectors to nodes having internationlized properties are already joined with
     * their translation nodes and we store the node types per selector.
     * 
     * In CHECK_FOR_MODIFICATION_MODE we analyze the query to see, whether modifications needs to be made because
     * of Jahia's internal datamodel changes (mainly internationalized properties, which are copied to subnodes and
     * propertynames are suffixed with the language code). We will also check if modifications need to be done due to
     * performance optimizations. This mode sets the ModificationInfo.modificationNecessary variable to mark the 
     * necessity of modification.
     * 
     * The MODIFY_MODE is only called if modification appears necessary (after previous step). This mode returns a 
     * modified query object model in ModificationInfo.newQueryObjectModel.
     * 
     */
    class QueryModifierAndOptimizerVisitor extends DefaultQOMTreeVisitor {
        private ModificationInfo modificationInfo;

        private Source originalSource;

        private Map<String, Selector> selectorsJoinedWithTranslation = new HashMap<String, Selector>();

        private Map<String, List<String>> languagesPerSelector = new HashMap<String, List<String>>();

        private Map<String, Set<String>> nodeTypesPerSelector = new HashMap<String, Set<String>>();

        private Locale currentLocale;

        /**
         * Constructor for the QueryModifierAndOptimizerVisitor 
         * @param modificationInfo object gathering all modification infos
         * @param originalSource Source object of original query
         * @param currentLocale current locale to be used if query itself has no language constraint
         */
        public QueryModifierAndOptimizerVisitor(ModificationInfo modificationInfo, Source originalSource,
                Locale currentLocale) {
            super();
            this.modificationInfo = modificationInfo;
            this.originalSource = originalSource;
            this.currentLocale = currentLocale;
        }

        /**
         * In INITIALIZE_MODE checks whether a selector is set to nt:base and in such a case looks, which nodes are actually 
         * placed as child nodes. If all are the same then store the primaryChildNodeType, otherwise the nodetypes, common to
         * all child nodes. This is needed to be able to obtain property definitions.
         * 
         * In MODIFY_MODE return the unchanged node.
         */
        @Override        
        public Object visit(ChildNodeImpl node, Object data) throws Exception {
            if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                if (Constants.NT_BASE.equals(getSelector(getOriginalSource(), node.getSelectorName()).getNodeTypeName())) {
                    Set<String> commonChildNodeTypes = new HashSet<String>();
                    String primaryChildNodeType = getCommonChildNodeTypes(node.getParentPath(), commonChildNodeTypes);
                    if (primaryChildNodeType != null) {
                        commonChildNodeTypes = new HashSet<String>();
                        commonChildNodeTypes.add(primaryChildNodeType);
                    }
                    getNodeTypesPerSelector().put(node.getSelectorName(), commonChildNodeTypes);
                }
            }

            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the translation
         * subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE. 
         * 
         * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
         */
        @Override        
        public Object visit(PropertyValueImpl node, Object data) throws Exception {
            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : returnedData);
        }

        /**
         * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the translation
         * subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE. 
         * 
         * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
         */
        @Override        
        public Object visit(ColumnImpl node, Object data) throws Exception {
            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : returnedData);
        }

        /**
         * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the translation
         * subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE. 
         * 
         * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
         */
        @Override        
        public Object visit(FullTextSearchImpl node, Object data) throws Exception {
            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : returnedData);
        }

        /**
         * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the translation
         * subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE. 
         * 
         * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
         */
        @Override        
        public Object visit(PropertyExistenceImpl node, Object data) throws Exception {
            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : returnedData);
        }

        /**
         * Calls accept on each of the attached constraints of the AND node.
         * 
         * In MODIFY_MODE check if the constraints returned were modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node.
         */
        @Override        
        public final Object visit(AndImpl node, Object data) throws Exception {
            Constraint constraint1 = node.getConstraint1();
            Constraint constraint2 = node.getConstraint2();
            boolean modified = false;
            Object returnedData = ((ConstraintImpl) node.getConstraint1()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE && returnedData != null
                    && !returnedData.equals(node.getConstraint1())) {
                constraint1 = (Constraint) returnedData;
                modified = true;
            }
            returnedData = ((ConstraintImpl) node.getConstraint2()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE && returnedData != null
                    && !returnedData.equals(node.getConstraint2())) {
                constraint2 = (Constraint) returnedData;
                modified = true;
            }
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = modified ? getModificationInfo().getQueryObjectModelFactory().and(constraint1, constraint2)
                        : node;
            }
            return data;
        }

        /**
         * Calls accept on the two operands in the comparison node.
         * 
         * In INITIALIZE_MODE check whether there is already a language based comparison in the original query, so 
         * that this language is used instead of the current locale in the session.
         * 
         * In MODIFY_MODE check if the dynamic operand returned was modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node.   
         */
        @Override        
        public Object visit(ComparisonImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand1()).accept(this, data);

            if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                if (QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(node.getOperator())
                        && node.getOperand1() instanceof PropertyValue
                        && Constants.JCR_LANGUAGE.equals(((PropertyValue) node.getOperand1()).getPropertyName())) {
                    Selector selector = getSelector(getOriginalSource(), ((PropertyValue) node.getOperand1())
                            .getSelectorName());
                    List<String> languages = getLanguagesPerSelector().get(selector.getSelectorName());
                    if (languages == null) {
                        languages = new ArrayList<String>();
                        getLanguagesPerSelector().put(selector.getSelectorName(), languages);
                    }
                    String language = ((Literal) node.getOperand2()).getLiteralValue().getString();
                    if (!languages.contains(language)) {
                        languages.add(language);
                    }
                }
            }

            ((StaticOperandImpl) node.getOperand2()).accept(this, data);

            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null && !returnedData.equals(node.getOperand1()) ? getModificationInfo()
                        .getQueryObjectModelFactory().comparison((DynamicOperand) returnedData, node.getOperator(),
                                node.getOperand2()) : node;
            }

            return data;
        }

        /**
         * Calls accept on the two sources and the join condition in the join
         * node.
         * 
         * In INITIALIZE_MODE check whether the original query already contains a childnode-JOIN on the translation
         * node, so that it then does not have to be added.
         * 
         * In MODIFY_MODE check if the sources or join condition returned were modified, and if yes create a new 
         * node and return it, otherwise return the unchanged node. 
         */
        @Override        
        public Object visit(JoinImpl node, Object data) throws Exception {
            if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                if (node.getJoinCondition() instanceof ChildNodeJoinCondition) {
                    ChildNodeJoinCondition childNodeJoin = (ChildNodeJoinCondition) node.getJoinCondition();
                    String childSelectorName = childNodeJoin.getChildSelectorName();
                    Selector childSelector = getSelector(getOriginalSource(), childSelectorName);
                    if (Constants.JAHIANT_TRANSLATION.equals(childSelector.getNodeTypeName())) {
                        getSelectorsJoinedWithTranslation().put(childNodeJoin.getParentSelectorName(), childSelector);
                    }
                }
            }

            Object returnedRight = ((SourceImpl) node.getRight()).accept(this, data);
            Object returnedLeft = ((SourceImpl) node.getLeft()).accept(this, data);
            Object returnedJoinCondition = ((JoinConditionImpl) node.getJoinCondition()).accept(this, data);

            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedRight != null && !returnedRight.equals(node.getRight()) || returnedLeft != null
                        && !returnedLeft.equals(node.getLeft()) || returnedJoinCondition != null
                        && !returnedJoinCondition.equals(node.getJoinCondition()) ? getModificationInfo()
                        .getQueryObjectModelFactory().join(
                                returnedLeft != null ? (Source) returnedLeft : node.getLeft(),
                                returnedRight != null ? (Source) returnedRight : node.getRight(),
                                node.getJoinType(),
                                returnedJoinCondition != null ? (JoinCondition) returnedJoinCondition : node
                                        .getJoinCondition()) : node;
            }

            return data;
        }

        /**
         * Calls accept on the property value in the length node.
         * 
         * In MODIFY_MODE check if the property value returned was modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node. 
         */
        @Override
        public Object visit(LengthImpl node, Object data) throws Exception {
            Object returnedData = ((PropertyValueImpl) node.getPropertyValue()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null && !returnedData.equals(node.getPropertyValue()) ? getModificationInfo()
                        .getQueryObjectModelFactory().length((PropertyValue) returnedData) : node;
            }
            return data;
        }

        /**
         * Calls accept on the dynamic operand in the lower-case node.
         * 
         * In MODIFY_MODE check if the operand returned was modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node. 
         */
        @Override
        public Object visit(LowerCaseImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null && !returnedData.equals(node.getOperand()) ? getModificationInfo()
                        .getQueryObjectModelFactory().lowerCase((DynamicOperand) returnedData) : node;
            }
            return data;
        }

        /**
         * Calls accept on the constraint in the NOT node.
         * 
         * In MODIFY_MODE check if the constraint returned was modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node. 
         */
        @Override        
        public Object visit(NotImpl node, Object data) throws Exception {
            Object returnedData = ((ConstraintImpl) node.getConstraint()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null && !returnedData.equals(node.getConstraint()) ? getModificationInfo()
                        .getQueryObjectModelFactory().not((Constraint) data) : node;
            }
            return data;
        }

        /**
         * Calls accept on the dynamic operand in the ordering node.
         * 
         * In MODIFY_MODE check if the operand returned was modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node.  
         */
        @Override        
        public Object visit(OrderingImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand()).accept(this, data);

            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null && !returnedData.equals(node.getOperand()) ? (node.isAscending() ? getModificationInfo()
                        .getQueryObjectModelFactory().ascending((DynamicOperand) returnedData)
                        : getModificationInfo().getQueryObjectModelFactory().descending((DynamicOperand) returnedData))
                        : node;
            }
            return data;
        }

        /**
         * Calls accept on each of the attached constraints of the OR node.
         * 
         * In MODIFY_MODE check if the constraints returned were modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node. 
         */
        @Override        
        public final Object visit(OrImpl node, Object data) throws Exception {
            Constraint constraint1 = node.getConstraint1();
            Constraint constraint2 = node.getConstraint2();
            boolean modified = false;
            Object returnedData = ((ConstraintImpl) node.getConstraint1()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE && returnedData != null
                    && !returnedData.equals(node.getConstraint1())) {
                constraint1 = (Constraint) returnedData;
                modified = true;
            }
            returnedData = ((ConstraintImpl) node.getConstraint2()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE && returnedData != null
                    && !returnedData.equals(node.getConstraint2())) {
                constraint2 = (Constraint) returnedData;
                modified = true;
            }
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = modified ? getModificationInfo().getQueryObjectModelFactory().or(constraint1, constraint2)
                        : node;
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
         * 
         * In MODIFY_MODE check if the nodes returned were modified, and if yes create a new QOM and return it, 
         * otherwise return the unchanged QOM.  
         */
        @Override        
        public Object visit(QueryObjectModelTree node, Object data) throws Exception {
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
                data = newConstraint != null && !newConstraint.equals(constraint) || newOrderingObjects != null
                        && !newOrderingObjects.equals(orderings) || newColumnObjects != null
                        && !newColumnObjects.equals(columns) ? getModificationInfo().getQueryObjectModelFactory()
                        .createQuery(getModifiedSource(node.getSource(), getModificationInfo()),
                                (Constraint) newConstraint, (Ordering[]) newOrderingObjects,
                                (Column[]) newColumnObjects) : node;
            }
            return data;
        }

        /**
         * Calls accept on the dynamic operand in the lower-case node.
         * 
         * In MODIFY_MODE check if the operand returned was modified, and if yes create a new node and return it, 
         * otherwise return the unchanged node. 
         */
        @Override
        public Object visit(UpperCaseImpl node, Object data) throws Exception {
            Object returnedData = ((DynamicOperandImpl) node.getOperand()).accept(this, data);
            if (getModificationInfo().getMode() == MODIFY_MODE) {
                data = returnedData != null && !returnedData.equals(node.getOperand()) ? getModificationInfo()
                        .getQueryObjectModelFactory().upperCase((DynamicOperand) data) : node;
            }
            return data;
        }


        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(BindVariableValueImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(DescendantNodeImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * In MODIFY_MODE check if the selector is based on a nodetype, which is modified so that the translation subnode
         * is used in the query. In this case create a new fullTextSearchScore node pointing to the selector of the
         * translation nodetype. 
         */
        @Override
        public Object visit(FullTextSearchScoreImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? (getSelectorsJoinedWithTranslation().get(
                    node.getSelectorName()) != null ? getModificationInfo().getQueryObjectModelFactory()
                    .fullTextSearchScore(
                            getSelectorsJoinedWithTranslation().get(node.getSelectorName()).getSelectorName()) : node)
                    : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(LiteralImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(NodeLocalNameImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(NodeNameImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(SameNodeImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(ChildNodeJoinConditionImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(DescendantNodeJoinConditionImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the translation
         * subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE. 
         * 
         * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
         */
        @Override
        public Object visit(EquiJoinConditionImpl node, Object data) throws Exception {
            Object returnedData = getNewPropertyBasedNodeIfRequired(node);
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : returnedData);            
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(SameNodeJoinConditionImpl node, Object data) throws Exception {
            return (getModificationInfo().getMode() == MODIFY_MODE ? node : data);
        }

        /**
         * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
         */
        @Override
        public Object visit(SelectorImpl node, Object data) throws Exception {
            if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                if (node.equals(getOriginalSource()) && Constants.JAHIANT_TRANSLATION.equals(node.getNodeTypeName())) {
                    getSelectorsJoinedWithTranslation().put(node.getSelectorName(), node);
                }
            }
            return super.visit(node, data);
        }

        private AbstractQOMNode getNewPropertyBasedNodeIfRequired(AbstractQOMNode node) throws RepositoryException {
            AbstractQOMNode newNode = node;
            if (node instanceof EquiJoinCondition) {
                newNode = getNewPropertyBasedNodeIfRequired(((EquiJoinCondition) node).getSelector1Name(),
                        ((EquiJoinCondition) node).getProperty1Name(), node);
                newNode = getNewPropertyBasedNodeIfRequired(((EquiJoinCondition) node).getSelector2Name(),
                        ((EquiJoinCondition) node).getProperty2Name(), newNode);
            } else {
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
                newNode = getNewPropertyBasedNodeIfRequired(selectorName, propertyName, node);
            }
            return newNode;
        }
        
        private AbstractQOMNode getNewPropertyBasedNodeIfRequired (String selectorName, String propertyName, AbstractQOMNode node) throws RepositoryException {
            Selector selector = getSelector(getOriginalSource(), selectorName);

            try {
                List<String> languageCodes = getLanguagesPerSelector().get(selector.getSelectorName());
                if ((languageCodes == null || languageCodes.isEmpty()) && currentLocale != null) {
                    languageCodes = new ArrayList<String>();
                    languageCodes.add(currentLocale.toString());
                }
                if (languageCodes != null && !languageCodes.isEmpty()) {
                    ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(selector.getNodeTypeName());
                    boolean isFulltextIncludingMultilingualProperties = (propertyName == null && node instanceof FullTextSearch) ? 
                            isFulltextIncludingMultilingualProperties(nodeType, selector) : false;
                    ExtendedPropertyDefinition propDef = propertyName != null ? getPropertyDefinition(nodeType,
                            selector, propertyName) : null;
                    if (propDef != null && propDef.isInternationalized() || isFulltextIncludingMultilingualProperties) {
                        if (getModificationInfo().getMode() == INITIALIZE_MODE) {
                            QueryObjectModelFactory qomFactory = getModificationInfo().getQueryObjectModelFactory();
                            Selector translationSelector = getSelectorsJoinedWithTranslation().get(
                                    selector.getSelectorName());
                            String translationSelectorName = null;
                            if (translationSelector == null) {
                                translationSelectorName = "translationAdded" + selector.getSelectorName();
                                translationSelector = qomFactory.selector(Constants.JAHIANT_TRANSLATION,
                                        translationSelectorName);
                                getSelectorsJoinedWithTranslation()
                                        .put(selector.getSelectorName(), translationSelector);
                                if (getLanguagesPerSelector().get(selector.getSelectorName()) == null) {
                                    getModificationInfo().getNewConstraints().add(
                                            qomFactory.comparison(qomFactory.propertyValue(translationSelectorName,
                                                    Constants.JCR_LANGUAGE),
                                                    QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, qomFactory
                                                            .literal(getValueFactory()
                                                                    .createValue(languageCodes.get(0)))));
                                }

                                Join newJoin = getModificationInfo().getQueryObjectModelFactory().join(
                                        getModificationInfo().getNewJoin() != null ? getModificationInfo().getNewJoin()
                                                : getOriginalSource(),
                                        translationSelector,
                                        QueryObjectModelConstants.JCR_JOIN_TYPE_INNER,
                                        qomFactory.childNodeJoinCondition(translationSelectorName, selector
                                                .getSelectorName()));
                                getModificationInfo().setNewJoin(newJoin);
                            }
                        } else if (getModificationInfo().getMode() == CHECK_FOR_MODIFICATION_MODE) {
                            if (!(isFulltextIncludingMultilingualProperties && !getSelectorsJoinedWithTranslation()
                                    .get(selector.getSelectorName()).getSelectorName().startsWith("translationAdded"))) {
                                getModificationInfo().setModificationNecessary(true);
                            }
                        }
                    }
                    if (getModificationInfo().getMode() == MODIFY_MODE) {
                        QueryObjectModelFactory qomFactory = getModificationInfo().getQueryObjectModelFactory();
                        Selector translationSelector = getSelectorsJoinedWithTranslation().get(
                                selector.getSelectorName());
                        if (translationSelector != null) {
                            String translationSelectorName = translationSelector.getSelectorName();
                            if (propDef != null && propDef.isInternationalized()) {
                                propertyName = propertyName + "_" + languageCodes.get(0);
                            }
                            if (node instanceof PropertyValue) {
                                node = (AbstractQOMNode) qomFactory.propertyValue(translationSelectorName,
                                        propertyName);
                            } else if (node instanceof FullTextSearch) {
                                node = (AbstractQOMNode) qomFactory.fullTextSearch(translationSelectorName,
                                        propertyName, ((FullTextSearch) node).getFullTextSearchExpression());
                            } else if (node instanceof PropertyExistence) {
                                node = (AbstractQOMNode) qomFactory.propertyExistence(translationSelectorName,
                                        propertyName);
                            } else if (node instanceof Column) {
                                node = (AbstractQOMNode) qomFactory.column(translationSelectorName, propertyName,
                                        ((Column) node).getColumnName());
                            } else if (node instanceof EquiJoinCondition) {
                                node = (AbstractQOMNode) qomFactory
                                        .equiJoinCondition(
                                                selector.getSelectorName().equals(
                                                        ((EquiJoinCondition) node).getSelector1Name()) ? translationSelectorName
                                                        : ((EquiJoinCondition) node).getSelector1Name(),
                                                selector.getSelectorName().equals(
                                                        ((EquiJoinCondition) node).getSelector1Name()) ? propertyName
                                                        : ((EquiJoinCondition) node).getProperty1Name(),
                                                selector.getSelectorName().equals(
                                                        ((EquiJoinCondition) node).getSelector2Name()) ? translationSelectorName
                                                        : ((EquiJoinCondition) node).getSelector2Name(),
                                                selector.getSelectorName().equals(
                                                        ((EquiJoinCondition) node).getSelector2Name()) ? propertyName
                                                        : ((EquiJoinCondition) node).getProperty2Name());                                
                            }
                        }
                    }

                }
            } catch (NoSuchNodeTypeException e) {
                logger.debug("Type " + selector.getNodeTypeName() + " not found in registry", e);
            }
            return node;
        }

        private ExtendedPropertyDefinition getPropertyDefinition(ExtendedNodeType nodeType, Selector selector,
                String propertyName) throws RepositoryException {
            ExtendedPropertyDefinition propDef = null;

            if (!Constants.JAHIANT_TRANSLATION.equals(nodeType.getName())) {
                if (Constants.NT_BASE.equals(nodeType.getName())) {
                    for (String commonNodeType : getNodeTypesPerSelector().get(selector.getSelectorName())) {
                        nodeType = NodeTypeRegistry.getInstance().getNodeType(commonNodeType);
                        propDef = nodeType.getPropertyDefinitionsAsMap().get(propertyName);
                        if (propDef != null) {
                            break;
                        }
                    }
                } else {
                    propDef = nodeType.getPropertyDefinitionsAsMap().get(propertyName);
                }
            }
            return propDef;
        }

        private boolean isFulltextIncludingMultilingualProperties(ExtendedNodeType nodeType, Selector selector) throws RepositoryException {
            boolean isFulltextIncludingMultilingualProperties = true;
            if (Constants.JAHIANT_TRANSLATION.equals(nodeType.getName())) {
                isFulltextIncludingMultilingualProperties = false;
            } else if (!Constants.NT_BASE.equals(nodeType.getName())) {
                isFulltextIncludingMultilingualProperties = false;
                for (ExtendedPropertyDefinition propDef : nodeType.getPropertyDefinitionsAsMap().values()) {
                    if (propDef.isInternationalized()) {
                        isFulltextIncludingMultilingualProperties = true;
                        break;
                    }
                }
            }
            return isFulltextIncludingMultilingualProperties;
        }

        private String getCommonChildNodeTypes(String parentPath, Set<String> commonNodeTypes)
                throws RepositoryException {
            String commonPrimaryType = null;
            JCRNodeWrapper node = sessionFactory.getCurrentUserSession().getNode(parentPath);
            Set<String> checkedPrimaryTypes = new HashSet<String>();
            if (node.hasNodes()) {
                NodeIterator children = node.getNodes();
                if (children.getSize() < 100) {
                    while (children.hasNext()) {

                        JCRNodeWrapper child = (JCRNodeWrapper) children.nextNode();
                        if (commonPrimaryType == null && commonNodeTypes.isEmpty()) {
                            commonPrimaryType = child.getPrimaryNodeType().getName();
                            commonNodeTypes.addAll(child.getNodeTypes());
                        } else if (commonPrimaryType != null
                                && child.getPrimaryNodeType().getName().equals(commonPrimaryType)) {
                            commonPrimaryType = null;
                        }
                        if (!checkedPrimaryTypes.contains(child.getPrimaryNodeType().getName())) {
                            checkedPrimaryTypes.add(child.getPrimaryNodeType().getName());
                            commonNodeTypes.retainAll(child.getNodeTypes());
                        }
                    }
                }
            }
            return commonPrimaryType;
        }

        /**
         * @return the object gathering the modification information
         */
        public ModificationInfo getModificationInfo() {
            return modificationInfo;
        }

        /**
         * @return the Source object of the original query 
         */
        public Source getOriginalSource() {
            return originalSource;
        }

        /**
         * @return the map holding all selector names joined with a translation selector
         */
        public Map<String, Selector> getSelectorsJoinedWithTranslation() {
            return selectorsJoinedWithTranslation;
        }

        /**
         * @return a map holding per selector languages already set in the original query
         */
        public Map<String, List<String>> getLanguagesPerSelector() {
            return languagesPerSelector;
        }

        /**
         * @return a map holding either the primary nodetype or common nodetypes per selector
         */
        public Map<String, Set<String>> getNodeTypesPerSelector() {
            return nodeTypesPerSelector;
        }

    };

    /**
     * This class is used to gather modification information mainly set during the INITIALIZE_MODE traversing.
     * During traversing in CHECK_FOR_MODIFICATION mode mainly only the modificationNecessary variable is set.
     * The information of this object is then used during the MODIFY_MODE traversing iteration.
     *
     */
    class ModificationInfo {
        private int mode = INITIALIZE_MODE;

        private boolean modificationNecessary = false;

        private Join newJoin = null;

        private QueryObjectModelFactory queryObjectModelFactory = null;

        private QueryObjectModel newQueryObjectModel = null;

        private List<Constraint> newConstraints = new ArrayList<Constraint>();

        /**
         * Constructor setting the QueryObjectModelFactory to be used for modifying the query
         * @param queryObjectModelFactory to be used for modifying the query
         */
        public ModificationInfo(QueryObjectModelFactory queryObjectModelFactory) {
            super();
            this.queryObjectModelFactory = queryObjectModelFactory;
        }

        /**
         * @return true when modification of the query is found to be necessary otherwise return value is false
         */
        public boolean isModificationNecessary() {
            return modificationNecessary;
        }

        /**
         * Set true when modification of the query is found to be necessary otherwise set false
         * @param modificationNecessary true when modification of the query is necessary otherwise false
         */
        public void setModificationNecessary(boolean modificationNecessary) {
            this.modificationNecessary = modificationNecessary;
        }

        /**
         * @return the QueryObjectModelFactory to be used when creating the modified query
         */
        public QueryObjectModelFactory getQueryObjectModelFactory() {
            return queryObjectModelFactory;
        }

        /**
         * @return the new Source, which in any case is a join when adding translation node queries
         */
        public Join getNewJoin() {
            return newJoin;
        }

        /**
         * Set the new Source, which in any case is a join when adding translation node queries
         * @param newJoin when a new translation node is added.
         */
        public void setNewJoin(Join newJoin) {
            setModificationNecessary(true);
            this.newJoin = newJoin;
        }

        /**
         * @return the mode of the current iteration
         * either <ul> <li>{@link org.jahia.services.query.QueryServiceImpl#INITIALIZE_MODE},</li>
         *         <li>{@link org.jahia.services.query.QueryServiceImpl#CHECK_FOR_MODIFICATION_MODE},</li>
         *         <li>{@link org.jahia.services.query.QueryServiceImpl#MODIFY_MODE}</li>
         *         </ul> 
         */
        public int getMode() {
            return mode;
        }

        /**
         * Set mode for next iteration which is either <ul> <li>{@link org.jahia.services.query.QueryServiceImpl#INITIALIZE_MODE},</li>
         *         <li>{@link org.jahia.services.query.QueryServiceImpl#CHECK_FOR_MODIFICATION_MODE},</li>
         *         <li>{@link org.jahia.services.query.QueryServiceImpl#MODIFY_MODE}</li>
         *         </ul>  
         * @param mode for the next iteration
         */
        public void setMode(int mode) {
            this.mode = mode;
        }

        /**
         * @return the modified query object model
         */
        public QueryObjectModel getNewQueryObjectModel() {
            return newQueryObjectModel;
        }

        /**
         * Set the new modified query object model
         * @param newQueryObjectModel set after modification
         */
        public void setNewQueryObjectModel(QueryObjectModel newQueryObjectModel) {
            this.newQueryObjectModel = newQueryObjectModel;
        }

        /**
         * @return new constraints, which need to be added with logical conjunction (AND)
         */
        public List<Constraint> getNewConstraints() {
            return newConstraints;
        }
    }
}
