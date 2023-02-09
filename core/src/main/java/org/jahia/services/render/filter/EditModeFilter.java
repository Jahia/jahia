/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
import java.util.Map;

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
        String targetName = "target";
        String editPart = "/edit/";
        String out = super.execute(previousOut, renderContext, resource, chain);
        if (renderContext.getServletPath().endsWith("frame")) {
            Source source = new Source(out);
            OutputDocument document = new OutputDocument(source);
            List<StartTag> tags = source.getAllStartTags("a");
            for (StartTag tag : tags) {
                String href = tag.getAttributeValue("href");
                if (href != null && ((href.startsWith("/") && !href.startsWith(renderContext.getRequest().getContextPath() + renderContext.getServletPath())) || href.contains("://"))) {
                    // Need to distinguish between modes because the filter applies and affects not only editmode
                    if (renderContext.getEditModeConfig().getName().equals("editmode")) {
                        // Rewrite link for edit mode to prevent nested rendering
                        if (href.contains(editPart)) {
                            Map<String, String> replaceMap = new HashMap<>();
                            for (Attribute attr : tag.getAttributes()) {
                                if (attr.getName().equals(targetName)) {
                                    replaceMap.put(targetName, null);
                                } else if (attr.getName().equals("href") && attr.getValue().contains(editPart)) {
                                    String hrefValue = attr.getValue();
                                    hrefValue = hrefValue.replace(editPart, "/editframe/");
                                    replaceMap.put("href", hrefValue);
                                } else {
                                    replaceMap.put(attr.getKey(), attr.getValue());
                                }
                            }
                            document.replace(tag.getAttributes(), replaceMap);
                        } else {
                            String target = tag.getAttributeValue(targetName);
                            if (target == null) {
                                document.insert(tag.getEnd()-1, " target=\"_blank\"");
                            }
                        }
                    } else {
                        String target = tag.getAttributeValue(targetName);
                        if (target == null) {
                            document.insert(tag.getEnd()-1, " target=\"_parent\"");
                        } else if (target.equals("_self")) {
                            HashMap<String, String> replaceMap = new HashMap<>();
                            for (Attribute attribute : tag.getAttributes()) {
                                String key = attribute.getKey();
                                if (targetName.equals(key)) {
                                    replaceMap.put(key, "_parent");
                                } else {
                                    replaceMap.put(key, attribute.getValue());
                                }
                            }
                            document.replace(tag.getAttributes(), replaceMap);
                        }
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
        //Allow access to block modes only for site node (access to site settings)
        if (blockableModes != null && blockableModes.contains(renderContext.getMode())
                && templatePackage.isEditModeBlocked() && !(site.getPath().equals(resource.getNodePath()))) {
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
