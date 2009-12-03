package org.jahia.wiki.filter;

import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.content.JCRNodeWrapper;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 5:24:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class WikiFilter extends AbstractFilter {

    

    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = chain.doFilter(renderContext, resource);

        Pattern p = Pattern.compile("\\[\\[([a-zA-Z0-9_]+)\\]\\]");
        Matcher m = p.matcher(out);

        JCRNodeWrapper page = renderContext.getMainResource().getNode().getParent();

        StringBuffer buf = new StringBuffer(out);

        int offset = 0;

        while (m.find()) {
            String value = m.group(1);
            String replacement;
            m.start();
            m.end();
            if (page.hasNode(value)) {
                replacement = "<a class=\"wikidef\" href=\""+ value + ".html\">" + value + "</a>";
            } else {
                replacement = "<a class=\"wikidef-new\" href=\""+ value + ".html\"> create " + value + "</a>";
            }

            buf.replace(m.start() + offset, m.end() + offset, replacement);
            offset = offset + replacement.length() - (m.end() - m.start());
            System.out.println("--"+value);
        }

        return buf.toString();

    }
}
