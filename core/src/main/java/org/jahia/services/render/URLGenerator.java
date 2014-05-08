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
package org.jahia.services.render;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.*;
import org.jahia.params.valves.LoginConfig;
import org.jahia.params.valves.LogoutConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Url;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * Main URL generation class. This class is exposed to the template developers to make it easy to them to access basic URLs such as <code>${url.edit}</code>, <code>${url.userProfile}</code>. User: toto Date: Sep 14, 2009 Time: 11:13:37 AM
 */
public class URLGenerator {
    private String base;

    private String live;
    private String edit;
    private String preview;
    private String contribute;
    private String studio;
    private String studioVisual;
    private String find;
    private String initializers;
    private Resource resource;
    private RenderContext context;

    private Map<String, String> languages;

    private Map<String, String> templates;

    private Map<String, String> templateTypes;

    private Map<String, String> bases;

    private String templatesPath;

    private String baseLive;
    private String baseContribute;
    private String baseEdit;
    private String basePreview;
    private String baseUserBoardEdit;
    private String baseUserBoardLive;
    private String baseUserBoardFrameEdit;
    private String baseUserBoardFrameLive;
    private String convert;
    private String myProfile;

    private String server;

    private String login;

    private String logout;

    public URLGenerator(RenderContext context, Resource resource) {
        this.context = context;
        this.resource = resource;
        initURL();
        if (context.getURLGenerator() == null) {
            context.setURLGenerator(this);
        }
    }

    /**
     * Set workspace url as attribute of the current request
     */
    protected void initURL() {
        base = getBase(resource.getLocale().toString());

        final String resourcePath = getResourcePath();

        baseLive = Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale();
        live = baseLive + resourcePath;
        if (!SettingsBean.getInstance().isDistantPublicationServerMode()) {
            baseEdit = "/cms/edit/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
            edit = baseEdit + resourcePath;
            baseContribute = "/cms/contribute/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
            contribute = baseContribute + resourcePath;
        }
        basePreview = Render.getRenderServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        preview = basePreview + resourcePath;
        find = Find.getFindServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        initializers = Initializers.getInitializersServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        convert = DocumentConverter.getPath() + "/" + resource.getWorkspace();
        templatesPath = "/modules";
        baseUserBoardEdit = "/cms/dashboard/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        baseUserBoardLive = "/cms/dashboard/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale();
        baseUserBoardFrameEdit = "/cms/dashboardframe/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        baseUserBoardFrameLive = "/cms/dashboardframe/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale();
    }

    public String getResourcePath() {
        return context.getMainResource().getNode().getPath() + ((!"default".equals(context.getMainResource().getTemplate())) ? "." + context.getMainResource().getTemplate() + "." : ".") + context.getMainResource().getTemplateType();
    }

    public String getContext() {
        return context.getRequest().getContextPath();
    }

    public String getFiles() {
        return "/files/" + resource.getWorkspace();
    }

    public String getFilesPlaceholders() {
        return "/files/{workspace}";
    }

    public String getBase() {
        return base;
    }

    public String getBasePlaceholders() {
        return StringUtils.substringBeforeLast(context.getServletPath(), "/") + "/{mode}/{lang}";
    }

    public String getLive() {
        return live;
    }

    public String getEdit() {
        return edit;
    }

    public String getPreview() {
        return preview;
    }

    public String getContribute() {
        return contribute;
    }

    /**
     * Returns the URL for the current resource in studio mode.
     * 
     * @return the URL for the current resource in studio mode
     */
    public String getStudio() {
        if (studio == null) {
            studio = getStudio(false);
        }
        return studio;
    }
    
    /**
     * Returns the URL for the current resource in specified studio mode.
     * 
     * @param isVisual
     *            if the visual studio mode is used
     * @return the URL for the current resource in specified studio mode
     */
    private String getStudio(boolean isVisual) {
        String mode = isVisual ? "studiovisual" : "studio";
        SettingsBean cfg = SettingsBean.getInstance();
        if (cfg.isDistantPublicationServerMode() || cfg.isProductionMode()) {
            return null;
        }
        String url;
        if (context.getSite() != null && context.getSite().getPath().startsWith("/modules/")) {
            url = "/cms/" + mode + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale()
                    + context.getSite().getPath() + ".html";
        } else {
            url = isVisual ? "/welcome/studiovisualmode" : "/welcome/studiomode";
        }
        return url;
    }

    /**
     * Returns the URL for the current resource in visual studio mode.
     * 
     * @return the URL for the current resource in visual studio mode
     */
    public String getStudioVisual() {
        if (studioVisual == null) {
            studioVisual = getStudio(true);
        }
        return studioVisual;
    }
    
