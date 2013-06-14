/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.noggit.JSONParser;
import org.apache.noggit.JSONUtil;
import org.apache.noggit.ObjectBuilder;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

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
        String out = previousOut;

        Source source = new Source(previousOut);
        List<StartTag> jahiaFormTags = source.getAllStartTags("jahia:token-form");
        for (StartTag formTag : jahiaFormTags) {
            String id = formTag.getAttributeValue("id");
            Map<String,List<String>> hiddenInputs = (Map<String, List<String>>) ObjectBuilder.fromJSON(formTag.getAttributeValue("forms-data"));
            Map<String,Map<String,List<String>>>forms = (Map<String, Map<String,List<String>>>) renderContext.getRequest().getAttribute(
                    "form-parameter");
            if (forms == null) {
                forms = new HashMap<String, Map<String, List<String>>>();
                renderContext.getRequest().setAttribute("form-parameter", forms);
            }
            forms.put(id,hiddenInputs);
        }
        source = new Source(out);
        jahiaFormTags = (new Source(out)).getAllStartTags("jahia:token-form");
        OutputDocument outputDocument = new OutputDocument(source);
        for (StartTag segment : jahiaFormTags) {
            outputDocument.replace(segment, "");
        }
        return outputDocument.toString().trim();
    }
}
