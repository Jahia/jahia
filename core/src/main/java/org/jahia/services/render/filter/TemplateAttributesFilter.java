package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.operations.valves.EngineValve;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 18, 2010
 * Time: 3:59:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplateAttributesFilter extends AbstractFilter {

    private static Logger logger = Logger.getLogger(BaseAttributesFilter.class);

    public String execute(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        final HttpServletRequest request = context.getRequest();

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

        overrideProperties(node, params, "jmix:layout");
        overrideProperties(node, params, "jmix:cache");

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            chain.pushAttribute(this, request, entry.getKey(), entry.getValue());
        }

        String out;
        out = chain.doFilter(context, resource);

        return out;
    }

    private void overrideProperties(JCRNodeWrapper node, Map<String, Object> params, String ll) throws RepositoryException {
        ExtendedNodeType layout = NodeTypeRegistry.getInstance().getNodeType(ll);
        ExtendedNodeType[] mixins = layout.getMixinSubtypes();
        for (ExtendedNodeType mixin : mixins) {
            if (node.isNodeType(mixin.getName())) {
                Map<String, ExtendedPropertyDefinition> props = mixin.getDeclaredPropertyDefinitionsAsMap();
                for (String key : props.keySet()) {
                    if (node.hasProperty(key)) {
                        String pkey = StringUtils.substringAfter(key, ":");
                        params.put(pkey, node.getProperty(key).getString());
                    }
                }
            }
        }
    }

}
