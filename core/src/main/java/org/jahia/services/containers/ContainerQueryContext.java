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

import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.registries.ServicesRegistry;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 oct. 2007
 * Time: 15:00:07
 * To change this template use File | Settings | File Templates.
 */
public class ContainerQueryContext {
    private int containerListID;
    private String containerDefinitionType;
    private List<String> containerDefinitionNames;
    private List<Integer> siteIDs;
    private boolean siteLevelQuery;
    
    private String facetFilterQueryParamName;
    private BitSet facetedFilterResult;
    
    private List<String> containerDefinitionNamesIncludingType;
    
    private Byte[] cntDefNameLock = new Byte[0];

    /**
     * Returns a ContainerQueryContext instance for the target container list
     *
     * @param queryModel The queryModel
     * @param containerListID The target container list
     * @param parameters the parameters used to override the the default parameters defined at the queryModel
     *          definition.
     * @param context
     * @return
     */
    public static ContainerQueryContext getQueryContext(QueryObjectModelImpl queryModel,
                                                        int containerListID,
                                                        Properties parameters, ProcessingContext context) {
        ContainerQueryContext newContext = new ContainerQueryContext(containerListID,
                null,null,false);        
        newContext.setFacetFilterQueryParamName((String) queryModel.getProperties().get(
                JahiaQueryObjectModelConstants.FACET_FILTER_QUERY_PARAM_NAME));
        return newContext;
    }

    protected ContainerQueryContext(int containerListID,
                                 List<String> containerDefinitionNames,
                                 List<Integer> siteIDs,
                                 boolean siteLevelQuery) {

        this.containerListID = containerListID;
        this.containerDefinitionNames = containerDefinitionNames;
        this.siteIDs = siteIDs;
        this.siteLevelQuery = siteLevelQuery;
    }

    public int getContainerListID() {
        return containerListID;
    }

    public void setContainerListID(int containerListID) {
        this.containerListID = containerListID;
    }

    public List<String> getContainerDefinitionNames() {
        if ( containerDefinitionNames == null ){
            containerDefinitionNames = new ArrayList<String>();
        }
        return containerDefinitionNames;
    }

    public void setContainerDefinitionNames(List<String> containerDefinitionNames) {
        this.containerDefinitionNames = containerDefinitionNames;
    }

    public String getContainerDefinitionType() {
        return containerDefinitionType;
    }

    public void setContainerDefinitionType(String containerDefinitionType) {
        this.containerDefinitionType = containerDefinitionType;
    }

    public List<Integer> getSiteIDs() {
        if ( siteIDs == null ){
            siteIDs = new ArrayList<Integer>();
        }
        return siteIDs;
    }

    public void setSiteIDs(List<Integer> siteIDs) {
        this.siteIDs = siteIDs;
    }

    public boolean isSiteLevelQuery() {
        return siteLevelQuery;
    }

    public void setSiteLevelQuery(boolean siteLevelQuery) {
        this.siteLevelQuery = siteLevelQuery;
    }

    /**
     * Returns the list of container definitions including thoses that have one of the internal alias names
     * @param reload recalculation
     * @return
     */
    public List<String> getContainerDefinitionsIncludingType(boolean reload) {
        if (!reload && containerDefinitionNamesIncludingType != null){
            return containerDefinitionNamesIncludingType;
        }
        List<String> definitionNames = getContainerDefinitionNames();
        if (definitionNames == null){
            definitionNames = new ArrayList<String>();
        }

        if (containerDefinitionType != null) {
            List<String> definitionNamesFromType = ServicesRegistry.getInstance().getJahiaContainersService().getContainerDefinitionNamesWithType(containerDefinitionType);
            if (definitionNamesFromType != null){
                for (String definitionName : definitionNamesFromType) {
                    if (!definitionNames.contains(definitionName)){
                        definitionNames.add(definitionName);
                    }
                }
            }
        }
        synchronized (cntDefNameLock) {
            List<String> newContainerDefinitionNamesIncludingType = new ArrayList<String>();
            if (containerDefinitionNamesIncludingType != null) {
                newContainerDefinitionNamesIncludingType.addAll(containerDefinitionNamesIncludingType);
            }
            newContainerDefinitionNamesIncludingType.addAll(definitionNames);
            containerDefinitionNamesIncludingType = newContainerDefinitionNamesIncludingType;
        }        
        
        return containerDefinitionNamesIncludingType;
    }

    public void setFacetFilterQueryParamName(String facetFilterQueryParamName) {
        this.facetFilterQueryParamName = facetFilterQueryParamName;
    }

    public String getFacetFilterQueryParamName() {
        return facetFilterQueryParamName;
    }

    public void setFacetedFilterResult(BitSet facetedFilterResult) {
        this.facetedFilterResult = facetedFilterResult;
    }

    public BitSet getFacetedFilterResult() {
        return facetedFilterResult;
    }   
}
