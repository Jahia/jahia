/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
import org.jahia.utils.i18n.ResourceBundles;
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
            final ResourceBundle bundle = ResourceBundles.getInternal(renderContext.getUILocale());
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
