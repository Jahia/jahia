/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.query.filtercreator;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearch;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Literal;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyValue;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.jahia.data.beans.PageBean;
import org.jahia.data.containers.ContainerFilterInterface;
import org.jahia.data.containers.ContainerLuceneSorterBean;
import org.jahia.data.containers.ContainerSearcherToFilterAdapter;
import org.jahia.data.containers.ContainerSorterInterface;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.ComparisonImpl;
import org.jahia.query.qom.FullTextSearchImpl;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.LiteralImpl;
import org.jahia.query.qom.OrderingImpl;
import org.jahia.query.qom.PropertyValueImpl;
import org.jahia.query.qom.QueryModelTools;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.query.qom.StaticOperandImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.search.lucene.JahiaLuceneSort;
import org.jahia.utils.JahiaTools;

/**
 * Default Jahia Search Filter Creator implementation.
 * This filter creator translates a Query Object Model to concrete Jahia Search Filters.
 * It extends JahiaDBFilterCreator and will use the method
 * <code>JahiaDBFilterCreator.getFilter(FullTextSearchImpl c, QueryObjectModelImpl queryModel,
 *                                       ContainerQueryContext queryContext,
 *                                       ProcessingContext context) throws JahiaException </code>
 * of its super class for any ConstraintImpl it can translate to a Search Filter.
 * 
 * User: hollis
 * Date: 15 nov. 2007
 * Time: 16:49:35
 */
