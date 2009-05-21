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
//
//  JahiaContainerListPagination
//  NK      05.05.2002
//
//

package org.jahia.data.containers;


import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import java.util.*;


/**
 * <p>Handle containers list pagination</p>
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @version 1.0
 */

public class JahiaContainerListPagination {

    private static final transient Logger logger = Logger
            .getLogger(JahiaContainerListPagination.class);
    
    /** The total number of containers. **/
    private int size = 0;
    /** The number of items ( containers ) to display per page. **/
    private int windowSize = -1;
    /** The current windowOffset. **/
    private int windowOffset = -1;
    /** The current page index ( the currently displayed page ). **/
    private int currentPageIndex = 0;
    /** The List of pages indexes **/
    private List<Integer> pages = new ArrayList<Integer>();
    /** The index of the first displayed item. **/
    private int firstItemIndex = 0;
    /** The index of the last displayed item. **/
    private int lastItemIndex = 0;
    /** Is the status of this instance valid or not **/
    private boolean isValid = false;

    /**
     * The list of lazy container Ids. This is required to help positioning the pagination to the last (Edited Container Item)
     * Not usefull for display.
     */
    private List<Integer> lazyCtnIds;

    /**
     * The id of the last edited container. Used to reposition the pagination to the last edited container.
     */
    private int lastItemId;

    private boolean lastEditedItemIsInTheRange;

