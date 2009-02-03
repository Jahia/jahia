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

//
//
//
//
//
// 27.05.2002 NK added in Jahia

package org.jahia.data.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.OrderedBitSet;
import org.springframework.context.ApplicationContext;

/**
 * Jahia Standard Containers Filtering Handler.
 * 
 * There are two main filtering modes: 1) Filtering on a containers of a unique container list 2) Filtering on all containers of a site or
 * of all sites
 * 
 * @see ContainerFilterInterface
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerFilters implements Serializable {

    private static final long serialVersionUID = -195878118592509014L;

    private int ctnListID = -1;

    private Integer[] siteIds;

    private String[] containerDefinitionNames;

    private boolean siteModeFiltering = false;

    private OrderedBitSet bits;

    private List<ContainerFilterInterface> containerFilters;

    private String query;

    private Map<String, Object> queryParameters;

    private boolean isValid = false;

    private boolean updated = false;

    private long lastFilteringTime = -1;

    private List<Integer> stagingFields = null;

    private String contextID;

    private ContainerQueryContext queryContext;

    private boolean oldMode = false;

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * Filtering on one unique container list
     * 
     * @param ctnListID
     *            , the container list id.
     * @param containerFilters
     *            , a List of ContainerFilterInterface objects.
     */
    public ContainerFilters(int ctnListID,
            List<ContainerFilterInterface> containerFilters)
            throws JahiaException {
        this.ctnListID = ctnListID;
        this.containerFilters = containerFilters;
        initContainerFilters();
        buildQuery();
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * Filtering on one unique container list
     * 
     * @param containerListName
     *            , the container list name.
     * @param params
     *            the param bean.
     * @param containerFilters
     *            , a List of ContainerFilterInterface objects.
     */
    public ContainerFilters(String containerListName, ProcessingContext params,
            List<ContainerFilterInterface> containerFilters)
            throws JahiaException {
        this.containerFilters = containerFilters;
        initContainerFilters();

        if (containerListName != null) {
            int clistID = ServicesRegistry.getInstance()
                    .getJahiaContainersService().getContainerListID(
                            containerListName, params.getPage().getID());
            if (clistID != -1) {
                this.ctnListID = clistID;
                buildQuery();
            }
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * Filtering on all containers of a given site or of all sites
     * 
     * If siteId = -1 , returns containers from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param containerFilters
     * @param siteId
     * @param containerDefinitionName
     * @throws JahiaException
     */
    public ContainerFilters(List<ContainerFilterInterface> containerFilters,
            int siteId, String containerDefinitionName) throws JahiaException {
        if (siteId > -1) {
            this.siteIds = new Integer[] { new Integer(siteId) };
        }
        this.siteModeFiltering = true;
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && !"".equals(containerDefinitionName.trim())) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        this.containerDefinitionNames = containerDefinitionNames;
        this.containerFilters = containerFilters;
        initContainerFilters();
        buildQuery();
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * Filtering on all containers of a given site or of all sites
     * 
     * If the containerDefinitionNames is null, return result from all containers no regards to it definition !
     * 
     * @param containerFilters
     * @param siteIds
     *            if empty or null, all sites are allowed.
     * @param containerDefinitionNames
     * @throws JahiaException
     */
    public ContainerFilters(List<ContainerFilterInterface> containerFilters,
            Integer[] siteIds, String[] containerDefinitionNames)
            throws JahiaException {
        this.siteIds = siteIds;
        this.siteModeFiltering = true;
        this.containerDefinitionNames = containerDefinitionNames;

        this.containerFilters = containerFilters;
        initContainerFilters();
        buildQuery();
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * Filtering on all containers of a given site or of all sites
     * 
     * If the containerDefinitionNames is null, return result from all containers no regards to it definition !
     * 
     * @param containerFilters
     * @param siteIds
     *            if empty or null, all sites are allowed.
     * @param containerDefinitionNames
     * @throws JahiaException
     */
    public ContainerFilters(List<ContainerFilterInterface> containerFilters,
            List<Integer> siteIds, List<String> containerDefinitionNames)
            throws JahiaException {
        if (siteIds != null && !siteIds.isEmpty()) {
            this.siteIds = (Integer[]) siteIds.toArray(new Integer[] {});
        }
        if (containerDefinitionNames != null
                && !containerDefinitionNames.isEmpty()) {
            this.containerDefinitionNames = new String[containerDefinitionNames
                    .size()];
            int i = 0;
            for (String containerDefName : containerDefinitionNames) {
                this.containerDefinitionNames[i] = containerDefName;
                i++;
            }
        }
        this.siteModeFiltering = true;
        this.containerFilters = containerFilters;
        initContainerFilters();
        buildQuery();
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering. The expected result is a bit set of matching container ids.
     * 
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    public BitSet doFilter() throws JahiaException {
        if (!isQueryValid())
            return null;

        // this.getStagingFields(true);

        OrderedBitSet result = new OrderedBitSet();

        BitSet bits = null;
        ContainerFilterInterface containerFilter = null;
        List<ContainerFilterInterface> v = getContainerFilters();
        int size = v.size();
        for (int i = 0; i < size; i++) {
            containerFilter = (ContainerFilterInterface) v.get(i);
            if (this.isSiteModeFiltering()) {
                if (oldMode) {
                    bits = containerFilter.doFilterBySite(getSiteId(), this
                            .getContainerDefinitionName(), getCtnListID());
                } else {
                    bits = containerFilter.doFilterBySite(getSiteIds(), this
                            .getContainerDefinitionNames(), getCtnListID());
                }
            } else {
                bits = containerFilter.doFilter(getCtnListID());
            }
            if (bits != null) {
                if (i == 0) {
                    result.or(bits);
                } else {
                    result.and(bits);
                }
            }
        }

        if (size == 1 && bits instanceof OrderedBitSet) {
            OrderedBitSet orderedBitSet = (OrderedBitSet) bits;
            if (orderedBitSet.isOrdered()) {
                result.setOrdered(true);
                result.setOrderedBits(orderedBitSet.getOrderedBits());
            }
        }
        this.bits = result;
        this.updated = true;

        // Set search time
        this.lastFilteringTime = System.currentTimeMillis();
        this.stagingFields = null;
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the bit set of matching ctn ids. YOu must call doFilter first.
     * 
     * @return BitSet the bit set of matching ctn ids.
     */
    public BitSet bits() {
        return this.bits;
    }

    // --------------------------------------------------------------------------
    /**
     * Return true if the filtering is done on an entire site ( or all sites ) false, if the filtering is done on one container list ( using
     * ctnListId )
     * 
     * @return
     */
    public boolean isSiteModeFiltering() {
        return this.siteModeFiltering;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the container list id.
     * 
     * @return int ctnListID, the container list id.
     */
    public int getCtnListID() {
        return this.ctnListID;
    }

    public void setCtnListID(int id) {
        this.ctnListID = id;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the site Id.
     * 
     * @deprecated use getSiteIds instead
     */
    public int getSiteId() {
        if (this.siteIds == null || this.siteIds.length == 0) {
            return -1;
        }
        return this.siteIds[0].intValue();
    }

    /**
     * array of allowed sites. if null or empty, all sites are allowed. if contains -1, all sites are allowed
     * 
     * @return
     */
    public Integer[] getSiteIds() {
        return siteIds;
    }

    public void setSiteIds(Integer[] siteIds) {
        this.siteIds = siteIds;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the container definition name.
     * 
     * @deprecated, use getContainerDefinitionNames instead
     */
    public String getContainerDefinitionName() {
        if (this.containerDefinitionNames == null
                || this.containerDefinitionNames.length == 0) {
            return null;
        }
        return this.containerDefinitionNames[0];
    }

    /**
     * Returns the allowed definitions. If null, all definitions are allowed
     * 
     * @return
     */
    public String[] getContainerDefinitionNames() {
        return containerDefinitionNames;
    }

    public void setContainerDefinitionNames(String[] containerDefinitionNames) {
        this.containerDefinitionNames = containerDefinitionNames;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the query as String. This query is only a SQL query representation of the combination of all filter select statement, but not
     * the one performed agains the DB ! "Intersect" Set operator is not available with all DB ( i.e : ACCESS ).
     * 
     * @return String, the query.
     */
    public String getQuery() {
        return this.query;
    }
    
    /**
     * Return the query parameters in a Map. 
     * 
     * @return String, the query.
     */
    public Map<String, Object> getQueryParameters() {
        return this.queryParameters;
    }    

    // --------------------------------------------------------------------------
    /**
     * Return the List of container filter bean.
     * 
     * @return List, the List ofcontainer filter bean.
     */
    public List<ContainerFilterInterface> getContainerFilters() {
        return this.containerFilters;
    }

    // --------------------------------------------------------------------------
    /**
     * Return true if the query is valid.
     * 
     * @return boolean , true if the query is valid.
     */
    public boolean isQueryValid() {
        return this.isValid;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the last filtering running time.
     * 
     * @return long , the last filtering time
     */
    public long getLastFilteringTime() {
        return this.lastFilteringTime;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the update status. Each time the doFilter method is called, this update status is set to true.
     * 
     * @return boolean, the internal updated status value.
     */
    public boolean getUpdateStatus() {
        return this.updated;
    }

    // --------------------------------------------------------------------------
    /**
     * Set the update status to true.
     * 
     */
    public void setUpdateStatus() {
        this.updated = true;
    }

    // --------------------------------------------------------------------------
    /**
     * You can reset the internal update status by setting it to false
     * 
     */
    public void resetUpdateStatus() {
        this.updated = false;
    }

    /**
     * This contextID is optional and is used for session cached filters. This is usefull for caching different filters applied to a same
     * container list
     * 
     * @return
     */
    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public ContainerQueryContext getQueryContext() {
        return queryContext;
    }

    public void setQueryContext(ContainerQueryContext queryContext) {
        this.queryContext = queryContext;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     * 
     * @param loadFromDB
     * @param containerDefinitionName
     *            , can be a comma separated definition names
     * @param entryLoadRequest
     * @return
     * @throws JahiaException
     */
    public List<Integer> getStagingFields(boolean loadFromDB,
            String containerDefinitionName, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && !"".equals(containerDefinitionName.trim())) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return getStagingFields(loadFromDB, containerDefinitionNames,
                entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     * 
     * @param loadFromDB
     * @param containerDefinitionNames
     * @param entryLoadRequest
     * @return
     * @throws JahiaException
     */
    public List<Integer> getStagingFields(boolean loadFromDB,
                                               String[] containerDefinitionNames, EntryLoadRequest entryLoadRequest)
            throws JahiaException {

        if (!loadFromDB && this.stagingFields != null) {
            return this.stagingFields;
        }

        StringBuffer buff = new StringBuffer(
                "select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b, JahiaCtnDef c where ");
        Map<String, Object> parameters = new HashMap<String, Object>();

        if (!this.isSiteModeFiltering()) {
            buff.append(" a.listid=(:ctnListId) AND ");
            parameters.put("ctnListId", ctnListID);
        } else {
            if (!this.allSitesAllowed()) {
                buff.append("a.siteId IN (:siteIds) AND ");
                parameters.put("siteIds", siteIds);
            }

            if (containerDefinitionNames != null && containerDefinitionNames.length > 0) {
                buff.append(" a.ctndef.id = c.id and c.name in (:ctnDefNames) AND ");
                parameters.put("ctnDefNames", containerDefinitionNames);
            }
        }
        buff.append(" a.comp_id.id=b.containerId AND b.comp_id.workflowState > 1 AND a.comp_id.workflowState > 1 AND (");
        buff.append(ContainerFilterBean.buildMultilangAndWorlflowQuery(entryLoadRequest, false, true));
        buff.append(" )");
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context.getBean(JahiaFieldsDataManager.class.getName());
        List<Integer> datas = new ArrayList<Integer>(fieldMgr.<Integer>executeQuery(buff.toString(), parameters));
        this.stagingFields = datas;

        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * Compare two queries.
     * 
     * @return boolean , true if the given query is different with the internal query.
     */
    public boolean compareQuery(String query) {
        if (!isQueryValid() || query == null) {
            return false;
        }
        return (this.query.equals(query));
    }

    /**
     * Return true only if there is only one single ContainerSearcherToFilterAdapter instance in the List of filters. This means that the
     * whole query will be done using search query.
     * 
     * @return
     */
    public boolean isSingleSearchFilter() {
        if (this.containerFilters != null && this.containerFilters.size() == 1) {
            ContainerFilterInterface filter = (ContainerFilterInterface) this.containerFilters
                    .get(0);
            if (filter instanceof ContainerSearcherToFilterAdapter) {
                return true;
            } else if (filter instanceof ContainerChainedFilter) {
                ContainerFilterInterface[] filters = ((ContainerChainedFilter) filter)
                        .getChain();
                if (filters != null && filters.length == 1) {
                    return (filters[0] instanceof ContainerSearcherToFilterAdapter);
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------
    /**
     *
     */
    private void initContainerFilters() {
        if (this.containerFilters == null || this.containerFilters.size() == 0) {
            return;
        }
        int size = this.containerFilters.size();
        for (int i = 0; i < size; i++) {
            ContainerFilterInterface filterBean = (ContainerFilterInterface) this.containerFilters
                    .get(i);
            if (filterBean != null) {
                filterBean.setContainerFilters(this);
            }
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Build the internal DB query.
     * 
     */
    private void buildQuery() {
        if (containerFilters == null || containerFilters.isEmpty()) {
            return;
        }
        this.queryParameters = new HashMap<String, Object>();
        StringBuffer buff = new StringBuffer();
        String fieldFilterQuery;
        ContainerFilterInterface containerFilter;
        int size = containerFilters.size();
        for (int i = 0; i < size; i++) {
            containerFilter = (ContainerFilterInterface) containerFilters
                    .get(i);
            if (containerFilter == null)
                return;
            if (this.isSiteModeFiltering()) {
                fieldFilterQuery = containerFilter.getSelect(this.getSiteId(), 0, 
                        queryParameters);
            } else {
                fieldFilterQuery = containerFilter.getSelect(this
                        .getCtnListID(), 0, queryParameters);
            }

            if (fieldFilterQuery != null && !fieldFilterQuery.trim().equals("")) {
                buff.append(fieldFilterQuery);
            }
            if (i < size - 1) {
                buff.append(" INTERSECT ");
            }
        }

        this.query = buff.toString();

        this.isValid = true;
    }

    // -------------------------------------------------------------------------
    public String toString() {
        StringBuffer result = new StringBuffer();
        if (containerFilters != null) {
            result.append("Container filters:");
            result.append(containerFilters);
            result.append("\n");
        }
        if (bits != null) {
            result.append("Bitset: ");
            result.append(bits);
            result.append("\n");
        }
        if (stagingFields != null) {
            result.append("Staging fields: ");
            result.append(stagingFields);
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Return true if all sites are allowed.
     * 
     * @return
     */
    public boolean allSitesAllowed() {
        return (this.siteIds == null || this.siteIds.length == 0);
    }

    /**
     * Return true if all definition are allowed.
     * 
     * @return
     */
    public boolean allDefinitionAllowed() {
        return (this.containerDefinitionNames == null || this.containerDefinitionNames.length == 0);
    }

    /**
     * If true, filtering must be done using old mode ( for backward compatibility ).
     * 
     * @return
     */
    public boolean isOldMode() {
        return oldMode;
    }

    public void setOldMode(boolean oldMode) {
        this.oldMode = oldMode;
    }

}
