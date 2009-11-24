package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Script;
import org.slf4j.profiler.Profiler;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * MetricsLoggingFilter
 *
 * Calls the logging service to log the display of a resource.
 * Also initializes profiling information.
 */
public class MetricsLoggingFilter extends AbstractFilter {
    private MetricsLoggingService loggingService;

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public String doFilter(RenderContext context, Resource resource, RenderChain chain) throws IOException, RepositoryException {
        JCRNodeWrapper node = resource.getNode();

        String profilerName = "render module " + node.getPath();
        Profiler profiler = loggingService.createNestedProfiler("MAIN", profilerName);
        profiler.start("render filters for "+node.getPath());
        context.getRequest().setAttribute("profiler", profiler);
        String output = chain.doFilter(context, resource);

        Script script = (Script) context.getRequest().getAttribute("script");

        loggingService.logContentEvent(context.getUser().getName(),context.getRequest().getRemoteAddr(),node.getPath(),node.getNodeTypes().get(0),"moduleViewed",script.getTemplate().getDisplayName());

        loggingService.stopNestedProfiler("MAIN", profilerName);

        return output;
    }

}
