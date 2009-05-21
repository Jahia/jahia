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
 package org.jahia.data.containers;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.audit.LogsBasedQueryConstant;
import org.jahia.services.audit.LogsQuery;
import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;
import org.jahia.services.version.EntryLoadRequest;

import java.io.Serializable;
import java.util.*;

/**
 * Sort container based on logs query
 *
 * @see ContainerSorterByLogsQuery
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerSorterByLogsQuery implements Serializable, ContainerSorterInterface {

    private static final long serialVersionUID = 271417564362876361L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerSorterByLogsQuery.class);

    protected int[] siteIds;

    protected int ctnListID = -1;

    protected boolean updated = false;

    protected long lastSortingTime = -1;

    protected boolean isValid = false;

    protected boolean ASC_Ordering = true; // by default ASCENDANT ORDER.

    //** sorted ctnids **/
    protected List<Integer> result;

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected ContainerSorterByLogsQuery(){}

    protected String contextID = "";

    protected LogsQuery logsQuery;

    protected int dbMaxLimit = -1;

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param ctnListID, the container list id.
     * @param ctnListID
     * @param logsQuery
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterByLogsQuery(int ctnListID, LogsQuery logsQuery,
                                      EntryLoadRequest entryLoadRequest)
    throws JahiaException
    {
        this.logsQuery = logsQuery;
        if ( this.logsQuery != null ){
            this.isValid = true;
        }
        this.ctnListID = ctnListID;

        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param containerListName, the container list name.
     * @param params
     * @param logsQuery
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterByLogsQuery(  String containerListName, ProcessingContext params,
                                        LogsQuery logsQuery,
                                        EntryLoadRequest entryLoadRequest)
    throws JahiaException
    {
        logger.debug("Created container sort for clist: " + containerListName);

        this.logsQuery = logsQuery;
        if ( containerListName != null ){
            int clistID = ServicesRegistry.getInstance().getJahiaContainersService().
               getContainerListID( containerListName, params.getPage().getID() );
            if ( this.logsQuery != null ){
                this.isValid = true;
            }
            this.ctnListID = clistID;
        }

        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
        } else if ( params.getEntryLoadRequest() != null ){
            this.entryLoadRequest = params.getEntryLoadRequest();
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Do the sort. Optionally, you can provide a BitSet where each bit set correspond the a container id you want in the result.
     * If you want all containers in the result, give a null BitSet.
     *
     * @param bits
     */
    public List<Integer> doSort(BitSet bits)
    {
        logger.debug("Started");

        this.result = new ArrayList<Integer>();

        try {
            if ( this.isValid ){
                LogsResultList<LogEntryItem> queryResult = logsQuery.executeQuery(Jahia.getThreadParamBean());
                if ( queryResult == null || queryResult.isEmpty() ){
                    return result;
                }
                Iterator<LogEntryItem> it = queryResult.iterator();
                LogEntryItem log = null;
                while ( it.hasNext() ){
                    log = it.next();
                    if ( log.getObjectType() != LogsBasedQueryConstant.CONTAINER_TYPE ){
                        continue;
                    }
                    if ( (bits == null || bits.get(log.getObjectID())) ){
                        this.result.add(new Integer(log.getObjectID()));
                    }
                }
            }
        } catch ( Exception t ){
            logger.error("Exception occured :" + t.getMessage(), t);
        }

        // Set search time
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance(tz);
        Date nowDate = cal.getTime();
        this.lastSortingTime = nowDate.getTime();

        return this.result;
    }


    //--------------------------------------------------------------------------
    /**
     * Return the List of sorted ctnids.
     *
     */
    public List<Integer> result()
    {
        return this.result;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the order , true - > ASC, false -> DESC.
     *
     */
    public boolean isAscOrdering()
    {
        return this.ASC_Ordering;
    }

    //--------------------------------------------------------------------------
    /**
     * Return true, if the values are converted to number before sorting.
     *
     */
    public boolean isNumberOrdering()
    {
        return true;
    }

    //--------------------------------------------------------------------------
    /**
     * Force or not value to be converted to number before doing the sort.
     *
     */
    public boolean setNumberOrdering(boolean val)
    {
        return true;
    }

    //--------------------------------------------------------------------------
    /**
     * Set DESC ordering.
     *
     */
    public void setDescOrdering()
    {
        this.ASC_Ordering = false;
    }

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public boolean setAscOrdering()
    {
        return this.ASC_Ordering = true;
    }

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public boolean setAscOrdering(boolean val)
    {
        return this.ASC_Ordering = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the container list id.
     *
     * @return int ctnListID, the container list id.
     */
    public int getCtnListID()
    {
        return this.ctnListID;
    }

    public void setCtnListID(int listId){
        this.ctnListID = listId;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the sorting date only.
     *
     */
    public String[] getSortingFieldNames()
    {
        if ( this.logsQuery == null ){
            return new String[]{};
        } else {
            return new String[]{this.logsQuery.toString()};
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return the last sorting time.
     *
     * @return long , the last sorting time
     */
    public long getLastSortingTime()
    {
        return this.lastSortingTime;
    }

    //--------------------------------------------------------------------------
    /**
     * Return true if the sorter initialited properly
     *
     * @return boolean, the valid state value.
     */
    public boolean isValid()
    {
        return this.isValid;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the update status. Each time the doSort method is called, this update status is set to true.
     *
     * @return boolean, the internal updated status value.
     */
    public boolean getUpdateStatus()
    {
        return this.updated;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the update status to true.
     *
     */
    public void setUpdateStatus()
    {
        this.updated = true;
    }

    //--------------------------------------------------------------------------
    public EntryLoadRequest getEntryLoadRequest(){
        return this.entryLoadRequest;
    }

    //--------------------------------------------------------------------------
    /**
     * You can reset the internal update status by setting it to false
     *
     */
    public void resetUpdateStatus()
    {
        this.updated = false;
    }

    /**
     * the site on which to limit the sort
     * @deprecated
     * @return
     */
    public int getSiteId() {
        if (this.siteIds==null || this.siteIds.length==0){
            return -1;
        }
        return this.siteIds[0];
    }

    /**
     * Limits to a site
     * @deprecated use setSiteIds()
     * @param siteId
     */
    public void setSiteId(int siteId) {
        this.siteIds = null;
        if (siteId>0){
            this.siteIds = new int[]{siteId};
        }
    }

    public int[] getSiteIds() {
        return this.siteIds;
    }

    public void setSiteIds(int[] siteIds) {
        this.siteIds = siteIds;
    }

    public String getContextID(){
        return this.contextID;
    }

    public void setContextID(String contextID){
        this.contextID = contextID;
    }

    /**
     * A dbMax Result may be defined and be used by the sorter if applicable.
     *
     * @param dbMaxResult
     */
    public void setDBMaxResult(int dbMaxResult){
        this.dbMaxLimit = dbMaxResult;
    }

    public int getDBMaxResult(){
        return this.dbMaxLimit;        
    }

}