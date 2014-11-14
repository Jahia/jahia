/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.serversettings.flow;

import java.io.Serializable;
import java.util.*;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.modules.serversettings.mount.MountPoint;
import org.jahia.modules.serversettings.mount.MountPointFactory;
import org.jahia.modules.serversettings.mount.MountPointManager;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author kevan
 */
public class MountPointsManagementFlowHandler implements Serializable{
    private static final long serialVersionUID = 1436197019769187454L;
    private static Logger logger = LoggerFactory.getLogger(MountPointsManagementFlowHandler.class);
    private static final String BUNDLE = "resources.JahiaServerSettings";
    public static enum Actions {
        mount, unmount, delete
    }

    @Autowired
    private transient JCRStoreService jcrStoreService;

    public MountPointManager getMountPointManagerModel() {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<MountPointManager>() {
                @Override
                public MountPointManager doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    // get mount points
                    final NodeIterator nodeIterator = getMountPoints(session);
                    List<MountPoint> mountPoints = new ArrayList<MountPoint>((int) nodeIterator.getSize());
                    while (nodeIterator.hasNext()) {
                        JCRMountPointNode mountPointNode = (JCRMountPointNode) nodeIterator.next();
                        mountPoints.add(new MountPoint(mountPointNode));
                    }
                    
                    // get provider factories
                    Map<String, ProviderFactory> providerFactories = jcrStoreService.getProviderFactories();
                    List<MountPointFactory> mountPointFactories = new ArrayList<MountPointFactory>(providerFactories.size());
                    for (ProviderFactory factory : providerFactories.values()) {
                        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(factory.getNodeTypeName());

                        // calcul the factory URL
                        String queryString = "select * from [jmix:mountPointFactory] as factory where ['j:mountPointType'] = '" + type.getName() + "'";
                        Query query = session.getWorkspace().getQueryManager().createQuery(queryString, Query.JCR_SQL2);
                        QueryResult queryResult = query.execute();
                        String endOfURL = null;
                        if(queryResult.getNodes().getSize() > 0){
                            JCRNodeWrapper factoryNode = (JCRNodeWrapper) queryResult.getNodes().next();
                            String templateName = factoryNode.getPropertyAsString("j:templateName");
                            if(StringUtils.isNotEmpty(templateName)){
                                endOfURL = Text.escapePath(factoryNode.getPath()) + "." + templateName + ".html";
                            }
                        }

                        mountPointFactories.add(new MountPointFactory(type.getName(), type.getLabel(LocaleContextHolder.getLocale()), endOfURL));
                    }

                    // return model
                    return new MountPointManager(mountPointFactories, mountPoints);
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error retrieving mount points", e);
            return new MountPointManager();
        }
    }

    public void doAction(final String mountPointName, Actions action, MessageContext messageContext){
        boolean success = false;

        switch (action){
            case mount:
                success = mount(mountPointName);
                break;
            case unmount:
                success = unmount(mountPointName);
                break;
            case delete:
                success = delete(mountPointName);
                break;
        }

        handleMessages(messageContext, action, mountPointName, success);
    }

    private boolean mount(final String mountPointName) {
        boolean success = false;
        try {
            success = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRMountPointNode mountPointNode = getMountPoint(session, mountPointName);
                    if(mountPointNode != null){
                        if(mountPointNode.getMountStatus() != JCRMountPointNode.MountStatus.unmounted) {
                            String detail = "Can't mount " + mountPointName + ", mount status of the mount point is not unmounted";
                            logger.error(detail);
                            return false;
                        }
                        ProviderFactory providerFactory = JCRStoreService.getInstance().getProviderFactories().get(mountPointNode.getPrimaryNodeTypeName());
                        JCRStoreProvider mountProvider = providerFactory.mountProvider(mountPointNode.getVirtualMountPointNode());
                        return mountProvider.isAvailable();
                    } else {
                        logger.error("Can't mount " + mountPointName + ", no mount point node found");
                        return false;
                    }
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error trying to mount " + mountPointName, e);
        }
        return success;
    }

    private boolean unmount(final String mountPointName) {
        boolean success = false;
        try {
            success = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRMountPointNode mountPointNode = getMountPoint(session, mountPointName);
                    if(mountPointNode != null){
                        if(mountPointNode.getMountStatus() != JCRMountPointNode.MountStatus.mounted) {
                            logger.error("Can't mount " + mountPointName + ", current mount status of the mount point is not mounted");
                            return false;
                        }

                        return mountPointNode.getMountProvider().unmount();
                    } else {
                        logger.error("Can't mount " + mountPointName + ", no mount point node found");
                        return false;
                    }
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error trying to unmount " + mountPointName, e);
        }
        return success;
    }

    private boolean delete(final String mountPointName) {
        boolean success = false;
        try {
            success = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRMountPointNode mountPointNode = getMountPoint(session, mountPointName);
                    if(mountPointNode != null){
                        mountPointNode.remove();
                        session.save();
                        return getMountPoint(session, mountPointName) == null;
                    } else {
                        logger.error("Can't delete " + mountPointName + ", no mount point node found");
                        return false;
                    }
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error trying to delete " + mountPointName, e);
        }
        return success;
    }

    private void handleMessages(MessageContext messageContext, Actions action, String mountPoint, boolean success) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = Messages.getWithArgs(BUNDLE, "serverSettings.mountPointsManagement.action." + (success ? "successMessage" : "failMessage"), locale,
                action, mountPoint);
        MessageBuilder messageBuilder = new MessageBuilder();
        if (success) {
            messageBuilder.info().defaultText(message);
        } else {
            messageBuilder.error().defaultText(message);
        }
        messageContext.addMessage(messageBuilder.build());
    }

    private JCRMountPointNode getMountPoint(JCRSessionWrapper sessionWrapper, String name) throws RepositoryException {
        Query query = sessionWrapper.getWorkspace().getQueryManager().createQuery(getMountPointQuery(name), Query.JCR_SQL2);
        QueryResult queryResult = query.execute();
        return queryResult.getNodes().getSize() > 0 ? (JCRMountPointNode) queryResult.getNodes().next() : null;
    }

    private NodeIterator getMountPoints(JCRSessionWrapper sessionWrapper) throws RepositoryException {
        Query query = sessionWrapper.getWorkspace().getQueryManager().createQuery(getMountPointQuery(null), Query.JCR_SQL2);
        return query.execute().getNodes();
    }

    private String getMountPointQuery(String name) {
        String query = "select * from [" + Constants.JAHIANT_MOUNTPOINT + "] as mount";
        if(StringUtils.isNotEmpty(name)) {
            query += (" where ['j:nodename'] = '" + name + "'");
        }
        return query;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }
}
