package org.jahia.modules.contribute.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

/**
 * Adds the contribution toolbar after body tag
 */
public class ContributeToolbarFilter extends AbstractFilter {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContributeToolbarFilter.class);

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        StringBuffer out = new StringBuffer(previousOut);
        if (renderContext.isContributionMode() && !renderContext.isAjaxRequest()) {
            final String toolbarBegin = service.render(new Resource(resource.getNode(), "html",
                    "html.contribute.toolbar", Resource.CONFIGURATION_INCLUDE), renderContext);
            out.insert(out.indexOf(">", out.indexOf("<body"))+1,toolbarBegin);

            final String toolbarEnd = service.render(new Resource(resource.getNode(), "html",
                    "html.contribute.toolbar.end", Resource.CONFIGURATION_INCLUDE), renderContext);
            out.insert(out.indexOf("</body>"),toolbarEnd);
        }

        return out.toString();
    }

}
