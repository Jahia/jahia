/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

/**
 * This filter hides the nodes that are "marked for deletion" nodes in preview mode
 */
public class MarkedForDeletionFilter extends AbstractFilter implements
        ApplicationListener<TemplatePackageRedeployedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MarkedForDeletionFilter.class);

    private String resolvedTemplate;
    private String template;
    private String templateExtension;

    protected String getTemplateContent() throws IOException {
        if (resolvedTemplate == null) {
            resolvedTemplate = WebUtils.getResourceAsString(template);
            if (resolvedTemplate == null) {
                logger.warn("Unable to lookup template at {}", template);
                resolvedTemplate = StringUtils.EMPTY;
            }
        }
        return resolvedTemplate;
    }

    protected String getTemplateOutput(RenderContext renderContext, Resource resource) {
        String out = StringUtils.EMPTY;
        try {
            String template = getTemplateContent();
            resolvedTemplate = null;

            if (StringUtils.isEmpty(template)) {
                return StringUtils.EMPTY;
            }

            ScriptEngine engine = ScriptEngineUtils.getInstance().scriptEngine(templateExtension);
            ScriptContext ctx = new SimpleScriptContext();
            ctx.setWriter(new StringWriter());
            Bindings bindings = engine.createBindings();
            bindings.put("renderContext", renderContext);
            bindings.put("resource", resource);
            final ResourceBundle bundle = JahiaResourceBundle.lookupBundle(
                    JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, renderContext.getUILocale());
            bindings.put("bundle", bundle);
            bindings.put("i18n",
                    LazyMap.decorate(new HashMap<String, String>(2), new Transformer() {
                        public Object transform(Object input) {
                            String value = null;
                            String key = String.valueOf(input);
                            try {
                                value = bundle.getString(key);
                            } catch (MissingResourceException e) {
                                value = key;
                            }
                            return value;
                        }
                    }));

            ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            engine.eval(template, ctx);
            out = ((StringWriter) ctx.getWriter()).getBuffer().toString();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return out;
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        resolvedTemplate = null;
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (resource.getNode().isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION)) {
            if ("page".equals(resource.getContextConfiguration())) {
                return getTemplateOutput(renderContext, resource);
            } else {
                return StringUtils.EMPTY;
            }
        }
        return null;
    }

    public void setTemplate(String template) {
        this.template = template;
        if (template != null) {
            templateExtension = StringUtils.substringAfterLast(template, ".");
        }
    }

}
