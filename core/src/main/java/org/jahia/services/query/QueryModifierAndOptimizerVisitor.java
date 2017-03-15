/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.qom.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.jackrabbit.spi.commons.query.qom.*;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

/**
 * We use this QOMTreeVisitor implementation to traverse through the query object model three times.
 * 
 * The ModificationInfo.mode changes before each iteration from INITIALIZE_MODE to CHECK_FOR_MODIFICATION_MODE and at last MODIFY_MODE,
 * which is only called if modification is necessary.
 * 
 * In INITIALIZE_MODE we analyze the query to check, whether language constraints are already set for selectors and we check whether
 * selectors to nodes having internationlized properties are already joined with their translation nodes and we store the node types per
 * selector.
 * 
 * In CHECK_FOR_MODIFICATION_MODE we analyze the query to see, whether modifications needs to be made because of Jahia's internal
 * datamodel changes (mainly internationalized properties, which are copied to subnodes and propertynames are suffixed with the language
 * code). We will also check if modifications need to be done due to performance optimizations. This mode sets the
 * ModificationInfo.modificationNecessary variable to mark the necessity of modification.
 * 
 * The MODIFY_MODE is only called if modification appears necessary (after previous step). This mode returns a modified query object
 * model in ModificationInfo.newQueryObjectModel.
 * 
 */
class QueryModifierAndOptimizerVisitor extends DefaultQOMTreeVisitor {

    protected enum TraversingMode {INITIALIZE_MODE, CHECK_FOR_MODIFICATION_MODE, MODIFY_MODE};
    
    private ValueFactory valueFactory;    

    private ModificationInfo modificationInfo;

    private Source originalSource;

    private Map<String, Selector> selectorsJoinedWithTranslation = new HashMap<String, Selector>();

    private Map<String, Set<String>> languagesPerSelector = new HashMap<String, Set<String>>();

    private Map<String, Set<String>> newLanguagesPerSelector = new HashMap<String, Set<String>>();

    private Map<String, Set<String>> nodeTypesPerSelector = new HashMap<String, Set<String>>();

    private static final String NO_LOCALE = "no_locale";

    private JCRSessionWrapper session = null;

    /**
     * Constructor for the QueryModifierAndOptimizerVisitor
     * 
     * @param modificationInfo
     *            object gathering all modification infos
     * @param originalSource
     *            Source object of original query
     * @param session
     *            the current JCR session used for the query
     * @param queryServiceImpl TODO
     */
    public QueryModifierAndOptimizerVisitor(ValueFactory valueFactory, ModificationInfo modificationInfo,
            Source originalSource, JCRSessionWrapper session) {
        super();
        this.valueFactory = valueFactory;        
        this.modificationInfo = modificationInfo;
        this.originalSource = originalSource;
        this.session = session;
    }

    /**
     * In INITIALIZE_MODE checks whether a selector is set to nt:base and in such a case looks, which nodes are actually placed as child
     * nodes. If all are the same then store the primaryChildNodeType, otherwise the nodetypes, common to all child nodes. This is
     * needed to be able to obtain property definitions.
     * 
     * In MODIFY_MODE return the unchanged node.
     */
    @Override
    public Object visit(ChildNodeImpl node, Object data) throws Exception {
        if (getModificationInfo().getMode() == TraversingMode.INITIALIZE_MODE) {
            Selector selector = getSelector(getOriginalSource(), node.getSelectorName());
            if (selector != null) {
                String nodeTypeName = selector.getNodeTypeName();
                if (Constants.NT_BASE.equals(nodeTypeName) || Constants.JAHIANT_CONTENT.equals(nodeTypeName)) {
                    Set<String> commonChildNodeTypes = new HashSet<String>();
                    String primaryChildNodeType = getCommonChildNodeTypes(node.getParentPath(),
                            commonChildNodeTypes);
                    if (primaryChildNodeType != null) {
                        commonChildNodeTypes = new HashSet<String>();
                        commonChildNodeTypes.add(primaryChildNodeType);
                    }
                    getNodeTypesPerSelector().put(node.getSelectorName(), commonChildNodeTypes);
                }
            }
        }

        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the
     * translation subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE.
     * 
     * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
     */
    @Override
    public Object visit(PropertyValueImpl node, Object data) throws Exception {
        Object returnedData = getNewPropertyBasedNodeIfRequired(node);
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? returnedData : node);
    }

