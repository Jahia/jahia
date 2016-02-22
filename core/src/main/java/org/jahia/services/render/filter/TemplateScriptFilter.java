/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.render.View;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.profiler.Profiler;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
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
        StringBuilder output = null;
        String outputString = null;
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
                output = new StringBuilder();
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
            outputString = script.execute(resource, renderContext);

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
                output.append(outputString);                
                stopWatch.stop();
                View view = script.getView();
                output.append("<legend>").append("<img src=\"").append(renderContext.getURLGenerator().getContext())
                        .append("/modules/default/images/icons/information.png").append("\" title=\"Module: ")
                        .append(view.getModule().getId()).append("-").append(view.getModuleVersion()).append(" ")
                        .append(view.getInfo()).append(" node : ").append(resource.getNode().getPath())
                        .append(" in total: ").append(System.currentTimeMillis() - start).append("ms")
                        .append(" , own time: ").append(stopWatch.getTotalTimeMillis()).append("ms")
                        .append("\"/></legend>");
                output.append("</fieldset>");
                
                outputString = output.toString();
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
        return outputString.trim();
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain renderChain, Exception e) {
        if (renderContext.isEditMode() && SettingsBean.getInstance().isDevelopmentMode()) {
            if (!ErrorFileDumper.isShutdown()) {
                try {
                    ErrorFileDumper.dumpToFile(e, renderContext.getRequest());
                } catch (IOException e1) {
                    logger.error("Cannot log error", e1);
                }
            }
            logger.error("Error while rendering content", e);
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