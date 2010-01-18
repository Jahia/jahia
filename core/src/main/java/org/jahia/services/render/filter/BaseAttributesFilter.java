package org.jahia.services.render.filter;

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.params.ParamBean;
import org.jahia.data.JahiaData;
import org.jahia.operations.valves.EngineValve;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores the required request parameters before evaluating the template and restores original after. 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class BaseAttributesFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(BaseAttributesFilter.class);

    public String execute(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();

        request.setAttribute("renderContext", context);

        final Script script = service.resolveScript(resource, context);

        chain.pushAttribute(request, "script", script);
        chain.pushAttribute(request, "scriptInfo", script.getTemplate().getInfo());
        chain.pushAttribute(request, "currentNode", node);
        chain.pushAttribute(request, "workspace", node.getSession().getWorkspace().getName());
        chain.pushAttribute(request, "currentResource", resource);
        chain.pushAttribute(request, "url",new URLGenerator(context, resource, service.getStoreService()));

        setJahiaAttributes(request, node, (ParamBean) Jahia.getThreadParamBean());

        String out;
        out = chain.doFilter(context, resource);

        return out;
    }

    /**
     * This set Jahia context attributes, so that legacy jahia tags can still be used in the templates
     * @param request Request where the attributes will be set
     * @param node Node to display
     * @param threadParamBean The "param bean"
     */
    private void setJahiaAttributes(HttpServletRequest request, JCRNodeWrapper node, ParamBean threadParamBean) {
        try {
            if (request.getAttribute(JahiaData.JAHIA_DATA) == null) {
                request.setAttribute(JahiaData.JAHIA_DATA,new JahiaData(threadParamBean, false));
            }
            if (request.getAttribute("jahia") == null) {
                // expose beans into the request scope
                EngineValve.setContentAccessBeans(threadParamBean);
            }

        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }


}
