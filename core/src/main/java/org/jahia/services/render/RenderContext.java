/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.channels.Channel;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Template rendering context with the information about current request/response pair and optional template parameters.
 *
 * @author toto
 */
public class RenderContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Resource mainResource;
    private JahiaUser user;
    private JCRSiteNode site;
    private URLGenerator URLGenerator;
    private Locale uiLocale;

    // TODO: BACKLOG-6496, this stack is not need anymore with new AggregateFilter that handle the stack of fragments generated
    private Stack<Resource> resourcesStack = new Stack<Resource>();
    private Set<String> renderedPaths = new HashSet<String>();

    private boolean isContributionMode = false;
    private boolean isEditMode = false;
    private EditConfiguration editModeConfig;
    private String servletPath;
    private String workspace = "default";

    private Set<String> displayedModules = new HashSet<String>();

    private String redirect;

    private String contentType;

    private Map<String, Map<String, Integer>> templatesCacheExpiration = new HashMap<String, Map<String, Integer>>();

    private boolean ajaxRequest = false;
    private Resource ajaxResource = null;

    // the current rendering channel, for example "iphone", "ipad", "android", etc...
    private Channel channel = null;
    private String mode;
    private SiteInfo siteInfo;
    private boolean forceUILocaleForJCRSession;

    private boolean portletActionRequest;

    private boolean ugcEnabled = true;

    public RenderContext(HttpServletRequest request, HttpServletResponse response, JahiaUser user) {
        this.request = request;
        this.response = response;
        this.user = user;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public JahiaUser getUser() {
        return user;
    }

    public JCRSiteNode getSite() {
        return site;
    }

    public void setSite(JCRSiteNode site) {
        this.site = site;
    }

    public URLGenerator getURLGenerator() {
        return URLGenerator;
    }

    public void setURLGenerator(URLGenerator URLGenerator) {
        this.URLGenerator = URLGenerator;
    }

    public Set<String> getDisplayedModules() {
        return displayedModules;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public String getMode() {
        if (mode == null) {
            // compute current mode
            mode = StringUtils.substringAfterLast(servletPath, "/");
            if (mode.endsWith("frame")) {
                mode = StringUtils.substringBefore(mode, "frame");
            }
            if ("render".equals(mode)) {
                if (workspace.equals("live")) {
                    mode = "live";
                } else {
                    mode = "preview";
                }
            }
        }
        return mode;
    }

    public String getEditModeConfigName() {
        return editModeConfig != null ? editModeConfig.getName() : null;
    }

    public EditConfiguration getEditModeConfig() {
        return editModeConfig;
    }

    public void setEditModeConfig(EditConfiguration editModeConfig) {
        this.editModeConfig = editModeConfig;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
        JCRSessionFactory.getInstance().setCurrentServletPath(servletPath);
        this.mode = null;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
        this.mode = null;
    }

    public boolean isContributionMode() {
        return isContributionMode;
    }

    public void setContributionMode(boolean contributionMode) {
        isContributionMode = contributionMode;
    }

    public boolean isLoggedIn() {
        return !JahiaUserManagerService.isGuest(getUser());
    }

    public Map<String, Map<String, Integer>> getTemplatesCacheExpiration() {
        return templatesCacheExpiration;
    }

    public void setMainResource(Resource mainResource) {
        this.mainResource = mainResource;
    }

    public Resource getMainResource() {
        return mainResource;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Set<String> getRenderedPaths() {
        return renderedPaths;
    }

    public Stack<Resource> getResourcesStack() {
        return resourcesStack;
    }

    public Locale getMainResourceLocale() {
        return getMainResource().getLocale();
    }

    public Locale getUILocale() {
        if (uiLocale == null) {
            Locale locale = null;
            if (!JahiaUserManagerService.isGuest(getUser())) {
                JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUserByPath(getUser().getLocalPath());
                locale = UserPreferencesHelper.getPreferredLocale(userNode, LanguageCodeConverters.resolveLocaleForGuest(request));
            }
            if (locale == null && mainResource != null) {
                locale = getMainResourceLocale();
            }
            uiLocale = locale;
            if (uiLocale != null) {
                request.getSession().setAttribute(Constants.SESSION_UI_LOCALE, uiLocale);
            }
        }
        return uiLocale;
    }

    public Locale getFallbackLocale() {
        if (site != null) {
            return site.isMixLanguagesActive() ? LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()) : null;
        }
        return null;
    }

    public boolean isLiveMode() {
        return getMode().equals("live");
    }

    public boolean isPreviewMode() {
        return getMode().equals("preview");
    }

    public void setAjaxRequest(boolean ajaxRequest) {
        this.ajaxRequest = ajaxRequest;
    }

    public boolean isAjaxRequest() {
        return ajaxRequest;
    }

    public void setAjaxResource(Resource ajaxResource) {
        this.ajaxResource = ajaxResource;
    }

    public Resource getAjaxResource() {
        return ajaxResource;
    }

    /**
     * @return the redirect
     */
    public String getRedirect() {
        return redirect;
    }

    /**
     * @param redirect the redirect to set
     */
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public boolean isEnterpriseEdition() {
        return Jahia.isEnterpriseEdition();
    }

    public SettingsBean getSettings() {
        return SettingsBean.getInstance();
    }

    /**
     * Returns the currently active channel
     *
     * @return
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets the currently active channel.
     *
     * @param channel a Channel containing the value for the currently active channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    /**
     * @param node to check
     * @return true if the node is visible (in visibleTypes or without nonVisibleTypes)
     * @throws javax.jcr.RepositoryException
     */
    public boolean isVisible(JCRNodeWrapper node) throws RepositoryException {
        if (editModeConfig != null) {
            if (editModeConfig.getBypassModeForTypes() != null && !editModeConfig.getBypassModeForTypes().isEmpty() && isNodeOfType(mainResource.getNode(), editModeConfig.getBypassModeForTypes())) {
                return true;
            }

            if (editModeConfig.getNonVisibleTypes() != null && !editModeConfig.getNonVisibleTypes().isEmpty() && isNodeOfType(node, editModeConfig.getNonVisibleTypes())) {
                return false;
            } else if (editModeConfig.getVisibleTypes() != null && !editModeConfig.getVisibleTypes().isEmpty() && !isNodeOfType(node, editModeConfig.getVisibleTypes())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param node to check
     * @return true if the node is editable (in editableTypes or without nonEditableTypes)
     * @throws RepositoryException
     */
    public boolean isEditable(JCRNodeWrapper node) throws RepositoryException {
        if (editModeConfig != null) {
            if (editModeConfig.getBypassModeForTypes() != null && !editModeConfig.getBypassModeForTypes().isEmpty() && isNodeOfType(mainResource.getNode(), editModeConfig.getBypassModeForTypes())) {
                return false;
            }

            if (editModeConfig.getNonEditableTypes() != null && !editModeConfig.getNonEditableTypes().isEmpty() && isNodeOfType(node, editModeConfig.getNonEditableTypes())) {
                return false;
            } else if (editModeConfig.getEditableTypes() != null && !editModeConfig.getEditableTypes().isEmpty() && !isNodeOfType(node, editModeConfig.getEditableTypes())) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isNodeOfType(JCRNodeWrapper node, Set<String> types) throws RepositoryException {
        if (types != null && node != null) {
            for (String s : types) {
                if (node.isNodeType(s)) {
                    return true;
                }
            }
        }
        return false;
    }


    public void setSiteInfo(SiteInfo siteInfo) {
        this.siteInfo = siteInfo;
    }

    public SiteInfo getSiteInfo() {
        return siteInfo;
    }

    public void setForceUILocaleForJCRSession(boolean forceUILocaleForJCRSession) {
        this.forceUILocaleForJCRSession = forceUILocaleForJCRSession;
    }

    public boolean isForceUILocaleForJCRSession() {
        return forceUILocaleForJCRSession;
    }

    public boolean isPortletActionRequest() {
        return portletActionRequest;
    }

    public void setPortletActionRequest(boolean portletActionRequest) {
        this.portletActionRequest = portletActionRequest;
    }

    public boolean isUgcEnabled() {
        return isLiveMode() && ugcEnabled;
    }

    public void setUgcEnabled(boolean ugcEnabled) {
        this.ugcEnabled = ugcEnabled;
    }

    /**
     * Get current read-only mode status.
     *
     * @return Current read-only mode status
     */
    public ReadOnlyModeController.ReadOnlyModeStatus getReadOnlyStatus() {
        return ReadOnlyModeController.getInstance().getReadOnlyStatus();
    }

}