    /**
     * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the
     * translation subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE.
     * 
     * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
     */
    @Override
    public Object visit(ColumnImpl node, Object data) throws Exception {
        Object returnedData = getNewPropertyBasedNodeIfRequired(node);
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? returnedData : node);
    }

    /**
     * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the
     * translation subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE.
     * 
     * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
     */
    @Override
    public Object visit(FullTextSearchImpl node, Object data) throws Exception {
        Object returnedData = getNewPropertyBasedNodeIfRequired(node);
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? returnedData : node);
    }

    /**
     * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the
     * translation subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE.
     * 
     * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
     */
    @Override
    public Object visit(PropertyExistenceImpl node, Object data) throws Exception {
        if (getModificationInfo().getMode() == TraversingMode.INITIALIZE_MODE) {
            if (Constants.JCR_LANGUAGE.equals(node.getPropertyName())) {
                Selector selector = getSelector(getOriginalSource(),
                        node.getSelectorName());
                Set<String> languages = getLanguagesPerSelector().get(
                        selector.getSelectorName());
                if (languages == null) {
                    languages = new HashSet<String>();
                    getLanguagesPerSelector().put(selector.getSelectorName(), languages);
                }

                languages.add(NO_LOCALE);
            }
        }
    	
        Object returnedData = getNewPropertyBasedNodeIfRequired(node);
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? returnedData : node);
    }

    /**
     * Calls accept on each of the attached constraints of the AND node.
     * 
     * In MODIFY_MODE check if the constraints returned were modified, and if yes create a new node and return it, otherwise return the
     * unchanged node.
     */
    @Override
    public final Object visit(AndImpl node, Object data) throws Exception {
        Constraint constraint1 = node.getConstraint1();
        Constraint constraint2 = node.getConstraint2();
        boolean modified = false;
        Object returnedData = ((ConstraintImpl) node.getConstraint1()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE && returnedData != null
                && !returnedData.equals(node.getConstraint1())) {
            constraint1 = (Constraint) returnedData;
            modified = true;
        }
        returnedData = ((ConstraintImpl) node.getConstraint2()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE && returnedData != null
                && !returnedData.equals(node.getConstraint2())) {
            constraint2 = (Constraint) returnedData;
            modified = true;
        }
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = modified ? getModificationInfo().getQueryObjectModelFactory().and(
                    constraint1, constraint2) : node;
        }
        return data;
    }

    /**
     * Calls accept on the two operands in the comparison node.
     * 
     * In INITIALIZE_MODE check whether there is already a language based comparison in the original query, so that this language is
     * used instead of the current locale in the session.
     * 
     * In MODIFY_MODE check if the dynamic operand returned was modified, and if yes create a new node and return it, otherwise return
     * the unchanged node.
     */
    @Override
    public Object visit(ComparisonImpl node, Object data) throws Exception {
        Object returnedData = ((DynamicOperandImpl) node.getOperand1()).accept(this, data);

        if (getModificationInfo().getMode() == TraversingMode.INITIALIZE_MODE) {
            if (QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(node.getOperator())
                    && node.getOperand1() instanceof PropertyValue
                    && Constants.JCR_LANGUAGE.equals(((PropertyValue) node.getOperand1())
                            .getPropertyName())) {
                Selector selector = getSelector(getOriginalSource(),
                        ((PropertyValue) node.getOperand1()).getSelectorName());
                Set<String> languages = getLanguagesPerSelector().get(
                        selector.getSelectorName());
                if (languages == null) {
                    languages = new HashSet<String>();
                    getLanguagesPerSelector().put(selector.getSelectorName(), languages);
                }
                String language = ((Literal) node.getOperand2()).getLiteralValue().getString();
                if (!languages.contains(language)) {
                    languages.add(language);
                }
            }
        }

        ((StaticOperandImpl) node.getOperand2()).accept(this, data);

        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = returnedData != null && !returnedData.equals(node.getOperand1()) ? getModificationInfo()
                    .getQueryObjectModelFactory().comparison((DynamicOperand) returnedData,
                            node.getOperator(), node.getOperand2()) : node;
        }

        return data;
    }

    /**
     * Calls accept on the two sources and the join condition in the join node.
     * 
     * In INITIALIZE_MODE check whether the original query already contains a childnode-JOIN on the translation node, so that it then
     * does not have to be added.
     * 
     * In MODIFY_MODE check if the sources or join condition returned were modified, and if yes create a new node and return it,
     * otherwise return the unchanged node.
     */
    @Override
    public Object visit(JoinImpl node, Object data) throws Exception {
        if (getModificationInfo().getMode() == TraversingMode.INITIALIZE_MODE) {
            if (node.getJoinCondition() instanceof ChildNodeJoinCondition) {
                ChildNodeJoinCondition childNodeJoin = (ChildNodeJoinCondition) node
                        .getJoinCondition();
                String childSelectorName = childNodeJoin.getChildSelectorName();
                Selector childSelector = getSelector(getOriginalSource(), childSelectorName);
                if (Constants.JAHIANT_TRANSLATION.equals(childSelector.getNodeTypeName())) {
                    getSelectorsJoinedWithTranslation().put(
                            childNodeJoin.getParentSelectorName(), childSelector);
                }
            }
        }

        Object returnedRight = ((SourceImpl) node.getRight()).accept(this, data);
        Object returnedLeft = ((SourceImpl) node.getLeft()).accept(this, data);
        Object returnedJoinCondition = ((JoinConditionImpl) node.getJoinCondition()).accept(
                this, data);

        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(returnedRight, node.getRight())
                    .append(returnedLeft, node.getLeft())
                    .append(returnedJoinCondition, node.getJoinCondition());
            data = equalsBuilder.isEquals() ? node
                    : getModificationInfo()
                            .getQueryObjectModelFactory()
                            .join(returnedLeft != null ? (Source) returnedLeft
                                    : node.getLeft(),
                                    returnedRight != null ? (Source) returnedRight
                                            : node.getRight(),
                                    node.getJoinType(),
                                    returnedJoinCondition != null ? (JoinCondition) returnedJoinCondition
                                            : node.getJoinCondition());
        }

        return data;
    }

    /**
     * Calls accept on the property value in the length node.
     * 
     * In MODIFY_MODE check if the property value returned was modified, and if yes create a new node and return it, otherwise return
     * the unchanged node.
     */
    @Override
    public Object visit(LengthImpl node, Object data) throws Exception {
        Object returnedData = ((PropertyValueImpl) node.getPropertyValue()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = returnedData != null && !returnedData.equals(node.getPropertyValue()) ? getModificationInfo()
                    .getQueryObjectModelFactory().length((PropertyValue) returnedData) : node;
        }
        return data;
    }

    /**
     * Calls accept on the dynamic operand in the lower-case node.
     * 
     * In MODIFY_MODE check if the operand returned was modified, and if yes create a new node and return it, otherwise return the
     * unchanged node.
     */
    @Override
    public Object visit(LowerCaseImpl node, Object data) throws Exception {
        Object returnedData = ((DynamicOperandImpl) node.getOperand()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = returnedData != null && !returnedData.equals(node.getOperand()) ? getModificationInfo()
                    .getQueryObjectModelFactory().lowerCase((DynamicOperand) returnedData)
                    : node;
        }
        return data;
    }

    /**
     * Calls accept on the constraint in the NOT node.
     * 
     * In MODIFY_MODE check if the constraint returned was modified, and if yes create a new node and return it, otherwise return the
     * unchanged node.
     */
    @Override
    public Object visit(NotImpl node, Object data) throws Exception {
        Object returnedData = ((ConstraintImpl) node.getConstraint()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = returnedData != null && !returnedData.equals(node.getConstraint()) ? getModificationInfo()
                    .getQueryObjectModelFactory().not((Constraint) data) : node;
        }
        return data;
    }

    /**
     * Calls accept on the dynamic operand in the ordering node.
     * 
     * In MODIFY_MODE check if the operand returned was modified, and if yes create a new node and return it, otherwise return the
     * unchanged node.
     */
    @Override
    public Object visit(OrderingImpl node, Object data) throws Exception {
        Object returnedData = ((DynamicOperandImpl) node.getOperand()).accept(this, data);

        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = returnedData != null && !returnedData.equals(node.getOperand()) ? (node
                    .isAscending() ? getModificationInfo().getQueryObjectModelFactory()
                    .ascending((DynamicOperand) returnedData) : getModificationInfo()
                    .getQueryObjectModelFactory().descending((DynamicOperand) returnedData))
                    : node;
        }
        return data;
    }

    /**
     * Calls accept on each of the attached constraints of the OR node.
     * 
     * In MODIFY_MODE check if the constraints returned were modified, and if yes create a new node and return it, otherwise return the
     * unchanged node.
     */
    @Override
    public final Object visit(OrImpl node, Object data) throws Exception {
        Constraint constraint1 = node.getConstraint1();
        Constraint constraint2 = node.getConstraint2();
        boolean modified = false;
        Object returnedData = ((ConstraintImpl) node.getConstraint1()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE && returnedData != null
                && !returnedData.equals(node.getConstraint1())) {
            constraint1 = (Constraint) returnedData;
            modified = true;
        }
        returnedData = ((ConstraintImpl) node.getConstraint2()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE && returnedData != null
                && !returnedData.equals(node.getConstraint2())) {
            constraint2 = (Constraint) returnedData;
            modified = true;
        }
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            data = modified ? getModificationInfo().getQueryObjectModelFactory().or(
                    constraint1, constraint2) : node;
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
     * In MODIFY_MODE check if the nodes returned were modified, and if yes create a new QOM and return it, otherwise return the
     * unchanged QOM.
     */
    @Override
    public Object visit(QueryObjectModelTree node, Object data) throws Exception {
        node.getSource().accept(this, data);

        Constraint newConstraint = null;
        if (node.getConstraint() != null) {
            newConstraint = (Constraint)node.getConstraint().accept(this, data);
        }
        Ordering[] newOrderingObjects = new Ordering[node.getOrderings().length];
        for (int i = 0; i < node.getOrderings().length; i++) {
            newOrderingObjects[i] = (Ordering)node.getOrderings()[i].accept(this, data);
        }
        Column[] newColumnObjects = new Column[node.getColumns().length];
        for (int i = 0; i < node.getColumns().length; i++) {
            newColumnObjects[i] = (Column)node.getColumns()[i].accept(this, data);
        }
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(newConstraint, node.getConstraint())
                    .append(newOrderingObjects, node.getOrderings())
                    .append(newColumnObjects, node.getColumns());
            data = equalsBuilder.isEquals() ? node : getModificationInfo()
                    .getQueryObjectModelFactory().createQuery(
                            getModificationInfo().getModifiedSource(
                                    node.getSource()), newConstraint,
                            newOrderingObjects, newColumnObjects);
        }
        return data;
    }

    /**
     * Calls accept on the dynamic operand in the lower-case node.
     * 
     * In MODIFY_MODE check if the operand returned was modified, and if yes create a new node and return it, otherwise return the
     * unchanged node.
     */
    @Override
    public Object visit(UpperCaseImpl node, Object data) throws Exception {
        Object returnedData = ((DynamicOperandImpl) node.getOperand()).accept(this, data);
        if (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
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
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(DescendantNodeImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * In MODIFY_MODE check if the selector is based on a nodetype, which is modified so that the translation subnode is used in the
     * query. In this case create a new fullTextSearchScore node pointing to the selector of the translation nodetype.
     */
    @Override
    public Object visit(FullTextSearchScoreImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? (getSelectorsJoinedWithTranslation()
                .get(node.getSelectorName()) != null ? getModificationInfo()
                .getQueryObjectModelFactory().fullTextSearchScore(
                        getSelectorsJoinedWithTranslation().get(node.getSelectorName())
                                .getSelectorName()) : node) : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(LiteralImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(NodeLocalNameImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(NodeNameImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(SameNodeImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(ChildNodeJoinConditionImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(DescendantNodeJoinConditionImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * In INITIALIZE_MODE checks whether the propertyName of the QOM node is internationalized. If yes and the selector for the
     * translation subnode is missing, indicate creation and that the query must be modified, which is done in MODIFY_MODE.
     * 
     * In MODIFY_MODE either return the modified node or if modification is not necessary, the unchanged node.
     */
    @Override
    public Object visit(EquiJoinConditionImpl node, Object data) throws Exception {
        Object returnedData = getNewPropertyBasedNodeIfRequired(node);
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? returnedData : node);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(SameNodeJoinConditionImpl node, Object data) throws Exception {
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    /**
     * Does nothing and returns <code>data</code>, but in MODIFY_NODE return the unchanged node.
     */
    @Override
    public Object visit(SelectorImpl node, Object data) throws Exception {
        if (getModificationInfo().getMode() == TraversingMode.INITIALIZE_MODE) {
            if (node.equals(getOriginalSource())
                    && Constants.JAHIANT_TRANSLATION.equals(node.getNodeTypeName())) {
                getSelectorsJoinedWithTranslation().put(node.getSelectorName(), node);
            }
        }
        return (getModificationInfo().getMode() == TraversingMode.MODIFY_MODE ? node : data);
    }

    private AbstractQOMNode getNewPropertyBasedNodeIfRequired(AbstractQOMNode node)
            throws RepositoryException {
        AbstractQOMNode newNode = node;
        if (node instanceof EquiJoinCondition) {
            newNode = getNewPropertyBasedNodeIfRequired(
                    ((EquiJoinCondition) node).getSelector1Name(),
                    ((EquiJoinCondition) node).getProperty1Name(), node);
            newNode = getNewPropertyBasedNodeIfRequired(
                    ((EquiJoinCondition) node).getSelector2Name(),
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

    private AbstractQOMNode getNewPropertyBasedNodeIfRequired(String selectorName,
            String propertyName, AbstractQOMNode node) throws RepositoryException {
        Selector selector = getSelector(getOriginalSource(), selectorName);
        if (selector == null) {
            return node;
        }

        try {
            if (getModificationInfo().getMode() == TraversingMode.CHECK_FOR_MODIFICATION_MODE
                    || getModificationInfo().getMode() == TraversingMode.MODIFY_MODE) {
                // check for language dependent modifications and use the translation selector on
                // jnt:translation node if user specified it in query
                
                if (getSelectorsJoinedWithTranslation().get(selector.getSelectorName()) != null) {
                    selector = getSelectorsJoinedWithTranslation().get(selector.getSelectorName());
                }
                Set<String> languageCodes = getLanguagesPerSelector().get(
                        selector.getSelectorName());
                if (((languageCodes == null || languageCodes.isEmpty())
                        && session != null && session.getLocale() != null)
                        && !(Constants.NT_QUERY.equals(selector
                                .getNodeTypeName()) || Constants.JAHIANT_QUERY
                                .equals(selector.getNodeTypeName()))) {
                    if (getModificationInfo().getMode() == TraversingMode.CHECK_FOR_MODIFICATION_MODE) {
                        
                        Set<String> newLanguageCodes = getNewLanguagesPerSelector().get(
                                selector.getSelectorName());
                        if (newLanguageCodes == null) {
                            newLanguageCodes = new HashSet<String>();
                            newLanguageCodes.add(session.getLocale().toString());
                            newLanguageCodes.add(NO_LOCALE);
                            getNewLanguagesPerSelector().put(selector.getSelectorName(),
                                    newLanguageCodes);
                        } 
                        if (newLanguageCodes.contains(NO_LOCALE)) {
                            ExtendedNodeType nodeType = NodeTypeRegistry.getInstance()
                                    .getNodeType(selector.getNodeTypeName());
                            ExtendedPropertyDefinition propDef = propertyName != null ? getPropertyDefinition(
                                    nodeType, selector, propertyName) : null;
                            if (!Constants.JAHIANT_FILE.equals(selector.getNodeTypeName())
                                    && propDef != null && propDef.isInternationalized()) {
                                newLanguageCodes.remove(NO_LOCALE);
                            }
                        }

                        getModificationInfo().setModificationNecessary(true);
                    } else {
                        QueryObjectModelFactory qomFactory = getModificationInfo()
                                .getQueryObjectModelFactory();
                        Set<String> newLanguageCodes = getNewLanguagesPerSelector().get(
                                selector.getSelectorName());
                        if (newLanguageCodes != null) {
                            Constraint langConstraint = null;
                            for (String newLanguageCode : newLanguageCodes) {
                                Constraint currentConstraint = NO_LOCALE
                                        .equals(newLanguageCode) ? qomFactory.not(qomFactory
                                        .propertyExistence(selector.getSelectorName(),
                                                Constants.JCR_LANGUAGE))
                                        : qomFactory
                                                .comparison(
                                                        qomFactory.propertyValue(
                                                                selector.getSelectorName(),
                                                                Constants.JCR_LANGUAGE),
                                                        QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                                                        qomFactory.literal(getValueFactory()
                                                                .createValue(newLanguageCode)));
                                langConstraint = langConstraint == null ? currentConstraint
                                        : qomFactory.or(langConstraint, currentConstraint);
                            }
                            getModificationInfo().getNewConstraints().add(langConstraint);
                            if (languageCodes == null) {
                                languageCodes = new HashSet<String>();
                                getLanguagesPerSelector().put(selector.getSelectorName(),
                                        languageCodes);
                            }
                            languageCodes.addAll(newLanguageCodes);
                        }
                    }
                }
                if (node instanceof Column) {
                    String columnName = ((Column) node).getColumnName();
                    if (StringUtils.startsWith(columnName, "rep:facet(")
                            && !StringUtils.contains(columnName, "locale=")) {
                        if (getModificationInfo().getMode() == TraversingMode.CHECK_FOR_MODIFICATION_MODE) {
                            getModificationInfo().setModificationNecessary(true);
                        } else {
                            String facetOptions = columnName.substring("rep:facet(".length());
                            if (languageCodes == null) {
                                languageCodes = getNewLanguagesPerSelector().get(selector.getSelectorName());
                            }
                            String languageCode = null;
                            for (String currentLanguageCode : languageCodes) {
                                if (!NO_LOCALE.equals(currentLanguageCode)) {
                                    languageCode = currentLanguageCode;
                                    break;
                                }

                            }
                            if (!StringUtils.isEmpty(languageCode)) {
                                columnName = "rep:facet(locale=" + languageCode + (facetOptions.trim().length() > 1 ? "&" : "")
                                        + facetOptions;
                                QueryObjectModelFactory qomFactory = getModificationInfo().getQueryObjectModelFactory();
                                node = (AbstractQOMNode) qomFactory.column(selector.getSelectorName(), propertyName, columnName);
                            }
                        }
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            QueryServiceImpl.logger.debug("Type " + selector.getNodeTypeName() + " not found in registry", e);
        }
        return node;
    }

    private ExtendedPropertyDefinition getPropertyDefinition(ExtendedNodeType nodeType,
            Selector selector, String propertyName) throws RepositoryException {
        ExtendedPropertyDefinition propDef = null;

        if (!Constants.JAHIANT_TRANSLATION.equals(nodeType.getName())) {
            if (Constants.NT_BASE.equals(nodeType.getName())
                    || Constants.JAHIANT_CONTENT.equals(nodeType.getName())) {
                Set<String> nodeTypes = getNodeTypesPerSelector().get(
                        selector.getSelectorName());
                if (nodeTypes != null) {
                    for (String commonNodeType : nodeTypes) {
                        nodeType = NodeTypeRegistry.getInstance().getNodeType(commonNodeType);
                        propDef = nodeType.getPropertyDefinitionsAsMap().get(propertyName);
                        if (propDef != null) {
                            break;
                        }
                    }
                }
            }
            if (propDef == null) {
                propDef = nodeType.getPropertyDefinitionsAsMap().get(propertyName);
            }
        }
        return propDef;
    }

    private String getCommonChildNodeTypes(String parentPath, Set<String> commonNodeTypes)
            throws RepositoryException {
        String commonPrimaryType = null;
        JCRNodeWrapper node = session.getNode(parentPath);
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
    public Map<String, Set<String>> getLanguagesPerSelector() {
        return languagesPerSelector;
    }

    /**
     * @return a map holding per selector new languages to be set
     */
    public Map<String, Set<String>> getNewLanguagesPerSelector() {
        return newLanguagesPerSelector;
    }

    /**
     * @return a map holding either the primary nodetype or common nodetypes per selector
     */
    public Map<String, Set<String>> getNodeTypesPerSelector() {
        return nodeTypesPerSelector;
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
    
    public ValueFactory getValueFactory() {
        return this.valueFactory;
    }    
}