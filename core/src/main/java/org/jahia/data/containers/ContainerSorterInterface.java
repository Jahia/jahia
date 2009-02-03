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

    public abstract List doSort(BitSet bits);

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
