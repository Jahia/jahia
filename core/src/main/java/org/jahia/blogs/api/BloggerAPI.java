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
 * Blogger 1.0 API interface.
 *
 * Blogger API spec can be found at http://plant.blogger.com/api/index.html
 * See also http://xmlrpc.free-conversant.com/docs/bloggerAPI
 *
 * @author Xavier Lawrence
 */
public interface BloggerAPI {
    
    /**
     * Create a new Post for a given blog. Optionally, will publish the blog
     * after posting.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param blogID Unique identifier of the blog the post will be added to
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     * @param content Content of the post
     * @param publish If true, the blog will be published immediately after the
     *                post is made
     *
     * @return The Post ID as a String object
     * @throws XmlRpcException If something goes wrong
     */
    public String newPost(
            final String appKey,
            final String blogID,
            final String userName,
            final String password,
            final String content,
            final boolean publish)
            throws XmlRpcException;
    
    /**
     * Edits a given post. Optionally, will publish the blog after making the
     * edit.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param postID Unique identifier of the post to be changed
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     * @param content Content of the post
     * @param publish If true, the blog will be published immediately after the
     *                post is made
     *
     * @return True if the edition was sucessful
     * @throws XmlRpcException If something goes wrong
     */
    public boolean editPost(
            final String appKey,
            final String postID,
            final String userName,
            final String password,
            final String content,
            final boolean publish)
            throws XmlRpcException;
    
    
    /**
     * Deletes a given post. Optionally, will publish the blog after making the
     * delete.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
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
            final String appKey,
            final String postID,
            final String userName,
            final String password,
            final boolean publish)
            throws XmlRpcException;
    
    /**
     * Get a particular post for a particular user.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param postID Unique identifier of the post to be retreive
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     *
     * @return The queried post in a Map object
     * @throws XmlRpcException If something goes wrong
     */
    public Map getPost(
            final String appKey,
            final String postID,
            final String userName,
            final String password)
            throws XmlRpcException;
    
    /**
     * Returns the quantity of most recent posts.
     * This method was added to the Blogger 1.0 API via an Email from Evan
     * Williams to the Yahoo Group bloggerDev, see the email message for details
     * http://groups.yahoo.com/group/bloggerDev/message/225
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param blogID Unique identifier of the blog the post will be added to
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     * @param numberOfPosts Number of Posts to Retrieve
     *
     * @return A List containing the posts' information
     * @throws XmlRpcException If something goes wrong
     */
    public List getRecentPosts(
            final String appKey,
            final String blogID,
            final String userName,
            final String password,
            final int numberOfPosts)
            throws XmlRpcException;
    
    /**
     * Edits the main index template of a given blog.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param blogID Unique identifier of the blog the post will be added to
     * @param userName Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param templateData The text for the new template (usually mostly HTML).
     * @param templateType Determines which of the blog's templates is to be set.
     *
     * @return True if the template was correctly set
     * @throws XmlRpcException If something goes wrong
     */
    public boolean setTemplate(
            final String appKey,
            final String blogID,
            final String userName,
            final String password,
            final String templateData,
            final String templateType)
            throws XmlRpcException;
    
    /**
     * Returns the main or archive index template of a given blog.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param blogID Unique identifier of the blog the post will be added to
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     * @param templateType Determines which of the blog's templates will be
     *                     returned. Currently, either "main" or "archiveIndex"
     *
     * @return The template as a String Object
     * @throws XmlRpcException If something goes wrong
     */
    public String getTemplate(
            final String appKey,
            final String blogID,
            final String userName,
            final String password,
            final String templateType)
            throws XmlRpcException;
    
    /**
     * Authenticates a user and returns basic user info (nickName, firstName,
     * lastName, url and email).
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param userName Login of the user to authenticate
     * @param password Password for given userName
     *
     * @return The user info in a Map Object
     * @throws XmlRpcException If something goes wrong
     */
    public Map getUserInfo(
            final String appKey,
            final String userName,
            final String password)
            throws XmlRpcException;
    
    /**
     * Returns information (blogID, blogName and url) of each blog a given user
     * is a member of.
     *
     * @param appKey Unique identifier/passcode of the application sending the post
     * @param userName Login for a user who has permission to post to the blog
     * @param password Password for given userName
     *
     * @return A List of Maps containing the blogs' information
     * @throws XmlRpcException If something goes wrong
     */
    public List getUsersBlogs(
            final String appKey,
            final String userName,
            final String password)
            throws XmlRpcException;
}
