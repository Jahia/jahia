package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
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
        Map<String, Object> moduleParams = resource.getModuleParams();
        for (Map.Entry<String, Object> entry : moduleParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("forced")) {
                key = StringUtils.uncapitalize(StringUtils.substringAfter(key,"forced"));
                params.put(key, entry.getValue());
            } else if (!moduleParams.containsKey("forced"+ StringUtils.capitalize(key))) {
                params.put(key, entry.getValue());
            }
        }

        ExtendedNodeType layout = NodeTypeRegistry.getInstance().getNodeType("jmix:layout");
        ExtendedNodeType[] mixins = layout.getMixinSubtypes();
        for (ExtendedNodeType mixin : mixins) {
            overrideProperties(node, params, moduleParams, mixin);
        }
        ExtendedNodeType cache = NodeTypeRegistry.getInstance().getNodeType("jmix:cache");
        overrideProperties(node, params, moduleParams, cache);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            chain.pushAttribute(request, entry.getKey(), entry.getValue());
        }

        Script script = (Script) request.getAttribute("script");
        chain.pushAttribute(context.getRequest(), Config.FMT_LOCALIZATION_CONTEXT + ".request",
                new LocalizationContext(new JahiaResourceBundle(resource.getLocale(), script.getTemplate().getModule().getName()), resource.getLocale()));

        String out;
        out = chain.doFilter(context, resource);

        return out;
    }

    private void overrideProperties(JCRNodeWrapper node, Map<String, Object> params, Map<String, Object> moduleParams, ExtendedNodeType mixin) throws RepositoryException {
        Map<String, ExtendedPropertyDefinition> props = mixin.getDeclaredPropertyDefinitionsAsMap();
        for (String key : props.keySet()) {
            String pkey = StringUtils.substringAfter(key, ":");
            if (!moduleParams.containsKey("forced"+ StringUtils.capitalize(pkey))) {
                if (node.isNodeType(mixin.getName()) && node.hasProperty(key)) {
                    params.put(pkey, node.getProperty(key).getString());
                } else {
                    params.put(pkey, null);
                }
            }
        }
    }

}
