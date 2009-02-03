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

package org.jahia.blogs.actions;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.data.containers.ContainerSorterBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Action used to get a user's most recent posts' titles from the Jahia 
 * content repository.
 * Compliant with MovableType API's getRecentPostTitles method.
 *
 * @author Xavier Lawrence
 */
public class GetRecentPostTitlesAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetRecentPostTitlesAction.class);
    
    private String blogID;
    private int numberOfPosts;
    
    /** Creates a new instance of GetRecentPostTitlesAction */
    public GetRecentPostTitlesAction(String blogID,
            String userName, String password, int numberOfPosts) {
        super.userName = userName;
        super.password = password;
        this.blogID = blogID;
        this.numberOfPosts = numberOfPosts;
    }
    
    /**
     * Retrieves a given number of posts based on the creation date.
     *
     * @return A List of Maps containing the posts
     */
    public Object execute() throws JahiaException {
        // Create commmon resources
        super.init();

        // Set the correct page
        super.changePage(Integer.parseInt(blogID));
        
        // First check that the user is registered to this site.
        super.checkLogin();

        // Name of the containerList containing all the posts of the blog
        final String containerListName = super.containerNames.getValue(
                BlogDefinitionNames.BLOG_POSTS_LIST_NAME);
        final int containerListID = containerService.getContainerListID(
                containerListName, Integer.parseInt(blogID));
        
        if (containerListID == -1) {
            return new ArrayList(0);
        }
        
        // This is the ContainerList containing all the entries of a Blog
        final JahiaContainerList entryList = containerService.loadContainerList(
                containerListID, LoadFlags.ALL, jParams);
        
        log.debug("ContainerList for Blog: "+blogID+" is: "+containerListID);
        
        int posts = entryList.getFullSize();
        
        List result = new ArrayList(numberOfPosts);
        entryList.setCtnListPagination(numberOfPosts, 0);
        
        if (numberOfPosts >= posts) {
            log.debug("Getting all the posts of blog: "+blogID+ "("+
                    numberOfPosts +" >= "+ posts + ")");
            // simply return all the posts stored in the container list
            Iterator enu = entryList.getContainers();
            
            while (enu.hasNext()) {
                JahiaContainer postContainer = (JahiaContainer)enu.next();
                
                result.add(super.createMovableTypePostInfo(postContainer));
            }
            
        } else {
            // Only return the "numberOfPosts" most recent posts
            log.debug("Getting "+numberOfPosts+" recent posts of blog: "+
                    blogID);
            
            // sort by date desc
            EntryLoadRequest elr = new EntryLoadRequest(
                    EntryLoadRequest.STAGING_WORKFLOW_STATE,
                    jParams.getEntryLoadRequest().getVersionID(),
                    jParams.getEntryLoadRequest().getLocales());
            ContainerSorterBean entries_sort_handler =
                    new ContainerSorterBean(containerListID, "date", true,
                    elr);
            entries_sort_handler.setDescOrdering();
            
            List sortedList = entries_sort_handler.doSort(null);
            
            for (int i=0; i<numberOfPosts; i++) {
                int cntID = ((Integer)sortedList.get(i)).intValue();
                JahiaContainer postContainer = super.getContainer(cntID);
                
                result.add(super.createMovableTypePostInfo(postContainer));
            }
        }
        
        log.debug("Post(s): "+result);
        return result;
    }
}
