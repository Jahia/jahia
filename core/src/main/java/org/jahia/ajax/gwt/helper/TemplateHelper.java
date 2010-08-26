/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.bin.Edit;
import org.jahia.bin.Render;
import org.jahia.bin.Studio;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.*;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:54:41 PM
 */
public class TemplateHelper {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TemplateHelper.class);

    private RenderService renderService;
    public static final int LIVE = 0;
    public static final int PREVIEW = 1;
    public static final int EDIT = 2;

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    /**
     * Get rendered content
     *
     * @param path
     * @param template
     * @param configuration
     * @param contextParams
     * @param editMode
     * @param configName
     * @param request
     * @param response
     * @param currentUserSession
     */
    public GWTRenderResult getRenderedContent(String path, String template, String configuration,
                                              Map<String, String> contextParams, boolean editMode, String configName,
                                              HttpServletRequest request, HttpServletResponse response,
                                              JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        GWTRenderResult result = null;
        try {
            JCRNodeWrapper node = currentUserSession.getNode(path);
            Resource r = new Resource(node, "html", template, configuration);
            request.setAttribute("mode", "edit");
            RenderContext renderContext = new RenderContext(request, response, currentUserSession.getUser());
            renderContext.setEditMode(editMode);
            renderContext.setEditModeConfigName(configName);
            renderContext.setMainResource(r);
            if (Edit.EDIT_MODE.equals(configName)) {
                renderContext.setServletPath(Edit.getEditServletPath());
            } else if (Studio.STUDIO_MODE.equals(configName)) {
                renderContext.setServletPath(Studio.getStudioServletPath());
            } else {
                renderContext.setServletPath(Render.getRenderServletPath());
            }

            if (contextParams != null) {
                for (Map.Entry<String, String> entry : contextParams.entrySet()) {
                    r.getModuleParams().put(entry.getKey(), entry.getValue());
                }
            }

            JCRSiteNode site = node.getResolveSite();
            renderContext.setSite(site);
            String res = renderService.render(r, renderContext);
            Map<String, Set<String>> map = renderContext.getStaticAssets();
            Set<String> stringSet = map.get("css");
            List<String> copy = new ArrayList<String>();
            copy.addAll(stringSet);
            Collections.reverse(copy);
            stringSet = new LinkedHashSet<String>();
            stringSet.addAll(copy);
            map.put("css",stringSet);
            result = new GWTRenderResult(res, new HashMap<String, Set<String>>(map));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (RenderException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public List<String[]> getTemplatesSet(String path, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        List<String[]> templatesPath = new ArrayList<String[]>();
        try {
            JCRNodeWrapper node = currentUserSession.getNode(path);
            String def = null;
            if (node.hasProperty("j:template")) {
                templatesPath.add(new String[]{"--unset--", "--unset--"});
                def = node.getProperty("j:template").getString();
            }

            SortedSet<Template> set = getTemplatesSet(node);
            for (Template s : set) {
                String tpl;
                if (s.getModule() == null) {
                    tpl = "Default";
                } else {
                    tpl = s.getModule().getName();
                }
                if (s.getKey().equals(def)) {
                    templatesPath.add(new String[]{s.getKey(), "* " + s.getKey() + " (" + tpl + ")"});
                } else {
                    templatesPath.add(new String[]{s.getKey(), s.getKey() + " (" + tpl + ")"});
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return templatesPath;
    }

    /**
     * Get node url depending
     *
     * @param request
     * @param response
     * @param currentUserSession
     * @return
     */
    public String getNodeURL(String path, int mode, HttpServletRequest request, HttpServletResponse response,
                             JCRSessionWrapper currentUserSession) {
        return getNodeURL(path, null, mode, request, response, currentUserSession);
    }

    /**
     * Get node url depending
     *
     * @param request
     * @param response
     * @param currentUserSession
     * @return
     */
    public String getNodeURL(String path, String versionNumber, int mode, HttpServletRequest request,
                             HttpServletResponse response, JCRSessionWrapper currentUserSession) {
        try {
            final JCRSessionWrapper session = currentUserSession;
            final JCRNodeWrapper node = session.getNode(path);
            final Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
            request.setAttribute("mode", "edit");
            final RenderContext renderContext = new RenderContext(request, response, currentUserSession.getUser());

            JCRSiteNode site = node.getResolveSite();
            renderContext.setSite(site);

            if (mode == EDIT) {
                renderContext.setEditMode(true);
            } else {
                renderContext.setEditMode(false);
            }
            renderContext.setMainResource(resource);

            final URLGenerator urlGenerator = new URLGenerator(renderContext, resource);
            if (mode == LIVE) {
                return urlGenerator.getLive(versionNumber);
            } else if (mode == PREVIEW) {
                return urlGenerator.getPreview(versionNumber);
            } else {
                return urlGenerator.getEdit(versionNumber);
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
            return "";
        }
    }

    /**
     * Get template set
     *
     * @param node
     * @return
     * @throws RepositoryException
     */
    public SortedSet<Template> getTemplatesSet(JCRNodeWrapper node) throws RepositoryException {
        ExtendedNodeType nt = node.getPrimaryNodeType();
        SortedSet<Template> set;
        SortedSet<Template> result = new TreeSet<Template>();
//        if (node.getPrimaryNodeTypeName().equals("jnt:nodeReference")) {
//
//            set = getTemplatesSet((JCRNodeWrapper) node.getProperty("j:node").getNode());
//        } else if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
//            set = renderService.getTemplatesSet(nt);
//            List<JCRNodeWrapper> l = node.getChildren();
//            for (JCRNodeWrapper c : l) {
//                set.addAll(getTemplatesSet(c));
//            }
//        } else {
        set = renderService.getTemplatesSet(nt);
//        }
        for (Template template : set) {
            final String key = template.getKey();
            if (!key.startsWith("wrapper.") && !key.startsWith("skins.") && !key.startsWith("debug.") &&
                    !key.matches("^.*\\\\.hidden\\\\..*")) {
                result.add(template);
            }
        }

        return result;
    }
}
