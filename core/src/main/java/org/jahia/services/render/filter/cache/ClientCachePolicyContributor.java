package org.jahia.services.render.filter.cache;/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * A client cache policy contributor can be used to provide a specific client cache policy.
 * It can be integrated in the CacheKeyPartGenerator to be evaluated during the cache key generation and integrated in the fragment
 * client cache policy.
 *
 * @author Jerome Blanchard
 */
public interface ClientCachePolicyContributor {

    Logger LOGGER = LoggerFactory.getLogger(ClientCachePolicyContributor.class);

    /**
     * Determine the cache client cache policy.
     *
     * @param resource, the resource to be rendered
     * @param renderContext, the render context
     * @param properties, the node properties
     * @param key, the cache key part value
     * @return The client cache policy
     */
    default ClientCachePolicy getClientCachePolicy(Resource resource, RenderContext renderContext, Properties properties, String key) {
        LOGGER.info("Default usage of ClientCachePolicyContributor.getClientCachePolicy() in class {}, "
                + "please implements interface to ensure best client cache policy optimization", this.getClass().getName());
        return ClientCachePolicy.PRIVATE;
    }

}
