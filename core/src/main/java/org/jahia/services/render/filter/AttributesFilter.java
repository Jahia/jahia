package org.jahia.services.render.filter;

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.params.ParamBean;
import org.jahia.data.JahiaData;
import org.jahia.operations.valves.EngineValve;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.PropertyIterator;
import javax.jcr.Property;
import javax.jcr.nodetype.NodeType;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 * Stores the required request parameters before evaluating the template and restores original after. 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class AttributesFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(AttributesFilter.class);

    public String execute(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        final Map<String, Object> old = new HashMap<String, Object>();
        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();


        request.setAttribute("renderContext", context);

        final Script script = service.resolveScript(resource, context);

        pushAttribute(request, "script", script, old);
        pushAttribute(request, "scriptInfo", script.getTemplate().getInfo(), old);
        pushAttribute(request, "currentNode", node, old);
        pushAttribute(request, "workspace", node.getSession().getWorkspace().getName(), old);
        pushAttribute(request, "currentResource", resource, old);
        pushAttribute(request, "url",new URLGenerator(context, resource, service.getStoreService()), old);

        // Resolve params
        Map<String,Object> params = new HashMap<String,Object>();
        Map<String, Object> moduleParams = context.getModuleParams();
        for (Map.Entry<String, Object> entry : moduleParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("forced")) {
                key = StringUtils.uncapitalize(StringUtils.substringAfter(key,"forced"));
                params.put(key, entry.getValue());
            } else if (!moduleParams.containsKey("forced"+ StringUtils.capitalize(key))) {
                params.put(key, entry.getValue());
            }
        }
        PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            Property property = pi.nextProperty();
            NodeType type = property.getDefinition().getDeclaringNodeType();
            if (type.isNodeType("jmix:layout")) {
                String key = StringUtils.substringAfter(property.getName(), ":");
                if (!moduleParams.containsKey("forced"+ StringUtils.capitalize(key))) {
                    params.put(key, property.getString());
                }
            } else if (type.isNodeType("jmix:cache") && property.getName().equals("j:expiration")) {
                pushAttribute(request, "cache.expiration", property.getString(), old);    
            }

        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            pushAttribute(request, entry.getKey(), entry.getValue(), old);
        }

        setJahiaAttributes(request, node, (ParamBean) Jahia.getThreadParamBean());


        String out;
        try {
            out = chain.doFilter(context, resource);
        } finally {
            popAttributes(request, old);
        }

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

    private void pushAttribute(HttpServletRequest request, String key, Object value, Map<String,Object> oldMap) {
        oldMap.put(key, request.getAttribute(key));
        request.setAttribute(key, value);
    }

    private void popAttributes(HttpServletRequest request, Map<String,Object> oldMap) {
        for (Map.Entry<String,Object> entry : oldMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }


}
