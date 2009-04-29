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
package org.jahia.blogs.api;

import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

/**
 * MetaWeblogAPI 1.0 API interface
 *
 * MetaWeblog API spec can be found at http://www.xmlrpc.com/metaWeblogApi
 *
 * @author Xavier Lawrence
 */
public interface MetaWeblogAPI extends BloggerAPI {
    
    /**
     * Authenticates a user and returns the categories available in the website
     *
     * @param blogID Unique identifier of the blog to get the categories from
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given username
     *
     * @return A Map containing the Categories
     * @throws XmlRpcException If something goes wrong
     */
    public Map getCategories(
            final String blogID,
            final String userName,
            final String password)
            throws XmlRpcException;
    
    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog 
     * after making the post
     *
     * @param blogID Unique identifier of the blog the post will be added to
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the 
     *                post is made
     *
     * @return The Post ID as a String object
     * @throws XmlRpcException If something goes wrong
     */
    public String newPost(
            final String blogID,
            final String userName,
            final String password,
            final Map struct,
            final boolean publish)
            throws XmlRpcException;
    
    /**
     * Edits a given post. Optionally, will publish the blog after making the 
     * edit.
     *
     * @param postID Unique identifier of the post to be changed
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the 
     *                post is made
     *
     * @return True if the edition was successful
     * @throws XmlRpcException If something goes wrong
     */
    public boolean editPost(
            final String postID,
            final String userName,
            final String password,
            final Map struct,
            final boolean publish)
            throws XmlRpcException;
    
    /**
     * Returns a specified post.
     *
     * @param postID Unique identifier of the post to be retrieved
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given username
     *
     * @return The post data in a Map
     * @throws XmlRpcException If something goes wrong
     */
    public Map getPost(
            final String postID,
            final String userName,
            final String password)
            throws XmlRpcException;
    
    /**
     * Deletes a given post. Optionally, will publish the blog after making the
     * delete.
     *
     * @param postID Unique identifier of the post to be removed
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     * @param publish If true, the blog will be published immediately after the
     *                post is made
     *
     * @return True if the edition was sucessful
     * @throws XmlRpcException If something goes wrong
     */
    public boolean deletePost(
            final String postID,
            final String userName,
            final String password,
            final boolean publish)
            throws XmlRpcException;
    
    /**
     * Allows user to post a binary object, a file, to the server. If the file is
     * allowed by the file-upload settings, then the file will be placed in the 
     * user's upload diretory.
     *
     * @param blogID Unique identifier of the blog the object will be added to
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given username
     * @param struct Contents of the object
     *
     * @return Returns a struct, which must contain at least one element, url, 
     * which is the url through which the object can be accessed. It must be 
     * either an FTP or HTTP url.
     * @throws XmlRpcException If something goes wrong
     */
    public Map newMediaObject(
            final String blogID,
            final String userName,
            final String password,
            final Map struct)
            throws XmlRpcException;
    
    /**
     * Get a list of recent posts for a category
     *
     * @param blogID Unique identifier of the blog to get posts from
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     *
     * @return A List of Maps containing the posts' data
     * @throws XmlRpcException If something goes wrong
     */
    public List getRecentPosts(
            final String blogID,
            final String userName,
            final String password,
            final int numposts)
            throws XmlRpcException;
}
