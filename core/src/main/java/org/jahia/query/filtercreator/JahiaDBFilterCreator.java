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
package org.jahia.query.filtercreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearch;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.lucene.search.HitCollector;
import org.jahia.content.CategoryKey;
import org.jahia.data.beans.PageBean;
import org.jahia.data.containers.ContainerFilterBean;
import org.jahia.data.containers.ContainerFilterByCategories;
import org.jahia.data.containers.ContainerFilterByContainerDefinitions;
import org.jahia.data.containers.ContainerFilterFieldValueProvider;
import org.jahia.data.containers.ContainerFilterInterface;
import org.jahia.data.containers.ContainerMetadataFilterBean;
import org.jahia.data.containers.ContainerMetadataSorterBean;
import org.jahia.data.containers.ContainerSearcherToFilterAdapter;
import org.jahia.data.containers.ContainerSorterBean;
import org.jahia.data.containers.ContainerSorterByContainerDefinition;
import org.jahia.data.containers.ContainerSorterByTimebasedPublishingDateBean;
import org.jahia.data.containers.ContainerSorterFieldValueProvider;
import org.jahia.data.containers.ContainerSorterInterface;
import org.jahia.data.containers.ValueProviderFilter;
import org.jahia.data.containers.ValueProviderSorter;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.FullTextSearchImpl;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.OrderingImpl;
import org.jahia.query.qom.PropertyValueImpl;
import org.jahia.query.qom.QueryModelTools;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.query.qom.StaticOperandImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.lucene.JahiaHitCollector;
import org.jahia.services.search.lucene.JahiaLuceneSort;
import org.jahia.utils.JahiaTools;

/**
 * Default Jahia Container DB Filter Creator implementation.
 * This filter creator translates a Query Object Model to concrete Jahia DB Filters.
 *
 * User: hollis
 * Date: 15 nov. 2007
 * Time: 16:49:35
 */
