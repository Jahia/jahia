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

package org.jahia.blogs;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.jahia.blogs.actions.BlogDefinitionNames;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Jahia Listener reacting to any added or updated Container matching
 * the BLOG_TB_PING_LIST definition. Once a Container containing a ping url is
 * added or updated, a trackback ping request is sent to the url present in the
 * container.
 *
 * @author Xavier Lawrence
 */
public class BlogPingListener extends JahiaEventListener {
    
    // log4j logger
    static Logger log = Logger.getLogger(BlogPingListener.class);
    
    private BlogDefinitionNames containerNames;
    
    /**
     * Sends a ping
     */
    public void containerAdded(JahiaEvent je) {
        JahiaContainer source = (JahiaContainer)je.getObject();
        
        containerNames = new BlogDefinitionNames(je.getProcessingContext());
        
        try {
            if (source.getDefinition().getName().equals(containerNames.getValue(
                    BlogDefinitionNames.BLOG_TB_PING_LIST))) {
      
                // send a ping       
                String[] res = preparePingData(je);
                String result = sendPing(res[0], res[1], res[2], res[3], res[4]);

                log.debug(result);
                log.debug("End containerAdded...");
            }
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
     
    /**
     * Does the same as containerAdded (calls method containerAdded)
     */
    public void containerUpdated(JahiaEvent je) {
        containerAdded(je);
    }
    
    /**
     * Get the URL of a given post
     */
    protected String getPostURL(JahiaEvent je, JahiaContainer container,
            ContentPage page) throws JahiaException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(je.getProcessingContext().getScheme());
        buffer.append("://");
        buffer.append(je.getProcessingContext().getServerName());
        buffer.append(":");
        buffer.append(je.getProcessingContext().getServerPort());
        
        buffer.append(page.getURL(je.getProcessingContext()));
        
        buffer.append("?entryId=");
        buffer.append(container.getID());
        return buffer.toString();
    }
    
    /**
     * Fetches the data required by the ping request
     * @return An Array of String containing in respective order:
     *         pingURL, postURL, postTitle, postExcerpt & blogName
     */
    protected String[] preparePingData(JahiaEvent je)
            throws JahiaException {
        
        JahiaContainer source = (JahiaContainer)je.getObject();
        
        ContentPage blogContentPage = ContentPage.getPage(
                source.getPageID());
        je.getProcessingContext().changePage(blogContentPage);
        
        JahiaField field = source.getField(containerNames.getValue(
                BlogDefinitionNames.TB_PING_URL));
        final String pingURL = field.getValue();
        
        log.debug("About to send ping to: "+pingURL);
        
        // Get the post information
        int ctnListID = source.getListID();
        
        JahiaContainersService cntService = ServicesRegistry.getInstance().
                getJahiaContainersService();
        
        JahiaContainerList tbPingList = cntService.loadContainerList(
                ctnListID, LoadFlags.ALL, je.getProcessingContext());
        
        int postContainerID = tbPingList.getParentEntryID();
        
        EntryLoadRequest elr = new EntryLoadRequest(
                EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, 
                je.getProcessingContext().getLocales());
        JahiaContainer postContainer = cntService.loadContainer(
                postContainerID, LoadFlags.ALL, je.getProcessingContext(), elr);
        
        String fieldName = containerNames.getValue(BlogDefinitionNames.POST_TITLE);
        field = postContainer.getField(fieldName);
        // set the postTitle
        final String postTitle = field != null ? field.getValue() : "";

        fieldName = containerNames.getValue(BlogDefinitionNames.POST_EXCERPT);
        field = postContainer.getField(fieldName);
        // set the excerpt
        final String postExcerpt = field != null ? field.getValue() : "";
             
        // set the blogName
        final String  blogName = blogContentPage.getTitle(je.getProcessingContext());
        
        // set the postURL
        final String postURL = getPostURL(je, postContainer, blogContentPage);

        return new String[]{pingURL, postURL, postTitle, postExcerpt, blogName};
    }
    
    
    /**
     * Sends a ping request by sending an HTTP POST
     */
    protected static String sendPing(String pingURL, String postURL,
            String postTitle, String postExcerpt, String blogName)
            throws IOException {
        
        log.debug("sendPing: " +pingURL+ ", " +postURL+ ", " +postTitle+ ", "
                +postExcerpt+ ", " +blogName);
        
        if (pingURL == null || pingURL.length() < 1) {
            throw new IllegalArgumentException("Argument 'pingURL' cannot be null or an empty String");
        }
        
        if (postURL == null || postURL.length() < 1) {
            throw new IllegalArgumentException("Argument 'postURL' cannot be null or an empty String");
        }
        
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(pingURL);
        
        post.setRequestHeader("Content-type",
                "application/x-www-form-urlencoded; charset=utf-8");
        
        post.addParameter("url", postURL);
        
        if (postTitle != null && postTitle.length() > 0) {
            post.addParameter("title", postTitle);
        }
        
        if (postExcerpt != null && postExcerpt.length() > 0) {
            post.addParameter("excerpt", postExcerpt);
        }
        
        if (blogName != null && blogName.length() > 0) {
            post.addParameter("blog_name", blogName);
        }
        
        int status = client.executeMethod(post);
        String body = post.getResponseBodyAsString();
        
        StringBuffer buff = new StringBuffer();
        buff.append("StatusCode: ");
        buff.append(status);
        buff.append("; ");
        buff.append(body);
        
        return buff.toString();
    }
}
