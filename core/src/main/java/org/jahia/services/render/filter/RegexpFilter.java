/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter;

import net.htmlparser.jericho.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

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


    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (regexp == null) {
            return previousOut;
        }

        if (!contentOnly) {
            for (Map.Entry<Pattern, String> replacement : regexp.entrySet()) {
                previousOut = replacement.getKey().matcher(previousOut).replaceAll(replacement.getValue());
            }
            return previousOut;
        } else {
            StringBuilder buffer = new StringBuilder(previousOut);
            Source source = new Source(previousOut);
            List<Tag> tags = source.getAllTags();
            int offset = 0;
            for (Tag tag : tags) {
                int begin = tag.getEnd() + offset;
                int end = tag.getNextTag() != null ? tag.getNextTag().getBegin() + offset : previousOut.length() + offset - 1;

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
