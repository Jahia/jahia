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

import org.apache.commons.text.StringEscapeUtils;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.bin.errors.StackTraceFilter;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.render.scripting.Script;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.profiler.Profiler;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Stack;
import java.util.UUID;

/**
 * TemplateScriptFilter
 * <p/>
 * Execute the template script associated to the current resource.
 * <p/>
 * This is a final filter, subsequent filters will not be chained.
 */
public class TemplateScriptFilter extends AbstractFilter {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateScriptFilter.class);

    private static final String RENDERING_FAILURE_VIEW_KEY = "jahiaTemplateScriptFilterRenderingFailure";

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Profiler profiler = (Profiler) renderContext.getRequest().getAttribute("profiler");
        if (profiler != null) {
            profiler.start("render template " + resource.getResolvedTemplate());
        }

        HttpServletRequest request = renderContext.getRequest();
        Script script = (Script) request.getAttribute("script");


        /*
            TODO BACKLOG-6561: used for retro compatibility for AggregateCacheFilter
                the stack of resources is now handle internally by the AggregateFilter
                we need to remove this line the day we stop supporting the AggregateCacheFilter implementation
         */
        renderContext.getResourcesStack().push(resource);

        StringBuilder output = null;
        String outputString = null;
        Stack<StopWatch> stopWatchStack = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Render {} for resource: {}", script.getView().getPath(), resource);
                if(renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_RESOLVED_TEMPLATE)!=null) {
                    logger.debug("previousTemplate object for rendering before script: {}",
                            ((Template) renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_RESOLVED_TEMPLATE)).serialize());
                } else {
                    logger.debug("previousTemplate object for rendering before script is null.");
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
            boolean skipAggregation = false;
            if(!AggregateCacheFilter.skipAggregation(request) && Boolean.valueOf(script.getView().getProperties().getProperty("skip.aggregation"))) {
                skipAggregation = true;
                request.setAttribute(AggregateCacheFilter.SKIP_AGGREGATION, true);
                resource.getRegexpDependencies().add(resource.getNodePath() + "/.*");
            }

            outputString = script.execute(resource, renderContext);

            if(skipAggregation) {
                request.removeAttribute(AggregateCacheFilter.SKIP_AGGREGATION);
            }

            if (logger.isDebugEnabled()) {
                if(renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_RESOLVED_TEMPLATE)!=null) {
                    logger.debug("Current previousTemplate object for rendering after script: {}",
                            ((Template) renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_RESOLVED_TEMPLATE)).serialize());
                } else {
                    logger.debug("previousTemplate object for rendering after script is null.");
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
            /*
                TODO BACKLOG-6561: used for retro compatibility for AggregateCacheFilter
                    the stack of resources is now handle internally by the AggregateFilter
                    we need to remove this line the day we stop supporting the AggregateCacheFilter implementation
             */
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
        if (renderContext.isEditMode()) {
            if (!ErrorFileDumper.isShutdown()) {
                try {
                    ErrorFileDumper.dumpToFile(e, renderContext.getRequest());
                } catch (IOException errorDumpingException) {
                    logger.error("Cannot dump error correctly", errorDumpingException);
                }
            }

            String printedError = getExceptionDetails(e);
            Script originalScript = (Script) renderContext.getRequest().getAttribute("script");
            String originalViewPath = null;
            String originalViewKey = null;
            View originalView = originalScript != null ? originalScript.getView() : null;
            if (originalView != null) {
                originalViewPath = originalView.getPath();
                originalViewKey = originalView.getKey();
                if (RENDERING_FAILURE_VIEW_KEY.equals(originalViewKey)) {
                    // avoid infinite loop, the error is happening when rendering the error view.
                    return printedError;
                }
            }

            // try to render the error using the error view
            Resource errorResource = new Resource(resource.getNode(), "html", RENDERING_FAILURE_VIEW_KEY, "module");
            try {
                Script errorScript = errorResource.getScript(renderContext);
                View errorView = errorScript != null ? errorScript.getView() : null;
                if (errorView != null && RENDERING_FAILURE_VIEW_KEY.equals(errorView.getKey())) {
                    if (SettingsBean.getInstance().isDevelopmentMode()) {
                        errorResource.getModuleParams().put("error", e);
                        errorResource.getModuleParams().put("errorId", UUID.randomUUID());
                        errorResource.getModuleParams().put("printedError", printedError);
                    }
                    errorResource.getModuleParams().put("originalViewPath", originalViewPath);
                    errorResource.getModuleParams().put("originalViewKey", originalViewKey);
                    return service.render(errorResource, renderContext);
                }
            } catch (Exception errorRenderingException) {
                // fallback to default error rendering
                logger.debug("Cannot render error correctly, original StackTrace will be rendered", errorRenderingException);
                return printedError;
            }

            // fallback to default error rendering
            return printedError;
        }

        return super.getContentForError(renderContext, resource, renderChain, e);
    }

    private String getExceptionDetails(Throwable ex) {
        StringWriter out = new StringWriter();
        out.append(ex.getMessage()).append("\n");
        out.append(StackTraceFilter.printStackTrace(ex));
        out.append("\n");
        return "<pre>" + StringEscapeUtils.escapeHtml4(out.toString()) + "</pre>";
    }
}
