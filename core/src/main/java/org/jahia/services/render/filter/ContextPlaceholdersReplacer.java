package org.jahia.services.render.filter;

import java.util.regex.Pattern;

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
    
    private static Pattern CTX_PATTERN = Pattern.compile(CURRENT_CONTEXT_PLACEHOLDER, Pattern.LITERAL);
    private static Pattern LANG_PATTERN = Pattern.compile(LANG_PLACEHOLDER, Pattern.LITERAL);
    
    public String visit(String value, RenderContext context, Resource resource) {
        if (value != null) {
            String contextPath = null;
            if(context.isEditMode()){
               contextPath = "edit";
            } else if(context.isContributionMode()){
               contextPath = "contribute";
            } else{
               contextPath = "render";
            }
            value = LANG_PATTERN.matcher(
                    CTX_PATTERN.matcher(value).replaceAll(contextPath+"/"+resource.getWorkspace())).replaceAll(resource.getLocale().toString());
        }
        
        return value;
    }
    
}