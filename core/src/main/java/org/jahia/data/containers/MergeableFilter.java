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
package org.jahia.data.containers;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 12 nov. 2007
 * Time: 13:28:21
 * To change this template use File | Settings | File Templates.
 */
public interface MergeableFilter {

    /**
     * A filter may be capable of merging with another filter instance for optimization.
     *
     * @param filter
     * @return true if the merging is performed, false if no merging was performed.
     *
     */
    public boolean mergeAnd(ContainerFilterInterface filter);

    /**
     * A filter may be capable of merging with another filter instance for optimization.
     *
     * @param filter
     * @return true if the merging is performed, false if no merging was performed.
     *
     */
    public boolean mergeOr(ContainerFilterInterface filter);

}
