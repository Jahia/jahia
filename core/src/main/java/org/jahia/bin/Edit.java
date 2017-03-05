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
package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Rendering controller for the edit mode.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 4:15:21 PM
 *
 * @see Render
 */
public class Edit extends Render {
    private static final long serialVersionUID = -6197445426874881036L;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Edit.class);

    private EditConfiguration editConfiguration;

    private boolean availableInProductionMode = true;
    private boolean availableInDistantPublicationServerMode = true;
    private boolean forceUILocaleForJCRSession = false;

    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = super.createRenderContext(req, resp, user);
        context.setEditMode(true);
        if ("contributemode".equals(editConfiguration.getName())) {
            context.setContributionMode(true);
        }
        context.setEditModeConfig(editConfiguration);

        context.setForceUILocaleForJCRSession(forceUILocaleForJCRSession);
        return context;
    }

    protected boolean hasAccess(JCRNodeWrapper node) {
        if (node == null) {
            logger.error("Site key is null.");
            return false;
        }
        try {
            if (node.getSession().getUser() == null) {
                return false;
            }
            if (Constants.LIVE_WORKSPACE.equals(node.getSession().getWorkspace().getName())) {
                logger.error("Someone have tried to access the live repository in edit mode");
                return false;
            }
            // the site cannot be resolved
            if (node.getResolveSite() == null) {
                return false;
            }
            String checkedPath = StringUtils.replace(StringUtils.replace(editConfiguration.getNodeCheckPermission(),"$site",node.getResolveSite().getPath()),"$user",node.getSession().getUser().getLocalPath());
            if (editConfiguration.getNodeCheckPermission() == null) {
                checkedPath = node.getResolveSite().getPath();
            }

            return node.getSession().getNode(checkedPath).hasPermission(editConfiguration.getRequiredPermission()) && super.hasAccess(node);
        } catch (RepositoryException e) {
            return false;
        }
    }

    @Override
    protected boolean isDisabled() {
        final SettingsBean settingsBean = getSettingsBean();
        return settingsBean.isReadOnlyMode()
                || (settingsBean.isDistantPublicationServerMode() && !isAvailableInDistantPublicationServerMode())
                || (settingsBean.isProductionMode() && !isAvailableInProductionMode());
    }

    public EditConfiguration getEditConfiguration() {
        return editConfiguration;
    }

    public void setEditConfiguration(EditConfiguration editConfiguration) {
        this.editConfiguration = editConfiguration;
    }

    public boolean isAvailableInProductionMode() {
        return availableInProductionMode;
    }

    public void setAvailableInProductionMode(boolean availableInProductionMode) {
        this.availableInProductionMode = availableInProductionMode;
    }

    public boolean isAvailableInDistantPublicationServerMode() {
        return availableInDistantPublicationServerMode;
    }

    public void setAvailableInDistantPublicationServerMode(boolean availableInDistantPublicationServerMode) {
        this.availableInDistantPublicationServerMode = availableInDistantPublicationServerMode;
    }

    public void setForceUILocaleForJCRSession(boolean forceUILocaleForJCRSession) {
        this.forceUILocaleForJCRSession = forceUILocaleForJCRSession;
    }

    public boolean isForceUILocaleForJCRSession() {
        return forceUILocaleForJCRSession;
    }

}
