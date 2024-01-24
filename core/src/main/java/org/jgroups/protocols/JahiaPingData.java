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
        super(data.getAddress(), data.isServer(), data.getLogicalName(), data.getPhysicalAddr());
        this.coord(data.isCoord());
        this.hazelcastPort = hazelcastPort;
    }

    /**
     * Returns the host address (in form of <code>&lt;address&gt;:&lt;port&gt;</code>) which is used for Hazelcast communication.
     *
     * @return the host address (in form of <code>&lt;address&gt;:&lt;port&gt;</code>) which is used for Hazelcast communication
     */
    public String getHazelcastAddress() {
        if (physical_addr != null) {
            String jGroupsAddr = physical_addr.toString();
            return StringUtils.substringBeforeLast(jGroupsAddr, ":") + ":" + hazelcastPort;
        }
        return null;
    }

    @Override
    public void readFrom(DataInput instream) throws Exception {
        super.readFrom(instream);
        hazelcastPort = Util.readObject(instream).toString();
    }

    @Override
    public void writeTo(DataOutput outstream) throws Exception {
        super.writeTo(outstream);
        Util.writeObject(hazelcastPort, outstream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
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
