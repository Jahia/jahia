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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import java.io.Serializable;

/**
 * Bean object for cluster wide flush event.
 */
public class CacheClusterEvent implements Serializable {

    private static final String EMPTY_EVENT = "";

    private final String event;
    private final long clusterRevision;

    /**
     * Initializes an instance of this class.
     *
     * @param event the cluster event message
     * @param clusterRevision current cluster journal revision to consider ordering of events. Use negative value (say
     *            {@link Integer#MIN_VALUE}) to execute event immediately on receipt.
     */
    public CacheClusterEvent(String event, long clusterRevision) {
        this.event = event;
        this.clusterRevision = clusterRevision;
    }

    /**
     * The cluster event message.
     *
     * @return a custom event message or an empty string if no message was provided for this event
     */
    public String getEvent() {
        // return an empty string instead of null for backward compatibility
        return event != null ? event : EMPTY_EVENT;
    }

    /**
     * The target cluster journal revision to consider when processing this event.
     *
     * @return cluster journal revision to consider when processing this event
     */
    public long getClusterRevision() {
        return clusterRevision;
    }
}
