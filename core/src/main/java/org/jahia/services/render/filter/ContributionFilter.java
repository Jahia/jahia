package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.scripting.Script;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 * Set contribution template for contentLists set as editable
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class ContributionFilter extends AbstractFilter {
    public String execute(RenderContext context, Resource resource, RenderChain chain) throws Exception {

        JCRNodeWrapper node = resource.getNode();

        try {
            // ugly tests to check whether or not to switch to contribution mode
            if (context.isContributionMode()
                    && node.isNodeType("jnt:contentList")
                    && node.hasProperty("j:editableInContribution")
                    && node.getProperty("j:editableInContribution").getBoolean()
                    && !resource.getTemplateType().equals("edit")
                    && resource.getModuleParams().get("isInclude") == null
                    && context.getRequest().getParameter("ajaxcall") == null) {
                resource.setTemplateType("edit");
                resource.setTemplate("listedit");
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return chain.doFilter(context, resource);
    }


}