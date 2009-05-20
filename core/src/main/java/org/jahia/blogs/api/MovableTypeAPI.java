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

import org.apache.xmlrpc.XmlRpcException;

/**
 * Movable Type adds a couple of other methods (to the MetawebLog API) of its
 * own for manipulating the categories assigned to your entries.
 *
 * @author Xavier Lawrence
 */
public interface MovableTypeAPI extends MetaWeblogAPI {
    
    /**
     * Returns a bandwidth-friendly list of the most recent posts in the system.
     *
     * @param blogID Unique identifier of the blog to get the post titles from
     * @param userName Login for a user who has permission to post to the blog
     * @param passWord Password for given username
     * @param numberOfPosts Number of Post titles to Retrieve
     *
     * @return A List of Maps containing dateCreated, userid, postid
     *         and title
     * @throws XmlRpcException If something goes wrong
     */
    public List getRecentPostTitles(
            final String blogID,
            final String userName,
            final String passWord,
            final int numberOfPosts)
            throws XmlRpcException;
    
    /**
     * Returns a list of all categories defined in the weblog.
     *
     * @param blogID Unique identifier of the blog to get the categories from
     * @param userName Login for a user who has permission to post to the blog
     * @param passWord Password for given username
     *
     * @return A List of Maps containing categoryId and categoryName
     * @throws XmlRpcException If something goes wrong
     */
    public List getCategoryList(
            final String blogID,
            final String userName,
            final String passWord)
            throws XmlRpcException;
    
    /**
     * Returns a list of all categories to which the post is assigned.
     *
     * @param postID Unique identifier of the post to get the categories from
     * @param userName Login for a user who has permission to post to the blog
     * @param passWord Password for given username
     *
     * @return A List of Maps containing categoryId, categoryName and
     *         isPrimary
     * @throws XmlRpcException If something goes wrong
     */
    public List getPostCategories(
            final String postID,
            final String userName,
            final String passWord)
            throws XmlRpcException;
    
    /**
     * Sets the categories for a post.
     *
     * @param postID Unique identifier of the post to set the categories
     * @param userName Login for a user who has permission to post to the blog
     * @param passWord Password for given username
     * @param categories List of Maps containing categoryId and categoryName
     *
     * @return true if the operation was successful
     * @throws XmlRpcException If something goes wrong
     */
    public boolean setPostCategories(
            final String postID,
            final String userName,
            final String passWord,
            final List categories)
            throws XmlRpcException;
    
    /**
     * Retrieve information about the XML-RPC methods supported by the server.
     *
     * @return a List of method names supported by the server
     * @throws XmlRpcException If something goes wrong
     */
    public List supportedMethods() throws XmlRpcException;
    
    /**
     * Retrieve information about the text formatting plugins supported by
     * the server.
     *
     * @return a List of supported Text Filters supported by the server
     * @throws XmlRpcException If something goes wrong
     */
    public List supportedTextFilters() throws XmlRpcException;
    
    /**
     * Retrieve the list of TrackBack pings posted to a particular entry.
     * This could be used to programmatically retrieve the list of pings for a
     * particular entry, then iterate through each of those pings doing the same,
     * until one has built up a graph of the web of entries referencing one
     * another on a particular topic.
     *
     * @param postID Unique identifier of the post to get the TrackBakc Pings from
     *
     * @return a List of Maps containing pingTitle (the title of the
     *         entry sent in the ping), pingURL (the URL of the entry),
     *         and pingIP (the IP address of the host that sent the ping).
     * @throws XmlRpcException If something goes wrong
     */
    public List getTrackbackPings(
            final String postID)
            throws XmlRpcException;
    
    /**
     * Publish (rebuild) all of the static files related to an entry from your
     * weblog. Equivalent to saving an entry in the system (but without the ping).
     *
     * @param blogID Unique identifier of the blog to publish the posts
     * @param userName Login for a user who has permission to post to the blog
     * @param passWord Password for given username
     *
     * @return true if the operation was successful
     * @throws XmlRpcException If something goes wrong
     */
    public boolean publishPost(
            final String blogID,
            final String userName,
            final String passWord)
            throws XmlRpcException;
}
