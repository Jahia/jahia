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
import java.util.Iterator;
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
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Action used to edit a post from the Jahia content repository. Compliant with
 * Blogger API's and MetaWeblogAPI API's editPost method.
 *
 * @author Xavier Lawrence
 */
public class EditPostAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(EditPostAction.class);
    
    private String postID;
    private String content;
    private Map struct;
    private boolean publish;
    
    /**
     * Creates a new instance of EditPostAction (Blogger API)
     */
    public EditPostAction(String appKey, String postID, String userName,
            String password, String content, boolean publish) {
        
        super.appKey = appKey;
        super.userName = userName;
        super.password = password;
        this.postID = postID;
        this.content = content;
        this.publish = publish;
    }
    
    /**
     * Creates a new instance of EditPostAction (MetaWeblog API)
     */
    public EditPostAction(String postID, String userName, String password,
            Map struct, boolean publish) {
        
        super.userName = userName;
        super.password = password;
        this.postID = postID;
        this.struct = struct;
        this.publish = publish;
    }
    
    /**
     * Edits a post of the blog of this user and saves the modifications into
     * Jahia
     *
     * @return True if the edition was sucessfull
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        // Load the Container and check the structure
        final JahiaContainer postContainer = super.getContainer(Integer.
                parseInt(postID));

        super.changePage(postContainer.getPageID());

        // First check that the user is registered to this site.
        JahiaUser user = super.checkLogin();
        
        if (!postContainer.checkWriteAccess(user)) {
            throw new JahiaException(
                    "You do not have write access to Post: "+postID,
                    "You do not have write access to Container: "+postID,
                    JahiaException.ACL_ERROR,
                    JahiaException.WARNING_SEVERITY);
        }
        
        log.debug("Working on Container: "+postContainer.getID());
        
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
                // contains the title value
                fieldValue =
                        content.substring(content.indexOf(tag) + 7,
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
        
        // Used to publish all the ping tb containers
        boolean publishPingContainers = false;
        
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
            
            // Urls sent by the client
            List tbURLs = (List)struct.get(MetaPostInfo.MT_TB_PING_URLS);
            
            if (tbURLs != null && tbURLs.size() > 0) {
                
                JahiaContainerDefinition def = JahiaContainerDefinitionsRegistry.
                        getInstance().getDefinition(jParams.getSiteID(),
                        containerNames.getValue(BlogDefinitionNames.BLOG_TB_PING_LIST));
                
                JahiaContainerList pingURLs = postContainer.getContainerList(
                        containerNames.getValue(BlogDefinitionNames.BLOG_TB_PING_LIST));
                
                int listID = 0, aclID = 0;
                if (pingURLs != null && pingURLs.getID() > 0) {
                    listID = pingURLs.getID();
                }
                
                List existingPings = getExistingPingURLs(pingURLs);
                for (int i=0; i<tbURLs.size(); i++) {
                    String url = (String)tbURLs.get(i);
                    
                    // Ignore any empty or too short urls
                    if (url.length() < 7) continue;
                    
                    // url is allready stored, don't add it again
                    if (existingPings.contains(url)) {
                        publishPingContainers = true;
                        continue;
                    }
                    
                    // load the containerlist after the 1st added container
                    if (i == 1) {
                        pingURLs = containerService.loadContainerList(
                                listID, LoadFlags.ALL, jParams);
                    }
                    
                    // check that the containerList exists
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
                    listID = pingURLContainer.getListID();
                    log.debug("on list: "+listID);
                    
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
                    
                    pingURLContainer = null;
                    url = null;
                }
            }
        }
        
        containerService.saveContainer(postContainer, 0, jParams);
        
        if (publish) {
            if (publishPingContainers) {
                JahiaContainerList pingURLs = postContainer.getContainerList(
                        containerNames.getValue(BlogDefinitionNames.BLOG_TB_PING_LIST));
                if (pingURLs != null && pingURLs.getID() > 0) {
                    activatePingContainers(pingURLs, user);
                }
            }
            
            super.activateContainer(postContainer.getID(), user);
        }
        
        super.flushPageCacheThatDisplayContainer(postContainer);
        
        log.debug("Post: "+postContainer.getID()+ " edited");
        return Boolean.TRUE;
    }
    
    /**
     * Returns the existing ping urls of a given post in a List Object
     */
    protected List getExistingPingURLs(JahiaContainerList list)
    throws JahiaException {
        List result = new ArrayList();
        
        if (list == null) return result;
        
        Iterator en = list.getContainers();
        
        while (en.hasNext()) {
            JahiaContainer c = (JahiaContainer)en.next();
            JahiaField f = c.getField(containerNames.getValue(BlogDefinitionNames.TB_PING_URL));
            
            result.add(f.getValue());
            
        }
        return result;
    }
    
    /**
     *
     */
    protected void activatePingContainers(JahiaContainerList list, JahiaUser user)
    throws JahiaException {
        Iterator en = list.getContainers();
        
        while (en.hasNext()) {
            JahiaContainer c = (JahiaContainer)en.next();
            super.activateContainer(c.getID(), user);
        }
    }
}
