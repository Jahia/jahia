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
//
//
//
//
//


package org.jahia.data.containers;

import java.util.BitSet;
import java.util.List;

import org.jahia.services.version.EntryLoadRequest;


/**
 * Jahia Standard Containers Sort Handler.
 *
 * @see ContainerSorterBean
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @author MC
 */

public interface ContainerSorterInterface {

    public abstract int getCtnListID();

    public abstract void setCtnListID(int id);

    /**
     * This contextID is optional and is used for session cached filters.
     * This is usefull for caching different filters applied to a same container list
     *
     * @return
     */
    public abstract String getContextID();

    public abstract void setContextID(String contextID);

    //--------------------------------------------------------------------------
    /**
     * Do the sort. Optionally, you can provide a BitSet where each bit set correspond the a container id you want in the result.
     * If you want all containers in the result, give a null BitSet.
     *
     * @param BitSet bits
     */

    public abstract List<Integer> doSort(BitSet bits);

    //--------------------------------------------------------------------------
    /**
     * Return the List of sorted ctnids.
     *
     */
    public abstract List<Integer> result();

    //--------------------------------------------------------------------------
    /**
     * Return the order , true - > ASC, false -> DESC.
     *
     */
    public abstract boolean isAscOrdering();

    //--------------------------------------------------------------------------
    /**
     * Return true, if the values are converted to number before sorting.
     *
     */
    public abstract boolean isNumberOrdering();

    //--------------------------------------------------------------------------
    /**
     * Force or not value to be converted to number before doing the sort.
     *
     */
    public abstract boolean setNumberOrdering(boolean val);

    //--------------------------------------------------------------------------
    /**
     * Set DESC ordering.
     *
     */
    public abstract void setDescOrdering();

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public abstract boolean setAscOrdering();

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public abstract boolean setAscOrdering(boolean val);

    //--------------------------------------------------------------------------
    /**
     * Return the sorting fields.
     *
     * Only first element is defined for @see ContainerSorterBean (the the extending @see ContainerMetadataSorterBean)
     * but more can be defined for @see ContainerSorterByContainerDefinition.
     *
     * @return String[] , the name of fields used for sorting.
     */
    public abstract String[] getSortingFieldNames();

    //--------------------------------------------------------------------------
    /**
     * Return the last sorting time.
     *
     * @return long , the last sorting time
     */
    public abstract long getLastSortingTime();

    //--------------------------------------------------------------------------
    /**
     * Return true if the sorter initialited properly
     *
     * @return boolean, the valid state value.
     */
    public abstract boolean isValid();

    //--------------------------------------------------------------------------
    /**
     * Return the update status. Each time the doSort method is called, this update status is set to true.
     *
     * @return boolean, the internal updated status value.
     */
    public abstract boolean getUpdateStatus();

    //--------------------------------------------------------------------------
    /**
     * Set the update status to true.
     *
     */
    public abstract void setUpdateStatus();

    //--------------------------------------------------------------------------
    public abstract EntryLoadRequest getEntryLoadRequest();

    //--------------------------------------------------------------------------
    /**
     * You can reset the internal update status by setting it to false
     *
     */
    public abstract void resetUpdateStatus();

    /**
     * A dbMax Result may be defined and be used by the sorter if applicable.
     * 
     * @param dbMaxResult
     */
    public void setDBMaxResult(int dbMaxResult);

    public int getDBMaxResult();
}
