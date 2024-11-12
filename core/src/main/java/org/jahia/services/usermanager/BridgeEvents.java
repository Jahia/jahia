/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import org.jahia.osgi.BundleUtils;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Send event in the OSGI context when an old users or groups provider need to do an operation (register, unregister)
 * Modules can listen on this events
 *
 * @author kevan
 */
public class BridgeEvents {
    public static final String USERS_GROUPS_BRIDGE_EVENT_KEY = "org/jahia/usersgroups/";
    public static final String USER_PROVIDER_REGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "userProvider/REGISTER";
    public static final String USER_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "userProvider/UNREGISTER";
    public static final String GROUP_PROVIDER_REGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "groupProvider/REGISTER";
    public static final String GROUP_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "groupProvider/UNREGISTER";
    public static final String PROVIDER_KEY = "provider";

    private BridgeEvents() {
    }

    protected static void sendEvent(String providerKey, String eventKey) {
        EventAdmin eventAdmin = BundleUtils.getOsgiService(EventAdmin.class.getName());
        if (eventAdmin != null) {
            Dictionary properties = new Hashtable();
            properties.put(PROVIDER_KEY, providerKey);

            Event reportGeneratedEvent = new Event(eventKey, properties);
            eventAdmin.postEvent(reportGeneratedEvent);
        }
    }
}
