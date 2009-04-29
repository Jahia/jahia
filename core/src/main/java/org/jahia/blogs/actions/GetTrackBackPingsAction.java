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

import java.util.*;

import org.apache.log4j.Logger;
import org.jahia.blogs.model.MetaPostInfo;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;

/**
 * Action used to get a post's TrackBakc pings from the Jahia content repository.
 * Compliant with MovableType API's getTrackBackPings method.
 *
 * @author Xavier Lawrence
 */
public class GetTrackBackPingsAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetTrackBackPingsAction.class);
    
    private String postID;
    
    /** Creates a new instance of GetTrackBackPingsAction */
    public GetTrackBackPingsAction(String postID) {
        this.postID = postID;
    }
    
    /**
     *
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        // Load the Container and check the structure
        final JahiaContainer postContainer = super.getContainer(
                Integer.parseInt(postID));
        
        if (postContainer == null) {
            throw new JahiaException("Post: "+postID+
                    " does not exist", "Container: "+postID+ " does not exist",
                    JahiaException.ENTRY_NOT_FOUND,
                    JahiaException.WARNING_SEVERITY);
        }
        
        log.debug("Working on post: "+postContainer.getID());
        
        super.changePage(postContainer.getPageID());
        
        JahiaContainerList trackBacks = postContainer.getContainerList(
                super.containerNames.getValue(BlogDefinitionNames.BLOG_TB_LIST));
        
        if (trackBacks == null || trackBacks.getID() < 1) {
            log.debug("No Trackbacks found");
            return Collections.emptyList();
        }
        
        log.debug("Found "+trackBacks.size()+" trackbacks");
        
        List result = new ArrayList(trackBacks.size());
        Iterator e = trackBacks.getContainers();
        
        while (e.hasNext()) {
            Map tb = new HashMap(3);
            JahiaContainer tbContainer = (JahiaContainer)e.next();
            
            log.debug("Working on Container: "+tbContainer.getID());
            
            String fieldName = containerNames.getValue(BlogDefinitionNames.TB_BLOG_NAME);
            JahiaField field = tbContainer.getField(fieldName); 
            if (field != null) tb.put(MetaPostInfo.PING_TITLE, field.getValue());
            
            fieldName = containerNames.getValue(BlogDefinitionNames.TB_URL);
            field = tbContainer.getField(fieldName);
            if (field != null) tb.put(MetaPostInfo.PING_URL, field.getValue());
            
            fieldName = containerNames.getValue(BlogDefinitionNames.TB_PING_IP);
            field = tbContainer.getField(fieldName);
            if (field != null) tb.put(MetaPostInfo.PING_IP, field.getValue());
            
            result.add(tb);
        }
        
        log.debug("TrackBack Pings are: "+result);
        return result;
    }
}
