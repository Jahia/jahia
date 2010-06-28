package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;

/**
 * Traverses the content and searches for URLs in the configured elements.
 * Executes the list of configured visitors to modify the URL value.
 */
public class URLFilter extends AbstractFilter {
    
    /**
     * Initializes an instance of this class.
     * 
     * @param urlTraverser the URL utility class to visit HTML tag attributes
     */
    public URLFilter(HtmlTagAttributeTraverser urlTraverser) {
        super();
        this.urlTraverser = urlTraverser;
    }

    private HtmlTagAttributeTraverser urlTraverser;
    
    private HtmlTagAttributeVisitor[] handlers;

    public  String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (handlers != null && handlers.length > 0) {
            previousOut = urlTraverser.traverse(previousOut, renderContext, resource, handlers);
        }

        return previousOut;
    }

    /**
     * @param visitors the visitors to set
     */
    public void setHandlers(HtmlTagAttributeVisitor... visitors) {
        this.handlers = visitors;
    }


}
