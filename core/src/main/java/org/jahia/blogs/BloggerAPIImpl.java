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
