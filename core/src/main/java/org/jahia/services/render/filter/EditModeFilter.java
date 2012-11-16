package org.jahia.services.render.filter;


import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ListModule;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.utils.StringResponseWrapper;

import java.util.List;

public class EditModeFilter extends AbstractFilter {

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = super.execute(previousOut, renderContext, resource, chain);
        if (renderContext.getServletPath().endsWith("frame")) {
            Source source = new Source(out);
            OutputDocument document = new OutputDocument(source);
            List<StartTag> tags = source.getAllStartTags("a");
            for (StartTag tag : tags) {
                String href = tag.getAttributeValue("href");
                if (href != null && ((href.startsWith("/") && !href.startsWith(renderContext.getServletPath())) || href.contains("://"))) {
                    if (tag.getAttributeValue("target") == null) {
                        document.insert(tag.getEnd()-1, " target=\"_parent\"");
                    }
                }
            }
            return document.toString();
        }
        return out;
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (!renderContext.getServletPath().endsWith("frame")) {
            StringResponseWrapper wrapper = new StringResponseWrapper(renderContext.getResponse());
            renderContext.getRequest().setAttribute("currentResource", resource);
            renderContext.getRequest().setAttribute("renderContext", renderContext);
            renderContext.getRequest().setAttribute("servletPath",renderContext.getRequest().getServletPath());
            renderContext.getRequest().setAttribute("url",new URLGenerator(renderContext, resource));
            renderContext.getRequest().getRequestDispatcher("/engines/edit.jsp").forward(renderContext.getRequest(), wrapper);
            return wrapper.getString();
        }
        return super.prepare(renderContext, resource, chain);
    }
}