    public String getFind() {
        return find;
    }

    public String getFindPrincipal() {
        return FindPrincipal.getFindPrincipalServletPath();
    }

    public String getLogout() {
        if (logout == null) {
            logout = StringUtils.defaultIfEmpty(
                    LogoutConfig.getInstance().getCustomLogoutUrl(context.getRequest()),
                    Logout.getLogoutServletPath());
        }

        return logout;
    }

    public String getCurrentModule() {
        View view = ((Script) context.getRequest().getAttribute(
                "script")).getView();
        return view.getModule().getRootFolderPath();
//                + (view.getModuleVersion() != null ? "/"
//                + view.getModuleVersion() : StringUtils.EMPTY);
    }

    public String getCurrent() {
        return buildURL(resource.getNode(), resource.getResolvedTemplate(), resource.getTemplateType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLanguages() {
        if (languages == null) {
            languages = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object lang) {
                    return getLanguage((String) lang);
                }
            });
        }

        return languages;
    }

    public String getLanguage(String languageCode) {
        return getContext() + context.getServletPath() + "/" + resource.getWorkspace() + "/" + languageCode + context.getMainResource().getNode().getPath() +
                ("default".equals(resource.getTemplate()) ? "" : "." + resource.getTemplate())
                + ".html";
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplates() {
        if (templates == null) {
            templates = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object template) {
                    return getTemplate((String) template);
                }
            });
        }
        return templates;
    }

    public String getTemplate(String template) {
        return buildURL(resource.getNode(), template, resource.getTemplateType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplateTypes() {
        if (templateTypes == null) {
            templateTypes = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object templateType) {
                    return getTemplateType((String) templateType);
                }
            });
        }
        return templateTypes;
    }

    public String getTemplateType(String templateType) {
        return buildURL(resource.getNode(), resource.getResolvedTemplate(), templateType);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getBases() {
        if (bases == null) {
            bases = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object lang) {
                    return getBase((String) lang);
                }
            });
        }
        return bases;
    }

    public String getBase(String languageCode) {
        return context.getServletPath() + "/" + resource.getWorkspace() + "/" + languageCode;
    }

    /**
     * Returns the path to the templates folder.
     *
     * @return the path to the templates folder
     */
    public String getTemplatesPath() {
        return templatesPath;
    }

    /**
     * Returns the URL of the main resource (normally, page), depending on the current mode.
     *
     * @return the URL of the main resource (normally, page), depending on the current mode
     */
    public String getMainResource() {
        return base + context.getMainResource().getNode().getPath() + "." + context.getMainResource().getResolvedTemplate() + ".html";
    }

    public String buildURL(JCRNodeWrapper node, String template, String templateType) {
    	JCRNodeWrapper usedNode = checkIfTemplateExistsForNode(node, template, templateType);
    	if(usedNode == null) {
    		usedNode = node;
    	}
        return base + usedNode.getPath() + (template != null && !"default".equals(template) ? "." + template : "") + "." + templateType;
    }

    public String buildURL(JCRNodeWrapper node, String languageCode, String template, String templateType) {
    	JCRNodeWrapper usedNode = checkIfTemplateExistsForNode(node, template, templateType);
    	if(usedNode == null) {
    		usedNode = node;
    	}
        if (StringUtils.isEmpty(languageCode)) {
            return buildURL(usedNode, template, templateType);
        }
        return context.getServletPath() + "/" + resource.getWorkspace() + "/" + languageCode + usedNode.getPath() + (template != null && !"default".equals(template) ? "." + template : "") + "." + templateType;
    }
    
    private JCRNodeWrapper checkIfTemplateExistsForNode (JCRNodeWrapper node, String template, String templateType) {
    	
    	try {
  			SortedSet<View> views = RenderService.getInstance().getViewsSet(node.getPrimaryNodeType(), node.getResolveSite(), templateType);
    		for (View view : views) {
    	        final String key = view.getKey();
    	        if ((template != null && !template.equals("default") && key.matches("^.*\\\\." + template + "\\\\..*"))
    	            || ((template == null || template.equals("default")) && key.equals("default"))) {
    	            return node;
    	            }
    	    }
    		if (node.getParent() != null) {
    			return checkIfTemplateExistsForNode(node.getParent(), template, templateType);
    		} else {
    			logger.error("No view " + (template != null && !template.equals("default") ? template : "default") + " for node (or parent nodes) " + node.getPath() + " exists!!");
    			return null;
    		}
    	} catch (RepositoryException ex) {
    		logger.error("Error when checking views for node " + node.getPath(), ex);
    	}
    	return null;
    }

    public String getInitializers() {
        return initializers;
    }

    public String getCaptcha() {
        return Captcha.getCaptchaServletPath();
    }

    public String getBaseContribute() {
        return baseContribute;
    }

    public String getBaseEdit() {
        return baseEdit;
    }

    public String getBaseLive() {
        return baseLive;
    }

    public String getBasePreview() {
        return basePreview;
    }

    public String getConvert() {
        return convert;
    }

    public String getBaseUserBoardEdit() {
        return baseUserBoardEdit;
    }

    public String getBaseUserBoardLive() {
        return baseUserBoardLive;
    }

    public String getBaseUserBoardFrameEdit() {
        return baseUserBoardFrameEdit;
    }

    public String getBaseUserBoardFrameLive() {
        return baseUserBoardFrameLive;
    }

    public String getRealResource() {
        if (context.isAjaxRequest() && context.getAjaxResource() != null) {
            if (context.isEditMode()) {
                return baseEdit + context.getAjaxResource().getNode().getPath() + ".html";
            } else if (context.isContributionMode()) {
                return baseContribute + context.getAjaxResource().getNode().getPath() + ".html";
            } else {
                return (Constants.LIVE_WORKSPACE.equals(
                        context.getAjaxResource().getWorkspace()) ? baseLive : basePreview) + context.getAjaxResource().getNode().getPath() + ".html";
            }
        } else {
            if (context.isEditMode()) {
                if (context.getEditModeConfigName().equals("studiomode")) {
                    return getStudio();
                }
                return getEdit();
            } else {
                return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
            }
        }
    }

    /**
     * Returns the server URL, including scheme, host and port, depending on the current site. The URL is in the form <code><scheme><host>:<port></code>, e.g. <code>http://www.jahia.org:8080</code>. The port is omitted in case of standard
     * HTTP (80) and HTTPS (443) ports.
     * <p/>
     * If the site's server name is configured to be "localhost", then take the servername from the request.
     *
     * @return the server URL, including scheme, host and port, depending on the current site
     */
    public String getServer() {
        if (server == null) {
            StringBuilder url = new StringBuilder();
            String scheme = context.getRequest().getScheme();
            String host = context.getSite().getServerName();
            if (Url.isLocalhost(host)) {
                host = context.getRequest().getServerName();
            }

            int port = SettingsBean.getInstance().getSiteURLPortOverride();

            if (port == 0) {
                port = context.getRequest().getServerPort();
            }

            url.append(scheme).append("://").append(host);

            if (!(port == 80 && "http".equals(scheme) || port == 443 && "https".equals(scheme))) {
                url.append(":").append(port);
            }

            server = url.toString();
        }

        return server;
    }

    public String getMyProfile() {
        if (myProfile == null) {
            JahiaUser user = context.getUser();
            myProfile = (JahiaUserManagerService.isNotGuest(user) && user.isMemberOfGroup(0, JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) ? "/start" : "";
        }
        return myProfile;
    }

    public String getLogin() {
        if (login == null) {
            login = StringUtils.defaultIfEmpty(
                    LoginConfig.getInstance().getCustomLoginUrl(context.getRequest()),
                    Login.getServletPath());
        }

        return login;
    }

    public String getFindUser() {
        return FindUser.getFindUserServletPath();
    }

    public String getFindUsersAndGroups() {
        return FindUsersAndGroups.getFindUsersAndGroupsServletPath();
    }

    public String getFindUsersAndGroupsInAcl() {
        return FindUsersAndGroupsInAcl.getFindUsersAndGroupsInAclServletPath();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("URLGenerator");
        sb.append("{base='").append(base).append('\'');
        sb.append(", live='").append(live).append('\'');
        sb.append(", edit='").append(edit).append('\'');
        sb.append(", preview='").append(preview).append('\'');
        sb.append(", contribute='").append(contribute).append('\'');
        sb.append(", studio='").append(studio).append('\'');
        sb.append(", find='").append(find).append('\'');
        sb.append(", initializers='").append(initializers).append('\'');
        sb.append(", resource=").append(resource);
        sb.append(", context=").append(context);
        sb.append(", languages=").append(languages);
        sb.append(", templates=").append(templates);
        sb.append(", templateTypes=").append(templateTypes);
        sb.append(", templatesPath='").append(templatesPath).append('\'');
        sb.append(", baseLive='").append(baseLive).append('\'');
        sb.append(", baseContribute='").append(baseContribute).append('\'');
        sb.append(", baseEdit='").append(baseEdit).append('\'');
        sb.append(", basePreview='").append(basePreview).append('\'');
        sb.append(", convert='").append(convert).append('\'');
        sb.append(", myProfile='").append(myProfile).append('\'');
        sb.append(", server='").append(server).append('\'');
        sb.append(", login='").append(login).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
