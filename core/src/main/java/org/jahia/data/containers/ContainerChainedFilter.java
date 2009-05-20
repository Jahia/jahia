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
 package org.jahia.data.containers;

import org.jahia.exceptions.JahiaException;

import java.util.BitSet;
import java.util.Map;
import java.io.Serializable;

/**
 * Use this class to combine multiple filters implementing the {@link ContainerFilterInterface} interface, such as
 * {@link ContainerFilterBean} or {@link ContainerChainedFilter}, with logical operators.
 * 
 * Since you can enclose multiple {@link ContainerChainedFilter} objects inside a ContainerChainedFilter object, this means you can have a
 * hierarchy of logical operators, giving an equilavent to braces in the following expression (a AND b OR (C XOR d))
 * 
 * Supported logical operators are: OR, AND, ANDNOT, XOR.
 * 
 * Example:
 * 
 * ContainerFilterBean containerFilter1 = new ContainerFilterBean("people", jParams.getEntryLoadRequest());
 * containerFilter1.addClause(ContainerFilterBean.COMP_EQUAL,"");
 * 
 * ContainerFilterBean containerFilter2 = new ContainerFilterBean("skills", jParams.getEntryLoadRequest());
 * containerFilter2.addClause(ContainerFilterBean.COMP_EQUAL,"computers");
 * 
 * ContainerFilterBean containerFilter3 = new ContainerFilterBean("john", jParams.getEntryLoadRequest());
 * containerFilter3.addClause(ContainerFilterBean.COMP_EQUAL,"");
 * 
 * ContainerFilterBean containerFilter4 = new ContainerFilterBean("Age",true, jParams.getEntryLoadRequest());
 * containerFilter4.addClause(ContainerFilterBean.COMP_SMALLER_OR_EQUAL,String.valueOf(ageLong));
 * 
 * ContainerChainedFilter someFilters = new ContainerChainedFilter( new
 * ContainerFilterInterface[]{containerFilter1,containerFilter2,containerFilter3,containerFilter4}, new
 * int[]{ContainerChainedFilter.OR,ContainerChainedFilter.OR,ContainerChainedFilter.OR});
 * 
 * ContainerFilterBean containerFilter5 = new ContainerFilterBean("Type", jParams.getEntryLoadRequest());
 * containerFilter4.addClause(ContainerFilterBean.COMP_NOTEQUAL,"Wood");
 * 
 * ContainerChainedFilter allFilters = new ContainerChainedFilter( new ContainerFilterInterface[]{someFilters,containerFilter5}, new
 * int[]{ContainerChainedFilter.AND});
 * 
 * This is equivalent to :
 * 
 * (people=="" OR skills=="computers" OR john=="" OR Age<=ageLong) AND Type!=Wood
 * 
 * @see ContainerFilterInterface
 * @see ContainerFilterBean
 * @see ContainerFilterByContainerDefinition
 * @see ContainerFilterByContainerDefinitions
 * 
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @author MC
 */
