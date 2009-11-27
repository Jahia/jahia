package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.bin.Edit;
import org.jahia.bin.Render;
import net.htmlparser.jericho.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 27, 2009
 * Time: 5:39:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class URLFilter extends AbstractFilter {
    public static String CURRENT_CONTEXT_PLACEHOLDER = "##current-context##";

    //    public static String DOC_CONTEXT_PLACEHOLDER = "##doc-context##";

    public static String LANG_PLACEHOLDER = "##lang##";
    public static String[][] TAG_ATTRIBUTE_PAIR = {
            {HTMLElementName.A, "href"},
            {HTMLElementName.IMG, "src"},
            {HTMLElementName.PARAM, "value"}
    };

    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = chain.doFilter(renderContext, resource);

        Source source = new Source(out);
        OutputDocument document = new OutputDocument(source);

        for (String[] tagAttrPair : URLFilter.TAG_ATTRIBUTE_PAIR) {
            List<StartTag> tags = source.getAllStartTags(tagAttrPair[0]);
            for (StartTag startTag : tags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute attribute = attributes.get(tagAttrPair[1]);
                replacePlaceholders(document, attribute, renderContext, resource);
            }
        }

        return document.toString();
    }


    void replacePlaceholders(OutputDocument document, Attribute attr, RenderContext context, Resource resource) {
        String ctx;
        if (context.isEditMode()) {
            ctx = "edit/"+resource.getWorkspace();
        } else {
            ctx = "render/"+resource.getWorkspace();
        }

        String value = attr.getValue();

        value = value.replace(CURRENT_CONTEXT_PLACEHOLDER, ctx);
        value = value.replace(LANG_PLACEHOLDER, resource.getLocale().toString());

        document.replace(attr.getValueSegment(), value);
    }


}
