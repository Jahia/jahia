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

import org.jahia.blogs.api.MovableTypeAPI;
import org.jahia.blogs.api.XMLRPCConstants;

import org.jahia.blogs.actions.AbstractAction;
import org.jahia.blogs.actions.GetRecentPostTitlesAction;
import org.jahia.blogs.actions.GetCategoriesAction;
import org.jahia.blogs.actions.GetPostCategoriesAction;
import org.jahia.blogs.actions.SetPostCategoriesAction;
import org.jahia.blogs.actions.GetTrackBackPingsAction;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;

import org.apache.log4j.Logger;

/**
 * Implementation of the MovableTypeAPI.
 *
 * @author Xavier Lawrence
 */
public class MovableTypeAPIImpl extends MetaWeblogAPIImpl 
        implements MovableTypeAPI, XMLRPCConstants {
    
    // log4j logger
    static Logger log = Logger.getLogger(MovableTypeAPIImpl.class);
    
    /**
     * Returns a bandwidth-friendly list of the most recent posts in the system.
     */
    public List getRecentPostTitles(
            final String blogID,
            final String userName,
            final String passWord,
            final int numberOfPosts)
            throws XmlRpcException {
        
        log.debug("mt.getRecentPostTitles: " +blogID+ ", " +userName+
                ", " +passWord+ ", " +numberOfPosts);
        
        AbstractAction action = new GetRecentPostTitlesAction(blogID, userName,
                passWord, numberOfPosts);
        
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
     * Returns a list of all categories defined in the weblog.
     */
    public List getCategoryList(
            final String blogID,
            final String userName,
            final String passWord)
            throws XmlRpcException {
        
        log.debug("mt.getCategoryList: " +blogID+ ", " +userName+
                ", " +passWord);
        
        AbstractAction action = new GetCategoriesAction(blogID, userName, 
                passWord, false);
        
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
     * Returns a list of all categories to which the post is assigned. 
     */
    public List getPostCategories(
            final String postID,
            final String userName,
            final String passWord)
            throws XmlRpcException {
        
        log.debug("mt.getPostCategories: " +postID+ ", " +userName+
                ", " +passWord);
        
        AbstractAction action = new GetPostCategoriesAction(postID, userName,
                passWord);
        
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
     * Sets the categories for a post. 
     */
    public boolean setPostCategories(
            final String postID,
            final String userName,
            final String passWord,
            final List categories)
            throws XmlRpcException {
        
        log.debug("mt.setPostCategories: " +postID+ ", " +userName+
                ", " +passWord+ ", " +categories);
        
        AbstractAction action = new SetPostCategoriesAction(postID, userName,
                passWord, categories);
        
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
     * Retrieve information about the XML-RPC methods supported by the server.
     */
    public List supportedMethods() throws XmlRpcException {
        log.debug("mt.supportedMethods()");
        List result = new ArrayList(21);
        
        result.add("blogger.newPost");
        result.add("blogger.editPost");
        result.add("blogger.getPost");
        result.add("blogger.deletePost");
        result.add("blogger.getRecentPosts");
        result.add("blogger.getUsersBlogs");
        result.add("blogger.getUserInfo");
        
        result.add("metaWeblog.newPost");
        result.add("metaWeblog.editPost");
        result.add("metaWeblog.getPost");
        result.add("metaWeblog.deletePost");
        result.add("metaWeblog.getRecentPosts");
        result.add("metaWeblog.getUsersBlogs");
        result.add("metaWeblog.getUserInfo");
        result.add("metaWeblog.getCategories");
        result.add("metaWeblog.newMediaObject");
        
        result.add("mt.getRecentPostTitles");
        result.add("mt.getCategoryList");
        result.add("mt.getPostCategories");
        result.add("mt.getTrackbackPings");
        result.add("mt.setPostCategories");
        result.add("mt.supportedMethods");
        result.add("mt.supportedTextFilters");
        
        return result;
    }
    
    /**
     * Retrieve information about the text formatting plugins supported by 
     * the server. 
     */
    public List supportedTextFilters() throws XmlRpcException {
        log.debug("mt.supportedTextFilters()");
        return new ArrayList(0);
    }
    
    /**
     * Retrieve the list of TrackBack pings posted to a particular entry. 
     * This could be used to programmatically retrieve the list of pings for a 
     * particular entry, then iterate through each of those pings doing the same, 
     * until one has built up a graph of the web of entries referencing one 
     * another on a particular topic. 
     */
    public List getTrackbackPings(
            final String postID)
            throws XmlRpcException {
        log.debug("mt.getTrackbackPings: "+postID);
        
        AbstractAction action = new GetTrackBackPingsAction(postID);
        
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
     * Publish (rebuild) all of the static files related to an entry from your
     * weblog. Equivalent to saving an entry in the system (but without the ping).
     */
    public boolean publishPost(
            final String blogID,
            final String userName,
            final String passWord)
            throws XmlRpcException {
        
        log.debug("mt.publishPost: " +blogID+ ", " +userName+
                ", " +passWord);
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION,
                UNSUPPORTED_EXCEPTION_MSG);
        
    }
}
