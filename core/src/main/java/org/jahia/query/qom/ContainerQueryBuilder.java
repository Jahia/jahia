/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.query.qom;

import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.data.containers.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.QueryService;
import org.jahia.query.filtercreator.FilterCreator;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.utils.JahiaTools;

import javax.jcr.Value;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyValue;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 21 nov. 2007
 * Time: 12:05:11
 * To change this template use File | Settings | File Templates.
 */
public class ContainerQueryBuilder extends JahiaBaseQueryModelInterpreter {

    private transient QueryObjectModelImpl queryModel;
    private transient ContainerQueryContext queryContext;

    /**
     *
     * @param context
     * @param bindingVariableValues
     */
    public ContainerQueryBuilder(ProcessingContext context, Map<String, Value> bindingVariableValues) {
        super(context, bindingVariableValues);
    }

    /**
     *
     * @param queryModel
     * @param queryContext
     * @return
     * @throws JahiaException
     */
    public ContainerQueryBean getContainerQueryBean(QueryObjectModelImpl queryModel,
                                                    ContainerQueryContext queryContext)
    throws JahiaException {
        if ( queryModel == null ){
            return null;
        }
        this.queryContext = queryContext;
        this.queryModel = queryModel;
        this.extractChildOrDescendantNodeConstraint(this.queryModel);
        this.queryModel.accept(this);
        ContainerQueryBean queryBean = new ContainerQueryBean(queryContext);
        queryBean.setSearcher(buildSearcher());
        queryBean.setFilter(buildContainerFilter());
        ContainerFilters filters = queryBean.getFilter();
        if (filters == null || filters.getContainerFilters().isEmpty() || !filters.isSingleSearchFilter()){
          // do not create the sorter if the whole QueryObjectModel was successfully converted as a SearchFilter
          queryBean.setSorter(buildContainerSorter());
        }
        return queryBean;
    }

    public void accept(SelectorImpl node) throws JahiaException {
        super.accept(node);

        if (node == null) {
            return;
        }
        String nodeType = node.getNodeTypeName();
        if ( !nodeType.equals(JahiaQueryObjectModelConstants.JAHIA_CONTAINER_NODE_TYPE)
                && !nodeType.equals(JahiaQueryObjectModelConstants.JAHIA_CONTENT_NODE_TYPE)
                && !nodeType.equals(JahiaQueryObjectModelConstants.JAHIA_PAGE_NODE_TYPE) ){
            List<String> contentDefinitionNames = new ArrayList<String>();
            contentDefinitionNames.add(nodeType);
//            this.queryContext.setContainerDefinitionNames(contentDefinitionNames);
            this.queryContext.setContainerDefinitionType(nodeType);
        }
    }

    public List<FilterCreator> getFilterCreators(ConstraintImpl c) {
        String filterCreatorNames = this.queryModel.getProperties()
                .getProperty(JahiaQueryObjectModelConstants.FILTER_CREATORS);
        QueryService queryService = ServicesRegistry.getInstance()
                .getQueryService();
        return (filterCreatorNames == null
                || filterCreatorNames.trim().length() == 0 ? queryService
                .getDefaultFilterCreators() : queryService
                .getFilterCreators(JahiaTools.getTokensList(filterCreatorNames,
                        ",")));
    }

    private ContainerSearcher buildSearcher()
    throws JahiaException {
        // searcher are wrapped in ContainerSearcherToFilterAdapter
        return null;
    }

    private ContainerFilters buildContainerFilter()
    throws JahiaException {
            ContainerFilters filters = null;
        ConstraintItem constraintItem = getRootConstraint();
        List<ContainerFilterInterface> filtersV = new ArrayList<ContainerFilterInterface>();
        if ( constraintItem != null ){
            ContainerFilterInterface filter = getContainerFilter(constraintItem);
            if ( filter != null ){
                filtersV.add(filter);
            }
        }
        if ( filtersV.isEmpty() ){
            return null;
        }
        List<String> definitionNames = queryContext.getContainerDefinitionsIncludingType(false);
        if ( !queryContext.isSiteLevelQuery() ){
            filters = new ContainerFilters(this.queryContext.getContainerListID(),filtersV);
        } else {
            filters = new ContainerFilters(filtersV, this.queryContext.getSiteIDs(),definitionNames);
        }
        filters.setQueryContext(this.queryContext);
        return filters;
    }

