/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.bin.Captcha;
import org.jahia.bin.Contribute;
import org.jahia.bin.DocumentConverter;
import org.jahia.bin.Edit;
import org.jahia.bin.Find;
import org.jahia.bin.FindPrincipal;
import org.jahia.bin.FindUser;
import org.jahia.bin.Initializers;
import org.jahia.bin.Login;
import org.jahia.bin.Logout;
import org.jahia.bin.Render;
import org.jahia.bin.Studio;
import org.jahia.params.valves.LoginConfig;
import org.jahia.params.valves.LogoutConfig;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;

/**
 * Main URL generation class. This class is exposed to the template developers to make it easy to them to access
 * basic URLs such as <code>${url.edit}</code>, <code>${url.userProfile}</code>.
 * User: toto
 * Date: Sep 14, 2009
 * Time: 11:13:37 AM
 */
public class URLGenerator {
    private static Logger logger = LoggerFactory.getLogger(URLGenerator.class);

    /**
     * Returns the server URL, including scheme, host and port.
     * The URL is in the form <code><scheme><host>:<port></code>,
     * e.g. <code>http://www.jahia.org:8080</code>. The port is omitted in case
     * of standard HTTP (80) and HTTPS (443) ports.
     *
     * @deprecated Please use Url.getServer(HttpServletRequest request) instead
     *
     * @return the server URL, including scheme, host and port
     */
    public static String getServer(HttpServletRequest request) {
        return Url.getServer(request);
    }

    private String base;

    private String live;
    private String edit;
    private String preview;
    private String contribute;
    private String studio;
    private String find;
    private String initializers;
    private Resource resource;
    private RenderContext context;

    private Map<String, String> languages;

    private Map<String, String> templates;

    private Map<String, String> templateTypes;

    private String templatesPath;

    private String baseLive;
    private String baseContribute;
    private String baseEdit;
    private String basePreview;
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
        base = context.getServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();

        final String resourcePath = context.getMainResource().getNode().getPath() + ((!"default".equals(context.getMainResource().getTemplate()))?"."+context.getMainResource().getTemplate()+".":".") + context.getMainResource().getTemplateType();

        baseLive = Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale();
        live = baseLive + resourcePath;
        if (!SettingsBean.getInstance().isDistantPublicationServerMode()) {
            baseEdit = Edit.getEditServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
            edit = baseEdit + resourcePath;
            baseContribute = Contribute.getContributeServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
            contribute = baseContribute + resourcePath;
        }
        basePreview = Render.getRenderServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale();
        preview = basePreview + resourcePath;
        find = Find.getFindServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        initializers = Initializers.getInitializersServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        convert = DocumentConverter.getPath() + "/" + resource.getWorkspace();
        templatesPath = "/modules";
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
        return StringUtils.substringBeforeLast(context.getServletPath(),"/")+"/{mode}/{lang}";
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

    public String getStudio() {
        if (!SettingsBean.getInstance().isDistantPublicationServerMode() && !SettingsBean.getInstance().isProductionMode()) {
            if (studio == null) {
                studio = Studio.getStudioServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + "/templateSets";
                if (context.getSite() != null) {
                    try {
                        if (context.getSite().hasProperty("j:templatesSet")) {
                            studio += "/" + context.getSite().getProperty("j:templatesSet").getString();
                            if (resource.getNode().hasProperty("j:templateNode")) {
                                try {
                                    studio += StringUtils.substringAfter(resource.getNode().getProperty("j:templateNode").getNode().getPath(), context.getSite().getPath());
                                    studio += ".html";
                                } catch (RepositoryException e) {
                                    studio += ".html";
                                }
                            } else {
                                studio += ".html";
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.error("Cannot get studio url", e);
                    }
                } else {
                    studio += ".html";
                }
            }
        }
        return studio;
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
        return getTemplatesPath() + "/" + ((Script) context.getRequest().getAttribute(
                "script")).getView().getModule().getRootFolder();
    }

    public String getCurrent() {
        return buildURL(resource.getNode(), resource.getResolvedTemplate(), resource.getTemplateType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLanguages() {
        if (languages == null) {
            languages = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object lang) {
                    return getContext() + context.getServletPath() + "/" + resource.getWorkspace() + "/" + lang + resource.getNode().getPath() +
                            ("default".equals(resource.getTemplate()) ? "" : "." + resource.getTemplate())
                            + ".html";
                }
            });
        }

        return languages;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplates() {
        if (templates == null) {
            templates = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object template) {
                    return buildURL(resource.getNode(), (String) template, resource.getTemplateType());
                }
            });
        }
        return templates;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplateTypes() {
        if (templateTypes == null) {
            templateTypes = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object templateType) {
                    return buildURL(resource.getNode(), resource.getResolvedTemplate(), (String) templateType);
                }
            });
        }
        return templateTypes;
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
     * Returns the URL of the main resource (normally, page), depending on the
     * current mode.
     *
     * @return the URL of the main resource (normally, page), depending on the
     *         current mode
     */
    public String getMainResource() {
        if (context.isEditMode()) {
            if (context.getEditModeConfigName().equals(Studio.STUDIO_MODE)) {
                return  Studio.getStudioServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + context.getMainResource().getNode().getPath() + ".html";
            }
            return getEdit();
        } else if (context.isContributionMode()) {
            if(context.getMainResource().getResolvedTemplate()!=null) {
                return baseContribute+context.getMainResource().getNode().getPath()+"."+context.getMainResource().getResolvedTemplate()+".html";
            }
            return contribute;
        } else {
            return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
        }
    }

    public String buildURL(JCRNodeWrapper node, String template, String templateType) {
        return base + node.getPath() + (template != null && !"default".equals(template) ? "." + template : "") + "." + templateType;
    }
    
    public String buildURL(JCRNodeWrapper node, String languageCode, String template, String templateType) {
        if (StringUtils.isEmpty(languageCode)) {
            return buildURL(node, template, templateType);
        }
        return context.getServletPath() + "/" + resource.getWorkspace() + "/" + languageCode + node.getPath() + (template != null && !"default".equals(template) ? "." + template : "") + "." + templateType;
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
                if (context.getEditModeConfigName().equals(Studio.STUDIO_MODE)) {
                    return getStudio();
                }
                return getEdit();
            } else {
                return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
            }
        }
    }
    
    /**
     * Returns the server URL, including scheme, host and port, depending on the
     * current site. The URL is in the form <code><scheme><host>:<port></code>,
     * e.g. <code>http://www.jahia.org:8080</code>. The port is omitted in case
     * of standard HTTP (80) and HTTPS (443) ports.
     * 
     * If the site's server name is configured to be "localhost", then take the
     * servername from the request.
     * 
     * @return the server URL, including scheme, host and port, depending on the
     *         current site
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
            
            if (!(("http".equals(scheme) && (port == 80)) ||
                  ("https".equals(scheme) && (port == 443)))) {
                url.append(":").append(port);
            }
            
            server = url.toString();
        }
        
        return server;
    }

    public String getMyProfile() {
        if (myProfile == null) {
            myProfile = JahiaUserManagerService.isNotGuest(context.getUser()) ? "/start" : "";
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
