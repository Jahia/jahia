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

package org.jahia.data.containers;


import org.jahia.content.ContentContainerKey;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainer;

import java.io.Serializable;
import java.util.*;


/**
 * A filter used to returns all containers in given categories.
 * Same container definition name
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @corrector Cédric Mailleux <a href="mailto:cmailleux@jahia.org">cmailleux@jahia.org</a>
 * @see FilterClause
 * @see ContainerFilters
 * @see JahiaContainerSet
 */
public class ContainerFilterByCategories implements Serializable, ContainerFilterInterface {

    private static final long serialVersionUID = 2964673693707730611L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerFilterByCategories.class);

    private EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    private ContainerFilters containerFilters = null;

    private Set<Category> categories;

    private boolean multiValueANDLogic = false;

    private boolean withAllContainersOfCompoundContainerList = false;

    //--------------------------------------------------------------------------
    /**
     * This filter will returns all containers matching a set of category
     * <p/>
     * if withAllContainersOfCompoundContainerList = true,
     * return the containers without any category belonging to this containerlist
     *
     * @param categories
     * @param entryLoadRequest
     * @param withAllContainersOfCompoundContainerList
     *
     */
    public ContainerFilterByCategories(Set<Category> categories,
                                       EntryLoadRequest entryLoadRequest,
                                       boolean withAllContainersOfCompoundContainerList) {

        if (entryLoadRequest != null) {
            this.entryLoadRequest = entryLoadRequest;
        }
        if (categories == null) {
            this.categories = new HashSet<Category>();
        } else
            this.categories = categories;

        this.withAllContainersOfCompoundContainerList =
                withAllContainersOfCompoundContainerList;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering.
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    public BitSet doFilter(int ctnListID)
            throws JahiaException {
        return doFiltering(ctnListID);
    }

    //--------------------------------------------------------------------------
    /**
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    private BitSet doFiltering(int ctnListID)
            throws JahiaException {
        BitSet result = new BitSet();
        try {
            result =  getContainers(this.categories, null, null, ctnListID, false);
        } catch (Exception t) {
            logger.debug("Exception :", t);
        }
        if ( result == null ){
            result = new BitSet();
        }

        if (this.withAllContainersOfCompoundContainerList) {
            int ctnListId = this.containerFilters.getCtnListID();
            List<Integer> ids = ServicesRegistry.getInstance().getJahiaContainersService()
                    .getctnidsInList(ctnListId, this.entryLoadRequest);
            for (int index : ids) {
                Set<Category> objectCategories =
                        Category.getObjectCategories(ContentContainer.getContainer(index).getObjectKey());
                if(objectCategories==null || objectCategories.size()==0)
                    result.set(index);
            }
        }
        /*
        ContainerFilterByLoadRequest cfblr =
                new ContainerFilterByLoadRequest(this.entryLoadRequest);
        BitSet result2 = cfblr.doFilter(ctnListID);
        result.and(result2);
        */

        return result;

    }

    public boolean isMultiValueANDLogic() {
        return multiValueANDLogic;
    }

    public void setMultiValueANDLogic(boolean multiValueANDLogic) {
        this.multiValueANDLogic = multiValueANDLogic;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for all container list of the site.
     *
     * @param ctnListID, the container list id
     * @return String , the sql statement. Null on error
     */
    public String getSelect(int ctnListID, int filterId, Map<String, Object> parameters) {
        String loadRequest = "";
        if (this.entryLoadRequest != null) {
            loadRequest = this.entryLoadRequest.toString();
        }
        return (getCategoriesSearchQuery() + "_" + loadRequest);
    }

    //--------------------------------------------------------------------------
    /**
     * Set reference to a containerFilters
     *
     * @return
     * @throws JahiaException
     */
    public void setContainerFilters(ContainerFilters containerFilters) {
        this.containerFilters = containerFilters;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * <p/>
     * The expected result is a bit set of matching container ids.
     * <p/>
     * If siteId = -1 , returns results from all sites
     * <p/>
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteId
     * @param containerDefinitionName
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(int siteId, String containerDefinitionName, int listId)
            throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return doFilterBySite(siteIds, containerDefinitionNames, listId);
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * <p/>
     * The expected result is a bit set of matching container ids.
     * <p/>
     * @param siteIds all sites allowed if null or empty
     * @param containerDefinitionNames all definitions allowed if null or empty
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds, String[] containerDefinitionNames, int listId)
            throws JahiaException {
        return doFilteringBySite(siteIds, containerDefinitionNames, listId);
    }

    //--------------------------------------------------------------------------
    /**
     * The expected result is a bit set of matching container ids for a given siteId.
     * <p/>
     *
     * @param siteIds all sites allowed if null or empty
     * @param containerDefinitionNames all definitions allowed if null or empty
     * @param listId
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    private BitSet doFilteringBySite(Integer[] siteIds,
                                     String[] containerDefinitionNames, int listId)
            throws JahiaException {
        BitSet result = new BitSet();
        try {
            result = getContainers(this.categories, siteIds, containerDefinitionNames, listId, true);
        } catch (Exception t) {
            logger.error("Exception :", t);
        }
        if ( result == null ){
            result = new BitSet();
        }

        if (this.withAllContainersOfCompoundContainerList) {
            List<Integer> ids = ServicesRegistry.getInstance().getJahiaContainersService().getctnidsInList(listId, this.entryLoadRequest);
            for (int index : ids) {
                Set<Category> objectCategories =
                        Category.getObjectCategories(ContentContainer.getContainer(index).getObjectKey());
                if(objectCategories==null || objectCategories.size()==0)
                    result.set(index);
            }
        }
        /*
        ContainerFilterByLoadRequest cfblr =
                new ContainerFilterByLoadRequest(this.entryLoadRequest);
        BitSet result2 = cfblr.doFilterBySite(siteId, containerDefinitionName, listId);
        result.and(result2);*/
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     * If siteId = -1 -> build query for all sites
     * <p/>
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteId
     * @param containerDefinitionName
     * @return
     */
    public String getSelectBySiteID(int siteId, String containerDefinitionName, int filterId, Map<String, Object> parameters) {
        // It's a dummy select
        String loadRequest = "";
        if (this.entryLoadRequest != null) {
            loadRequest = this.entryLoadRequest.toString();
        }
        return (getCategoriesSearchQuery() + "_" + loadRequest);
    }

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     *
     * @param siteIds if null or empty all sites allowed
     * @param containerDefinitionNames if null or empty all definitions allowed
     * @return
     */
    public String getSelectBySiteID(Integer[] siteIds, String[] containerDefinitionNames, int filterId, Map<String, Object> parameters) {
        // It's a dummy select
        String loadRequest = "";
        if (this.entryLoadRequest != null) {
            loadRequest = this.entryLoadRequest.toString();
        }
        return (getCategoriesSearchQuery() + "_" + loadRequest);
    }

    private BitSet getContainers(Set<Category> categories, Integer[] siteIds, String[] containerDefinitionNames, int ctnListId,
                               boolean siteFiltering)
            throws JahiaException {

        BitSet bits = null;
        if ( siteFiltering ){
            if (containerDefinitionNames == null ){
                containerDefinitionNames = new String[]{};
            }
            ContainerFilterByContainerDefinitions filter =
                    new ContainerFilterByContainerDefinitions(containerDefinitionNames,
                    this.entryLoadRequest);
            bits = filter.doFilterBySite(siteIds,containerDefinitionNames,this.containerFilters.getCtnListID());
        } else {
            ContainerFilterByContainerDefinitions filter =
                    new ContainerFilterByContainerDefinitions(containerDefinitionNames,
                    this.entryLoadRequest);
            bits = filter.doFilter(ctnListId);
        }
        BitSet result = new BitSet();
        Map<String, BitSet> bitsByCategoryMap = new HashMap<String, BitSet>();
        for (Category category : categories) {
            BitSet bitsByCategory = (BitSet)bitsByCategoryMap.get(category.getKey());
            if (bitsByCategory == null){
                bitsByCategory = new BitSet();
                bitsByCategoryMap.put(category.getKey(),bitsByCategory);
            }
            for (ObjectKey curObjectKey : category.getChildObjectKeys()) {
                if (curObjectKey.getType().equals(ContentContainerKey.CONTAINER_TYPE)) {
                    if (bits!=null && !bits.get(curObjectKey.getIdInType())){
                        continue;
                    }
                    if (!this.multiValueANDLogic){
                        result.set(curObjectKey.getIdInType());
                    } else {
                        bitsByCategory = (BitSet)bitsByCategoryMap.get(category.getKey());
                        bitsByCategory.set(curObjectKey.getIdInType());
                    }
                    /*
                    try {
                        ContentContainer contentContainer =
                                (ContentContainer) ContentContainer.getInstance(curObjectKey);
                        if (contentContainer != null && (siteId == -1 || siteId == contentContainer.getSiteID())) {
                            if (containerDefinitionName != null) {
                                try {
                                    ContentDefinition definition = ContentDefinition
                                            .getContentDefinitionInstance(contentContainer.getDefinitionKey(null));
                                    if (definition != null && containerDefinitionName.equalsIgnoreCase(
                                            definition.getName())) {
                                        val.add(contentContainer);
                                    }
                                } catch (Exception t) {
                                    logger.debug("Error retrieving container definition for container "
                                                 + contentContainer.getID(), t);
                                }
                            } else {
                                val.add(contentContainer);
                            }
                        }
                    } catch (Exception t) {
                        logger.debug("Error loading contentContainer " + curObjectKey.toString());
                    }*/
                }
            }
        }
        if (this.multiValueANDLogic){
            result = null;
            for (BitSet bitsByCategory : bitsByCategoryMap.values()){
                if (result == null ){
                    result = bitsByCategory;
                } else {
                    result.and(bitsByCategory);
                }
            }
        }
        if (result==null){
            result = new BitSet();
        }
        return result;
    }

    private String getCategoriesSearchQuery() {
        StringBuffer buff = new StringBuffer("(");
        int i = 0;
        for (Category cat : this.categories) {
            if (i > 0) {
                if ( this.multiValueANDLogic ){
                    buff.append(" AND ");
                } else {
                    buff.append(" OR ");
                }
            }
            buff.append(JahiaSearchConstant.CATEGORY_ID);
            buff.append(":");
            if (cat != null) {
                buff.append(cat.getKey());
            }
            i++;
        }
        buff.append(")");
        return buff.toString();
    }
}