public class ContainerChainedFilter implements Serializable,
        ContainerFilterInterface {

    private static final long serialVersionUID = -1123817010093670641L;

    /**
     * {@link BitSet#or}.
     */
    public static final int OR = 0;

    /**
     * {@link BitSet#and}.
     */
    public static final int AND = 1;

    /**
     * {@link BitSet#andNot}.
     */
    public static final int ANDNOT = 2;

    /**
     * {@link BitSet#xor}.
     */
    public static final int XOR = 3;

    /**
     * Logical operation when none is declared. Defaults to {@link BitSet#or}.
     */
    public static final int DEFAULT = OR;

    /** The filter chain */
    private ContainerFilterInterface[] chain = null;

    private int[] logicArray;

    private int logic = -1;

    private ContainerFilters containerFilters;

    /**
     * Ctor.
     * 
     * @param chain
     *            The chain of filters
     */
    public ContainerChainedFilter(ContainerFilterInterface[] chain) {
        this.chain = chain;
    }

    /**
     * Ctor.
     * 
     * @param chain
     *            The chain of filters
     * @param logicArray
     *            Logical operations to apply between filters
     */
    public ContainerChainedFilter(ContainerFilterInterface[] chain,
            int[] logicArray) {
        this.chain = chain;
        this.logicArray = logicArray;
    }

    /**
     * Ctor.
     * 
     * @param chain
     *            The chain of filters
     * @param logic
     *            Logicial operation to apply to ALL filters
     */
    public ContainerChainedFilter(ContainerFilterInterface[] chain, int logic) {
        this.chain = chain;
        this.logic = logic;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering. The expected result is a bit set of matching container ids.
     * 
     * @ param int ctnListID, the container list id @ return BitSet bitsFromLogic, the expected result as a bit set of matching ctn ids,each
     * bit position set to true correspond to matching ctn ids.
     */
    public BitSet doFilter(int ctnListID) throws JahiaException {
        return this.bits(new FilterData(ctnListID));
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for all container list of the site.
     * 
     * @ param int ctnListID, the container list id 
     * @ return String , the sql statement. Null on error
     */
    public String getSelect(int ctnListID, int filterId,
            Map<String, Object> parameters) {
        if (chain == null) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < chain.length; i++) {
            ContainerFilterInterface filter = chain[i];
            String val = filter.getSelect(ctnListID, filterId == 0 ? i : filterId
                    * 100 + i, parameters);
            if (val != null) {
                if (i > 0) {
                    int logop = (this.logic != -1) ? this.logic
                            : this.logicArray[i - 1];
                    switch (logop) {
                    case OR:
                        buff.append(" UNION ");
                        break;
                    case AND:
                        buff.append(" INTERSECT ");
                        break;
                    case XOR:
                        buff.append(" XOR "); // probably not supported in SQL but this SQL statement is not executed anyway, just used as
                                              // ID for caching
                        break;
                    case ANDNOT:
                        buff.append(" ANDNOT "); // probably not supported in SQL but this SQL statement is not executed anyway, just used
                                                 // as ID for caching
                        break;
                    default:
                        buff.append(" DEFAULT_OPERATOR ");// probably not supported in SQL but this SQL statement is not executed anyway,
                                                          // just used as ID for caching
                        break;
                    }
                }

                buff.append(val);
            }
        }
        return buff.toString();
    }

    // --------------------------------------------------------------------------
    /**
     * Set reference to a containerFilters
     * 
     * @ return @ throws JahiaException
     */
    public void setContainerFilters(ContainerFilters containerFilters) {
        this.containerFilters = containerFilters;
        if (chain == null) {
            return;
        }
        ContainerFilterInterface filter = null;
        for (int i = 0; i < chain.length; i++) {
            filter = chain[i];
            filter.setContainerFilters(containerFilters);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param siteId
     *            if -1, all sites are allowed
     * @param containerDefinitionName
     *            if null or empty all definitions are allowed
     * @param listId
     *            , optionally, a listID can be passed to returns all containers of this containerlist.
     * @return BitSet bitsFromLogic, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to
     *         matching ctn ids.
     * @deprecated, use doFilterBySite(int[] siteIds, String[] containerDefinitionNames, int listId)
     * @throws JahiaException
     */
    public BitSet doFilterBySite(int siteId, String containerDefinitionName,
            int listId) throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && !"".equals(containerDefinitionName.trim())) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return this.bits(new FilterData(siteIds, containerDefinitionNames,
                listId));
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param siteIds
     *            if null or empty, all sites are allowed
     * @param containerDefinitionNames
     *            if null or empty all definition are allowed
     * @param listId
     *            , optionally, a listID can be passed to returns all containers of this containerlist.
     * @return BitSet bitsFromLogic, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to
     *         matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds,
            String[] containerDefinitionNames, int listId)
            throws JahiaException {
        return this.bits(new FilterData(siteIds, containerDefinitionNames,
                listId));
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteIds
     *            if null or empty, all sites are allowed
     * @param containerDefinitionNames
     *            if null or empty, all sites are allowed
     * @return
     */
    public String getSelectBySiteID(Integer[] siteIds,
            String[] containerDefinitionNames, int filterId,
            Map<String, Object> parameters) {
        if (chain == null) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        ContainerFilterInterface filter = null;
        String val = "";
        for (int i = 0; i < chain.length; i++) {
            filter = chain[i];
            val = filter.getSelectBySiteID(siteIds, containerDefinitionNames,
                    filterId == 0 ? i : filterId * 100 + i, parameters);
            if (val != null) {
                if (i > 0) {
                    int logop = (this.logic != -1) ? this.logic
                            : this.logicArray[i - 1];
                    switch (logop) {
                    case OR:
                        buff.append(" UNION ");
                        break;
                    case AND:
                        buff.append(" INTERSECT ");
                        break;
                    case XOR:
                        buff.append(" XOR "); // probably not supported in SQL but this SQL statement is not executed anyway, just used as
                                              // ID for caching
                        break;
                    case ANDNOT:
                        buff.append(" ANDNOT ");// probably not supported in SQL but this SQL statement is not executed anyway, just used as
                                                // ID for caching
                        break;
                    default:
                        buff.append(" DEFAULT_OPERATOR ");// probably not supported in SQL but this SQL statement is not executed anyway,
                                                          // just used as ID for caching
                        break;
                    }
                }
                buff.append(val);
            }
        }
        return buff.toString();
    }

    // --------------------------------------------------------------------------
    /**
     * @deprecated for backward compatibility
     * @return
     */
    public String getSelectBySiteID(int siteId, String containerDefinitionName,
            int filterId, Map<String, Object> parameters) {
        if (chain == null) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        ContainerFilterInterface filter = null;
        String val = "";
        for (int i = 0; i < chain.length; i++) {
            filter = chain[i];
            val = filter.getSelectBySiteID(siteId, containerDefinitionName,
                    filterId == 0 ? i : filterId * 100 + i, parameters);
            if (val != null) {
                if (i > 0) {
                    int logop = (this.logic != -1) ? this.logic
                            : this.logicArray[i - 1];
                    switch (logop) {
                    case OR:
                        buff.append(" UNION ");
                        break;
                    case AND:
                        buff.append(" INTERSECT ");
                        break;
                    case XOR:
                        buff.append(" XOR "); // probably not supported in SQL but this SQL statement is not executed anyway, just used as
                                              // ID for caching
                        break;
                    case ANDNOT:
                        buff.append(" ANDNOT ");// probably not supported in SQL but this SQL statement is not executed anyway, just used as
                                                // ID for caching
                        break;
                    default:
                        buff.append(" DEFAULT_OPERATOR ");// probably not supported in SQL but this SQL statement is not executed anyway,
                                                          // just used as ID for caching
                        break;
                    }
                }
                buff.append(val);
            }
        }
        return buff.toString();
    }

    public ContainerFilterInterface[] getChain() {
        return this.chain;
    }

    private BitSet bits(FilterData filterData) throws JahiaException {
        if (logic != -1)
            return bitsFromLogic(logic, filterData);
        else if (logicArray != null)
            return bits(logicArray, filterData);
        else
            return bitsFromLogic(DEFAULT, filterData);
    }

    /**
     * Delegates to each filter in the chain.
     * 
     * @param logic
     *            Logical operation
     * @return BitSet
     */
    private BitSet bitsFromLogic(int logic, FilterData filterData)
            throws JahiaException {
        BitSet result;
        int i = 0;

        /**
         * First AND operation takes place against a completely false bitset and will always return zero results. Thanks to Daniel Armbrust
         * for pointing this out and suggesting workaround.
         */
        if (logic == AND) {
            if (!filterData.isSiteSearch()) {
                result = getSafeClonedBitSet(chain[i].doFilter(filterData
                        .getListId()));
            } else {
                if (this.containerFilters.isOldMode()) {
                    int siteId = -1;
                    if (filterData.getSiteIds() != null
                            && filterData.getSiteIds().length > 0) {
                        siteId = filterData.getSiteIds()[0].intValue();
                    }
                    String definitionName = null;
                    if (filterData.getContainerDefinitionNames() != null
                            && filterData.getContainerDefinitionNames().length > 0) {
                        definitionName = filterData
                                .getContainerDefinitionNames()[0];
                    }
                    result = getSafeClonedBitSet(chain[i].doFilterBySite(
                            siteId, definitionName, filterData.getListId()));
                } else {
                    result = getSafeClonedBitSet(chain[i].doFilterBySite(
                            filterData.getSiteIds(), filterData
                                    .getContainerDefinitionNames(), filterData
                                    .getListId()));
                }
            }
            ++i;
        } else {
            result = new BitSet();
        }

        for (; i < chain.length; i++) {
            doChain(result, logic, chain[i], filterData);
        }
        return result;
    }

    /**
     * Delegates to each filter in the chain.
     * 
     * @param logic
     *            Logical operation
     * @return BitSet
     */
    private BitSet bits(int[] logic, FilterData filterData)
            throws JahiaException {
        if (logic.length < chain.length - 1)
            throw new IllegalArgumentException(
                    "Invalid number of elements in logic array");

        BitSet result = null;
        if (!filterData.isSiteSearch()) {
            result = getSafeClonedBitSet(chain[0].doFilter(filterData
                    .getListId()));
        } else {
            result = getSafeClonedBitSet(chain[0].doFilterBySite(filterData
                    .getSiteIds(), filterData.getContainerDefinitionNames(),
                    filterData.getListId()));
        }

        for (int i = 1; i < chain.length; i++) {
            doChain(result, logic[i - 1], chain[i], filterData);
        }
        return result;
    }

    private void doChain(BitSet result, int logic,
            ContainerFilterInterface filter, FilterData filterData)
            throws JahiaException {
        if (result == null) {
            return;
        }
        switch (logic) {
        case OR:
            if (!filterData.isSiteSearch()) {
                result.or(filter.doFilter(filterData.getListId()));
            } else {
                result.or(filter.doFilterBySite(filterData.getSiteIds(),
                        filterData.getContainerDefinitionNames(), filterData
                                .getListId()));
            }
            break;
        case AND:
            if (result.isEmpty()) // --> no need to executeQuery next filter
                break;
            if (!filterData.isSiteSearch()) {
                result.and(filter.doFilter(filterData.getListId()));
            } else {
                result.and(filter.doFilterBySite(filterData.getSiteIds(),
                        filterData.getContainerDefinitionNames(), filterData
                                .getListId()));
            }
            break;
        case ANDNOT:
            if (result.isEmpty()) // --> no need to executeQuery next filter
                break;
            if (!filterData.isSiteSearch()) {
                result.andNot(filter.doFilter(filterData.getListId()));
            } else {
                result.andNot(filter.doFilterBySite(filterData.getSiteIds(),
                        filterData.getContainerDefinitionNames(), filterData
                                .getListId()));
            }
            break;
        case XOR:
            if (!filterData.isSiteSearch()) {
                result.xor(filter.doFilter(filterData.getListId()));
            } else {
                result.xor(filter.doFilterBySite(filterData.getSiteIds(),
                        filterData.getContainerDefinitionNames(), filterData
                                .getListId()));
            }
            break;
        default:
            doChain(result, DEFAULT, filter, filterData);
            break;
        }
    }

    private BitSet getSafeClonedBitSet(BitSet bits) {
        if (bits == null) {
            return null;
        }
        return (BitSet) bits.clone();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ChainedFilter: [");

        for (int i = 0; i < chain.length; i++) {
            if (i > 0) {
                int logop = (this.logic != -1) ? this.logic
                        : this.logicArray[i - 1];
                switch (logop) {
                case OR:
                    sb.append(" OR ");
                    break;
                case AND:
                    sb.append(" AND ");
                    break;
                case XOR:
                    sb.append(" XOR ");
                    break;
                case ANDNOT:
                    sb.append(" XOR ");
                    break;
                default:
                    sb.append(" OR ");
                    break;
                }
            }
            sb.append(chain[i]);
            sb.append(' ');
        }

        sb.append(']');
        return sb.toString();
    }

    private class FilterData {

        private int listId;
        private String[] containerDefinitionNames;
        private Integer[] siteIds;
        private boolean siteSearch = false;

        public FilterData(int listId) {
            this.listId = listId;
        }

        public FilterData(Integer[] siteIds, String[] containerDefinitionNames,
                int listId) {
            this.listId = listId;
            this.containerDefinitionNames = containerDefinitionNames;
            this.siteIds = siteIds;
            this.siteSearch = true;
        }

        public int getListId() {
            return listId;
        }

        public void setListId(int listId) {
            this.listId = listId;
        }

        public String[] getContainerDefinitionNames() {
            return containerDefinitionNames;
        }

        public void setContainerDefinitionNames(
                String[] containerDefinitionNames) {
            this.containerDefinitionNames = containerDefinitionNames;
        }

        public Integer[] getSiteIds() {
            return siteIds;
        }

        public void setSiteIds(Integer[] siteIds) {
            this.siteIds = siteIds;
        }

        public boolean isSiteSearch() {
            return siteSearch;
        }

        public void setSiteSearch(boolean siteSearch) {
            this.siteSearch = siteSearch;
        }
    }
}
