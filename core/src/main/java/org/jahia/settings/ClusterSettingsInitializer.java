/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.settings;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.utils.DatabaseUtils;
import org.jgroups.conf.PropertyConverter;
import org.jgroups.conf.PropertyConverters;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jgroups.Global;

import java.io.File;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collections;

import static org.jahia.bin.listeners.JahiaContextLoaderListener.setSystemProperty;
import static org.jahia.settings.StartupOptions.OPTION_RESET_DISCOVERY_INFO;

/**
 * Cluster settings initializer
 */
public class ClusterSettingsInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterSettingsInitializer.class);
    public static final String CLUSTER_HAZELCAST_BIND_PORT = "cluster.hazelcast.bindPort";

    /**
     * Based on DX configuration settings, initialize system properties JGroups and Hazelcast rely on.
     *
     * @param settings settings bean
     */
    public void initClusterSettings(SettingsBean settings) {
        checkNeedDiscoveryInfoReset(settings);

        // Make JGroups prefer IPv4.
        setSystemProperty(Global.IPv4, Boolean.TRUE.toString());

        // Unless it is specified explicitly, we do not allow Hazelcast bind to any address, but rather use the configured (or detected) one.
        if (org.apache.commons.lang.StringUtils.isEmpty(System.getProperty("hazelcast.socket.bind.any"))) {
            setSystemProperty("hazelcast.socket.bind.any", Boolean.FALSE.toString());
        }

        initBindAddress(settings);

        // Expose binding port: use also cluster.tcp.ehcache.jahia.port for backward compatibility with Jahia 6.6
        String bindPort = settings.getString("cluster.tcp.bindPort", settings.getString("cluster.tcp.ehcache.jahia.port", null));
        if (org.apache.commons.lang.StringUtils.isNotEmpty(bindPort)) {
            setSystemProperty("cluster.tcp.bindPort", bindPort);
        }

        setSystemProperty("cluster.configFile.jahia", settings.getString("cluster.configFile.jahia", "tcp.xml"));
        if (System.getProperty(CLUSTER_HAZELCAST_BIND_PORT) == null) {
            setSystemProperty(CLUSTER_HAZELCAST_BIND_PORT, settings.getString(CLUSTER_HAZELCAST_BIND_PORT, "7860"));
        }

        if (System.getProperty("cluster.hazelcast.manager.enabled") == null) {
            setSystemProperty("cluster.hazelcast.manager.enabled", settings.getString("cluster.hazelcast.manager.enabled", "false"));
        }

        if (System.getProperty("cluster.hazelcast.manager.url") == null) {
            setSystemProperty("cluster.hazelcast.manager.url", settings.getString("cluster.hazelcast.manager.url", "http://localhost:8080/mancenter"));
        }
    }

    @SuppressWarnings("deprecation")
    private void checkNeedDiscoveryInfoReset(SettingsBean settings) {

        if (settings.isStartupOptionSet(OPTION_RESET_DISCOVERY_INFO) || Boolean.getBoolean(SettingsBean.JAHIA_BACKUP_RESTORE_SYSTEM_PROP)) {
            try {
                LOGGER.info("Detected startup option for resetting cluster discovery information");
                LOGGER.info("Cleaning database table [JGROUPSPING] ...");
                try {
                    DatabaseUtils.executeStatements(Collections.singletonList("DELETE FROM JGROUPSPING"));
                    LOGGER.info("Database table [JGROUPSPING] successfully cleaned");
                } catch (SQLException e) {
                    LOGGER.error("Unable to clean database table: JGROUPSPING", e);
                }

                LOGGER.info("Search and remove [org.apache.karaf.cellar.discovery.config] file from [bundles-deployed] ...");
                File targetConfig = null;
                File bundlesDeployed = new File(settings.getJahiaVarDiskPath() + File.separator + "bundles-deployed");
                if (bundlesDeployed.exists()) {
                    boolean removed = false;
                    File[] bundleFiles = bundlesDeployed.listFiles();
                    if (bundleFiles != null) {
                        for (File bundleFile : bundleFiles) {
                            if (bundleFile.isDirectory()) {
                                File discoveryConfig = new File(bundleFile, "data" + File.separator + "config" + File.separator + "org" + File.separator + "apache" + File.separator + "karaf" + File.separator + "cellar" + File.separator + "discovery.config");
                                if (discoveryConfig.exists()) {
                                    targetConfig = discoveryConfig;
                                    removed = discoveryConfig.delete();
                                    break;
                                }
                            }
                        }
                    }

                    if (removed) {
                        LOGGER.info("Config file [org.apache.karaf.cellar.discovery.config] at location {} successfully removed", targetConfig);
                    } else {
                        if (targetConfig != null) {
                            LOGGER.warn("Unable to delete the [org.apache.karaf.cellar.discovery.config] file at location {}", targetConfig);
                        } else {
                            LOGGER.warn("Config file [org.apache.karaf.cellar.discovery.config] not found");
                        }
                    }
                } else {
                    LOGGER.error("Unable to find [bundles-deployed] under {}", settings.getJahiaVarDiskPath());
                }
            } catch (Exception e) {
                LOGGER.error("Unexpected error resetting cluster discovery information. Cause: {}",e.getMessage(), e);
            }
        }
    }

    // DX EE clustering heavily relies on JGroups and Hazelcast.
    //
    // Particularly, Hazelcast node discovery is customized to rely on discovery done by JGroups via reading JGroups' ping data from the database
    // (see JahiaDiscoveryService).
    //
    // The specificity of this approach is that JGroups transforms the bind address configured, before storing it as a part of the ping data, while
    // Hazelcast does not transform it in any way. As a result, nodes discovered by JGroups and presented to Hazelcast by JahiaDiscoveryService
    // can not be recognized by Hazelcast in certain cases, which prevents building the Hazelcast cluster.
    //
    // In order to avoid this effect, we perform JGroups-like transformation of the bind address before passing it to JGroups/Hazelcast, so that
    // there is no need for this transformation afterwards, and JGroups/Hazelcast speak a common language. For these transformations we use JGRoups'
    // own code as much as possible.
    private void initBindAddress(SettingsBean settings) {

        // Expose tcp ip binding address: use also cluster.tcp.start.ip_address for backward compatibility with Jahia 6.6
        String bindAddress = settings.getString("cluster.tcp.bindAddress", settings.getString("cluster.tcp.start.ip_address", null));
        if (org.apache.commons.lang.StringUtils.isEmpty(bindAddress)) {
            String jgroupsBindAddress = System.getProperty(Global.BIND_ADDR);
            if (org.apache.commons.lang.StringUtils.isNotEmpty(jgroupsBindAddress)) {
                bindAddress = jgroupsBindAddress;
                LOGGER.info("Using value, supplied via jgroups.bind_addr system property, for the bind address: {}", bindAddress);
            }
        }

        InetAddress ip;
        if (StringUtils.isEmpty(bindAddress)) {
            LOGGER.info("No bind address configured, using auto-detected non-loopback one {}", bindAddress);
            ip = getNonLoopbackIp();
            if (ip == null) {
                LOGGER.warn("Unable to detect non-loopback network bind address."
                        + " Please configure cluster.tcp.bindAddress in jahia.node.properties explicitly."
                        + " For this start we will take the loopback IP.");
                ip = getLocalHostIp();
            } else {
                LOGGER.info("Detected non-loopback network bind address: {}", ip.getHostAddress());
            }
        } else {

            // In case there is host name rather than IP configured, convert it to IP to simulate JGroups behavior and avoid inconsistency between
            // JGroups and Hazelcast.
            ip = toIp(bindAddress);
            if (!ip.getHostAddress().equals(bindAddress)) {
                LOGGER.info("Converted configured bind address to IP: {} to {}", bindAddress, ip.getHostAddress());
            }

            // For unclear reasons, JGRoups converts any loopback IP like '127.0.0.N', except for '127.0.0.1', to either automatically detected non-loopback
            // one or just '127.0.0.1'. We perform this transformation to '127.0.0.1' beforehand to avoid inconsistency between JGroups and Hazelcast.
            if (ip.isLoopbackAddress()) {
                InetAddress normalizedIp = getLocalHostIp();
                if (!normalizedIp.equals(ip)) {
                    ip = normalizedIp;
                    LOGGER.info("Normalized the loopback bind IP: {} to {}", bindAddress, ip.getHostAddress());
                }
            }
        }

        LOGGER.info("Setting JGroups bind address to: {}", ip.getHostAddress());
        setSystemProperty("cluster.tcp.bindAddress", ip.getHostAddress());
        setSystemProperty(Global.BIND_ADDR, ip.getHostAddress());
    }

    private static InetAddress getNonLoopbackIp() {
        try {
            return Util.getNonLoopbackAddress();
        } catch (SocketException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private static InetAddress toIp(String address) {
        PropertyConverter propertyConverter = new PropertyConverters.Default();
        try {
            return (InetAddress) propertyConverter.convert(null, InetAddress.class, null, address, true);
        } catch (Exception e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private static InetAddress getLocalHostIp() {
        try {
            return Util.getLocalhost(Util.getIpStackType());
        } catch (UnknownHostException e) {
            throw new JahiaRuntimeException(e);
        }
    }
}