public class JahiaDBFilterCreator extends AbstractFilterCreator {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaDBFilterCreator.class);

    /**
     * Returns a concrete Filter for the given ComparisonImpl ConstraintImpl
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getFilter( Comparison c,
                                               QueryObjectModel queryModel,
                                               ContainerQueryContext queryContext,
                                               ProcessingContext context)
    throws JahiaException {

        ContainerFilterInterface filter = null;
        PropertyValueImpl op1 = (PropertyValueImpl)c.getOperand1();
        StaticOperandImpl op2 = (StaticOperandImpl)c.getOperand2();
        if ( op1 == null ){
            return null;
        }
        String propertyName = op1.getPropertyName();
        boolean multiValue = op1.getMultiValue();
        String values[] = op2.getStringValues();
        String operator = getFilterOperatorFromQueryModelOperator(c.getOperator());
        if ( operator == null ){
            return null;
        }
        if (FilterCreator.CONTENT_DEFINITION_NAME.equals(propertyName)){
            filter = new ContainerFilterByContainerDefinitions(values,
                context.getEntryLoadRequest());
        } else if (JahiaQueryObjectModelConstants.CATEGORY_LINKS.equals(propertyName)){
            values = getCategoryIncludingChilds(values,context);
            Set<Category> categories = getCategories(values);
            ContainerFilterByCategories filterBean = new ContainerFilterByCategories(categories,
                    context.getEntryLoadRequest(),false);
            filterBean.setMultiValueANDLogic(op2.isMultiValueANDLogic());
            filter = filterBean;
        } else {
            JahiaFieldDefinition fieldDef = QueryModelTools
                    .getFieldDefinitionForPropertyName(propertyName,
                            queryContext.getContainerDefinitionNames(), context);
            if ( fieldDef == null ){
                return null;
            }
            int fieldType = -1;
            if ( context.getContentPage()!=null ){
                fieldType = fieldDef.getType();
            }
            boolean numberType =  numberType(fieldType) || op1.getNumberValue();
            String numberFormat = op1.getNumberFormat();
            if (numberFormat == null){
                numberFormat = numberFormat(fieldType);
            }

            if ( fieldType == FieldTypes.CATEGORY ){
                values = getCategoryIncludingChilds(values,context);
            }
            if (!op1.isMetadata() && !fieldDef.getIsMetadata()){
                ContainerFilterBean filterBean = new ContainerFilterBean(fieldDef.getCtnType(),numberType,numberFormat,multiValue,
                        context.getEntryLoadRequest());
                if (!op2.isMultiValueANDLogic() || values.length<=1 ){
                    filterBean.addClause(operator,values);
                } else  {
                    filterBean.setMultiClauseANDLogic(true);
                    for (int i=0; i<values.length; i++){
                        filterBean.addClause(operator,values[i]);
                    }
                }
                filterBean.setMultipleFieldValue(op1.getMultiValue());
                filter = filterBean;
            } else {
                ContainerMetadataFilterBean filterBean = new ContainerMetadataFilterBean(fieldDef.getCtnType(),numberType,
                        numberFormat,false,context,context.getEntryLoadRequest());
                if (!op2.isMultiValueANDLogic() || values.length<=1 ){
                    filterBean.addClause(operator,values);
                } else  {
                    filterBean.setMultiClauseANDLogic(true);
                    for (int i=0; i<values.length; i++){
                        filterBean.addClause(operator,values[i]);
                    }
                }
                filterBean.setMultipleFieldValue(op1.getMultiValue());
                filter = filterBean;
            }
        }
        setFilterValueProvider(filter,op1.getValueProviderClass());
        return filter;
    }

    /**
     * Include if necessary all child categories given the parent category keys or parent category expressed as path
     * /rootCategory/cat1/cat2/*
     *
     * @param parentsCategoryKeys
     * @param context
     * @return
     */
    public static String[] getCategoryIncludingChilds(String[] parentsCategoryKeys, ProcessingContext context){
        if (parentsCategoryKeys == null || parentsCategoryKeys.length==0){
            return parentsCategoryKeys;
        }
        List<String> result = new ArrayList<String>();
        String catKey = null;
        for (int i=0; i<parentsCategoryKeys.length; i++){
            catKey = parentsCategoryKeys[i];
            /*
            if (catKey.startsWith(JahiaQueryObjectModelConstants.USE_CHILD_VALUES_EXCLUSIVE_PREFIX)
                && catKey.endsWith(JahiaQueryObjectModelConstants.USE_CHILD_VALUES_EXCLUSIVE_POSTFIX)){
                catKey = catKey.substring(1,catKey.length()-3);
            } else if (catKey.startsWith(JahiaQueryObjectModelConstants.USE_CHILD_VALUES_INCLUSIVE_PREFIX)
                && catKey.endsWith(JahiaQueryObjectModelConstants.USE_CHILD_VALUES_INCLUSIVE_POSTFIX)) {
                catKey = catKey.substring(1,catKey.length()-3);
                if (!result.contains(catKey)){
                    result.add(catKey);
                }
            */
            if (catKey.startsWith("/")){
                // maybe it's a category path
                try {
                    Category cat = Category.getLastCategoryNode(catKey);
                    if (cat!=null && (cat.getACL()== null ||
                            cat.getACL().getPermission(context.getUser(), JahiaBaseACL.READ_RIGHTS))){
                        if (!result.contains(cat.getKey())){
                            result.add(cat.getKey());
                        }
                        if (!catKey.endsWith("/*")){
                            continue;
                        }
                        catKey = cat.getKey();
                    } else {
                        continue;
                    }
                } catch ( Exception t ){
                    logger.debug(t);
                    continue;
                }
            } else {
                try {
                    Category cat = Category.getCategory(catKey,context.getUser());
                    if (cat != null){
                        result.add(catKey);
                    }
                    continue;
                } catch ( Exception t ){
                    logger.debug(t);
                }
            }
            try {
                Category cat = Category.getCategory(catKey, context.getUser());
                List<CategoryKey> childCategoryKeys = cat.getChildCategoryKeys(true);
                for (CategoryKey categoryKey : childCategoryKeys){
                    try {
                        Category category = (Category)Category.getChildInstance(categoryKey);
                        if (!result.contains(category.getKey())){
                            result.add(category.getKey());
                        }
                    } catch ( Exception t ){
                        logger.debug("Error loading Child Category " + categoryKey,t);
                    }
                }
            } catch (Exception t){
                logger.debug("Error loading Category " + catKey,t);
            }
        }
        String[] catKeys = new String[]{};
        catKeys = (String[])result.toArray(catKeys);
        return catKeys;
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
                FullTextSearch fullTextSearch = ((QueryObjectModelImpl)queryModel).getQueryFactory()
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

        ContainerFilterInterface filter = null;
        PropertyValueImpl operand = (PropertyValueImpl)lowerRangeComp.getOperand1();
        StaticOperandImpl lowerValue = (StaticOperandImpl)lowerRangeComp.getOperand2();
        StaticOperandImpl upperValue = (StaticOperandImpl)upperRangeComp.getOperand2();

        String propertyName = operand.getPropertyName();
        JahiaFieldDefinition fieldDef = QueryModelTools
                .getFieldDefinitionForPropertyName(propertyName, queryContext.getContainerDefinitionNames(),
                        context);
        if ( fieldDef == null ){
            return null;
        }
        boolean numberType = operand.getNumberValue();
        String numberFormat = operand.getNumberFormat();
        boolean multiValue = operand.getMultiValue();

        String lowerOperator = getFilterOperatorFromQueryModelOperator(lowerRangeComp.getOperator());
        String upperOperator = getFilterOperatorFromQueryModelOperator(upperRangeComp.getOperator());

        if (!operand.isMetadata() && !fieldDef.getIsMetadata()){
            ContainerFilterBean filterBean = new ContainerFilterBean(fieldDef.getCtnType(),numberType,numberFormat,multiValue,
                    context.getEntryLoadRequest());
            filterBean.addRangeClause(lowerOperator,upperOperator,lowerValue.getValueAsString(),
                    upperValue.getValueAsString());
            filter = filterBean;
        } else {
            ContainerMetadataFilterBean filterBean = new ContainerMetadataFilterBean(fieldDef.getCtnType(),numberType,
                    numberFormat,multiValue,context,context.getEntryLoadRequest());
            filterBean.addRangeClause(lowerOperator,upperOperator,lowerValue.getValueAsString(),
                    upperValue.getValueAsString());
            filter = filterBean;
        }

        setFilterValueProvider(filter,operand.getValueProviderClass());

        return filter;
    }

    /**
     * Returns a concrete Filter for the given ConstraintImpl
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getFilter(FullTextSearch c,
                                              QueryObjectModel queryModel,
                                              ContainerQueryContext queryContext,
                                              ProcessingContext context) throws JahiaException {
        QueryObjectModelImpl queryModelImpl = (QueryObjectModelImpl)queryModel;
        ContainerFilterInterface filter = null;
        ContainerSearcher searcher = null;
        String searchExpression = c.getFullTextSearchExpression();
        if ( searchExpression == null || "".equals(searchExpression.trim()) ){
            return null;
        }
        searchExpression = searchExpression.trim();
        String openParenthesis = "";
        String closeParenthesis =  "";
        if (!searchExpression.startsWith("(") && !searchExpression.startsWith("{")
                && !searchExpression.startsWith("[")){
            openParenthesis = "(";
        }
        if (!searchExpression.endsWith(")") && !searchExpression.endsWith("}")
                && !searchExpression.endsWith("]")){
            closeParenthesis = ")";
        }
        boolean prefixedQuery = false;
        String propertyName = c.getPropertyName();
        
        String fieldName = QueryModelTools.getFieldNameForSearchEngine(propertyName,
                ((FullTextSearchImpl) c).isMetadata(), queryContext.getContainerDefinitionNames(), context, QueryModelTools.NO_TYPE);

        if (fieldName != null) {
            String luceneSearchExpression = fieldName + ":" + openParenthesis + searchExpression + closeParenthesis;

            if (((FullTextSearchImpl) c).getAliasNames() != null) {
                luceneSearchExpression = "(" + luceneSearchExpression;
                for (String aliasName : ((FullTextSearchImpl) c).getAliasNames()) {
                    luceneSearchExpression += " OR " + JahiaSearchConstant.CONTAINER_FIELD_ALIAS_PREFIX
                            + aliasName.toLowerCase() + ":" + openParenthesis + searchExpression + closeParenthesis;
                }
                luceneSearchExpression += ")";
            }
            searchExpression = luceneSearchExpression;
            prefixedQuery = true;
        }
        
        StringBuffer buff = new StringBuffer();
        List<String> definitions = queryContext
                .getContainerDefinitionsIncludingType(false);
        if (!FilterCreator.CONTENT_DEFINITION_NAME.equals(propertyName)) {
            if (definitions != null && !definitions.isEmpty()) {
                buff.append(JahiaSearchConstant.DEFINITION_NAME)
                .append(":(");
                for (String defName : definitions) {
                    buff.append(defName).append(" ");
                }
                buff.append(") ");
            }
        } else {
            if (definitions != null){
                // don't add again content definition test as it's already in the search expression 
                definitions.clear();
            }
        }
        if (!prefixedQuery && "(".equals(openParenthesis)){
            searchExpression = openParenthesis + searchExpression + ")";
        }
        if ( buff.length()>0 ){
            searchExpression = searchExpression + " AND " + buff.toString();
        }
        if (queryContext.isSiteLevelQuery()){
            Integer[] siteIDs = new Integer[]{};
            siteIDs = (Integer[])queryContext.getSiteIDs().toArray(siteIDs);
            String[] definitionNames = new String[]{};
            if (definitions != null){
                definitionNames = (String[])definitions.toArray(definitionNames);
            }
            searcher = new ContainerSearcher(siteIDs,definitionNames,
                    searchExpression,
                    context.getEntryLoadRequest());
            if ( searcher != null ){
                searcher.setSiteModeSearching(true);
            }
        } else {
            searcher = new ContainerSearcher(queryContext.getContainerListID(),searchExpression,
                    context.getEntryLoadRequest());
            searcher.setSiteIds(new Integer[]{new Integer(context.getSiteID())});
        }
        int searchMaxHit = -1;
        String searchMaxHitStr = queryModelImpl.getProperties().getProperty(JahiaQueryObjectModelConstants.SEARCH_MAX_HITS);
        if ( searchMaxHitStr != null && searchMaxHitStr.length() > 0){
            searchMaxHit = Integer.parseInt(searchMaxHitStr);
        }
        if ( searcher != null ){
            searcher.setLanguageCodes(QueryModelTools.getLanguageCodes(queryModelImpl.getProperties()));
            if ("true".equals(queryModelImpl.getProperties().get(
                    JahiaQueryObjectModelConstants.USE_BACKEND_CACHE))) {
                searcher.setCacheQueryResultsInBackend(true);
            }
            JahiaLuceneSort sorter = null;
            try {
                sorter = QueryModelTools.getSorter((OrderingImpl[])queryModel.getOrderings(),context, queryContext);
            } catch ( Exception t ){
                logger.debug("Exception occured when trying to create Lucene Sort from QueryObjectModelImpl",t);
            }
            if ( sorter != null ){
                searcher.getSearchResultBuilder().setSorter(sorter);
            }
            if ( searchMaxHit != -1 ){
                searcher.getSearchResultBuilder().setMaxHits(searchMaxHit);
                HitCollector hitCollector = searcher.getSearchResultBuilder().getHitCollector();
                if ( hitCollector instanceof JahiaHitCollector){
                    ((JahiaHitCollector)hitCollector).setMaxHits(searchMaxHit);
                }
            }
            long cacheExpiration = 30000;
            String sessionCacheTime = queryModelImpl.getProperties()
                    .getProperty(JahiaQueryObjectModelConstants.SESSION_CACHE_EXPIRATION);
            if ( sessionCacheTime != null ){
                cacheExpiration = JahiaTools.getTimeAsLong(sessionCacheTime,"30s").longValue();
            }
            searcher.setCacheTime(cacheExpiration);
            ContainerSearcherToFilterAdapter filterAdapter = new ContainerSearcherToFilterAdapter(searcher);
            filter = filterAdapter;
        }
        return filter;
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
        OrderingImpl[] orderings = (OrderingImpl[])queryModel.getOrderings();
        if (orderings == null || orderings.length==0){
            return null;
        }

        String dbMaxResultStr = ((QueryObjectModelImpl)queryModel).getProperties().getProperty(JahiaQueryObjectModelConstants.DB_MAX_RESULT);
        int dbMaxResult = -1;
        if ( dbMaxResultStr != null ){
            try {
                dbMaxResult = Integer.parseInt(dbMaxResultStr);
            } catch ( Exception t ){
            }
        }

        // @todo support multiple orderings
        OrderingImpl ordering = orderings[0];
        PropertyValueImpl operand = (PropertyValueImpl)ordering.getOperand();
        String propertyName = operand.getPropertyName();
        if ( JahiaQueryObjectModelConstants.PUBLICATION_DATE.equals(propertyName) ){
            sorter = new ContainerSorterByTimebasedPublishingDateBean(queryContext.getContainerListID(),
                    JahiaQueryObjectModelConstants.PUBLICATION_DATE,context.getEntryLoadRequest());
            sorter.setAscOrdering(ordering.getOrder()== JahiaQueryObjectModelConstants.ORDER_ASCENDING);
            return sorter;
        } else if ( JahiaQueryObjectModelConstants.EXPIRATION_DATE.equals(propertyName) ){
            sorter = new ContainerSorterByTimebasedPublishingDateBean(queryContext.getContainerListID(),
                    JahiaQueryObjectModelConstants.EXPIRATION_DATE,context.getEntryLoadRequest());
            sorter.setAscOrdering(ordering.getOrder()== JahiaQueryObjectModelConstants.ORDER_ASCENDING);
            return sorter;
        }
        JahiaFieldDefinition fieldDef = QueryModelTools
                .getFieldDefinitionForPropertyName(propertyName, queryContext.getContainerDefinitionNames(),
                        context);
        if (fieldDef == null) {
            return null;
        }
        int fieldType = context.getContentPage()!=null ? fieldDef.getType() : -1;
        boolean numberSort =  operand.getNumberValue() || numberType(fieldType);
        String numberFormat = operand.getNumberFormat();
        if (numberFormat == null){
            numberFormat = numberFormat(fieldType);
        }
        if ( !operand.isMetadata() && !fieldDef.getIsMetadata() ){
            if ( !queryContext.isSiteLevelQuery() ){
                sorter = new ContainerSorterBean(queryContext.getContainerListID(),
                        fieldDef.getCtnType(),
                        numberSort, numberFormat, context.getEntryLoadRequest());
            } else {
                List<String> definitionNames = queryContext.getContainerDefinitionsIncludingType(false);
                String[] propertyNames = new String[]{fieldDef.getCtnType()};
                sorter = new ContainerSorterByContainerDefinition(queryContext.getSiteIDs(),propertyNames,
                        definitionNames,numberSort,
                        numberFormat,context.getEntryLoadRequest());
            }
        } else {
            if ( !queryContext.isSiteLevelQuery() ){
                sorter = new ContainerMetadataSorterBean(queryContext.getContainerListID(),
                        fieldDef.getCtnType(),numberSort,numberFormat,context,context.getEntryLoadRequest());
            } else {
                List<String> definitionNames = queryContext.getContainerDefinitionsIncludingType(false);
                sorter = new ContainerMetadataSorterBean(queryContext.getSiteIDs(),context,fieldDef.getCtnType(),
                        definitionNames,numberSort, numberFormat,context.getEntryLoadRequest());
            }
        }
        if ( sorter != null ){
            setSorterValueProvider(sorter,operand.getValueProviderClass());
            sorter.setAscOrdering(ordering.getOrder()== JahiaQueryObjectModelConstants.ORDER_ASCENDING);
            if (dbMaxResult > 0){
                sorter.setDBMaxResult(dbMaxResult);
            }
        }
        return sorter;
    }

    protected void setSorterValueProvider(ContainerSorterInterface sorter,String valueProviderClass)
    throws JahiaException {
        if (sorter == null || !(sorter instanceof ValueProviderSorter)){
            return;
        }
        if (valueProviderClass!=null && !"".equals(valueProviderClass.trim())){
            ContainerSorterFieldValueProvider valueProvider = null;
            try {
                Class<? extends ContainerSorterFieldValueProvider> instanceClass = Class.forName(valueProviderClass).asSubclass(ContainerSorterFieldValueProvider.class);
                valueProvider = instanceClass.newInstance();
                ((ValueProviderSorter)sorter).setFieldValueProvider(valueProvider);
            } catch (ClassNotFoundException cnfe) {
                String errorMsg = "ClassNotFound when trying to load " + valueProviderClass +
                                  "(" + cnfe.getMessage() + ")";
                throw new JahiaException("ClassNotFoundException",
                    errorMsg, JahiaException.APPLICATION_ERROR,
                    JahiaException.WARNING_SEVERITY, cnfe);

            } catch (InstantiationException cie) {
                String errorMsg = "InstantiationException when trying to load " +
                                  valueProviderClass + "(" + cie.getMessage() + ")";
                throw new JahiaException("InstanciationException",
                    errorMsg, JahiaException.APPLICATION_ERROR,
                    JahiaException.WARNING_SEVERITY, cie);

            } catch (IllegalAccessException iae) {
                String errorMsg = "IllegalAccessException when trying to load " +
                                  valueProviderClass + "(" + iae.getMessage() + ")";
                throw new JahiaException("IllegalAccessException",
                    errorMsg, JahiaException.APPLICATION_ERROR,
                    JahiaException.WARNING_SEVERITY, iae);
            }
        }
    }

    protected void setFilterValueProvider(ContainerFilterInterface filter,String valueProviderClass)
    throws JahiaException {
        if (filter==null || !(filter instanceof ValueProviderFilter)){
            return;
        }
        if (valueProviderClass!=null && !"".equals(valueProviderClass.trim())){
            ContainerFilterFieldValueProvider valueProvider = null;

            try {
                Class<? extends ContainerFilterFieldValueProvider> instanceClass = Class.forName(valueProviderClass).asSubclass(ContainerFilterFieldValueProvider.class);
                valueProvider = instanceClass.newInstance();
                ((ValueProviderFilter)filter).setFieldValueProvider(valueProvider);
            } catch (ClassNotFoundException cnfe) {
                String errorMsg = "ClassNotFound when trying to load " + valueProviderClass +
                                  "(" + cnfe.getMessage() + ")";
                throw new JahiaException("ClassNotFoundException",
                    errorMsg, JahiaException.APPLICATION_ERROR,
                    JahiaException.WARNING_SEVERITY, cnfe);

            } catch (InstantiationException cie) {
                String errorMsg = "InstantiationException when trying to load " +
                                  valueProviderClass + "(" + cie.getMessage() + ")";
                throw new JahiaException("InstanciationException",
                    errorMsg, JahiaException.APPLICATION_ERROR,
                    JahiaException.WARNING_SEVERITY, cie);

            } catch (IllegalAccessException iae) {
                String errorMsg = "IllegalAccessException when trying to load " +
                                  valueProviderClass + "(" + iae.getMessage() + ")";
                throw new JahiaException("IllegalAccessException",
                    errorMsg, JahiaException.APPLICATION_ERROR,
                    JahiaException.WARNING_SEVERITY, iae);
            }
        }
    }

    protected static Set<Category> getCategories(String[] catKeys) throws JahiaException {
        if (catKeys ==null){
            return new HashSet<Category>();
        }
        Category cat = null;
        Set<Category> categories = new HashSet<Category>();
        for (int i=0;i<catKeys.length; i++){
            cat = Category.getCategory(catKeys[i]);
            if (cat != null){
                categories.add(cat);
            }
        }
        return categories;
    }
}
