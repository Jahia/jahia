/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.commons.server;

import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.settings.SettingsBean;
import org.jgroups.JChannel;
import org.jgroups.jmx.JmxConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 * Class managing the JGroups Channel for Atmosphere
 */
public class ChannelHolderImpl implements InitializingBean, DisposableBean, ChannelHolder {

    private static final String JMX_DOMAIN_NAME = "JGroupsReplication";

    private static final Logger logger = LoggerFactory.getLogger(ChannelHolderImpl.class);

    private JGroupsChannelImpl channel;

    private String clusterName;

    private SpringJackrabbitRepository jackrabbitRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (SettingsBean.getInstance().isClusterActivated() && SettingsBean.getInstance().getBoolean("atmosphere.jgroups", true)) {
            JChannel jChannel = new JChannel(System.getProperty("cluster.configFile.jahia", "tcp.xml"));
            this.channel = new JGroupsChannelImpl(jChannel, clusterName, jackrabbitRepository);
            this.channel.init();
            registerMBeans();
        }
    }

    @Override
    public void destroy() throws Exception {
        unregisterMBeans();
        channel.destroy();
    }

    @Override
    public JGroupsChannelImpl getChannel() {
        return channel;
    }

    public String getClusterName() {
        return clusterName;
    }

    private void registerMBeans() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (mBeanServer != null) {
            try {
                JmxConfigurator.registerChannel(this.channel.jChannel, mBeanServer, JMX_DOMAIN_NAME, clusterName, true);
            } catch (Exception e) {
                logger.warn("Unable to register JMX beans for the JGroups channel {} due to {}", clusterName, e.getMessage());
            }
        }
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    private void unregisterMBeans() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (mBeanServer != null) {
            try {
                JmxConfigurator.unregisterChannel(this.channel.jChannel, mBeanServer, JMX_DOMAIN_NAME, clusterName);
            } catch (Exception e) {
                logger.warn("Unable to unregister JMX beans for the JGroups channel {} due to {}", clusterName, e.getMessage());
            }
        }
    }

    public void setJackrabbitRepository(SpringJackrabbitRepository jackrabbitRepository) {
        this.jackrabbitRepository = jackrabbitRepository;
    }
}
