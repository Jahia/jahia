/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software; you can redistribute it and/or
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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * External listener for the creation and deletion of a mount point nodes on other DF nodes to be able to mount/unmount the provider
 * locally.
 *
 * @author Sergiy Shyrkov
 */
public class MountPointListener extends DefaultEventListener implements ExternalEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MountPointListener.class);

    private static final String[] NODETYPES = new String[]{Constants.JAHIANT_MOUNTPOINT};

    private JCRStoreProviderChecker providerChecker;

    public void setProviderChecker(JCRStoreProviderChecker providerChecker) {
        this.providerChecker = providerChecker;
    }

    private static final ThreadLocal<Boolean> inListener = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public MountPointListener() {
        super();
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED
                + Event.PROPERTY_REMOVED;
    }

    @Override
    public String[] getNodeTypes() {
        return NODETYPES;
    }

    @Override
    public String getPath() {
        return "/mounts";
    }

    private void mount(final String uuid, final JCRStoreProvider provider) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNodeByIdentifier(uuid);
                    if (!(node instanceof JCRMountPointNode)) {
                        return false;
                    }

                    // perform mount of the provider
                    ProviderFactory providerFactory = JCRStoreService.getInstance().getProviderFactories()
                            .get(node.getPrimaryNodeTypeName());
                    if (providerFactory == null) {
                        return false;
                    }

                    JCRMountPointNode mountPoint = (JCRMountPointNode) node;
                    if (mountPoint.getMountStatus() == JCRMountPointNode.MountStatus.waiting && provider != null) {
                        providerChecker.checkPeriodically(provider);
                    } else if (mountPoint.getMountStatus() == JCRMountPointNode.MountStatus.mounted) {
                        JCRNodeWrapper mountPointTarget = mountPoint.getVirtualMountPointNode();
                        logger.info("Mounting the provider {} to {}", uuid, mountPointTarget.getPath());
                        final JCRStoreProvider provider = providerFactory.mountProvider(mountPointTarget);
                        if (!provider.isAvailable(true)) {
                            logger.warn("Issue while trying to mount an external provider ({}) upon startup,"
                                            + " all references to file coming from this mount won't be"
                                            + " available until it is fixed. If you are migrating from Jahia 6.6 this"
                                            + " might be normal until the migration scripts have been completed.",
                                    mountPointTarget.getPath());
                            mountPoint.setMountStatus(JCRMountPointNode.MountStatus.waiting);
                            session.save();
                            providerChecker.checkPeriodically(provider);
                        }
                    }

                    return true;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onEvent(EventIterator events) {
        if (inListener.get()) {
            return;
        }
        try {
            inListener.set(true);
            Map<String, Integer> changeLog = new LinkedHashMap<String, Integer>(1);
            while (events.hasNext()) {
                try {
                    final Event evt = events.nextEvent();
                    final int evtType = evt.getType();
                    if ((evtType & (Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED)) != 0) {
                        // if property-level event -> check ignored properties
                        String propertyName = StringUtils.substringAfterLast(evt.getPath(), "/");
                        if (propertiesToIgnore.contains(propertyName)) {
                            continue;
                        }
                    }
                    setStatus(changeLog, evt.getIdentifier(), evtType);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            for (Map.Entry<String, Integer> change : changeLog.entrySet()) {
                String uuid = change.getKey();
                Integer status = change.getValue();
                JCRStoreProvider p = JCRStoreService.getInstance().getSessionFactory().getProviders().get(uuid);
                unmount(uuid, p);
                if (status != Event.NODE_REMOVED) {
                    mount(uuid, p);
                }
            }
        } finally {
            inListener.set(false);
        }
    }

    private void setStatus(Map<String, Integer> changeLog, String identifier, int evtType) {
        Integer status = changeLog.get(identifier);
        if (status == null) {
            changeLog.put(identifier, evtType);
        } else {
            if ((evtType & (Event.NODE_ADDED + Event.NODE_REMOVED)) != 0) {
                // override change status only in case of node-level event type
                changeLog.put(identifier, evtType);
            }
        }
    }

    private void unmount(String uuid, JCRStoreProvider p) {
        providerChecker.remove(uuid);
        if (p != null) {
            logger.info("Unmounting the provider {} with key {}", p.getMountPoint(), p.getKey());
            p.stop();
        }
    }
}