    private ContainerSorterInterface buildContainerSorter()
    throws JahiaException {
        List<FilterCreator> filterCreators = getFilterCreatorsForOrdering();
        if (filterCreators == null || filterCreators.isEmpty()) {
            return null;
        }
        for (FilterCreator filterCreator : filterCreators){
            ContainerSorterInterface containerSorter = filterCreator.getContainerSorter(this.queryModel,this.queryContext,this.getContext());
            if ( containerSorter != null ){
                return containerSorter;
            }
        }
        return null;
    }

    private List<FilterCreator> getFilterCreatorsForOrdering() {
        String filterCreatorNames = this.queryModel.getProperties()
                .getProperty(JahiaQueryObjectModelConstants.FILTER_CREATORS);
        QueryService queryService = ServicesRegistry.getInstance()
                .getQueryService();
        return (filterCreatorNames == null
                || filterCreatorNames.trim().length() == 0 ? queryService
                .getDefaultFilterCreators() : queryService
                .getFilterCreators(JahiaTools.getTokensList(filterCreatorNames,
                        ",")));
    }

    private ContainerFilterInterface getContainerFilter(ConstraintItem cItem) throws JahiaException {
        ContainerFilterInterface filter = null;
        ConstraintImpl c = cItem.getConstraint();
        if (c instanceof AndImpl){
            AndImpl andC = (AndImpl)c;
            if ( andC.getConstraint1() instanceof ComparisonImpl
                    && andC.getConstraint2() instanceof ComparisonImpl ) {
                ComparisonImpl comp1 = (ComparisonImpl) andC.getConstraint1();
                ComparisonImpl comp2 = (ComparisonImpl) andC.getConstraint2();
                String propName1 = ((PropertyValue)comp1.getOperand1()).getPropertyName();
                String propName2 = ((PropertyValue)comp2.getOperand1()).getPropertyName();
                if ( propName1.equals(propName2) && (
                        (((comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN)
                        || (comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO))
                        && ((comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN)
                        || (comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO)))
                    ||  (((comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN)
                        || (comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO))
                        && ((comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN)
                        || (comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO))) ) ) {
                    filter = getRangeQueryFilter(comp1,comp2);
                } else {
                    filter = getChainedFilter(cItem,ContainerChainedFilter.AND);
                }
            } else {
                filter = getChainedFilter(cItem,ContainerChainedFilter.AND);
            }
        } else if ( c instanceof OrImpl){
            filter = getChainedFilter(cItem,ContainerChainedFilter.OR);
        } else if ( c instanceof NotImpl){
            filter = getChainedFilter(cItem,ContainerChainedFilter.ANDNOT);
        } else if ( c instanceof ComparisonImpl){
            filter = getFilter(cItem);
        } else if ( c instanceof FullTextSearchImpl){
            filter = getFilter(cItem);
        } else if ( c instanceof ChildNodeImpl){
            filter = getFilter(cItem);
        } else if ( c instanceof DescendantNodeImpl){
            filter = getFilter(cItem);
        }
        return filter;
    }

