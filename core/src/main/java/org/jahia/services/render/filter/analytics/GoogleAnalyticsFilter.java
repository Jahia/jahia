/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.analytics;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 25, 2010
 * Time: 2:04:14 PM
 * 
 */
public class GoogleAnalyticsFilter extends AbstractFilter {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GoogleAnalyticsFilter.class);
    public static final String GOOGLE_ANALYTICS_TRACKED_NODES = "gaTrackedNodes";


    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        // add current node to gaTrackedNodes
        List<JCRNodeWrapper> trackedNode = (List<JCRNodeWrapper>) renderContext.getRequest().getAttribute(GOOGLE_ANALYTICS_TRACKED_NODES);

        if (trackedNode == null) {
            trackedNode = new ArrayList<JCRNodeWrapper>();
        }
        if (!trackedNode.contains(resource.getNode())) {
            trackedNode.add(resource.getNode());
        }

        if (resource.getNode().hasProperty("j:gaenabaled") && resource.getNode().getProperty("j:gaenabaled").getBoolean()) {
            renderContext.getRequest().setAttribute(GOOGLE_ANALYTICS_TRACKED_NODES, trackedNode);
        }

        // execute other filters
        return previousOut;
    }
}
