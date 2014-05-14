/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.utils.Patterns;
import org.jahia.utils.StringResponseWrapper;

import javax.jcr.AccessDeniedException;
import java.util.Arrays;
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
                    if (tag.getAttributeValue("target") == null) {
                        document.insert(tag.getEnd()-1, " target=\"_parent\"");
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
        if (blockableModes != null && blockableModes.contains(renderContext.getMode())
                && resource.getNode().getResolveSite().getTemplatePackage().isEditModeBlocked()) {
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
}