public class JahiaSearchFilterCreator extends JahiaDBFilterCreator {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaSearchFilterCreator.class);

    /**
     * Returns a concrete Filter for the given ChildNode ConstraintImpl
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getFilter( ChildNode c,
                                               QueryObjectModel queryModel,
                                               ContainerQueryContext queryContext,
                                               ProcessingContext context)
    throws JahiaException {

        ContainerFilterInterface filter = null;
        String pathString = c.getPath();
        Object nodeObject = ServicesRegistry.getInstance().getQueryService()
                .getPathObject(pathString,context);
        if (nodeObject==null){
            throw new JahiaException("Node Object is null for path " + pathString,
                    "Node Object is null for path " + pathString, JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY);
        } else if (nodeObject instanceof PageBean){
            PageBean pageBean = (PageBean)nodeObject;
            ContentPage contentPage = (ContentPage)pageBean.getContentObject();
            try {
                FullTextSearch fullTextSearch = ((QueryObjectModelImpl)queryModel).getQueryFactory()
                    .fullTextSearch(FilterCreator.PAGE_PATH,contentPage.getPagePathString(context));
                return this.getFilter(fullTextSearch,queryModel,queryContext,context);
            } catch ( Exception t ){
                throw new JahiaException("Error creating PagePath filter","Error creating PagePath filter",
                        JahiaException.APPLICATION_ERROR,JahiaException.ERROR_SEVERITY,t);
            }
        }

        return filter;
    }

    /**
     * Returns a concrete Filter for the given ChildNode ConstraintImpl
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getFilter( DescendantNode c,
                                               QueryObjectModel queryModel,
                                               ContainerQueryContext queryContext,
                                               ProcessingContext context)
    throws JahiaException {

        ContainerFilterInterface filter = null;
        String pathString = c.getPath();
        Object nodeObject = ServicesRegistry.getInstance().getQueryService()
                .getPathObject(pathString,context);
        if (nodeObject==null){
            throw new JahiaException("Node Object is null for path " + pathString,
                    "Node Object is null for path " + pathString, JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY);
        } else if (nodeObject instanceof PageBean){
            PageBean pageBean = (PageBean)nodeObject;
            ContentPage contentPage = (ContentPage)pageBean.getContentObject();
            try {
                FullTextSearchImpl fullTextSearch = (FullTextSearchImpl)((QueryObjectModelImpl)queryModel).getQueryFactory()
                    .fullTextSearch(FilterCreator.PAGE_PATH,contentPage.getPagePathString(context) + "*");
                return this.getFilter(fullTextSearch,queryModel,queryContext,context);
            } catch ( Exception t ){
                throw new JahiaException("Error creating PagePath filter","Error creating PagePath filter",
                        JahiaException.APPLICATION_ERROR,JahiaException.ERROR_SEVERITY,t);
            }
        }

        return filter;
    }

    /**
     * Returns a concrete Filter for the given ComparisonImpl ConstraintImpl
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    public ContainerFilterInterface getFilter( Comparison c,
                                               QueryObjectModel queryModel,
                                               ContainerQueryContext queryContext,
                                               ProcessingContext context)
    throws JahiaException {

        int operator = c.getOperator();
        String luceneOperator = null;
        boolean wildCardSearch = false;
        PropertyValueImpl propValue = (PropertyValueImpl)c.getOperand1();
        if ( operator == JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO ){
            luceneOperator = "";
            if (((StaticOperandImpl)c.getOperand2()).isMultiValueANDLogic()){
                luceneOperator = "+";
            }
            if (propValue != null && propValue.getValueProviderClass() != null){
                return null;
            }
        } else if ( operator == JahiaQueryObjectModelConstants.OPERATOR_LIKE ){
            wildCardSearch = true;
        } else if ( operator == JahiaQueryObjectModelConstants.OPERATOR_NOT_EQUAL_TO ){
            luceneOperator = "-";
        } else if ( (operator == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN)
                || (operator == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO)
                || (operator == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN)
                || (operator == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO) ){
            if (propValue != null && propValue.getValueProviderClass() != null){
                return null;
            }
            return getRangeQueryFilter(c,queryModel,queryContext,context);
        } else {
            return null;
        }
        List<String> values = JahiaTools.getTokensList(((StaticOperandImpl)c.getOperand2()).getValueAsString(),
                JahiaQueryObjectModelConstants.MULTI_VALUE_SEP);

        PropertyValueImpl op1 = (PropertyValueImpl)c.getOperand1();
        String propertyName = null;
        String[] aliasNames = null;
        if (op1 !=null){
            propertyName = op1.getPropertyName();
            aliasNames = op1.getAliasNames();                        
        }
        if (propertyName != null){
            int fieldType = -1;
            if (!propertyName.equals(FilterCreator.CONTENT_DEFINITION_NAME) &&
                !propertyName.equals(JahiaQueryObjectModelConstants.CATEGORY_LINKS)){
                JahiaFieldDefinition fieldDef = QueryModelTools
                        .getFieldDefinitionForPropertyName(propertyName,
                                queryContext.getContainerDefinitionNames(), context);
                if ( fieldDef == null ){
                    return null;
                }
                if ( context.getContentPage()!=null ){
                    fieldType = fieldDef.getType();
                }
            }
            if (fieldType == FieldTypes.CATEGORY ||
                propertyName.equals(JahiaQueryObjectModelConstants.CATEGORY_LINKS)){
                if (values.iterator().hasNext()){
                    String val = (String)values.iterator().next();
                    wildCardSearch = wildCardSearch && !val.startsWith("/");
                }
                String[] valuesAr = new String[]{};
                valuesAr = (String[])values.toArray(valuesAr);
                valuesAr = getCategoryIncludingChilds(valuesAr,context);
                values = Arrays.asList(valuesAr);
            }
        }
        StringBuffer buff = new StringBuffer();
        for (String value : values){
            if (value.trim().indexOf(' ') != -1 && !value.startsWith("\"")){
                value = "\"" + value + "\"";
            }
            value = (luceneOperator != null ? luceneOperator : "") + value;
            if (wildCardSearch && value.indexOf('*') == -1){
                value += "*";
            }
            buff.append(value).append(" ");
        }
        String searchExpression = buff.toString();
        FullTextSearchImpl fullTextSearch = new FullTextSearchImpl(propertyName, searchExpression, aliasNames);
        fullTextSearch.setMetadata(((PropertyValueImpl)c.getOperand1()).isMetadata());
        return getFilter(fullTextSearch,queryModel,queryContext,context);
    }


    /**
     * Returns a concrete Range Query Filter.
     *
     * @param lowerRangeComp
     * @param upperRangeComp
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getRangeQueryFilter(   Comparison lowerRangeComp,
                                                                    Comparison upperRangeComp,
                                                                    QueryObjectModel queryModel,
                                                                    ContainerQueryContext queryContext,
                                                                    ProcessingContext context)
    throws JahiaException {

        PropertyValueImpl operand = (PropertyValueImpl)lowerRangeComp.getOperand1();
        StaticOperandImpl lowerValue = (StaticOperandImpl)lowerRangeComp.getOperand2();
        StaticOperandImpl upperValue = (StaticOperandImpl)upperRangeComp.getOperand2();

        int lowerOperator = lowerRangeComp.getOperator();
        String lowerValueStr = lowerValue.getValueAsString();
        int upperOperator = upperRangeComp.getOperator();
        String upperValueStr = upperValue.getValueAsString();
        return getRangeQueryFilter(operand, lowerOperator, lowerValueStr, upperOperator,
                upperValueStr, queryModel, queryContext, context);
    }

    /**
     * Returns a concrete Range Query Filter.
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    protected ContainerFilterInterface getRangeQueryFilter(Comparison c,
                                                                    QueryObjectModel queryModel,
                                                                    ContainerQueryContext queryContext,
                                                                    ProcessingContext context)
    throws JahiaException {

        if (c==null || c.getOperand1()==null){
            return null;
        }
        PropertyValueImpl operand = (PropertyValueImpl)c.getOperand1();
        StaticOperandImpl opValue = (StaticOperandImpl)c.getOperand2();
        String lowerValueStr = opValue.getValueAsString();
        String upperValueStr = opValue.getValueAsString();

        int lowerOperator = c.getOperator();
        int upperOperator = lowerOperator;
        if ( lowerOperator== JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN ){
            upperOperator = JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN;
            upperValueStr = String.valueOf(Long.MAX_VALUE);
        } else if ( lowerOperator== JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO ){
            upperOperator = JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO;
            upperValueStr = String.valueOf(Long.MAX_VALUE);
        } else if ( lowerOperator== JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN ){
            lowerOperator = JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN;
            lowerValueStr = String.valueOf(Long.MIN_VALUE);
        } else if ( lowerOperator== JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO ){
            lowerOperator = JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO;
            lowerValueStr = String.valueOf(Long.MIN_VALUE);
        } else {
            return null;
        }
        return getRangeQueryFilter(operand, lowerOperator, lowerValueStr, upperOperator, upperValueStr,
                queryModel, queryContext, context);
    }

    /**
     * Returns a concrete Range Query Filter.
     *
     * @param operand
     * @param lowerOperator
     * @param lowerValue
     * @param upperOperator
     * @param upperValue
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    protected ContainerFilterInterface getRangeQueryFilter(PropertyValueImpl operand,
                                                                    int lowerOperator,
                                                                    String lowerValue,
                                                                    int upperOperator,
                                                                    String upperValue,
                                                                    QueryObjectModel queryModel,
                                                                    ContainerQueryContext queryContext,
                                                                    ProcessingContext context)
    throws JahiaException {

        StringBuffer searchExpression = new StringBuffer();
        int operator = lowerOperator;
        if ( operator == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN ){
            searchExpression.append("{");
        } else {
            searchExpression.append("[");
        }
        searchExpression.append(lowerValue);
        searchExpression.append(" ");
        searchExpression.append(upperValue);
        operator = upperOperator;
        if ( operator == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN ){
            searchExpression.append("}");
        } else {
            searchExpression.append("]");
        }
        FullTextSearchImpl fullTextSearch = new FullTextSearchImpl(operand.getPropertyName(),
                searchExpression.toString(), operand.getAliasNames());
        fullTextSearch.setMetadata(operand.isMetadata());
        return getFilter(fullTextSearch,queryModel,queryContext,context);
    }

    /**
     * Returns a concrete Sorter
     *
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerSorterInterface getContainerSorter(QueryObjectModel queryModel,
                                                       ContainerQueryContext queryContext,
                                                       ProcessingContext context) throws JahiaException {
        ContainerSorterInterface sorter = null;
        Ordering[] orderings = queryModel.getOrderings();
        if (orderings !=null) {
            JahiaLuceneSort luceneSorter = null;
            try {
                luceneSorter = QueryModelTools.getSorter((OrderingImpl[])queryModel.getOrderings(),context, queryContext);
                if (luceneSorter == null){
                    return null;
                }
            } catch ( Exception t ){
                logger.debug("Exception occured when trying to create Lucene Sort from QueryObjectModelImpl",t);
                return null;
            }
            // create a new query object model with a constraint when the Query Object Model only contains Selector
            // but no Constraint and no Sorter
            ValueFactory valueFactory = ServicesRegistry.getInstance().getQueryService().getValueFactory();
            List<String> definitions = queryContext.getContainerDefinitionsIncludingType(false);
            QueryObjectModelFactory queryFactory = ((QueryObjectModelImpl)queryModel).getQueryFactory();
            if (definitions != null && !definitions.isEmpty()){
                String[] definitionsAr = (String[])definitions.toArray(new String[]{});
                String definitionNamesStr = JahiaTools.getStringArrayToString(definitionsAr,JahiaQueryObjectModelConstants.MULTI_VALUE_SEP);
                Value val = valueFactory.createValue(definitionNamesStr);
                try {
                    Literal literal = queryFactory.literal(val);
                    ((LiteralImpl)literal).setMultiValueANDLogic(false);
                    PropertyValue prop = queryFactory.propertyValue(FilterCreator.CONTENT_DEFINITION_NAME);
                    Comparison compConstraint = queryFactory.comparison(prop,
                            JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO,literal);
                    ContainerFilterInterface filterBean = this.getFilter((ComparisonImpl)compConstraint,queryModel,
                            queryContext,context);
                    if (filterBean != null && filterBean instanceof ContainerSearcherToFilterAdapter){
                        ContainerSearcher searcher = ((ContainerSearcherToFilterAdapter)filterBean).getSearcher();
                        if (searcher != null){
                            sorter = new ContainerLuceneSorterBean(queryContext.getContainerListID(),searcher,
                                    QueryModelTools.getSortPropertyNames(orderings),context.getEntryLoadRequest());
                        }
                    }
                } catch ( Exception t ){
                    throw new JahiaException("Failed creating ContainerQueryBean","Failed creating ContainerQueryBean",
                            JahiaException.APPLICATION_ERROR,
                            JahiaException.ERROR_SEVERITY,t);
                }
            }
        }
        return sorter;
    }

}