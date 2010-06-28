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
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAnalyticsFilter extends AbstractFilter {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GoogleAnalyticsFilter.class);
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
