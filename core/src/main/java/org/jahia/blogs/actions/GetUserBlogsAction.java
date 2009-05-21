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
