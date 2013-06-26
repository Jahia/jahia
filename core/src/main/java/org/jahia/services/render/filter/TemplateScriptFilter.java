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

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.profiler.Profiler;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Stack;

/**
 * TemplateScriptFilter
 * <p/>
 * Execute the template script associated to the current resource.
 * <p/>
 * This is a final filter, subsequent filters will not be chained.
 */
public class TemplateScriptFilter extends AbstractFilter {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateScriptFilter.class);

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Profiler profiler = (Profiler) renderContext.getRequest().getAttribute("profiler");
        if (profiler != null) {
            profiler.start("render template " + resource.getResolvedTemplate());
        }

        HttpServletRequest request = renderContext.getRequest();
        Script script = (Script) request.getAttribute("script");
        renderContext.getResourcesStack().push(resource);
        StringBuffer output = new StringBuffer();
        Stack<StopWatch> stopWatchStack = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Render " + script.getView().getPath() + " for resource: " + resource);
                if(renderContext.getRequest().getAttribute("previousTemplate")!=null) {
                    logger.debug("previousTemplate object for rendering before script: "+((Template) renderContext.getRequest().getAttribute("previousTemplate")).serialize());
                } else {
                    logger.debug("previousTemplate object for rendering before script is null.");
                }
                if(renderContext.getRequest().getAttribute("usedTemplate")!=null) {
                    logger.debug("usedTemplate object for rendering before script: "+((Template) renderContext.getRequest().getAttribute("usedTemplate")).serialize());
                } else {
                    logger.debug("usedTemplate object for rendering before script is null.");
                }
            }
            long start = 0;

            StopWatch stopWatch = null;
            boolean moduleInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("moduleinfo")) && !resource.getNode().isNodeType("jnt:template");

            if (moduleInfo) {
                output.append("\n<fieldset class=\"moduleinfo\"> ");
                start = System.currentTimeMillis();

                stopWatchStack = (Stack<StopWatch>) renderContext.getRequest().getAttribute("stopWatchStack");
                if (stopWatchStack == null) {
                    stopWatchStack = new Stack<StopWatch>();
                    renderContext.getRequest().setAttribute("stopWatchStack",stopWatchStack);
                }
                if (!stopWatchStack.isEmpty()) {
                    stopWatchStack.peek().stop();
                }

                stopWatch = new StopWatch();
                stopWatchStack.push(stopWatch);
                stopWatch.start();
            }
            output.append(script.execute(resource, renderContext));

            if (logger.isDebugEnabled()) {
                if(renderContext.getRequest().getAttribute("previousTemplate")!=null) {
                    logger.debug("Current previousTemplate object for rendering after script: "+((Template) renderContext.getRequest().getAttribute("previousTemplate")).serialize());
                } else {
                    logger.debug("previousTemplate object for rendering after script is null.");
                }
                if(renderContext.getRequest().getAttribute("usedTemplate")!=null) {
                    logger.debug("Current usedTemplate object for rendering after script: "+((Template) renderContext.getRequest().getAttribute("usedTemplate")).serialize());
                } else {
                    logger.debug("usedTemplate object for rendering after script is null.");
                }
            }

            if (moduleInfo) {
                stopWatch.stop();
                output.append("<legend>")
                        .append("<img src=\"")
                        .append(renderContext.getURLGenerator().getContext())
                        .append("/modules/default/images/icons/information.png")
                        .append("\" title=\"").append(script.getView().getInfo()).append(" node : ").append(resource.getNode().getPath())
                        .append(" in total: ").append(System.currentTimeMillis() - start).append( "ms")
                        .append(" , own time: ").append(stopWatch.getTotalTimeMillis()).append( "ms")
                        .append("\"/></legend>");
                output.append("</fieldset> ");
            }
        } finally {
            renderContext.getResourcesStack().pop();

            if (stopWatchStack != null) {
                stopWatchStack.pop();

                if (!stopWatchStack.isEmpty()) {
                    stopWatchStack.peek().start();
                }
            }
        }
        return output.toString().trim();
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain renderChain, Exception e) {
        if (renderContext.isEditMode() && SettingsBean.getInstance().isDevelopmentMode()) {
            return "<pre>"+getExceptionDetails(e)+"</pre>";
        }
        return super.getContentForError(renderContext, resource, renderChain, e);
    }

    private String getExceptionDetails(Throwable ex) {
        StringWriter out = new StringWriter();
        out.append(ex.getMessage()).append("\n");
        ex.printStackTrace(new PrintWriter(out));
        out.append("\n");
        return out.toString();
    }
}