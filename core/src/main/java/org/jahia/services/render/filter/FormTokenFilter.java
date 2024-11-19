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
