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
package org.jahia.services.containers;

import org.jahia.data.containers.*;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.search.ContainerSearcher;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 9 nov. 2007
 * Time: 09:22:36
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerToQueryBeanBuilder {

    public static final String SEARCH_QUERY = "searchQuery";
    public static final String CONTENT_TYPE = "contentType";

    private Map<String, ContainerFilterInterface> namedFilters;

    public JahiaContainerToQueryBeanBuilder() {
        namedFilters = new HashMap<String, ContainerFilterInterface>();
    }

    /**
     *
     * @param queryContainer
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerQueryBean getQueryBean( JahiaContainer queryContainer,
                                   ContainerQueryContext queryContext,
                                   ProcessingContext context)
    throws JahiaException {
        if ( queryContainer == null ){
            return null;
        }
        ContainerQueryBean queryBean = new ContainerQueryBean(queryContext);
        queryBean.setSearcher(buildSearcher(queryContainer, queryContext, context));
        queryBean.setFilter(buildContainerFilter(queryContainer, queryContext, context));
        queryBean.setSorter(buildContainerSorter(queryContainer, queryContext, context));
        return queryBean;
    }

    /**
     *
     * @param queryContainer
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    private ContainerSearcher buildSearcher( JahiaContainer queryContainer,
                                             ContainerQueryContext queryContext,
                                             ProcessingContext context)
    throws JahiaException {
        if ( queryContainer == null ){
            return null;
        }
        JahiaField field = queryContainer.getFieldByName(SEARCH_QUERY);
        String query = getFieldValue(field, "");
        if ( "".equals(query) ){
            return null;
        }
        ContainerSearcher searcher = null;
        if ( queryContext.isSiteLevelQuery() ){
            List<String> definitions = queryContext.getContainerDefinitionsIncludingType(false);
            String[] definitionNames = new String[]{};
            if (definitions != null){
                definitionNames = definitions.toArray(definitionNames);
            }
            Integer[] siteIds = null;
            if (queryContext.getSiteIDs() != null && !queryContext.getSiteIDs().isEmpty()){
                siteIds = new Integer[queryContext.getSiteIDs().size()];
                int i=0;
                for (Iterator<Integer> it = queryContext.getSiteIDs().iterator(); it.hasNext();){
                    siteIds[i] = it.next();
                    i++;
                }
            } else if ( queryContext.getSiteIDs() == null || queryContext.getSiteIDs().isEmpty() ) {
                siteIds = ServicesRegistry.getInstance().getJahiaSitesService().getSiteIds();
            } else {
                siteIds = new Integer[]{new Integer(context.getSiteID())};
            }
            searcher = new ContainerSearcher(siteIds,
                    definitionNames,query,context.getEntryLoadRequest());
        } else {
            searcher = new ContainerSearcher(queryContext.getContainerListID(),query,
                    context.getEntryLoadRequest());
        }
        return searcher;
    }

    /**
     *
     * @param queryContainer
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    private ContainerFilters buildContainerFilter(JahiaContainer queryContainer,
                                      ContainerQueryContext queryContext,
                                      ProcessingContext context) throws JahiaException {
        if ( queryContainer == null ){
            return null;
        }

        ContainerFilters containerFilters = null;
        List<ContainerFilterInterface> filters = new ArrayList<ContainerFilterInterface>();

        ContainerFilterInterface filter = getContentTypeFilter(queryContainer, queryContext, context);
        if ( filter != null ){
            filters.add(filter);
        }
        List<ContainerFilterInterface> predCritFilters = getPredefinedCriteriasFilters(queryContainer, queryContext, context);
        if ( !predCritFilters.isEmpty() ){
            filters.addAll(predCritFilters);
        }
        if ( filters.isEmpty() ){
            return null;
        }
        if ( queryContext.isSiteLevelQuery() ){
            List<String> definitionNames = queryContext.getContainerDefinitionsIncludingType(false);
            containerFilters = new ContainerFilters(filters,queryContext.getSiteIDs(),definitionNames);
            containerFilters.setQueryContext(queryContext);
        } else {
            containerFilters = new ContainerFilters(queryContext.getContainerListID(),filters);
            containerFilters.setQueryContext(queryContext);
        }
        return containerFilters;
    }

    private ContainerFilterInterface getContentTypeFilter(JahiaContainer queryContainer,
                                                          ContainerQueryContext queryContext,
                                                          ProcessingContext context)
    throws JahiaException {
        List<String> definitionNamesList = queryContext.getContainerDefinitionsIncludingType(false);
        if ( definitionNamesList != null && definitionNamesList.size()>0 ){
            String[] definitionNames = new String[]{};
            definitionNames = (String[])definitionNamesList.toArray(definitionNames);
            return new ContainerFilterByContainerDefinitions(definitionNames,
                    context.getEntryLoadRequest());
        }
        return null;
    }

    private List<ContainerFilterInterface> getPredefinedCriteriasFilters( JahiaContainer queryContainer,
                                                ContainerQueryContext queryContext,
                                                ProcessingContext context)
    throws JahiaException {
        List<ContainerFilterInterface> filters = new ArrayList<ContainerFilterInterface>();
        for (Iterator<JahiaField> fields = queryContainer.getFields(); fields.hasNext(); ){
            JahiaField field = fields.next();
            String fieldName = field.getDefinition().getName();
            String fieldValue = getFieldValue(field,"");
            if ( "".equals(fieldValue) ){
                continue;
            }
            if ( fieldName.startsWith("metadata_") ){
                ContainerFilterInterface filter = getMetadataFilter(field,queryContext,context);
                if ( filter != null ){
                    filters.add(filter);
                }
            }
        }
        return filters;
    }

    private ContainerFilterInterface getMetadataFilter(JahiaField field,
                                                       ContainerQueryContext queryContext,
                                                       ProcessingContext context)
    throws JahiaException {
        String fieldName = field.getDefinition().getName();
        fieldName = fieldName.substring("metadata_".length());
        String clauseComparator = ContainerFilterBean.COMP_EQUAL;
        if ( fieldName.endsWith("After") ){
            clauseComparator = ContainerFilterBean.COMP_BIGGER_OR_EQUAL;
            fieldName = fieldName.substring(0,fieldName.indexOf("After"));
        } else if ( fieldName.endsWith("Before") ){
            clauseComparator = ContainerFilterBean.COMP_SMALLER_OR_EQUAL;
            fieldName = fieldName.substring(0,fieldName.indexOf("Before"));
        }
        boolean numberFiltering = false;
        if ( field.getType() == FieldTypes.DATE ||
                field.getType() == FieldTypes.FLOAT ||
                field.getType() == FieldTypes.INTEGER ){
            numberFiltering = true;
        }
        if ( field.getType() != FieldTypes.CATEGORY ){
            boolean newFilter = false;
            ContainerMetadataFilterBean filter = (ContainerMetadataFilterBean)namedFilters.get(fieldName);
            if ( filter == null ){
                filter = new ContainerMetadataFilterBean(fieldName,
                    numberFiltering,context,context.getEntryLoadRequest());
                newFilter = true;
            }
            if ( field.getType() != FieldTypes.DATE ){
            StringTokenizer tokenizer = new StringTokenizer(field.getValue(),",");
            List<String> valuesList = new ArrayList<String>();
            while (tokenizer.hasMoreElements()){
                valuesList.add(tokenizer.nextToken().trim());
            }
            String[] values = new String[]{};
                values = (String[])valuesList.toArray(values);
            filter.addClause(clauseComparator,values);
            } else if ( field.getObject()!= null ){
                filter.addClause(clauseComparator,field.getObject().toString());
            }
            namedFilters.put(fieldName, filter);
            if ( newFilter ){
                return filter;
            }
        } else {
            String[] values = field.getValues();
            String value = null;
            Category category = null;
            Set<Category> categories = new HashSet<Category>();
            for ( int i=0; i<values.length; i++ ){
                value = values[i];
                try {
                    category = Category.getCategory(value);
                    if ( category != null ){
                        categories.add(category);
                    }
                } catch ( Exception t ){
                }
            }
            if ( categories.isEmpty() ){
                return null;
            }
            ContainerFilterByCategories filter =  new ContainerFilterByCategories(categories,
                    context.getEntryLoadRequest(),false);
            return filter;
        }
        return null;
    }

    /**
     *
     * @param queryContainer
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    private ContainerSorterInterface buildContainerSorter( JahiaContainer queryContainer,
                                                         ContainerQueryContext queryContext,
                                                         ProcessingContext context)
    throws JahiaException {
        JahiaField field = queryContainer.getFieldByName("queryOrder");
        boolean ascendingOrder = !"desc".equalsIgnoreCase(getFieldValue(field,""));
        field = queryContainer.getFieldByName("numberSort");
        boolean numberSort = "true".equalsIgnoreCase(getFieldValue(field,""));

        field = queryContainer.getFieldByName("queryOrderBy");
        String fieldName = getFieldValue(field, "");

        List<String> definitionNames = queryContext.getContainerDefinitionsIncludingType(false);
        ContainerSorterInterface sorter = null;
        if ( !"".equals(fieldName) ){
            if ( fieldName.startsWith("metadata_") ){
                fieldName = fieldName.substring("metadata_".length());
                if ( queryContext.isSiteLevelQuery() ){
                    sorter = new ContainerMetadataSorterBean(queryContext.getSiteIDs(),context,fieldName,
                            definitionNames,numberSort,null,
                            context.getEntryLoadRequest());
                } else {
                    sorter = new ContainerMetadataSorterBean(queryContext.getContainerListID(),
                            fieldName,numberSort,context,context.getEntryLoadRequest());
                }
            } else {
                if ( queryContext.isSiteLevelQuery() ){
                    sorter = new ContainerSorterByContainerDefinition(queryContext.getSiteIDs(),
                            new String[]{fieldName},definitionNames,numberSort,"",
                        context.getEntryLoadRequest());
                } else {
                    sorter = new ContainerSorterBean(queryContext.getContainerListID(),
                            fieldName,numberSort,context.getEntryLoadRequest());
                }
            }
        }
        if ( sorter != null ){
            sorter.setAscOrdering(ascendingOrder);
        }
        return sorter;
    }

    /**
     *
     * @param field
     * @param defaultValue
     * @return
     */
    private String getFieldValue(JahiaField field, String defaultValue){
        String value = defaultValue;
        if ( field == null || field.getValue() == null || "".equals(field.getValue().trim()) ){
            return value;
        }
        return field.getValue().trim();
    }

}
