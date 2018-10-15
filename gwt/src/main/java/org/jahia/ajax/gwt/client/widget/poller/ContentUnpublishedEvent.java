/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.jahia.ajax.gwt.client.util.JsonSerializable;

/**
 * Notification event sent after unpublication of a content has been performed.
 * 
 * @author Sergiy Shyrkov
 *
 */
public class ContentUnpublishedEvent extends RPCEvent implements Serializable, JsonSerializable {

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
    public Map<String, Object> getDataForJsonSerialization() {
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
