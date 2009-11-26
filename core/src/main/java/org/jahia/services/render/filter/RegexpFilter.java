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
 *
 * Make replacements of all entries of the regexp table. Examples of use :
 *
 * <bean class="org.jahia.services.render.filter.RegexpFilter" >
 *   <property name="regexp">
 *      <map>
 *         <entry key="(banana)">
 *             <value><![CDATA[<em>fried $1</em>]]></value>
 *         </entry>
 *         <entry key="\[([a-zA-Z]*)\]">
 *             <value><![CDATA[<em>$1</em>]]></value>
 *         </entry>
 *       </map>
 *   </property>
 * </bean>
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
