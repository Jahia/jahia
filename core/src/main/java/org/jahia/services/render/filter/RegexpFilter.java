package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

/**
 * Simple regular expression filter
 */
public class RegexpFilter extends AbstractFilter {
    private Map<String,String> regexp;

    public Map getRegexp() {
        return regexp;
    }

    public void setRegexp(Map regexp) {
        this.regexp = regexp;
    }

    public String doFilter(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException, TemplateNotFoundException {
        String out = chain.doFilter(renderContext, resource);

        if (regexp == null) {
            return out;
        }

        for (String s : regexp.keySet()) {
            Matcher m = Pattern.compile(s).matcher(out);
            out = m.replaceAll(regexp.get(s));
        }

        return out;
    }
}
