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
 package org.jahia.operations;

import org.jahia.services.cache.GroupCacheKey;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 6 janv. 2006
 * Time: 18:06:46
 * To change this template use File | Settings | File Templates.
 */
public class PageState {
        boolean isCacheable;
        GroupCacheKey key;

        public PageState(boolean cacheable, GroupCacheKey key) {
            isCacheable = cacheable;
            this.key = key;
        }

        public boolean isCacheable() {
            return isCacheable;
        }

        public GroupCacheKey getKey() {
            return key;
        }
    }