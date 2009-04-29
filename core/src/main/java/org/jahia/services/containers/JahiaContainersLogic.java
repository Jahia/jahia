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
    public void logic_sort_container_tree (List<JahiaContainerList> theTree)
            throws JahiaException {
        // build dependencies between container lists
        for (int i = 0; i < theTree.size (); i++) {
            JahiaContainerList theList = theTree.get(i);
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
            List<JahiaContainerList> theTree)
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
                                                         Iterator<JahiaContainerList> containerLists) {
        while (containerLists.hasNext ()) {
            JahiaContainerList theContainerList = containerLists.next ();
            Iterator<JahiaContainer> containers = theContainerList.getContainers ();
            while (containers.hasNext ()) {
                JahiaContainer theContainer = containers.next ();
                if (theContainer.getID () != thectnid) {
                    Iterator<JahiaContainerList> listInContainer = theContainer.getContainerLists ();
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
