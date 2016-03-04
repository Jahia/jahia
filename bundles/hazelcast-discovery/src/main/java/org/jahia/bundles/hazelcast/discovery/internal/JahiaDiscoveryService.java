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
package org.jahia.bundles.hazelcast.discovery.internal;

import org.apache.karaf.cellar.core.discovery.DiscoveryService;
import org.jahia.bundles.hazelcast.discovery.internal.osgi.Activator;

import org.jahia.utils.DatabaseUtils;
import org.jgroups.protocols.PingData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import org.jgroups.util.*;

/**
 * Jahia Cluster Discovery Service
 * @author achaabni
 */
public class JahiaDiscoveryService implements DiscoveryService{

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaDiscoveryService.class);

    public static final String JGROUPS_REPLICATION_TYPE_CHANNEL_CLUSTER_EHCACHE_JAHIA = "JGroupsReplication:type=channel,cluster=\"ehcache-jahia\"";

    public static final String CLUSTER_NAME_ATTRIBUTE = "cluster_name";

    public static final String JGROUPSPING_TABLE = "JGROUPSPING";


    @Override
    public Set<String> discoverMembers() {
        Set<String> members = new HashSet<String>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName channel = new ObjectName(JGROUPS_REPLICATION_TYPE_CHANNEL_CLUSTER_EHCACHE_JAHIA);
            Connection conn = DatabaseUtils.getDatasource().getConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            stmt = conn.prepareStatement("SELECT * FROM " + JGROUPSPING_TABLE+ " WHERE "+ CLUSTER_NAME_ATTRIBUTE
                            +"=" +"\'" +
            mBeanServer
                    .getAttribute
                    (channel, CLUSTER_NAME_ATTRIBUTE)+ "\'" );
            rs = stmt.executeQuery();
            while (rs.next()){
                Object data = rs.getObject("ping_data");
                byte[] byteData = null;
                if (data instanceof Blob) {
                    Blob blobData = (Blob) data;
                    byteData = blobData.getBytes(1, (int) blobData.length());
                } else {
                    byteData = (byte[]) data;
                }
                PingData pd = (PingData) Util.streamableFromByteBuffer(PingData.class, byteData);
                members.add(pd.getPhysicalAddrs().toString());
                //close the connection.
                conn.close();
            }
            logger.info("Members : " + members);
        return members;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return members;
    }

    @Override
    public void signIn() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void signOut() {

    }
}
