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
