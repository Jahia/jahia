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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.blogs.model.BlogInfo;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Action used to get users' blogs information from the Jahia content repository.
 * Compliant with Blogger API's getUserBlogs method.
 *
 * @author Xavier Lawrence
 */
public class GetUserBlogsAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetUserBlogsAction.class);
    
    /** Creates a new instance of GetUserBlogsAction */
    public GetUserBlogsAction(String appKey, String userName, String password) {
        super.appKey = appKey;
        super.userName = userName;
        super.password = password;
    }
    
    /**
     * Returns information (blogID, blogName and url) of each blog a given user
     * is a member of.
     *
     * @return A List of Maps containing the blogs' information
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();

        // Get all pages with template definition name "Blog"
        final JahiaPageDefinition pageDef =
                servicesRegistry.getJahiaPageTemplateService()
                .lookupPageTemplateByName("Blog", jParams.getSiteID());
        final List blogPagesId =
                JahiaPageBaseService.getInstance().getPageIDsWithTemplate(
                pageDef.getID());
        
        Iterator blogPagesIdEnum = blogPagesId.iterator();
        List result = new ArrayList();

        // For each page matching the Blog definition
        while (blogPagesIdEnum.hasNext()) {
            int blogPageId = ((Integer)blogPagesIdEnum.next()).intValue();

            ContentPage blogContentPage = super.changePage(blogPageId);   

            // First check that the user is registered to this site.
            final JahiaUser user = super.checkLogin();

            int deleteVersionID = blogContentPage.getDeleteVersionID();
            
            // ignore all deleted pages
            if ((deleteVersionID != -1)) continue;
            
            JahiaPage blogPage =
                    blogContentPage.getPage(jParams.getEntryLoadRequest(), 
                    jParams.getOperationMode(), user);
            if (blogPage != null) {
                log.debug("Found blog ["+ blogPageId + "]: " +
                        blogPage.getTitle());
                
                // get the entries container List
                final String containerListName = super.containerNames.getValue(
                        BlogDefinitionNames.BLOG_POSTS_LIST_NAME);
                final int containerListID = containerService.getContainerListID(
                        containerListName, blogPageId);
                
                // In that case, there is no post on the blog page, so the 
                // container list does not exist yet. We thus have to check the
                // write access of the page
                boolean ok = false;
                JahiaContainerList entryList = null;
                if (containerListID == -1) {
                    log.debug("entryList does not exist");
                    ok = blogPage.checkWriteAccess(user);
                    
                } else {
                    entryList = containerService.
                            loadContainerList(containerListID, LoadFlags.ALL,
                            jParams);
                }
                
                // Check ACL to see if the user has write access to the blog.
                // If the container list does not exist, test the boolean
                if ((entryList != null && entryList.checkWriteAccess(user)) || 
                        ok) {
                    log.debug("User: "+user.getUsername()+" has write "+
                            "access to blog: "+blogPage.getTitle()+ "(" +
                            blogPage.getID() + ")");
                    
                    Map blogInfo = new HashMap(3);
                    
                    blogInfo.put(BlogInfo.BLOG_URL, super.getPageURL(blogPage));
                    blogInfo.put(BlogInfo.BLOG_ID, Integer.toString(
                            blogPage.getID()));
                    blogInfo.put(BlogInfo.BLOG_NAME, blogPage.getTitle());
                    
                    result.add(blogInfo);
                }
            }
        }
        
        log.debug("Returning Blog info of "+result.size()+" blogs");
        return result; 
    }
}
