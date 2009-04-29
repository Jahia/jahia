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
 package org.jahia.services.acl;

/**
 * This interface allows pluggeable ACL parent lookup functionality. This
 * interface is used when resolving permissions to navigate to an object's
 * parent object in a graph or hierachy. Various implementations of this
 * interface can offer various ways of navigating "up" an object hierarchy or
 * graph.
 * @author Serge Huber.
 * @version 1.0
 */
public interface ParentACLFinder {

    /**
     * Return the parent object of the object passed in parameter.
     * @param aclResource ACLResourceInterface
     * @return ACLResourceInterface
     */
    public ACLResourceInterface getParent(ACLResourceInterface aclResource);

}
