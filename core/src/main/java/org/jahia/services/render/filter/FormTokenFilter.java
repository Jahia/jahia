package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 1/12/11
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public class FormTokenFilter extends AbstractFilter {
    public static final String RANDOMTOKEN = "$$$formtoken$$$";

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String s = super.execute(previousOut, renderContext, resource, chain);
        int i = s.indexOf(RANDOMTOKEN);
        if (i > -1) {
            StringBuffer res = new StringBuffer(s);
            while (i > -1) {
                String id = UUID.randomUUID().toString();
                res.replace(i, i + RANDOMTOKEN.length(), id);
                i = res.indexOf(RANDOMTOKEN);

                Set<String> set = (Set<String>) renderContext.getRequest().getSession().getAttribute("form-tokens");
                if (set == null) {
                    set = new HashSet<String>();
                    renderContext.getRequest().getSession().setAttribute("form-tokens", set);
                }
                set.add(id);
            }
            return res.toString();
        }

        return s;
    }

}
