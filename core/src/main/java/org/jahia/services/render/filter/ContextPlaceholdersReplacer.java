package org.jahia.services.render.filter;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.OutputDocument;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;

/**
 * Replaces contextual placeholders in internal links like ##mode## and ##lang## with their actual value.
 *
 * ##mode## is the servlet name followed by the active workspace ( edit/default or render/live )
 * ##lang## is the current language.
 *
 */
public class ContextPlaceholdersReplacer implements HtmlTagAttributeVisitor {

    public static String CURRENT_CONTEXT_PLACEHOLDER = "{mode}";
    public static String LANG_PLACEHOLDER = "{lang}";
    
    public void visit(OutputDocument document, Attribute attr, RenderContext context, Resource resource) {
        String ctx = context.isEditMode() ?  "edit/"+resource.getWorkspace(): "render/"+resource.getWorkspace();

        if (attr != null) {
            String value = attr.getValue();

            value = value.replace(CURRENT_CONTEXT_PLACEHOLDER, ctx);
            value = value.replace(LANG_PLACEHOLDER, resource.getLocale().toString());

            document.replace(attr.getValueSegment(), value);
        }
    }
    
}