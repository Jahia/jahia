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
package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.GWTStaticAssetEntry;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.bin.Render;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
<<<<<<< .working
import org.jahia.utils.i18n.Messages;
=======
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
>>>>>>> .merge-right.r49694

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:54:41 PM
 */
public class TemplateHelper {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateHelper.class);
    private static final String CSS = "css";
    private static final String JAVASCRIPT = "javascript";

    private RenderService renderService;
    private ChannelService channelService;
    public static final int LIVE = 0;
    public static final int PREVIEW = 1;
    public static final int EDIT = 2;
    
    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
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
     * @param uiLocale
     * @param channelIdentifier
     * @param channelVariant
     */
    public GWTRenderResult getRenderedContent(String path, String template, String configuration,
                                              final Map<String, List<String>> contextParams, boolean editMode, String configName,
                                              HttpServletRequest request, HttpServletResponse response,
                                              JCRSessionWrapper currentUserSession,
                                              Locale uiLocale, String channelIdentifier, String channelVariant) throws GWTJahiaServiceException {
        GWTRenderResult result = null;
        try {
            JCRNodeWrapper node = currentUserSession.getNode(path);

            Resource r = new Resource(node, "html", template, configuration);
            request.setAttribute("mode", "edit");

            request = new HttpServletRequestWrapper(request) {
                @Override
                public String getParameter(String name) {
                    if (contextParams != null && contextParams.containsKey(name)) {
                        return contextParams.get(name).get(0);
                    }
                    return super.getParameter(name);
                }

                @Override
                public Map getParameterMap() {
                    Map r = new HashMap(super.getParameterMap());
                    if (contextParams != null) {
                        for (Map.Entry<String, List<String>> entry : contextParams.entrySet()) {
                            r.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
                        }
                    }
                    return r;
                }

                @Override
                public Enumeration getParameterNames() {
                    return new Vector(getParameterMap().keySet()).elements();
                }

                @Override
                public String[] getParameterValues(String name) {
                    if (contextParams != null && contextParams.containsKey(name)) {
                        List<String> list = contextParams.get(name);
                        return list.toArray(new String[list.size()]);
                    }
                    return super.getParameterValues(name);
                }
            };

            RenderContext renderContext = new RenderContext(request, response, currentUserSession.getUser());
            renderContext.setEditMode(editMode);
            if ("contributemode".equals(configName)) {
                renderContext.setContributionMode(true);
            }
            renderContext.setMainResource(r);

            EditConfiguration editConfiguration = null;
            if (configName != null) {
                editConfiguration= (EditConfiguration) SpringContextSingleton.getBean(configName);
            }
            renderContext.setEditModeConfig(editConfiguration);

            String permission = null;
            if (editConfiguration != null) {
                permission = editConfiguration.getRequiredPermission();
                renderContext.setServletPath(editConfiguration.getDefaultUrlMapping());
            } else {
                renderContext.setServletPath(Render.getRenderServletPath());
            }

            if (permission != null) {
                if (!node.getResolveSite().hasPermission(permission)) {
                    throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.access.denied",uiLocale));
                }
            }

            if (contextParams != null) {
                for (Map.Entry<String, List<String>> entry : contextParams.entrySet()) {
                    r.getModuleParams().put(entry.getKey(), entry.getValue().get(0));
                }
            }

            JCRSiteNode site = node.getResolveSite();
            renderContext.setSite(site);
            if (channelIdentifier != null) {
                Channel activeChannel = channelService.getChannel(channelIdentifier);
                if (activeChannel != null) {
                    renderContext.setChannel(activeChannel);
                }
            }

            response.setCharacterEncoding(SettingsBean.getInstance().getCharacterEncoding());
            String res = renderService.render(r, renderContext);
            Map<String, Map<String,Map<String,String>>> map = (Map<String, Map<String,Map<String,String>>>) renderContext.getRequest().getAttribute("staticAssets");

            if (channelIdentifier != null && !channelIdentifier.equals("generic")) {
                Map<String,Map<String,String>> css  = map.get(CSS);
                SortedMap<String,Map<String,String>> cssWithParam  = new TreeMap<String, Map<String, String>>();
                for (Map.Entry<String, Map<String, String>> entry : css.entrySet()) {
                    String k = entry.getKey() + "?channel="+channelIdentifier+(channelVariant!=null?"&variant="+channelVariant:"");
                    cssWithParam.put(k, entry.getValue());
                }
                map.put(CSS,cssWithParam);
            }


            String constraints = ConstraintsHelper.getConstraints(node);
            if (constraints == null) {
                constraints = "";
            }

            Map<String, List<GWTStaticAssetEntry>> m = new HashMap<String, List<GWTStaticAssetEntry>>();
            if (map != null) {
                  for (Map.Entry<String, Map<String,Map<String,String>>> entry : map.entrySet()) {
                    List<GWTStaticAssetEntry> fileEntries = new ArrayList<GWTStaticAssetEntry>();
                    for (Map.Entry<String,Map<String,String>> filetypeEntries : entry.getValue().entrySet()) {
                        String filePath = filetypeEntries.getKey();
                        Map<String,String> fileOptions = filetypeEntries.getValue();
                        fileEntries.add(new GWTStaticAssetEntry(filePath, fileOptions));
                    }
                    m.put(entry.getKey(), fileEntries);
                }
            }
            result = new GWTRenderResult(res, m, constraints, node.getDisplayableName());
        } catch (PathNotFoundException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.not.found.for.user",uiLocale, path, currentUserSession.getUser().getName()));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.repository.exception.on.path",uiLocale, path));
        } catch (RenderException e) {
            if(e.getCause() instanceof AccessDeniedException ) {
                throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.access.denied",uiLocale));
            } else {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.render.exception",uiLocale, e.getMessage()));
            }
        }
        return result;
    }
    

    public Map<String,Set<String>> getAvailableResources(String moduleName) {
        Map<String, Set<String>> m  = new HashMap<String, Set<String>>();
        m.put(CSS,getAvailableResources(moduleName, CSS, ".css"));
        m.put(JAVASCRIPT, getAvailableResources(moduleName, JAVASCRIPT, ".js"));
        return m;
    }

    public Set<String> getAvailableResources(String moduleName, String type, String ext) {
        Set<String> resources = new HashSet<String>();

        JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(moduleName);

        Set<JahiaTemplatesPackage> packages = new LinkedHashSet<JahiaTemplatesPackage>();
        packages.add(aPackage);
        packages.addAll(aPackage.getDependencies());

        for (JahiaTemplatesPackage pack : packages) {
            if (pack.getSourcesFolder() != null && new File(pack.getSourcesFolder(), "src/main/resources").exists()) {
                File f = new File(pack.getSourcesFolder(), "src/main/resources/" + type);
                if (f.exists() && f.isDirectory()) {
                    for (File r : f.listFiles()) {
                        String resourceName = r.getName();
                        if (resourceName.endsWith(ext)) {
                            resources.add(resourceName);
                        }
                    }
                }
            } else {
                org.springframework.core.io.Resource[] rs = pack.getResources(type);
                for (org.springframework.core.io.Resource r : rs) {
                    String resourceName = r.getFilename();
                    if (resourceName.endsWith(ext)) {
                        resources.add(resourceName);
                    }
                }
            }
        }
        return resources;
    }

}
