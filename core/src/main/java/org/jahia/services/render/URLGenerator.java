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
package org.jahia.services.render;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.*;
import org.jahia.params.valves.LoginConfig;
import org.jahia.params.valves.LogoutConfig;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Url;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Main URL generation class. This class is exposed to the template developers to make it easy to them to access basic URLs such as <code>${url.edit}</code>, <code>${url.userProfile}</code>. User: toto Date: Sep 14, 2009 Time: 11:13:37 AM
 */
public class URLGenerator {
    private static final String SLASH_LIVE_SLASH = "/" + Constants.LIVE_WORKSPACE + "/";
    private static final String SLASH_EDIT_SLASH = "/" + Constants.EDIT_WORKSPACE + "/";
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

    private String languageCode;

    public URLGenerator(RenderContext context, Resource resource) {
        this.context = context;
        this.resource = resource;
        initURL();
        if (context.getURLGenerator() == null) {
            context.setURLGenerator(this);
        }
    }

    public boolean uses(Resource resource) {
        return this.resource.equals(resource);
    }

    /**
     * Set workspace url as attribute of the current request
     */
    protected void initURL() {
        languageCode = resource.getLocale().toString();
        base = getBase(languageCode);

        final String resourcePath = getResourcePath();

        final String renderServletPath = Render.getRenderServletPath();
        final String liveLanguage = SLASH_LIVE_SLASH + languageCode;
        final String editLanguage = SLASH_EDIT_SLASH + languageCode;

        baseLive = renderServletPath + liveLanguage;
        live = baseLive + resourcePath;
        if (!SettingsBean.getInstance().isDistantPublicationServerMode()) {
            baseEdit = "/cms/edit" + editLanguage;
            edit = baseEdit + resourcePath;
            baseContribute = "/cms/contribute" + editLanguage;
            contribute = baseContribute + resourcePath;
        }
        basePreview = renderServletPath + editLanguage;
        preview = basePreview + resourcePath;
        final String workspaceLanguage = "/" + resource.getWorkspace() + "/" + languageCode;
        find = Find.getFindServletPath() + workspaceLanguage;
        initializers = Initializers.getInitializersServletPath() + workspaceLanguage;
        convert = DocumentConverter.getPath() + "/" + resource.getWorkspace();
        templatesPath = "/modules";
        baseUserBoardEdit = "/cms/dashboard" + editLanguage;
        baseUserBoardLive = "/cms/dashboard" + liveLanguage;
        baseUserBoardFrameEdit = "/cms/dashboardframe" + editLanguage;
        baseUserBoardFrameLive = "/cms/dashboardframe" + liveLanguage;
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

        final JCRSiteNode site = context.getSite();
        if (site != null) {
            final String path = site.getPath();
            if (path.startsWith("/modules/")) {
                return "/cms/" + mode + SLASH_EDIT_SLASH + languageCode + path + ".html";
            }
        }
        return isVisual ? "/welcome/studiovisualmode" : "/welcome/studiomode";
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
        View view = ((Script) context.getRequest().getAttribute("script")).getView();
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
        final String template = resource.getTemplate();
        return getContext() + context.getServletPath() + "/" + resource.getWorkspace() + "/" + languageCode + context.getMainResource().getNode().getPath()
                + getTemplateExtensionFrom(template) + ".html";
    }

    private String getTemplateExtensionFrom(String template) {
        return (template != null && !"default".equals(template) ? "." + template : "");
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
        return buildURL(node, null, template, templateType);
    }

    public String buildURL(JCRNodeWrapper node, String languageCode, String template, String templateType) {
        return buildURL(node.getPath(), languageCode, template, templateType);
    }

    public String buildURL(String nodePath, String languageCode, String template, String templateType) {
        String baseURL = this.base;
        if (!StringUtils.isEmpty(languageCode)) {
            baseURL = context.getServletPath() + "/" + resource.getWorkspace() + "/" + languageCode;
        }
        return baseURL + nodePath + getTemplateExtensionFrom(template) + "." + templateType;
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
        final Resource ajaxResource = context.getAjaxResource();
        if (context.isAjaxRequest() && ajaxResource != null) {
            final String path = ajaxResource.getNode().getPath();
            if (context.isEditMode()) {
                return baseEdit + path + ".html";
            } else if (context.isContributionMode()) {
                return baseContribute + path + ".html";
            } else {
                return (Constants.LIVE_WORKSPACE.equals(ajaxResource.getWorkspace()) ? baseLive : basePreview) + path + ".html";
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
            StringBuilder url = new StringBuilder(255); // use a greater than default (16) allocated StringBuilder to avoid having to resize it
            final HttpServletRequest request = context.getRequest();
            String scheme = request.getScheme();
            String host = context.getSite().getServerName();
            if (Url.isLocalhost(host)) {
                host = request.getServerName();
            }

            int port = SettingsBean.getInstance().getSiteURLPortOverride();

            if (port == 0) {
                port = request.getServerPort();
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
            JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUserByPath(user.getLocalPath());
            myProfile = (!JahiaUserManagerService.isGuest(user) && userNode != null && userNode.isMemberOfGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) ? "/start" : "";
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
        final StringBuilder sb = new StringBuilder(1024);
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
