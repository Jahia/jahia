/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
