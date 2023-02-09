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
package org.jahia.ajax.gwt.client.widget.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.jahia.ajax.gwt.client.util.EventDataSupplier;

/**
 * Notification event sent after unpublication of a content has been performed.
 *
 * @author Sergiy Shyrkov
 *
 */
public class ContentUnpublishedEvent extends RPCEvent implements Serializable, EventDataSupplier {

    private static final long serialVersionUID = -4629106522818813015L;

    private List<String> uuids;

    /**
     * Initializes an instance of this class.
     */
    public ContentUnpublishedEvent() {
        this(null);
    }

    /**
     * Initializes an instance of this class.
     *
     * @param uuids the list of node UUIDs the unpublication was performed for
     */
    public ContentUnpublishedEvent(List<String> uuids) {
        super();
        this.uuids = uuids != null ? new ArrayList<String>(uuids) : new ArrayList<String>();
    }

    @Override
    public Map<String, Object> getEventData() {
        Map<String, Object> data = new HashMap<String, Object>(3);
        data.put("type", "contentUnpublished");
        data.put("uuids", getUuids());

        return data;
    }

    public List<String> getUuids() {
        return uuids;
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }

}