    private ContainerFilterInterface getChainedFilter(ConstraintItem cItem,int logic) throws JahiaException {
        if ( cItem.getChildConstraintItems().isEmpty() ){
            return null;
        }
        if ( (logic == ContainerChainedFilter.AND
                || logic == ContainerChainedFilter.OR) && cItem.getChildConstraintItems().size()==1){
            return getContainerFilter((ConstraintItem)cItem.getChildConstraintItems().get(0));
        }
        List<ContainerFilterInterface> chainedFilters = new ArrayList<ContainerFilterInterface>();
        List<ConstraintItem> childConstraintItems = cItem.getChildConstraintItems();
        ConstraintItem[] constraintItemAr = new ConstraintItem[]{};
        constraintItemAr = (ConstraintItem[])childConstraintItems.toArray(constraintItemAr);
        ComparisonImpl comp1 = null;
        ComparisonImpl comp2 = null;
        ConstraintImpl c = null;
        ContainerFilterInterface filter = null;
        Map<Integer, Integer> mergedRangeFilter = new HashMap<Integer, Integer>();
        for (int i=0; i<constraintItemAr.length; i++){
            c = constraintItemAr[i].getConstraint();
            if (mergedRangeFilter.containsKey(new Integer(i))){
                continue;
            }
            if ( c instanceof ComparisonImpl ){
                comp1 = (ComparisonImpl)c;
                for(int j=i+1; j<constraintItemAr.length; j++){
                    c = constraintItemAr[j].getConstraint();
                    if (mergedRangeFilter.containsKey(new Integer(j))){
                        continue;
                    }
                    if ( c instanceof ComparisonImpl ){
                        comp2 = (ComparisonImpl)c;
                        String propName1 = ((PropertyValue)comp1.getOperand1()).getPropertyName();
                        String propName2 = ((PropertyValue)comp2.getOperand1()).getPropertyName();
                        if ( propName1.equals(propName2) && (
                                (((comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN)
                                || (comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO))
                                && ((comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN)
                                || (comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO)))
                            ||  (((comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN)
                                || (comp2.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO))
                                && ((comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN)
                                || (comp1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO))) ) ) {
                            filter = getRangeQueryFilter(comp1,comp2);
                            mergedRangeFilter.put(new Integer(i),new Integer(i));
                            mergedRangeFilter.put(new Integer(j),new Integer(j));
                            if ( filter!= null ){
                                chainedFilters.add(filter);
                            } else {
                                continue;
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        ConstraintItem constraintItem = null;
        for (int i=0; i<constraintItemAr.length; i++){
            constraintItem = constraintItemAr[i];
            if (mergedRangeFilter.containsKey(new Integer(i))){
                continue;
            }
            filter = getContainerFilter(constraintItem);
            if ( filter!= null ){
                chainedFilters.add(filter);
            }
        }
        if (chainedFilters.size()==0){
            return null;
        } else {
            if ( chainedFilters.size()>1 && ( logic == ContainerChainedFilter.AND ||
                    logic == ContainerChainedFilter.OR) ){
                chainedFilters = mergeFilters(chainedFilters,logic,0);
                if (chainedFilters == null || chainedFilters.size()==0){
                    return null;
                } else if ( chainedFilters.size()==1 ){
                    return (ContainerFilterInterface)chainedFilters.get(0);
                }
            }
            return new ContainerChainedFilter((ContainerFilterInterface[])chainedFilters
                            .toArray(new ContainerFilterInterface[]{}),logic);
        }
    }

    private List<ContainerFilterInterface> mergeFilters(List<ContainerFilterInterface> chainedFilters, int logic, int index) throws JahiaException {

        if ( index == chainedFilters.size()-1 ){
            return chainedFilters;
        }

        ContainerFilterInterface f1 = null;
        ContainerFilterInterface f2 = null;
        
        List<ContainerFilterInterface> result = new ArrayList<ContainerFilterInterface>();
        boolean merged = false;
        int pos = 0;
        for (Iterator<ContainerFilterInterface> it = chainedFilters.iterator(); it.hasNext();){
            f2 = null;
            if ( pos<index ){
                pos++;
                result.add(it.next());
                continue;
            }
            if ( f1==null ){
                f1 = it.next();
                if ( !(f1 instanceof MergeableFilter) ){
                    return mergeFilters(chainedFilters,logic,index+1);
                }
                result.add(f1);
            } else {
                f2 = it.next();
                if ( f2 instanceof MergeableFilter ){
                    if (logic == ContainerChainedFilter.AND) {
                        merged = ((MergeableFilter)f1).mergeAnd(f2);
                    } else if (logic == ContainerChainedFilter.OR) {
                        merged = ((MergeableFilter)f1).mergeOr(f2);
                    }
                    if ( merged ){
                        while (it.hasNext()){
                            result.add(it.next());
                        }
                        return mergeFilters(result,logic,0);
                    }
                }
                result.add(f2);
            }
            pos++;
        }
        return mergeFilters(chainedFilters,logic,index+1);
    }

    private final ContainerFilterInterface getFilter(ConstraintItem c) throws JahiaException {

        List<FilterCreator> filterCreators = getFilterCreators(c.getConstraint());
        if (filterCreators == null || filterCreators.isEmpty()) {
            return null;
        }
        for (FilterCreator filterCreator : filterCreators){
            ContainerFilterInterface containerFilter = filterCreator.getContainerFilter(c.getConstraint(),
                    this.queryModel,this.queryContext,this.getContext());
            if ( containerFilter != null ){
                return containerFilter;
            }
        }
        return null;
    }

    private final ContainerFilterInterface getRangeQueryFilter(ComparisonImpl c1, ComparisonImpl c2)
            throws JahiaException {

        List<FilterCreator> filterCreators = getFilterCreators(c1);
        if (filterCreators == null || filterCreators.isEmpty()) {
            return null;
        }
        ComparisonImpl lowerComp = c1;
        ComparisonImpl upperComp = c2;
        if ( (c1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN) ||
                (c1.getOperator() == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO) ){
            lowerComp = c2;
            upperComp = c1;
        }

        for (FilterCreator filterCreator : filterCreators){
            ContainerFilterInterface containerFilter = filterCreator.getRangeQueryFilter(lowerComp,upperComp,
                    this.queryModel,this.queryContext,this.getContext());
            if ( containerFilter != null ){
                return containerFilter;
            }
        }
        return null;
    }

    protected void extractChildOrDescendantNodeConstraint(QueryObjectModelImpl queryObjectModel)
    throws JahiaException {

        final ProcessingContext context = this.getContext();
        final ContainerQueryContext queryContext = this.queryContext;
        QueryObjectModelInterpreter interpreter = new DefaultQueryModelInterpreter(){

            public void accept(ChildNodeImpl node) throws JahiaException {

                if (node == null) {
                    return;
                }
                String pathString = node.getPath();
                Object nodeObject = ServicesRegistry.getInstance().getQueryService()
                        .getPathObject(pathString,context);
                if (nodeObject==null){
                    throw new JahiaException("Node Object is null for path " + pathString,
                            "Node Object is null for path " + pathString, JahiaException.APPLICATION_ERROR,
                            JahiaException.ERROR_SEVERITY);
                /*
                } else if (nodeObject instanceof String && "/".equals(nodeObject)){
                    // it's root "/"
                    queryContext.setSiteLevelQuery(true);
                } else if (nodeObject instanceof SiteBean){
                    SiteBean siteBean = (SiteBean)nodeObject;
                    List siteIDs = new ArrayList();
                    siteIDs.add(new Integer(siteBean.getId()));
                    queryContext.setSiteIDs(siteIDs);
                    queryContext.setSiteLevelQuery(true);
                */
                } else if (nodeObject instanceof String && "/".equals(nodeObject)){
                    // it's root "/"
                    queryContext.setSiteLevelQuery(true);
                } else if (nodeObject instanceof PageBean){
//                  queryContext.setSiteLevelQuery(true);                                         
                } else if (nodeObject instanceof SiteBean){
                    SiteBean siteBean = (SiteBean)nodeObject;
                    List<Integer> siteIDs = queryContext.getSiteIDs();
                    if (siteIDs == null){
                        siteIDs = new ArrayList<Integer>();
                    }
                    siteIDs.add(new Integer(siteBean.getId()));
                    queryContext.setSiteIDs(siteIDs);
                    queryContext.setSiteLevelQuery(true);
                } else if (nodeObject instanceof ContainerListBean){
                    ContainerListBean containerListBean = (ContainerListBean)nodeObject;
                    queryContext.setContainerListID(containerListBean.getID());
                    queryContext.setSiteLevelQuery(false);
                } else {
                    /*
                    throw new JahiaException("Child constraint not supported for this kind of parent Node " + pathString,
                            "Node Object is null for path " + pathString, JahiaException.APPLICATION_ERROR,
                            JahiaException.ERROR_SEVERITY);*/
                }
            }

            public void accept(DescendantNodeImpl node) throws JahiaException {

                if (node == null) {
                    return;
                }
                String pathString = node.getPath();
                Object nodeObject = ServicesRegistry.getInstance().getQueryService()
                        .getPathObject(pathString,context);
                if (nodeObject==null){
                    throw new JahiaException("Node Object is null for path " + pathString,
                            "Node Object is null for path " + pathString, JahiaException.APPLICATION_ERROR,
                            JahiaException.ERROR_SEVERITY);
                } else if (nodeObject instanceof String && "/".equals(nodeObject)){
                    // it's root "/"
                    queryContext.setSiteLevelQuery(true);
                } else if (nodeObject instanceof PageBean){
                    queryContext.setSiteLevelQuery(true);                                         
                } else if (nodeObject instanceof SiteBean){
                    SiteBean siteBean = (SiteBean)nodeObject;
                    List<Integer> siteIDs = queryContext.getSiteIDs();
                    if (siteIDs == null){
                        siteIDs = new ArrayList<Integer>();
                    }
                    siteIDs.add(new Integer(siteBean.getId()));
                    queryContext.setSiteIDs(siteIDs);
                    queryContext.setSiteLevelQuery(true);
                } else {
                    /*
                    throw new JahiaException("Descendant constraint not supported for this kind of ancestor Node " + pathString,
                            "Node Object is null for path " + pathString, JahiaException.APPLICATION_ERROR,
                            JahiaException.ERROR_SEVERITY);*/
                }
            }
        };
        queryObjectModel.accept(interpreter);
    }

}
