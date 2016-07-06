/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
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
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 *
 * For more information, please visit http://www.jahia.com
 */
package org.jahia.bin;

import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.LinkGenerator;
import org.jahia.services.search.MatchInfo;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * A servlet providing delayed resolution of JCR nodes targeted by links created by {@link LinkGenerator}. This
 * indirection allows {@link org.jahia.services.search.SearchProvider} implementations to avoid resolving JCR nodes
 * when returning results to be displayed. This servlet is configured in
 * {@code servlet-applicationcontext-renderer.xml} and is mapped on {@code /resolve}.
 *
 * @author Christophe Laprun
 */
public class TemplateResolverServlet extends JahiaController {
    private JCRSessionFactory sessionFactory;

    @Override
    public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        final JahiaUser currentUser = sessionFactory.getCurrentUser();
        final RenderContext context = new RenderContext(req, resp, currentUser);
        final String pathInfo = req.getPathInfo();
        final int index = pathInfo.indexOf('/', 1);
        if (index == -1 || index == pathInfo.length() - 1) {
            throw new JahiaBadRequestException("Invalid path");
        }
        final String resolve = pathInfo.substring(0, index);
        context.setServletPath(req.getServletPath() + resolve);

        try {
            // extract match info from URI
            final MatchInfo info = LinkGenerator.decomposeLink(pathInfo);

            // retrieve the path of the displayable node associated with the node identified by the match info
            final String redirect = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(
                    currentUser, info.getWorkspace(), Locale.forLanguageTag(info.getLang()), new JCRCallback<String>() {
                        @Override
                        public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            final JCRNodeWrapper node = session.getNodeByIdentifier(info.getId());
                            final JCRNodeWrapper displayableNode = JCRContentUtils.findDisplayableNode(node, context);
                            return displayableNode != null ? displayableNode.getPath() : null;
                        }
                    });

            // if we have found a displayable node, redirect to it
            if (redirect != null) {
                resp.sendRedirect(req.getContextPath() + Render.getRenderServletPath() + '/' + info.getWorkspace() +
                        '/' + info.getLang() + redirect + ".html");
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        return null;
    }

    public void setJcrSessionFactory(JCRSessionFactory jcrSessionFactory) {
        this.sessionFactory = jcrSessionFactory;
    }
}
