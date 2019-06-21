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
package org.jahia.ajax.gwt.helper;

import com.google.common.collect.Sets;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.utils.security.AccessManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.security.Privilege;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class related to Working in progress actions
 */
public class WIPHelper {

    private static final transient Logger logger = LoggerFactory.getLogger(WIPHelper.class);

    private JCRPublicationService publicationService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * Set the WIP status on the given node according to the given gwt properties.
     * @param node where to set the property
     * @param props
     * @throws RepositoryException
     */
    public void saveWipPropertiesIfNeeded(JCRNodeWrapper node, List<GWTJahiaNodeProperty> props)
            throws RepositoryException {
        // do we have anything to update at all or we have other properties than WIP to update?
        if (props == null || props.isEmpty()
                || (props.stream().filter(prop -> (!Constants.WORKINPROGRESS_LANGUAGES.equals(prop.getName()))
                && !Constants.WORKINPROGRESS_STATUS.equals(prop.getName())).count() > 0)) {
            return;
        }

        // we have only WIP properties in the list of non-i18n properties to update

        String newWipStatus = null;
        Set<String> newWipLanguages = null;
        JCRSessionWrapper session = node.getSession();

        GWTJahiaNodeProperty wipStatusProperty = props.stream()
                .filter(prop -> prop.getName().equals(Constants.WORKINPROGRESS_STATUS)).findFirst().orElse(null);
        if (wipStatusProperty != null && wipStatusProperty.getValues() != null
                && !wipStatusProperty.getValues().isEmpty()) {
            newWipStatus = wipStatusProperty.getValues().get(0).getString();
            if ((Constants.WORKINPROGRESS_STATUS_ALLCONTENT.equals(newWipStatus)
                    || Constants.WORKINPROGRESS_STATUS_DISABLED.equals(newWipStatus))
                    && !node.hasPermission(AccessManagerUtils.getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES,
                    session.getWorkspace().getName()))) {
                // we do not allow translators to change WIP status type to all content or disabled
                newWipStatus = null;
            }
        }

        // check languages
        GWTJahiaNodeProperty languageProperty = props.stream()
                .filter(prop -> prop.getName().equals(Constants.WORKINPROGRESS_LANGUAGES)).findFirst().orElse(null);
        if (languageProperty != null) {
            newWipLanguages = languageProperty.getValues() != null
                    ? languageProperty.getValues().stream().map(v -> v.getString()).collect(Collectors.toSet())
                    : Collections.emptySet();

            Set<String> existingWipLanguages = Collections.emptySet();
            if (node.hasProperty(Constants.WORKINPROGRESS_LANGUAGES)) {
                existingWipLanguages = new HashSet<>();
                for (JCRValueWrapper lang : node.getProperty(Constants.WORKINPROGRESS_LANGUAGES).getValues()) {
                    existingWipLanguages.add(lang.getString());
                }
            }

            // check for removed or added languages
            Sets.SetView<String> modifiedLanguages = Sets.union(Sets.difference(existingWipLanguages, newWipLanguages),
                    Sets.difference(newWipLanguages, existingWipLanguages));
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
            } else {
                // no changes so far
                newWipLanguages = null;
            }
        }

        updateWipStatus(node, newWipStatus, newWipLanguages);

        // we remove the WIP properties, as we've already handled them
        props.clear();
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
