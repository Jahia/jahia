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
package org.jahia.ajax.gwt.commons.server;

import org.atmosphere.config.service.BroadcasterService;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.DefaultBroadcaster;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;

import java.net.URI;

/**
 * Extend atmoshpere default broadcaster to broadcast messages on a JGroups channel
 */
@BroadcasterService
public class JGroupsBroadcaster extends DefaultBroadcaster {

    @Override
    @SuppressWarnings("java:S112")
    public Broadcaster initialize(String name, URI uri, AtmosphereConfig config) {
        Broadcaster broadcaster = super.initialize(name, uri, config);

        if (SettingsBean.getInstance().isClusterActivated() && SettingsBean.getInstance().getBoolean("atmosphere.jgroups", true)) {
            try {
                JGroupsChannelImpl jc = ((ChannelHolderImpl)SpringContextSingleton.getBean("org.jahia.ajax.gwt.commons.server.ChannelHolderImpl")).getChannel();
                broadcaster.getBroadcasterConfig().addFilter(new JGroupsFilter(jc, broadcaster));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create JGroupsChannel", e);
            }
        }
        return broadcaster;
    }

}
