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

/***
 * JahiaContainersLogic
 * @author Eric Vassalli
 *
 * Holds all the methods to build a container logical tree structure
 *
 * @todo we MUST get rid of this code, it's slow, it's ugly, it's outdated !
 *
 */

//
//  logic_build_container_tree( int pageID, ProcessingContext jParams )
//


package org.jahia.services.containers;

import java.util.Iterator;
import java.util.List;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaException;


public class JahiaContainersLogic {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaContainersLogic.class);


    /**
     * constructor
     */
    public JahiaContainersLogic () {
    }


    /**
     * sorts the container tree
     *
     * @param theTree the actual tree, unsorted
     *
     * @see org.jahia.data.containers.JahiaContainerList
     */
    public void logic_sort_container_tree (List theTree)
            throws JahiaException {
        // build dependencies between container lists
        for (int i = 0; i < theTree.size (); i++) {
            JahiaContainerList theList = (JahiaContainerList) theTree.get(i);
            if (theList.getParentEntryID () != 0) {
                if (logic_add_container_list_to_container (theList, theList.getParentEntryID (),
                        theTree)) {
                    theTree.remove(i);
                    i--;
                }
            }
        }
    }


    /**
     * adds a container list to a container in the tree
     *
     * @param theContainerList the container list to add
     * @param thectnid         the container id to add the container list to
     * @param theTree          the Tree List
     *
     * @return true if succeeded, false if failed
     */
    private boolean logic_add_container_list_to_container (
            JahiaContainerList theContainerList,
            int thectnid,
            List theTree)
            throws JahiaException {
        JahiaContainer theContainer = logic_find_container_in_list (thectnid,
                theTree.iterator());
        if (theContainer != null) {
            if (theContainerList == null) {
                logger.error ("Hey, the container list is null !!");
            }
            theContainer.addContainerList (theContainerList);
            return true;
        } else {
            String errorMsg = "Error in container structure : container " + thectnid + " is referrenced";
            errorMsg += " by list " + theContainerList.getID () + ", but does not exist.";
            logger.error (errorMsg);
            return false;
        }
    }


    /**
     * finds a container object by its id in the tree structure
     *
     * @param thectnid the container id
     */
    private JahiaContainer logic_find_container_in_list (int thectnid,
                                                         Iterator containerLists) {
        while (containerLists.hasNext ()) {
            JahiaContainerList theContainerList = (JahiaContainerList) containerLists.next ();
            Iterator containers = theContainerList.getContainers ();
            while (containers.hasNext ()) {
                JahiaContainer theContainer = (JahiaContainer) containers.next ();
                if (theContainer.getID () != thectnid) {
                    Iterator listInContainer = theContainer.getContainerLists ();
                    while (listInContainer.hasNext ()) {
                        JahiaContainer tmpContainer = logic_find_container_in_list (thectnid,
                                listInContainer);
                        if (tmpContainer != null) {
                            return tmpContainer;
                        }
                    }
                } else {
                    return theContainer;
                }
            }
        }
        return null;
    }

}
