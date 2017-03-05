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


import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.utils.Patterns;
import org.jahia.utils.StringResponseWrapper;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This filter handles edit modes requests.
 */
public class EditModeFilter extends AbstractFilter {

    private List<String> blockableModes;

    /**
     * Handle edit mode frame requests : open external url in new window
     * @param previousOut Result from the previous filter
     * @param renderContext The render context
     * @param resource The resource to render
     * @param chain The render chain
     * @return Filtered content
     * @throws Exception
     */
    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = super.execute(previousOut, renderContext, resource, chain);
        if (renderContext.getServletPath().endsWith("frame")) {
            Source source = new Source(out);
            OutputDocument document = new OutputDocument(source);
            List<StartTag> tags = source.getAllStartTags("a");
            for (StartTag tag : tags) {
                String href = tag.getAttributeValue("href");
                if (href != null && ((href.startsWith("/") && !href.startsWith(renderContext.getRequest().getContextPath() + renderContext.getServletPath())) || href.contains("://"))) {
                    String target = tag.getAttributeValue("target");
                    if (target == null) {
                        document.insert(tag.getEnd()-1, " target=\"_parent\"");
                    } else if (target.equals("_self")) {
                        HashMap<String, String> replaceMap = new HashMap<>();
                        for (Attribute attribute : tag.getAttributes()) {
                            String key = attribute.getKey();
                            if ("target".equals(key)) {
                                replaceMap.put(key, "_parent");
                            } else {
                                replaceMap.put(key, attribute.getValue());
                            }
                        }
                        document.replace(tag.getAttributes(), replaceMap);
                    }
                }
            }
            return document.toString();
        }
        return out;
    }

    /**
     * Handle edit mode requests : dispatch to edit.jsp if edit mode
     *
     * @param renderContext The render context
     * @param resource The resource to render
     * @param chain The render chain
     * @return The content of edit.jsp if in edit mode, null otherwise
     * @throws Exception
     */
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final JCRSiteNode site = resource.getNode().getResolveSite();
        final JahiaTemplatesPackage templatePackage = site.getTemplatePackage();
        if(templatePackage == null) {
            throw new PathNotFoundException("Couldn't find the template associated with site " + site.getName() + ". Please check that all its dependencies are started.");
        }
        if (blockableModes != null && blockableModes.contains(renderContext.getMode())
                && templatePackage.isEditModeBlocked()) {
            throw new AccessDeniedException("This site is not accessible in Edit mode.");
        }

        if (!renderContext.getServletPath().endsWith("frame")) {
            StringResponseWrapper wrapper = new StringResponseWrapper(renderContext.getResponse());
            renderContext.getRequest().setAttribute("currentResource", resource);
            renderContext.getRequest().setAttribute("renderContext", renderContext);
            renderContext.getRequest().setAttribute("servletPath",renderContext.getRequest().getServletPath());
            renderContext.getRequest().setAttribute("url",new URLGenerator(renderContext, resource));
            renderContext.getRequest().getRequestDispatcher("/engines/edit.jsp").forward(renderContext.getRequest(), wrapper);
            return wrapper.getString();
        }
        return super.prepare(renderContext, resource, chain);
    }

    public void setBlockableModes(String blockableModes) {
        this.blockableModes = Arrays.asList(Patterns.COMMA.split(blockableModes));
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain renderChain, Exception e) {
        try {
            StringResponseWrapper wrapper = new StringResponseWrapper(renderContext.getResponse());
            final HttpServletRequest request = renderContext.getRequest();
            request.setAttribute("org.jahia.exception", e);
            request.getRequestDispatcher("/errors/error.jsp").forward(request, wrapper);
            return wrapper.getString();
        } catch (Exception ex) {
            return null;
        }
    }
}
