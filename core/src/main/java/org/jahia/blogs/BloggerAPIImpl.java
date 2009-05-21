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
package org.jahia.blogs;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.jahia.blogs.actions.AbstractAction;
import org.jahia.blogs.actions.DeletePostAction;
import org.jahia.blogs.actions.EditPostAction;
import org.jahia.blogs.actions.GetPostAction;
import org.jahia.blogs.actions.GetRecentPostsAction;
import org.jahia.blogs.actions.GetUserBlogsAction;
import org.jahia.blogs.actions.GetUserInfoAction;
import org.jahia.blogs.actions.NewPostAction;
import org.jahia.blogs.api.BloggerAPI;
import org.jahia.blogs.api.XMLRPCConstants;

/**
 * Implementation of the BloggerAPI.
 *
 * @author Xavier Lawrence
 */
public class BloggerAPIImpl implements BloggerAPI, XMLRPCConstants {
    
    // log4j logger
    static Logger log = Logger.getLogger(BloggerAPIImpl.class);
    
    /**
     */
    public String newPost(final String appKey, final String blogID,
            final String userName, final String password, final String content,
            final boolean publish) throws XmlRpcException {
        log.debug("blogger.newPost: "+appKey+ ", " +blogID+ ", " +userName+
                ", " +password+ ", " +content+ ", " +publish);
        
        AbstractAction action = new NewPostAction(appKey, blogID, userName,
                password, content, publish);
        
        try {
            return (String)action.execute();
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
            
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public boolean editPost(final String appKey, final String postID,
            final String userName, final String password, final String content,
            final boolean publish) throws XmlRpcException {
        log.debug("blogger.editPost: "+appKey+ ", " +postID+ ", " +userName+
                ", " +password+ ", " +content+ ", " +publish);
        
        AbstractAction action = new EditPostAction(appKey, postID, userName,
                password, content, publish);
        
        try {
            return ((Boolean)action.execute()).booleanValue();
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public Map getPost(final String appKey, final String postID,
            final String userName, final String password) throws XmlRpcException {
        log.debug("blogger.getPost: "+appKey+ ", " +postID+ ", " +userName+
                ", " +password);
        
        AbstractAction action = new GetPostAction(appKey, postID, userName,
                password);
        
        try {
            return (Map)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public boolean deletePost(final String appKey, final String postID,
            final String userName, final String password,
            final boolean publish) throws XmlRpcException {
        log.debug("blogger.deletePost: "+appKey+ ", " +postID+ ", " +userName+
                ", " +password+ ", " +publish);
        
        AbstractAction action = new DeletePostAction(appKey, postID, userName,
                password, publish);
        
        try {
            return ((Boolean)action.execute()).booleanValue();
        } catch (Exception e) {
            if (e.getMessage().indexOf("Login") != -1) {
                log.error(e.getMessage(), e);
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public List getRecentPosts(final String appKey, final String blogID,
            final String userName, final String password,
            final int numberOfPosts) throws XmlRpcException {
        log.debug("blogger.getRecentPosts: "+appKey+ ", " +blogID+ ", " +userName+
                ", " +password+ ", " +numberOfPosts);
        
        AbstractAction action = new GetRecentPostsAction(appKey, blogID, userName,
                password, numberOfPosts);
        
        try {
            return (List)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     * Not implemented. The user should change the template directly from 
     * Jahia
     */
    public boolean setTemplate(final String appKey, final String blogID,
            final String userName, final String password,
            final String templateData, final String templateType) throws XmlRpcException {
        log.debug("blogger.setTemplate: "+appKey+ ", " +blogID+ ", " +userName+
                ", " +password+ ", " +templateData+ ", " +templateType);
        
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION,
                UNSUPPORTED_EXCEPTION_MSG);
    }
    
    /**
     * Not implemented. The user should change the template directly from 
     * Jahia
     */
    public String getTemplate(final String appKey, final String blogID,
            final String userName, final String password,
            final String templateType) throws XmlRpcException {
        log.debug("blogger.getTemplate: "+appKey+ ", " +blogID+ ", " +userName+
                ", " +password+ ", " +templateType);
        
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION,
                UNSUPPORTED_EXCEPTION_MSG);
    }
    
    /**
     */
    public Map getUserInfo(final String appKey, final String userName,
            final String password) throws XmlRpcException {
        log.debug("blogger.getUserInfo: "+appKey+ ", " +userName+ ", " +password);
        
        AbstractAction action = new GetUserInfoAction(appKey, userName,
                password);
        
        try {
            return (Map)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public List getUsersBlogs(final String appKey, final String userName,
            final String password) throws XmlRpcException {
        log.debug("blogger.getUsersBlogs: "+appKey+ ", " +userName+ ", " +password);
        
        AbstractAction action = new GetUserBlogsAction(appKey, userName,
                password);
        
        try {
            return (List)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
}
