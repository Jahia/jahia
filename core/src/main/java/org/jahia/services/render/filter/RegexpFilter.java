package org.jahia.services.render.filter;

import net.htmlparser.jericho.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.NodeIterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    private Map<Pattern, String> regexp;
    private boolean contentOnly;

    public void setRegexp(Map<String, String> regexp) {
        if (!regexp.isEmpty()) {
            // compile patterns
            this.regexp = new LinkedHashMap<Pattern, String>();
            for (Map.Entry<String, String> replacement : regexp.entrySet()) {
                this.regexp.put(Pattern.compile(replacement.getKey()), replacement.getValue());
            }
        }
    }

    public void setContentOnly(boolean contentOnly) {
        this.contentOnly = contentOnly;
    }

    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = chain.doFilter(renderContext, resource);

        if (regexp == null) {
            return out;
        }

        if (!contentOnly) {
            for (Map.Entry<Pattern, String> replacement : regexp.entrySet()) {
                out = replacement.getKey().matcher(out).replaceAll(replacement.getValue());
            }
            return out;
        } else {
            StringBuffer buffer = new StringBuffer(out);
            Source source = new Source(out);
            List<Tag> tags = source.getAllTags();
            int offset = 0;
            for (Tag tag : tags) {
                int begin = tag.getEnd() + offset;
                int end = tag.getNextTag() != null ? tag.getNextTag().getBegin() + offset : out.length() + offset - 1;

                String repl = buffer.substring(begin, end);
                for (Map.Entry<Pattern, String> replacement : regexp.entrySet()) {
                     repl = replacement.getKey().matcher(repl).replaceAll(replacement.getValue());
                }

                buffer.replace(begin, end, repl);
                offset += repl.length() - (end-begin);
            }
            return buffer.toString();
        }
    }

}
