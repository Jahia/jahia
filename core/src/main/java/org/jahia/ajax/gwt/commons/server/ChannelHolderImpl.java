/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
