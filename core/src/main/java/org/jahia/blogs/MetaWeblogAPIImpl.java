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
import org.jahia.blogs.actions.GetCategoriesAction;
import org.jahia.blogs.actions.GetPostAction;
import org.jahia.blogs.actions.GetRecentPostsAction;
import org.jahia.blogs.actions.NewMediaObjectAction;
import org.jahia.blogs.actions.NewPostAction;
import org.jahia.blogs.api.MetaWeblogAPI;
import org.jahia.blogs.api.XMLRPCConstants;

/**
 * Implementation of the MetaWeblogAPI.
 *
 * @author Xavier Lawrence
 */
public class MetaWeblogAPIImpl extends BloggerAPIImpl implements MetaWeblogAPI,
        XMLRPCConstants {
    
    // log4j logger
    static Logger log = Logger.getLogger(MetaWeblogAPIImpl.class);
    
    /**
     */
    public boolean editPost(final String postID, final String userName,
            final String password, final Map struct,
            final boolean publish) throws XmlRpcException {
        log.debug("metaWebLog.editPost: " +postID+ ", " +userName+ ", " +
                password+ ", " +struct+ ", "+publish);
        
        AbstractAction action = new EditPostAction(postID, userName,
                password, struct, publish);
        
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
    public Map getPost(final String postID, final String userName,
            final String password) throws XmlRpcException {
        log.debug("metaWebLog.getPost: " +postID+ ", " +userName+ ", " +
                password);
        
        AbstractAction action = new GetPostAction(postID, userName,
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
    public List getRecentPosts(final String blogID, final String userName,
            final String password, final int numposts) throws XmlRpcException {
        log.debug("metaWebLog.getRecentPosts: " +blogID+ ", " +userName+ ", " +
                password+ ", " +numposts);
        
        AbstractAction action = new GetRecentPostsAction(blogID, userName,
                password, numposts);
        
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
     */
    public String newPost(final String blogID, final String userName,
            final String password, final Map struct,
            final boolean publish) throws XmlRpcException {
        log.debug("metaWebLog.newPost: " +blogID+ ", " +userName+ ", " +
                password+ ", " +struct+ ", "+publish);
        
        AbstractAction action = new NewPostAction(blogID, userName,
                password, struct, publish);
        
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
     * Added to support some of the cell phone implementation of the MetaWeblog
     * implementation client side (Sony Ericsson P900). Has the same behavior as 
     * its twin method.
     */
    public String newPost(final String blogID, final String userName,
            final String password, final Map struct,
            final boolean publish, boolean tralala) throws XmlRpcException {
        log.debug("metaWebLog.newPost: " +blogID+ ", " +userName+ ", " +
                password+ ", " +struct+ ", "+publish+ ", " +tralala);
        
        AbstractAction action = new NewPostAction(blogID, userName,
                password, struct, publish);
        
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
    public boolean deletePost(String postID, String userName, String password,
            boolean publish) throws XmlRpcException {
        log.debug("metaWebLog.deletePost: " +postID+ ", " +userName+ ", " +
                password+ ", "+publish);
        
        AbstractAction action = new DeletePostAction(postID, userName,
                password, publish);
        
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
    public Map getCategories(final String blogID, final String userName,
            final String password) throws XmlRpcException {
        log.debug("metaWebLog.getCategories: " +blogID+ ", " +userName+ ", " +
                password);
        
        AbstractAction action = new GetCategoriesAction(blogID, userName,
                password, true);
        
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
    public Map newMediaObject(final String blogID, final String userName,
            final String password, final Map struct) throws XmlRpcException {
        log.debug("metaWebLog.newMediaObject: " +blogID+ ", " +userName+ ", " +
                password+ ", " +struct);
        
        AbstractAction action = new NewMediaObjectAction(blogID, userName,
                password, struct);
        
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
}
