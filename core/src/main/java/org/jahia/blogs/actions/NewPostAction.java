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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.blogs.model.MetaPostInfo;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Action used to add a new post to the Jahia content repository. Compliant with
 * Blogger API's and MetaWeblog API's newPost method.
 *
 * @author Xavier Lawrence
 */
public class NewPostAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(NewPostAction.class);
    
    private String blogID;
    private String content;
    private Map struct;
    private boolean publish;
    
    private boolean isNewList;
    
    /**
     * Creates a new instance of NewPostAction (Blogger API)
     */
    public NewPostAction(String appKey, String blogID,
            String userName, String password, String content,
            boolean publish) {
        
        super.appKey = appKey;
        super.userName = userName;
        super.password = password;
        this.blogID = blogID;
        this.content = content;
        this.publish = publish;
    }
    
    /**
     * Creates a new instance of NewPostAction (MetaWeblog API)
     */
    public NewPostAction(String blogID, String userName, String password,
            Map struct, boolean publish) {
        
        super.userName = userName;
        super.password = password;
        this.blogID = blogID;
        this.struct = struct;
        this.publish = publish;
    }
    
    /**
     * Adds a post to the blog of this user into Jahia
     *
     * @return The PostID as a String
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        ContentPage blogContentPage = super.changePage(Integer.parseInt(blogID));

        // First check that the user is registered to this site.
        JahiaUser user = super.checkLogin();
        
        // Set the correct page and check write access
        if (!blogContentPage.checkWriteAccess(user)) {
            throw new JahiaException(
                    "You do not have write access to Blog: "+blogID,
                    "You do not have write access to Page: "+blogID,
                    JahiaException.ACL_ERROR,
                    JahiaException.WARNING_SEVERITY);
        }
        
        // Name of the containerList containing all the posts of the blog
        final String containerListName = super.containerNames.getValue(
                BlogDefinitionNames.BLOG_POSTS_LIST_NAME);
        final int containerListID = containerService.getContainerListID(
                containerListName, Integer.parseInt(blogID));
        
        JahiaContainerList entryList = null;
        if (containerListID != -1) {
            // This is the ContainerList containing all the entries of a Blog
            entryList = containerService.loadContainerList(
                    containerListID, LoadFlags.ALL, jParams);
        }
        
        int containerID, listID, rank, aclID, versionID;
        containerID = listID = rank = aclID = versionID = 0;
        
        // If the ContainerList does not exist (== null), we are posting the
        // very first post and all the container params have allready been set
        // above (= 0)
        if (entryList != null) {
            listID = entryList.getID();
            aclID = entryList.getAclID();
            isNewList = false;
            
        } else {
            isNewList = true;
        }
        
        JahiaContainerDefinition def = JahiaContainerDefinitionsRegistry.
                getInstance().getDefinition(jParams.getSiteID(), containerListName);
        
        // The container for the new post
        JahiaContainer postContainer = new JahiaContainer(
                containerID,
                jParams.getJahiaID(),
                jParams.getPageID(),
                listID,
                rank,
                aclID,
                def.getID(),
                versionID,
                EntryLoadRequest.STAGING_WORKFLOW_STATE);
        
        // Save the new Container
        containerService.saveContainer(postContainer, 0, jParams);
        log.debug("Saving Container for new Post: "+postContainer.getID());
        
        // Load the Container and check the structure
        postContainer = super.getContainer(postContainer.getID(),
                jParams.getLocale().toString());
        log.debug("Loaded and working on Container: "+postContainer.getID());
        
        /* Set all the fields' value with the right data */
        // set title field
        String fieldName = super.containerNames.getValue(BlogDefinitionNames.POST_TITLE);
        String fieldValue = "";
        
        // If content != null -> we are using the Blogger API
        // If content == null -> we are using the MetaWebLog API
        if (content != null) {
            String tag = "<"+fieldName+">";
            String endTag = "</"+fieldName+">";
            if (content.indexOf(tag) != -1) {
                // contains only the title value
                fieldValue =
                        content.substring(content.indexOf(tag) + tag.length(),
                        content.indexOf(endTag));
                // contains the post body without the title
                content = StringUtils.replace(content, tag + fieldValue +
                        endTag, "");
            }
            
        } else {
            fieldValue = (String)struct.get(MetaPostInfo.TITLE);
            content = (String)struct.get(MetaPostInfo.DESCRIPTION);
            List categories = (List)struct.get(MetaPostInfo.CATEGORIES);
            super.setCategories(categories, postContainer);
        }
        
        JahiaField field = postContainer.getField(fieldName);
        log.debug("Setting value of field: "+field.getID() +"; "+
                field.getLanguageCode());
        field.setValue(fieldValue);
        
        // set body field
        fieldName = super.containerNames.getValue(BlogDefinitionNames.POST_BODY);
        field = postContainer.getField(fieldName);
        log.debug("Setting value of field: "+field.getID() +"; "+
                field.getLanguageCode());
        super.setValue(field, content);
        
        // set posting date
        fieldName = super.containerNames.getValue(BlogDefinitionNames.POST_DATE);
        field = postContainer.getField(fieldName);
        field.setObject(Long.toString(System.currentTimeMillis()));
        
        // set the author of the message
        fieldName = super.containerNames.getValue(BlogDefinitionNames.POST_AUTHOR);
        field = postContainer.getField(fieldName);
        log.debug("Setting value of field: "+field.getID() +"; "+
                field.getLanguageCode());
        field.setValue(userName);
        
        // check if extra Movable Type are present in the struct
        if (struct != null) {
            
            fieldName = containerNames.getValue(BlogDefinitionNames.POST_EXCERPT);
            field = postContainer.getField(fieldName);
            if (struct.containsKey(MetaPostInfo.MT_EXCERPT)) {
                String excerpt = (String)struct.get(MetaPostInfo.MT_EXCERPT);
                
                if (excerpt != null && excerpt.length() > 0) {
                    field.setValue(excerpt);
                }
            }
            
            fieldName = containerNames.getValue(BlogDefinitionNames.POST_KEYWORDS);
            field = postContainer.getField(fieldName);
            if (struct.containsKey(MetaPostInfo.MT_KEYWORDS)) {
                String keywords = (String)struct.get(MetaPostInfo.MT_KEYWORDS);
                
                if (keywords != null && keywords.length() > 0) {
                    field.setValue(keywords);
                }
            }
            
            // Save the fields since some are required for the TB ping
            containerService.saveContainer(postContainer, 0, jParams);
            
            List tbURLs = (List)struct.get(MetaPostInfo.MT_TB_PING_URLS);
            
            if (tbURLs != null && tbURLs.size() > 0) {
                
                def = JahiaContainerDefinitionsRegistry.
                        getInstance().getDefinition(jParams.getSiteID(),
                        containerNames.getValue(BlogDefinitionNames.BLOG_TB_PING_LIST));
                
                JahiaContainerList pingURLs = null;
                int ctnListID = -1;
                
                for (int i=0; i<tbURLs.size(); i++) {
                    String url = (String)tbURLs.get(i);
                    
                    // Ignore any empty or too short urls
                    if (url.length() < 7) continue;
                    
                    if (i == 1) {
                        pingURLs = containerService.loadContainerList(
                                ctnListID, LoadFlags.ALL, jParams);
                    }
                    
                    if (pingURLs != null && pingURLs.getID() > 0) {
                        listID = pingURLs.getID();
                        aclID = pingURLs.getAclID();
                        
                    } else {
                        listID = aclID = 0;
                    }
                    
                    JahiaContainer pingURLContainer = new JahiaContainer(
                            0, jParams.getJahiaID(), jParams.getPageID(),
                            listID, 0, aclID, def.getID(), 0,
                            EntryLoadRequest.STAGING_WORKFLOW_STATE);
                    
                    // Save the new Container
                    containerService.saveContainer(pingURLContainer,
                            postContainer.getID(), jParams);
                    
                    // Load the Container and check the structure
                    pingURLContainer = super.getContainer(pingURLContainer.getID(),
                            jParams.getLocale().toString());
                    
                    log.debug("pingURLContainer: "+pingURLContainer.getID());
                    ctnListID = pingURLContainer.getListID();
                    
                    field = pingURLContainer.getField(containerNames.
                            getValue(BlogDefinitionNames.TB_PING_URL));
                    field.setValue(url);
                    
                    containerService.saveContainer(pingURLContainer,
                            postContainer.getID(), jParams);
                    
                    // Notify the listener so it can send the ping
                    JahiaEvent theEvent = new JahiaEvent(this, jParams,
                            pingURLContainer);
                    servicesRegistry.getJahiaEventService().
                            fireAddContainer(theEvent);
                    
                    if (i == 0) {
                        super.activateContainerList(pingURLContainer.getListID(),
                                user, pingURLContainer.getPageID());
                    }
                    
                    if (publish) {
                        // publish the trackBack ping url container
                        super.activateContainer(pingURLContainer.getID(), user);
                    }
                }
            }
        }
        
        containerService.saveContainer(postContainer, 0, jParams);
        
        if (isNewList) {
            super.activateContainerList(postContainer.getListID(), user,
                    postContainer.getPageID());
        }
        
        if (publish) {
            super.activateContainer(postContainer.getID(), user);
        }
        
        super.flushPageCacheThatDisplayContainer(postContainer);
        
        // The container ID is the postID
        log.debug("New Post OK, returning postID: "+postContainer.getID());
        return Integer.toString(postContainer.getID());
    }
}
