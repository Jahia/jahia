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
// 27.05.2002 NK Creation
package org.jahia.data.containers;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.version.EntryLoadRequest;
import java.io.Serializable;
import org.jahia.services.fields.ContentField;
import org.springframework.context.ApplicationContext;

/**
 * A filter used to returns all containers of a given definition. Same container definition name
 * 
 * @see FilterClause
 * @see ContainerFilters
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */
public class ContainerFilterByContainerDefinition implements Serializable,
        ContainerFilterInterface {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerFilterByContainerDefinition.class);

    public static final String VERSION_ID = "comp_id.versionId";
    public static final String WORKFLOW_STATE = "comp_id.workflowState";
    public static final String LANGUAGE_CODE = "comp_id.languageCode";

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;
    private String[] containerDefinitionNames;

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param containerDefinitionName
     * @param entryLoadRequest
     */
    public ContainerFilterByContainerDefinition(String containerDefinitionName,
            EntryLoadRequest entryLoadRequest) {

        if (this.containerDefinitionNames != null
                && containerDefinitionName.trim().length() > 0) {
            this.containerDefinitionNames = new String[] { containerDefinitionName };
        }
        if (entryLoadRequest != null) {
            this.entryLoadRequest = entryLoadRequest;
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Return the container definition name
     * 
     * @deprecated use getContainerDefinitionNames()
     * @return
     */
    public String getContainerDefinitionName() {
        if (this.containerDefinitionNames == null
                || this.containerDefinitionNames.length == 0) {
            return null;
        }
        return this.containerDefinitionNames[0];
    }

    public String[] getContainerDefinitionNames() {
        return containerDefinitionNames;
    }

    public void setContainerDefinitionNames(String[] containerDefinitionNames) {
        this.containerDefinitionNames = containerDefinitionNames;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering. The expected result is a bit set of matching container ids.
     * 
     * @param ctnListID
     *            , the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    public BitSet doFilter(int ctnListID) throws JahiaException {
        BitSet result = null;

        result = doFiltering(ctnListID);

        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * The expected result is a bit set of matching container ids.
     * 
     * @param ctnListID
     *            , the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    private BitSet doFiltering(int ctnListID) throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        String fieldFilterQuery = getSelect(ctnListID, true, 0, parameters);
        if (StringUtils.isEmpty(fieldFilterQuery)) {
            return null;
        }

        BitSet bits = new BitSet();

        List<Integer> deletedCtns = ContainerFilterBean
                .getDeletedContainers(ctnListID);

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());
        List<Object[]> datas = fieldMgr.executeQuery(fieldFilterQuery,
                parameters);

        for (Object[] result : datas) {
            int ctnID = ((Integer) result[0]).intValue();
            int workflowState = ((Integer) result[1]).intValue();
            if (this.entryLoadRequest.isCurrent()
                    || !deletedCtns.contains(new Integer(ctnID))) {
                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
                if (this.entryLoadRequest.isCurrent()
                        && workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    bits.set(ctnID);
                } else if (this.entryLoadRequest.isStaging()
                        && workflowState >= EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    bits.set(ctnID);
                }
            }
        }

        return bits;
    }

    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for all container list of the site. Do not executeQuery this query !! It's only
     * used for comparison between query. It's not a valable sql query.
     * 
     * @param ctnListID
     *            , the container list id
     * @return String , the sql statement. Null on error
     */
    public String getSelect(int ctnListID, int filterId, Map<String, Object> parameters) {
        return getSelect(ctnListID, false, filterId, parameters);
    }

    /**
     * Return the select statement, build with the clauses for all container list of the site.
     * 
     * @param ctnListID
     *            , the container list id
     * @param ignoreLang
     *            boolean, add language in query or not ( should not when performing sql query, because containre table do not have lang
     *            column
     * @return String , the sql statement. Null on error
     */
    protected String getSelect(int ctnListID, boolean ignoreLang,
            int filterId, Map<String, Object> parameters) {
        StringBuffer buff = new StringBuffer(
                "select distinct comp_id.id, comp_id.workflowState from JahiaContainer WHERE listid=(:listID_" + filterId + ") and ");
        buff.append(buildMultilangAndWorlflowQuery(this.entryLoadRequest,
                ignoreLang));
        buff.append(" order by comp_id.id, comp_id.workflowState");
        parameters.put("listID_" + filterId, ctnListID);
        return buff.toString();
    }

    // --------------------------------------------------------------------------
    /**
     * Set reference to a containerFilters
     * 
     * @return
     * @throws JahiaException
     */
    public void setContainerFilters(ContainerFilters containerFilters) {
        // do nothing
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @return
     */
    public static String buildMultilangAndWorlflowQuery(
            EntryLoadRequest entryLoadRequest) {

        return buildMultilangAndWorlflowQuery(entryLoadRequest, false);
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @return
     */
    public static String buildMultilangAndWorlflowQuery(
            EntryLoadRequest entryLoadRequest, boolean ignoreLang) {

        StringBuffer strBuf = new StringBuffer(" ");
        if (entryLoadRequest.isCurrent()) {
            strBuf.append(WORKFLOW_STATE);
            strBuf.append("=");
            strBuf.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else if (entryLoadRequest.isStaging()) {
            strBuf.append(WORKFLOW_STATE);
            strBuf.append(">");
            strBuf.append(EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
            strBuf.append(" and ");
            strBuf.append(VERSION_ID);
            strBuf.append(" <> -1 ");
        } else {
            strBuf.append(VERSION_ID);
            strBuf.append("=");
            strBuf.append(entryLoadRequest.getVersionID());
        }
        if (!ignoreLang) {
            String languageCode = entryLoadRequest.getFirstLocale(true)
                    .toString();
            strBuf.append(" and (");
            strBuf.append(LANGUAGE_CODE);
            strBuf.append(" in ('");
            strBuf.append(languageCode);
            strBuf.append("', '");
            strBuf.append(ContentField.SHARED_LANGUAGE);
            strBuf.append("') ");
        }

        return strBuf.toString();

    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * If siteId = -1 , returns results from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteId
     * @param containerDefinitionName
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(int siteId, String containerDefinitionName,
            int listId) throws JahiaException {

        return doFilteringBySite(siteId, containerDefinitionName, listId);
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * If siteId = -1 , returns results from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteIds
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds,
            String[] containerDefinitionNames, int listId)
            throws JahiaException {
        return doFilteringBySite(siteIds, containerDefinitionNames, listId);
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * The expected result is a bit set of matching container ids for a given siteId. if siteId = -1 , return result from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteId
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    private BitSet doFilteringBySite(int siteId,
            String containerDefinitionName, int listId) throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return doFilteringBySite(siteIds, containerDefinitionNames, listId);
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * The expected result is a bit set of matching container ids for a given siteId. if siteId = -1 , return result from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteIds
     * @param containerDefinitionNames
     * @param listId
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    private BitSet doFilteringBySite(Integer[] siteIds,
            String[] containerDefinitionNames, int listId)
            throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        String fieldFilterQuery = getSelectBySiteID(siteIds,
                containerDefinitionNames, true, 0, parameters);
        if (StringUtils.isEmpty(fieldFilterQuery)) {
            return null;
        }

        BitSet bits = new BitSet();

        List<Integer> deletedCtns = ContainerFilterBean
                .getDeletedContainersBySite(siteIds, containerDefinitionNames);

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());
        List<Object[]> queryResults = fieldMgr.executeQuery(fieldFilterQuery,
                parameters);
        for (Object[] result : queryResults) {
            int ctnID = ((Integer) result[0]).intValue();
            int workflowState = ((Integer) result[1]).intValue();
            if (this.entryLoadRequest.isCurrent()
                    || !deletedCtns.contains(new Integer(ctnID))) {
                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
                if (this.entryLoadRequest.isCurrent()
                        && workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    bits.set(ctnID);
                } else if (this.entryLoadRequest.isStaging()
                        && workflowState >= EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    bits.set(ctnID);
                }
            }
        }

        return bits;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site. If siteId = -1 -> build query for all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * Do not executeQuery this query !! It's only used for comparison between query. It's not a valable sql query.
     * 
     * @param siteId
     * @param containerDefinitionName
     * @return
     */
    public String getSelectBySiteID(int siteId, String containerDefinitionName,
            int filterId, Map<String, Object> parameters) {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return getSelectBySiteID(siteIds, containerDefinitionNames, false,
                filterId, parameters);
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     * 
     * Do not executeQuery this query !! It's only used for comparison between query. It's not a valable sql query.
     * 
     * @param siteIds
     * @param containerDefinitionNames
     * @return
     */
    public String getSelectBySiteID(Integer[] siteIds,
            String[] containerDefinitionNames, int filterId, Map<String, Object> parameters) {
        return getSelectBySiteID(siteIds, containerDefinitionNames, false,
                filterId, parameters);
    }

    /**
     * Return the select statement, build with the clauses for a given site.
     * 
     * @param siteIds
     * @param containerDefinitionNames
     * @param ignoreLang
     *            boolean, add language in query or not ( should not when performing sql query, because containre table do not have lang
     *            column
     * @return
     */
    public String getSelectBySiteID(Integer[] siteIds,
            String[] containerDefinitionNames, boolean ignoreLang,
            int filterId, Map<String, Object> parameters) {
        StringBuffer buff = new StringBuffer(
                "select distinct comp_id.id, comp_id.workflowState from JahiaContainer a, JahiaCtnDef b where ");
        buff.append(buildMultilangAndWorlflowQuery(this.entryLoadRequest,
                ignoreLang));

        if (siteIds != null && siteIds.length > 0) {
            buff.append(" and siteId in (:siteIds_" + filterId + ")");
            parameters.put("siteIds_" + filterId, siteIds);
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" and a.ctndef.id = b.id AND b.name in (:defNames_" + filterId + ") ");
            parameters.put("defNames_" + filterId, containerDefinitionNames);
        }

        buff.append(" order by comp_id.id,");
        buff.append(WORKFLOW_STATE);

        return buff.toString();
    }
}
