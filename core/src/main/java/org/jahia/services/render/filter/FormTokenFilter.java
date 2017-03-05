/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.noggit.ObjectBuilder;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.*;

/**
 * User: toto
 * Date: 1/12/11
 * Time: 19:29
 */
public class FormTokenFilter extends AbstractFilter {

    /**
     * @param previousOut   Result from the previous filter
     * @param renderContext The render context
     * @param resource      The resource to render
     * @param chain         The render chain
     * @return Filtered content
     * @throws Exception
     */
    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        Source source = new Source(previousOut);

        List<StartTag> jahiaFormTags = source.getAllStartTags("jahia:token-form");
        if (!jahiaFormTags.isEmpty()) {
            Map<StartTag, String> m = new HashMap<StartTag, String>();
            for (StartTag formTag : jahiaFormTags) {
                String formid = formTag.getAttributeValue("id");
                Map<String, List<String>> hiddenInputs = (Map<String, List<String>>) ObjectBuilder.fromJSON(formTag.getAttributeValue("forms-data"));
                if (hiddenInputs != null) {
                    String id = UUID.randomUUID().toString();
                    Map<String, Map<String, List<String>>> toks = (Map<String, Map<String, List<String>>>) renderContext.getRequest().getSession().getAttribute("form-tokens");
                    if (toks == null) {
                        toks = new HashMap<String, Map<String, List<String>>>();
                        renderContext.getRequest().getSession().setAttribute("form-tokens", toks);
                    }
                    toks.put(id, hiddenInputs);
                    m.put(formTag, id);
                    renderContext.getRequest().setAttribute("form-" + formid, id);
                }

            }

            OutputDocument outputDocument = new OutputDocument(source);

            Collections.reverse(jahiaFormTags);
            for (StartTag jahiaFormTag : jahiaFormTags) {
                outputDocument.replace(jahiaFormTag, "<input type=\"hidden\" name=\"form-token\" value=\"" + m.get(jahiaFormTag) + "\"/>");
            }

            return outputDocument.toString().trim();
        }

        // we don't have any jahia:token-form so just return what we were given
        return previousOut;
    }
}
