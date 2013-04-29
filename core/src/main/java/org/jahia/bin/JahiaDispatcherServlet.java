package org.jahia.bin;

import org.jahia.registries.ServicesRegistry;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Jahia dispatcher servlet
 * Allows additional mappings by modules
 */
public class JahiaDispatcherServlet extends DispatcherServlet {

    @Override
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        HandlerExecutionChain h =  super.getHandler(request);
        if (h == null) {
            List<HandlerMapping> l = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getSpringHandlerMappings();
            for (HandlerMapping mapping : l) {
                h = mapping.getHandler(request);
                if (h != null) {
                    return h;
                }
            }
        }
        return h;
    }
}
