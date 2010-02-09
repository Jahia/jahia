//package org.jahia.services.render.filter;
//
//import java.util.Collections;
//import java.util.List;
//
//import org.jahia.data.templates.JahiaTemplatesPackage;
//import org.jahia.services.render.RenderContext;
//import org.jahia.services.render.Resource;
//import org.jahia.services.render.scripting.Script;
//import org.jahia.services.templates.JahiaTemplateManagerService;
//
///**
// * ModuleFilter
// *
// * Composite filter containing all filters specifics to the module and/or the
// * template to display.
// *
// */
//public class ModuleFilter extends AbstractFilter {
//
//    private JahiaTemplateManagerService templateManagerService;
//
//    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
//        Script script = (Script) renderContext.getRequest().getAttribute("script");
//        List<RenderFilter> filters = Collections.emptyList();
//        if (script != null) {
//            JahiaTemplatesPackage pack = script.getTemplate().getModule();
//            if (pack != null) {
//                filters = templateManagerService.getRenderFiltersForModule(pack.getName());
//            }
//        }
//
//        if (!filters.isEmpty()) {
//            RenderChain adjustedChain = new RenderChain();
//            // add module filters
//            adjustedChain.addFilters(filters);
//            // after module filters, continue with the original chain
//            adjustedChain.addFilter(chain);
//
//            chain = adjustedChain;
//        }
//
//        return chain.doFilter(renderContext, resource);
//    }
//
//    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
//        this.templateManagerService = templateManagerService;
//    }
//}