    //-------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param size, the total number of items.
     * @param windowSize, the number of items ( containers ) to display per page.
     * @param windowOffset, the current windowOffset.
     */
    public JahiaContainerListPagination(  int size,
                                          int windowSize ,
                                          int windowOffset )
    {
        this.size = size;
        this.windowSize = windowSize;
        this.windowOffset = windowOffset;
        this.paginate();
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor
     *
     * windowSize and windowOffset are computed from request parameters :
     * <pre>
     *		ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER + containerListName
     * </pre>
     *
     * Or if not present, loaded from container list scrolling properties :
     * <pre>
     *		"windowSize" and "windowOffset"
     * </pre>
     *
     * size is retrieved from theContainerList.getFullSize()
     *
     *
     * @param theContainerList, the container list.
     * @param jParams, the param bean.
     * @param newWindowSize, the new window size option, set to -1 to deactivate.
     */
    public JahiaContainerListPagination(  JahiaContainerList theContainerList,
                                          ProcessingContext jParams,
                                          int newWindowSize )
    throws JahiaException {
        init(theContainerList,jParams,newWindowSize,null,0, null);
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor
     *
     * windowSize and windowOffset are computed from request parameters :
     * <pre>
     *		ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER + containerListName
     * </pre>
     *
     * Or if not present, loaded from container list scrolling properties :
     * <pre>
     *		"windowSize" and "windowOffset"
     * </pre>
     *
     * size is retrieved from theContainerList.getFullSize()
     *
     *
     * @param theContainerList the container list.
     * @param jParams the param bean.
     * @param newWindowSize, the new window size option, set to -1 to deactivate.
     * @param lazyCtnIds The list of lazy container Ids. This is required to help positioning the pagination to the last (Edited Container Item)
     * Not usefull for display.
     * @param lastItemId The id of the last edited container. Used to reposition the pagination to the last edited container.
     * @param listViewId This is the ID used in the containerList tag to allow muplitple views of the same list in a page
     * @throws JahiaException
     */
    public JahiaContainerListPagination(  JahiaContainerList theContainerList,
                                          ProcessingContext jParams,
                                          int newWindowSize, List<Integer> lazyCtnIds, int lastItemId, String listViewId)
    throws JahiaException {
        init(theContainerList,jParams,newWindowSize,lazyCtnIds,lastItemId, listViewId);
    }

    private void init(JahiaContainerList theContainerList,
                      ProcessingContext jParams,
                      int newWindowSize, List<Integer> lazyCtnIds, int lastItemId, String listViewId)
    throws JahiaException {
        if ( theContainerList == null )
        {
            return;
        }
        this.lazyCtnIds = lazyCtnIds;
        this.lastItemId = lastItemId;

        // we now check in we are in a scrollable container list, and retrieve
        // only the values of the containers that must be displayed.
        int windowSize = -1;
        int windowOffset = -1;
        boolean windowOffsetTakenFromParameter = false;

        int defWindowSize = -1;
        int defWindowOffset = -1;

        if (theContainerList.getID() > 1) {
            JahiaContainerDefinition ctnDef = theContainerList.getDefinition();
            Properties ctnDefProps = ctnDef.getProperties();
            if (!ctnDefProps.isEmpty()) {
                try {
                    if (ctnDefProps.getProperty("windowSize") != null) {
                        defWindowSize = Integer.parseInt(ctnDefProps.getProperty("windowSize"));
                        defWindowOffset = 0;
                    }
                    if (ctnDefProps.getProperty("windowOffset") != null) {
                        defWindowOffset = Integer.parseInt(ctnDefProps.getProperty("windowOffset"));
                    }
                } catch (NumberFormatException nfe) {
                }
            }
        }

        if ( (defWindowSize >= 1) &&
             (defWindowOffset >= 0) ) {
            windowSize = defWindowSize;
            windowOffset = defWindowOffset;
        }

        if (jParams != null) {
            String containerListName = "truc";
            if (theContainerList.getID() != -1) {
                containerListName = theContainerList.getDefinition().getName();
            }

            // Check for customized window size
            String shortListName = getShortListName(containerListName);
            String custWindowSize = jParams.getParameter(getWindowSizeKey(shortListName, listViewId));
            try {
                if (custWindowSize != null) {
                    int val = Integer.parseInt(custWindowSize);
                    if (val > 0) {
                        windowSize = val;
                    }
                }
            } catch ( Exception t ){
            }

            // Check for customized window offset
            String custWindowOffset = jParams.getParameter(getWindowOffsetKey(shortListName, listViewId));
            try {
                if (custWindowOffset != null) {
                    int val = Integer.parseInt(custWindowOffset);
                    if (val >= 0) {
                        windowOffset = val;
                        windowOffsetTakenFromParameter = true;
                    }
                }
            } catch ( Exception t ){
            }            
            
            String scrollStr = jParams
                    .getParameter(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER
                            + (listViewId != null ? listViewId + "_"
                                    : "") + containerListName);
            if (scrollStr != null) {
                // we have an window scrolling code for this container list.
                int separatorPos = scrollStr.indexOf("_");
                if (separatorPos != -1 && (separatorPos + 1 < scrollStr.length())) {
                    // seperator is present, looks like a valid parameter. We
                    // must still test if we have two integer values.
                    String windowSizeStr = scrollStr.substring(0, separatorPos);
                    String windowOffsetStr = scrollStr.substring(separatorPos + 1, scrollStr.length());
                    windowOffsetTakenFromParameter = true;
                    try {
                        windowSize = Integer.parseInt(windowSizeStr);
                        windowOffset = Integer.parseInt(windowOffsetStr);
                        if ((windowSize < -1) || (windowOffset < -1) ) {
                            // little sanity check to avoid simple hacks.
                            windowSize = -1;
                            windowOffset = -1;
                        }
                    } catch (NumberFormatException nfe) {
                        windowSize = -1;
                        windowOffset = -1;
                    }
                }
            }
        }

        // set the new window size if needed
        if ( newWindowSize > 0 ){
            windowSize = newWindowSize;
        }

        int magicSize = Math.max(theContainerList.size(),lazyCtnIds.size());
        if ( windowOffset > (magicSize -1) )
        {
            windowOffset = 0; // Perhaps the number of container has changed ( i.e : some ctn has been deleted ),
                              // so display the first page
        }

        if ((windowOffset < 0) || windowSize > 1
                && (windowOffset % windowSize) != 0) {
            windowOffset = 0;
        }


        this.size = magicSize;
        if ( this.size> theContainerList.getMaxSize() ){
            this.size = theContainerList.getMaxSize();
        }
        this.windowSize = windowSize;
        this.windowOffset = windowOffset;

        //JahiaConsole.println(CLASS_NAME+".Constructor","size : " + this.size +
        //                                               " windowSize : " + windowSize +
        //                                               " windowOffset : " + windowOffset);
        paginate();

        if (!windowOffsetTakenFromParameter && this.lastItemId>0 && this.lazyCtnIds != null
                && !this.lazyCtnIds.isEmpty()){
            int count = 0;
            for (Integer id : this.lazyCtnIds){
                count++;
                lastEditedItemIsInTheRange = (id.intValue()==this.lastItemId);
                if (lastEditedItemIsInTheRange){
                    break;
                }
            }
            if (lastEditedItemIsInTheRange){
                if (count<=this.windowSize){
                    this.windowOffset = 0;
                } else if (this.windowSize == 1) {
                    this.windowOffset = count - 1;                                        
                } else {
                    this.windowOffset = (count / this.windowSize) * this.windowSize;
                }
                this.paginate();
            }
        }        
    }
    private String getShortListName(String containerListName) {
        String shortListName = containerListName;
        int index = containerListName.lastIndexOf("_");
        if (index > -1) {
            shortListName = containerListName.substring(index + 1);
        }
        return shortListName;
    }
    private String getWindowSizeKey(String containerListName,
            String listViewId) {
        return (listViewId != null && listViewId.length() > 0 ? listViewId
                + "_"
                : "")
                + containerListName + "_windowsize";
    }
    private String getWindowOffsetKey(String containerListName,
            String listViewId) {
        return (listViewId != null && listViewId.length() > 0 ? listViewId
                + "_"
                : "")
                + containerListName + "_windowoffset";
    }
    //-------------------------------------------------------------------------
    /**
     * Return the total number of items ( containers ).
     *
     * @return int the total number of itms.
     */
    public int getSize()
    {
        return this.size;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the windowSize , the number of items per page.
     *
     * @return int windowSize, the number of items per page
     */
    public int getWindowSize()
    {
        return this.windowSize;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the windowOffset , the current window Offset.
     *
     * @return int windowOffset, the current window offset.
     */
    public int getWindowOffset()
    {
        return this.windowOffset;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the valid status.
     *
     * @return boolean true if valid, false on error.
     */
    public boolean isValid()
    {
        return this.isValid;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the current page Index, ( the index of the displayed page ).
     * Starting from 1
     *
     * @return int current pageIndex.
     */
    public int getCurrentPageIndex()
    {
        return this.currentPageIndex;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the index of the first item listed in the current displayed page.
     * Starting from 0
     *
     * @return int index of the first item listed in the current displayed page.
     */
    public int getFirstItemIndex()
    {
        return this.firstItemIndex;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the index of the last item listed in the current displayed page.
     * Starting from 0
     *
     * @return int index of the last item listed in the current displayed page.
     */
    public int getLastItemIndex()
    {
        return this.lastItemIndex;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the List of paginated pages as index numbers starting from 1 to n.
     *
     *	1,2,3,4..
     *
     * @return List the List of paginated pages as index numbers ( Integer ).
     */
    public List<Integer> getPages() {
        return this.pages;
    }

    public Integer getPage(int index) {
        return this.pages.get(index);
    }

    //-------------------------------------------------------------------------
    /**
     * Return the number of pages.
     *
     * @return int the number of pages.
     */
    public int getNbPages()
    {
        if ( this.pages == null ){
            return 0;
        }
        return this.pages.size();
    }

    //-------------------------------------------------------------------------
    /**
     * Return the windowOffset for a given page index ( page index start from 1 to n).
     * The computed value is equals to : (pageIndex-1) * windowSize
     *
     * @param pageIndex the page index must be > 0.
     * @return int the windowOffset for a given page index, -1 on error.
     */
    public int getWindowOffset(int pageIndex)
    {
        if ( !isValid() || (pageIndex<=0) ){
            return -1;
        }
        try {
            int val = (pageIndex-1) * this.windowSize ;
            if ( val <= this.size ){
                return val;
            }
        } catch ( Exception e ) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the scrolling value ( i.e : '5_10' ) for a given page index ( page index start from 1 to n).
     *
     * <pre>
     *		Scrolling value format is : windowSize + "_" + windowOffset
     * </pre>
     *
     * @param pageIndex the page index must be > 0.
     * @return String the scrolling value for the given page index, null on error.
     */
    public String getScrollingValue(int pageIndex)
    {
        int windowOffset = getWindowOffset(pageIndex);
        if ( pageIndex <0 ){
            return null;
        }
        return ( String.valueOf(getWindowSize()) + "_" + String.valueOf(windowOffset) );
    }

    /**
     * The list of lazy container Ids. This is required to help positioning the pagination to the last (Edited Container Item)
     * Not usefull for display.
     * @return
     */
    public List<Integer> getLazyCtnIds() {
        return lazyCtnIds;
    }

    /**
     * Set the list of lazy container Ids. This is required to help positioning the pagination to the last (Edited Container Item)
     *
     * @param lazyCtnIds
     */
    public void setLazyCtnIds(List<Integer> lazyCtnIds) {
        this.lazyCtnIds = lazyCtnIds;
    }

    public int getLastItemId() {
        return lastItemId;
    }

    public void setLastItemId(int lastItemId) {
        this.lastItemId = lastItemId;
    }

    /**
     * The last Edited Item is in the list of containers
     * @return
     */
    public boolean isLastEditedItemIsInTheRange() {
        return lastEditedItemIsInTheRange;
    }

    public void setLastEditedItemIsInTheRange(boolean lastEditedItemIsInTheRange) {
        this.lastEditedItemIsInTheRange = lastEditedItemIsInTheRange;
    }

    //-------------------------------------------------------------------------
    /**
     * Perform pagination tasks.
     *
     */
    private void paginate()
    {
        //JahiaConsole.println(CLASS_NAME+".paginate","Started");
        this.pages.clear();
        
        try {

            if ( ( this.windowSize <= 0 ) || ( this.windowOffset < 0 ) || ( ( this.windowOffset % this.windowSize ) != 0) )
            {
                return; // not valid value.
            }

            // Compute current page index.
            this.currentPageIndex = ( this.windowOffset / this.windowSize ) + 1;

            // Compute the first and last item indexes
            if ( this.currentPageIndex == 1 )
            {
                this.firstItemIndex = 0;
            } else {
                this.firstItemIndex = ( this.currentPageIndex * this.windowSize ) - this.windowSize;
            }
            this.lastItemIndex = ( this.currentPageIndex * this.windowSize ) -1;
            if ( this.lastItemIndex > this.size-1 )
            {
                this.lastItemIndex = this.size-1;
            }

            //JahiaConsole.println(CLASS_NAME+".paginate","firtsItemIndex : " + this.firstItemIndex);
            //JahiaConsole.println(CLASS_NAME+".paginate","lastItemIndex : " + this.lastItemIndex);

            // Compute the List of page indexes
            if ( this.size>0 )
            {
                if ( this.size <= this.windowSize )
                {
                    this.pages.add( new Integer(1) );
                } else {
                    int nbPages = ( this.size / this.windowSize );
                    for ( int i=0 ; i<nbPages; i++ )
                    {
                        this.pages.add( new Integer(i+1) );
                    }
                    if ( (this.size % this.windowSize) != 0 )
                    {
                        int newPageIndex = this.pages.size() + 1;
                        this.pages.add( new Integer(newPageIndex) );
                    }
                }
            }

            // everything seems ok
            this.isValid = true;

        } catch ( Exception e ) {
            logger.error(e.getMessage(), e);
        }
    }

}
