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
