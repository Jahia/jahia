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

import org.jahia.bin.Jahia;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.OrderedBitSet;

import java.io.Serializable;
import java.util.*;

/**
 * A Sorter that use a ContainerSearcher internally.
 *
 * @see ContainerSorterByTimebasedPublishingDateBean
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerLuceneSorterBean implements Serializable, ContainerSorterInterface {

    private static final long serialVersionUID = -1834320264085126197L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerLuceneSorterBean.class);

    protected int ctnListID = -1;

    protected String[] fieldNames;

    protected boolean updated = false;

    protected long lastSortingTime = -1;

    protected boolean isValid = false;

    protected boolean ASC_Ordering = true; // by default ASCENDANT ORDER.

    //** sorted ctnids **/
    protected List<Integer> result;

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected String contextID = "";

    protected ContainerLuceneSorterBean(){}

    protected ContainerSearcher containerSearcher;

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param ctnListID, the container list id.
     * @param containerSearcher
     * @param fieldNames on which to sort.
     * @param entryLoadRequest
     * @throws org.jahia.exceptions.JahiaException
     */
    public ContainerLuceneSorterBean(int ctnListID,
                                     ContainerSearcher containerSearcher,
                                     String[] fieldNames,
                                     EntryLoadRequest entryLoadRequest)
    throws JahiaException
    {
        this.ctnListID = ctnListID;
        this.containerSearcher = containerSearcher;
        this.fieldNames = fieldNames;
        if ( this.containerSearcher != null && this.fieldNames != null && this.fieldNames.length>0 )
        {
            this.isValid = true;
        }
        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
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
        this.result = new ArrayList<Integer>();

        try {
            if ( this.isValid ){
                JahiaSearchResult searchResult = this.containerSearcher.search(this.containerSearcher.getQuery(),
                        Jahia.getThreadParamBean());
                if ( searchResult != null ){
                    BitSet bitsResult = searchResult.bits();
                    if ( bitsResult instanceof OrderedBitSet ){
                        OrderedBitSet orderedBitSet = (OrderedBitSet)bitsResult;
                        if (orderedBitSet.isOrdered() && orderedBitSet.getOrderedBits()!=null){
                            if (bits == null){
                                this.result.addAll(orderedBitSet.getOrderedBits());
                            } else {
                                for (Integer id : orderedBitSet.getOrderedBits()){
                                    if (bits.get(id.intValue())){
                                        this.result.add(id);
                                    }
                                }
                            }
                        }
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
        return this.fieldNames;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the last sorting time.
     *
     * @return long , the last sorting time
     */
    public long getLastSortingTime()
    {
        if ( this.containerSearcher == null ){
            return -1;
        } else {
            return this.lastSortingTime + this.containerSearcher.getCacheTime();
        }
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

    public ContainerSearcher getContainerSearcher() {
        return containerSearcher;
    }

    public void setContainerSearcher(ContainerSearcher containerSearcher) {
        this.containerSearcher = containerSearcher;
    }
    
}