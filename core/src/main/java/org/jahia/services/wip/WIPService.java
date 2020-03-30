/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.wip;

import com.google.common.collect.Sets;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.security.AccessManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.security.Privilege;
import java.util.*;

/**
 * Work in progress Service
 * Service that handle Wip Information for a given node
 * A WIP Information is:
 *  - a status that is one of: "DISABLED" (default value), "ALL_CONTENTS", "LANGUAGES"
 *  - languages: list of language codes on witch WIP is set (only used by LANGUAGES status)
 */
public class WIPService {

    private static final transient Logger logger = LoggerFactory.getLogger(WIPService.class);

    private JCRPublicationService publicationService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * Return work in progress information for the given node
     * @param node to on witch to get WIP Information
     * @return WIP Info (status + languages)
     * @throws RepositoryException
     */
    public WIPInfo getWipInfo(JCRNodeWrapper node) throws RepositoryException {
        // status
        String status = Constants.WORKINPROGRESS_STATUS_DISABLED;
        if (node.hasProperty(Constants.WORKINPROGRESS_STATUS)) {
            status = node.getProperty(Constants.WORKINPROGRESS_STATUS).getString();
        }
        // language
        Set<String> languages = new HashSet<>();
        if (node.hasProperty(Constants.WORKINPROGRESS_LANGUAGES)) {
            // can't use lambda because of exception handling
            for (Value langValue : node.getProperty(Constants.WORKINPROGRESS_LANGUAGES).getValues()) {
                languages.add(langValue.getString());
            }
        }
        return new WIPInfo(status, languages);

    }

    /**
     * Read "wip.checkbox.checked" system property value to get default WIP status.
     * @return ALL_CONTENT if enabled
     */
    public WIPInfo getDefaultWipInfo() {
        boolean isWipEnabled = Boolean.parseBoolean(SettingsBean.getInstance().getString("wip.checkbox.checked", "false"));
        return new WIPInfo(isWipEnabled ? Constants.WORKINPROGRESS_STATUS_ALLCONTENT : Constants.WORKINPROGRESS_STATUS_DISABLED, Collections.emptySet());
    }

    /**
     * Add work in progress properties on a created node.
     * Use this method to set WIP properties on a newly created node
     * @param node
     * @param wipInfo
     * @throws RepositoryException
     */
    public void createWipPropertiesOnNewNode(JCRNodeWrapper node, WIPInfo wipInfo) throws RepositoryException {
        node.setProperty(Constants.WORKINPROGRESS_STATUS, wipInfo.getStatus());
        final Collection<String> languages = wipInfo.getLanguages();
        node.setProperty(Constants.WORKINPROGRESS_LANGUAGES, languages.toArray(new String[0]));
    }

    /**
     * Set the WIP status on the given node according to the given WIPInfo.
     * For new node, please use "createWipPropertiesOnNewNode(JCRNodeWrapper node, WIPInfo wipInfo)"
     * @param node where to set the property
     * @param wipInfo
     * @throws RepositoryException
     */
    public void saveWipPropertiesIfNeeded(JCRNodeWrapper node, WIPInfo wipInfo)
            throws RepositoryException {
        // do we have anything to update at all or we have other properties than WIP to update?
        if (wipInfo.getStatus() == null) {
            return;
        }

        // we have only WIP properties in the list of non-i18n properties to update

        String newWipStatus = null;
        Set<String> newWipLanguages = null;
        JCRSessionWrapper session = node.getSession();


        if (wipInfo.getStatus() != null
                && !wipInfo.getStatus().isEmpty()) {
            newWipStatus = wipInfo.getStatus();
            if ((Constants.WORKINPROGRESS_STATUS_ALLCONTENT.equals(newWipStatus)
                    || Constants.WORKINPROGRESS_STATUS_DISABLED.equals(newWipStatus))
                    && !node.hasPermission(AccessManagerUtils.getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES,
                    session.getWorkspace().getName()))) {
                // we do not allow translators to change WIP status type to all content or disabled
                return;
            }
        }

