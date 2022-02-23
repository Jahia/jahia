/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jgroups.protocols;

import org.apache.commons.lang.StringUtils;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Objects;

/**
 * Extended information about cluster node, additionally containing port for Hazelcast communication.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaPingData extends PingData {

    private String hazelcastPort;

    /**
     * Initializes an instance of this class.
     */
    public JahiaPingData() {
        super();
    }

    /**
     * Initializes an instance of this class without the cluster membership view.
     *
     * @param data          the cluster node data
     * @param hazelcastPort the port number for Hazelcast communication
     */
    JahiaPingData(PingData data, String hazelcastPort) {
        super(data.sender, null, data.is_server, data.logical_name, data.physical_addrs);
        this.hazelcastPort = hazelcastPort;
    }

    /**
     * Returns the host address (in form of <code>&lt;address&gt;:&lt;port&gt;</code>) which is used for Hazelcast communication.
     *
     * @return the host address (in form of <code>&lt;address&gt;:&lt;port&gt;</code>) which is used for Hazelcast communication
     */
    public String getHazelcastAddress() {
        if (physical_addrs != null && !physical_addrs.isEmpty()) {
            String jGroupsAddr = physical_addrs.iterator().next().toString();
            return StringUtils.substringBeforeLast(jGroupsAddr, ":") + ":" + hazelcastPort;
        }
        return null;
    }

    @Override
    public void readFrom(DataInput instream) throws Exception {
        super.readFrom(instream);
        hazelcastPort = Util.readString(instream);
    }

    @Override
    public void writeTo(DataOutput outstream) throws Exception {
        super.writeTo(outstream);
        Util.writeString(hazelcastPort, outstream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        JahiaPingData that = (JahiaPingData) o;
        return hazelcastPort.equals(that.hazelcastPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hazelcastPort);
    }
}
