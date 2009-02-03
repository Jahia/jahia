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

import java.io.Serializable;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.ArrayList;

import org.jahia.content.ContentContainerKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Sort container by time based publishing dates
 *
 * @see org.jahia.data.containers.ContainerSorterByTimebasedPublishingDateBean
 * @see org.jahia.data.containers.JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerSorterByTimebasedPublishingDateBean implements Serializable, ContainerSorterInterface {

    private static final long serialVersionUID = 475923698430662845L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerSorterByTimebasedPublishingDateBean.class);

    // sorting by publication date
    public static final String PUBLICATION_DATE = "PUBLICATION_DATE";

    // sorting by expiration date
    public static final String EXPIRATIONDATE = "EXPIRATION_DATE";

    protected int[] siteIds;

    protected int ctnListID = -1;

    protected String date;

    protected boolean updated = false;

    protected long lastSortingTime = -1;

    protected boolean isValid = false;

    protected boolean ASC_Ordering = true; // by default ASCENDANT ORDER.

    //** sorted ctnids **/
    protected List<Integer> result;

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected ContainerSorterByTimebasedPublishingDateBean(){}

    protected String contextID = "";

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param ctnListID, the container list id.
     * @param date on which to sort.
     * @param entryLoadRequest
     * @throws org.jahia.exceptions.JahiaException
     */
    public ContainerSorterByTimebasedPublishingDateBean(int ctnListID, String date,
                                                        EntryLoadRequest entryLoadRequest)
    throws JahiaException
    {
        if ( ContainerSorterByTimebasedPublishingDateBean.EXPIRATIONDATE.equals(date)
            || ContainerSorterByTimebasedPublishingDateBean.PUBLICATION_DATE.equals(date) )
        {
            this.isValid = true;
        }
        this.ctnListID = ctnListID;
        this.date = date;

        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param containerListName, the container list name.
     * @param params the param bean.
     * @param date on which to sort.
     * @param entryLoadRequest
     * @throws org.jahia.exceptions.JahiaException
     */
    public ContainerSorterByTimebasedPublishingDateBean(String containerListName, ProcessingContext params,
                                                        String date, EntryLoadRequest entryLoadRequest)
    throws JahiaException
    {
        ContainerSorterByTimebasedPublishingDateBean.logger.debug("Created container sort for clist: " + containerListName + " on the date " + date);

        if ( containerListName != null ){
            int clistID = ServicesRegistry.getInstance().getJahiaContainersService().
               getContainerListID( containerListName, params.getPage().getID() );
            if ( ContainerSorterByTimebasedPublishingDateBean.EXPIRATIONDATE.equals(date)
            || ContainerSorterByTimebasedPublishingDateBean.PUBLICATION_DATE.equals(date) )
            {
                this.isValid = true;
            }
            this.ctnListID = clistID;
        }
        this.date = date;

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
     * @param BitSet bits
     */
    public List<Integer> doSort(BitSet bits)
    {
        ContainerSorterByTimebasedPublishingDateBean.logger.debug("Started");

        this.result = new ArrayList<Integer>();

        try {
            if ( this.isValid ){
                // get all container ids
                ContainerSorterByTimebasedPublishingDateBean.logger.debug("Sorting : On date : " + this.date);

                JahiaObjectManager jahiaObjectManager =
                        (JahiaObjectManager) SpringContextSingleton.getInstance()
                                .getContext().getBean(JahiaObjectManager.class.getName());
                StringBuffer buffer = new StringBuffer("select jobj.comp_id.id from JahiaObject jobj where jobj.comp_id.type=? ");
                if ( this.siteIds != null && this.siteIds.length>0 ) {
                    buffer.append(" and jobj.site.id IN ( ");
                    for (int i=0; i<this.siteIds.length; i++){
                        buffer.append(String.valueOf(this.siteIds[i]));
                        if (i<this.siteIds.length-1){
                            buffer.append(",");
                        }
                    }
                    buffer.append(" ) ");
                }
                buffer.append(" order by ");
                if ( PUBLICATION_DATE.equals(date) ){
                    buffer.append(" jobj.validFromDate ");
                } else {
                    buffer.append(" jobj.validToDate ");
                }
                if ( !this.ASC_Ordering ){
                    buffer.append(" desc ");
                }
                Object params[] = null;
                params = new Object[]{ContentContainerKey.CONTAINER_TYPE};
                List<Integer> ids = jahiaObjectManager.<Integer>executeQuery(buffer.toString(), params);
                Iterator<Integer> it = ids.iterator();
                Integer id = null;

                List<Integer> listCtnIds = null;

                if ( this.ctnListID > 0 ){
                    try {
                        listCtnIds = ServicesRegistry.getInstance().getJahiaContainersService()
                        .getctnidsInList(this.ctnListID,this.entryLoadRequest);
                    } catch ( Exception t ){
                        logger.debug("Error retrieving containerlist's container id ",t);
                        return this.result;
                    }
                }
                while ( it.hasNext() ){
                    id = (Integer)it.next();
                    if ( (bits == null || bits.get(id.intValue())) && (listCtnIds == null || listCtnIds.contains(id)) ){
                        this.result.add(id);
                    }
                }
            }
        } catch ( Exception t ){
            ContainerSorterByTimebasedPublishingDateBean.logger.error("Exception occured :" + t.getMessage(), t);
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
        return new String[]{this.date};
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
        if (this.siteIds == null || this.siteIds.length==0){
            return -1;
        }
        return this.siteIds[0];
    }

    /**
     * Limits to a site
     * @deprecated
     * @param siteId
     */
    public void setSiteId(int siteId) {
        this.siteIds = null;
        if (siteId >0){
            this.siteIds = new int[]{siteId};
        }
    }

    public String getContextID(){
        return this.contextID;
    }

    public void setContextID(String contextID){
        this.contextID = contextID;
    }
    
    /**
     * A dbMax Limit may be defined and be used by the sorter if applicable.
     * IGNORED
     * @param dbMaxResult
     */
    public void setDBMaxResult(int dbMaxResult) {

    }

    /**
     * Not used. Returns -1
     * @return
     */
    public int getDBMaxResult(){
       return -1;
    }

}
