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
package org.jahia.services.cache.clusterservice.batch;

import org.jahia.services.cache.clusterservice.ClusterCacheMessage;

/**
 * User: Serge Huber
 * Date: 8 mai 2006
 * Time: 12:35:30
 * Copyright (C) Jahia Inc.
 */
public interface ClusterCacheMessageBatcher {
    void addMessageToBatch(ClusterCacheMessage clusterCacheMessage);
}
