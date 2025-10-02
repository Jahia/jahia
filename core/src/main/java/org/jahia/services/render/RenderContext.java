/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.render.filter.cache.ClientCachePolicy;
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
    private final HttpServletRequest request;
    private HttpServletResponse response;
    private Resource mainResource;
    private final JahiaUser user;
    private JCRSiteNode site;
    private URLGenerator URLGenerator;
    private Locale uiLocale;

    // TODO: BACKLOG-6496, this stack is not need anymore with new AggregateFilter that handle the stack of fragments generated
    private final Stack<Resource> resourcesStack = new Stack<Resource>();
    private final Set<String> renderedPaths = new HashSet<String>();

    private boolean isContributionMode = false;
    private boolean isEditMode = false;
    private EditConfiguration editModeConfig;
    private String servletPath;
    private String workspace = "default";

    private final Set<String> displayedModules = new HashSet<String>();

    private String redirect;

    private String contentType;

    private final Map<String, Map<String, Integer>> templatesCacheExpiration = new HashMap<String, Map<String, Integer>>();

    private boolean ajaxRequest = false;
    private Resource ajaxResource = null;

    // the current rendering channel, for example "iphone", "ipad", "android", etc...
    private Channel channel = null;
    private String mode;
    private SiteInfo siteInfo;
    private boolean forceUILocaleForJCRSession;

    private boolean ugcEnabled = true;
    private ClientCachePolicy clientCachePolicy;

    public RenderContext(HttpServletRequest request, HttpServletResponse response, JahiaUser user) {
        this.request = request;
        this.response = response;
        this.user = user;
        this.clientCachePolicy = ClientCachePolicy.DEFAULT;
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
            if (servletPath != null) {
                mode = StringUtils.substringAfterLast(servletPath, "/");
                if (mode.endsWith("frame")) {
                    mode = StringUtils.substringBefore(mode, "frame");
                }
                if ("render".equals(mode)) {
                    if (workspace.equals(Constants.LIVE_WORKSPACE)) {
                        mode = Constants.LIVE_MODE;
                    } else {
                        mode = Constants.PREVIEW_MODE;
                    }
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
        return getMode().equals(Constants.LIVE_MODE);
    }

    public boolean isPreviewMode() {
        return getMode().equals(Constants.PREVIEW_MODE);
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
            } else return editModeConfig.getVisibleTypes() == null || editModeConfig.getVisibleTypes().isEmpty() || isNodeOfType(node, editModeConfig.getVisibleTypes());
        }
        return true;
    }

    /**
     * @param node to check
     * @return true if the node is editable (in editableTypes or without nonEditableTypes)
     * @throws RepositoryException in case of JCR-related errors
     */
    public boolean isEditable(JCRNodeWrapper node) throws RepositoryException {
        if (editModeConfig != null) {
            if (editModeConfig.getBypassModeForTypes() != null && !editModeConfig.getBypassModeForTypes().isEmpty() && isNodeOfType(mainResource.getNode(), editModeConfig.getBypassModeForTypes())) {
                return false;
            }

            if (editModeConfig.getNonEditableTypes() != null && !editModeConfig.getNonEditableTypes().isEmpty() && isNodeOfType(node, editModeConfig.getNonEditableTypes())) {
                return false;
            } else return editModeConfig.getEditableTypes() == null || editModeConfig.getEditableTypes().isEmpty() || isNodeOfType(node, editModeConfig.getEditableTypes());
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

    /**
     * @return Whether the application is in read only mode, dependent on read only, full read only, and maintenance status currently activated/deactivated.
     */
    public boolean isReadOnly() {
        SettingsBean settings = SettingsBean.getInstance();
        return (settings.isReadOnlyMode() || settings.isMaintenanceMode() || settings.isFullReadOnlyMode());
    }

    /**
     * Compute the client cache policy for the current render context.
     * It is only possible to upgrade to a stronger level :
     * If level is public, you can update to custom(with ttl) or private.
     * If level is custom, you can update to custom with lower ttl or private.
     * If level is private, you can't update it.
     * As soon as a policy is encountered in the render chain, on any fragment, stored in cache or calculated live, it will
     * be computed in the render context to reflect the according policy level in the end.
     *
     *
     * @param policy, the new policy to apply
     */
    public synchronized void computeClientCachePolicy(ClientCachePolicy policy) {
        if (policy.isStronger(this.clientCachePolicy)) {
            this.clientCachePolicy = policy;
        }
    }

    public ClientCachePolicy getClientCachePolicy() {
        return clientCachePolicy;
    }
}