        // check languages
        if (wipInfo.getLanguages() != null) {
            newWipLanguages = wipInfo.getLanguages();

            Set<String> existingWipLanguages = Collections.emptySet();
            if (node.hasProperty(Constants.WORKINPROGRESS_LANGUAGES)) {
                existingWipLanguages = new HashSet<>();
                for (JCRValueWrapper lang : node.getProperty(Constants.WORKINPROGRESS_LANGUAGES).getValues()) {
                    existingWipLanguages.add(lang.getString());
                }
            }

            // check for removed or added languages
            Sets.SetView<String> modifiedLanguages = Sets.symmetricDifference(newWipLanguages, existingWipLanguages);
            if (!modifiedLanguages.isEmpty()) {
                // we do have changes
                if (!node.hasPermission(AccessManagerUtils.getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES,
                        session.getWorkspace().getName()))) {
                    for (String modifiedLang : modifiedLanguages) {
                        if (!node.hasPermission(AccessManagerUtils.getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES,
                                session.getWorkspace().getName()) + "_" + modifiedLang)) {
                            throw new AccessDeniedException("Unable to update Work In Progress information on node "
                                    + node.getPath() + " for user " + session.getUser().getName() + " in locale "
                                    + modifiedLang);
                        }
                    }
                }
            }
        }

        updateWipStatus(node, newWipStatus, newWipLanguages);

    }

    private void updateWipStatus(JCRNodeWrapper node, String wipStatusToSet, final Set<String> wipLangugagesToSet)
            throws RepositoryException {

        if (wipStatusToSet == null) {
            return;
        }
        JCRSessionWrapper session = node.getSession();

        boolean autoPublishNode = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), session.getWorkspace().getName(),
                session.getLocale(), systemSession -> {
                    Node targetNode = systemSession.getProviderSession(node.getProvider())
                            .getNodeByIdentifier(node.getIdentifier());

                    boolean debugEnabled = logger.isDebugEnabled();
                    boolean checkForAutoPublish = false;

                    if (wipStatusToSet.equals(Constants.WORKINPROGRESS_STATUS_DISABLED) ||
                            (wipStatusToSet.equals(Constants.WORKINPROGRESS_STATUS_LANG) && (wipLangugagesToSet == null || wipLangugagesToSet.isEmpty()))) {
                        targetNode.setProperty(Constants.WORKINPROGRESS_LANGUAGES, (Value[]) null);
                        targetNode.setProperty(Constants.WORKINPROGRESS_STATUS, (Value) null);

                        if (debugEnabled) {
                            logger.debug("Removing WIP status property on node {}", targetNode.getPath());
                        }
                        checkForAutoPublish = true;
                    } else {
                        targetNode.setProperty(Constants.WORKINPROGRESS_STATUS, wipStatusToSet);

                        switch (wipStatusToSet) {
                            case Constants.WORKINPROGRESS_STATUS_LANG:
                                targetNode.setProperty(Constants.WORKINPROGRESS_LANGUAGES,
                                        JCRContentUtils.createValues(wipLangugagesToSet, systemSession.getValueFactory()));
                                if (debugEnabled) {
                                    logger.debug("Setting WIP languages on node {} to {}", targetNode.getPath(), wipLangugagesToSet);
                                }
                                checkForAutoPublish = true;
                                break;
                            case Constants.WORKINPROGRESS_STATUS_ALLCONTENT:
                                targetNode.setProperty(Constants.WORKINPROGRESS_LANGUAGES, (Value[]) null);
                                if (debugEnabled) {
                                    logger.debug("Setting WIP on node {}", targetNode.getPath());
                                }
                                break;
                            default:
                                throw new IllegalStateException("Unknown work in progress status: " + wipStatusToSet);
                        }
                    }
                    targetNode.getSession().save();
                    return checkForAutoPublish;
                });

        // flush cache of the node wrapper because we may have done modification on the real node directly, and cache may be corrupted
        if (node instanceof JCRNodeWrapperImpl) {
            ((JCRNodeWrapperImpl) node).flushLocalCaches();
        }

        // check for auto publish
        if (autoPublishNode) {
            checkForAutoPublication(node);
        }
    }

    private synchronized void checkForAutoPublication(JCRNodeWrapper node) throws RepositoryException {
        // in case there is modification on the nodes during the time it was WIP,
        // and it's an auto published node, we need to publish it manually since the LastModifiedListener is not triggered by WIP properties update

        if (node.isNodeType("jmix:autoPublish")) {
            List<String> uuids = new ArrayList<>();
            uuids.add(node.getIdentifier());

            NodeIterator translationNodes = node.getI18Ns();
            while (translationNodes.hasNext()) {
                uuids.add(translationNodes.nextNode().getIdentifier());
            }

            // if some languages are WIP or even the node is WIP, it will be blocked automatically by the publication service,
            // no need for additional check here, just send all the uuids related to this node, and the translation nodes
            publicationService.publish(uuids, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, false, null);
        }
    }
}
