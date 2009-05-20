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
package org.jahia.blogs.actions;

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.ContainerSorterBean;

import org.jahia.services.version.EntryLoadRequest;

import org.jahia.services.categories.Category;

import org.jahia.data.fields.LoadFlags;

import org.jahia.exceptions.JahiaException;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Action used to get a user's most recent posts' information from the Jahia 
 * content repository.
 * Compliant with Blogger API's and MetaWeblog API's getRecentPosts method.
 *
 * @author Xavier Lawrence
 */
public class GetRecentPostsAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetRecentPostsAction.class);
    
     private String blogID;
     private int numberOfPosts;
     private boolean meta;
    
    /** 
     * Creates a new instance of GetRecentPostsAction (Blogger API)
     */
     public GetRecentPostsAction(String appKey, String blogID,
             String userName, String password, int numberOfPosts) {
         super.appKey = appKey;
         super.userName = userName;
         super.password = password;
         this.blogID = blogID;
         this.numberOfPosts = numberOfPosts;
         meta = false;
     }
     
    /** 
     * Creates a new instance of GetRecentPostsAction (MetaWeblog API)
     */
     public GetRecentPostsAction(String blogID,
             String userName, String password, int numberOfPosts) {
         super.userName = userName;
         super.password = password;
         this.blogID = blogID;
         this.numberOfPosts = numberOfPosts;
         meta = true;
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
         
         EntryLoadRequest elr = new EntryLoadRequest(
                 EntryLoadRequest.STAGING_WORKFLOW_STATE,
                 0,
                 jParams.getEntryLoadRequest().getLocales());
         
         // This is the ContainerList containing all the entries of a Blog
         final JahiaContainerList entryList = containerService.loadContainerList(
                 containerListID, LoadFlags.ALL, jParams, elr, null, null, null);
         
         log.debug("ContainerList for Blog: "+blogID+" is: "+containerListID);
         
         int posts = entryList.size();    
         
         List result = new ArrayList(numberOfPosts);
         entryList.setCtnListPagination(numberOfPosts, 0);
         
         if (numberOfPosts >= posts) {
             log.debug("Getting all the posts of blog: "+blogID+ " ("+
                     numberOfPosts +" >= "+ posts + ")");
            // simply return all the posts stored in the container list
             Iterator enu = entryList.getContainers();
             
             while (enu.hasNext()) {
                 JahiaContainer postContainer = (JahiaContainer)enu.next();
                 
                 if (meta) {
                     Set set = Category.getObjectCategories(postContainer.
                             getContentContainer().getObjectKey());
                     result.add(super.createMetaPostInfo(postContainer, 
                             set));
                 } else {
                     result.add(super.createPostInfo(postContainer));
                 }
             }
             
         } else {
            // Only return the "numberOfPosts" most recent posts
             log.debug("Getting "+numberOfPosts+" recent posts of blog: "+
                     blogID);
             
             // sort by date desc
             ContainerSorterBean entries_sort_handler =
                     new ContainerSorterBean(containerListID, "date", true, 
                     elr);
             entries_sort_handler.setDescOrdering();
             
             List sortedList = entries_sort_handler.doSort(null);
             
             for (int i=0; i<numberOfPosts; i++) {
                 int cntID = ((Integer)sortedList.get(i)).intValue();
                 JahiaContainer postContainer = super.getContainer(cntID);
                 
                 if (meta) {
                     Set set = Category.getObjectCategories(postContainer.
                             getContentContainer().getObjectKey());
                     result.add(super.createMetaPostInfo(postContainer,
                             set));
                 } else {
                     result.add(super.createPostInfo(postContainer));
                 }
             }
         }
         
         log.debug("Post(s): "+result);
         return result;  
     }  
}
