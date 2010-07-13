package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;

/**
 * Set contribution template for contentLists set as editable
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class ContributionFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(ContributionFilter.class);

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        try {
            // ugly tests to check whether or not to switch to contribution mode
            if (node.hasProperty("j:editableInContribution")
                    && node.getProperty("j:editableInContribution").getBoolean()
                    && !resource.getTemplateType().equals("edit")
                    && !context.isAjaxRequest()) {
                resource.setTemplateType("edit");
            }
        } catch (RepositoryException e) {
            logger.error(e,e);
        }
        return null;
    }


}