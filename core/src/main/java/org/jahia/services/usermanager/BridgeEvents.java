/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.usermanager;

import org.jahia.osgi.FrameworkService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Send event in the OSGI context when an old users or groups provider need to do an operation (register, unregister)
 * Modules can listen on this events
 * @author kevan
 */
public class BridgeEvents {
    public static final String USERS_GROUPS_BRIDGE_EVENT_KEY = "org/jahia/usersgroups/";
    public static final String USER_PROVIDER_REGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "userProvider/REGISTER";
    public static final String USER_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "userProvider/UNREGISTER";
    public static final String GROUP_PROVIDER_REGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "groupProvider/REGISTER";
    public static final String GROUP_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY = USERS_GROUPS_BRIDGE_EVENT_KEY + "groupProvider/UNREGISTER";
    public static final String PROVIDER_KEY = "provider";

    protected static void sendEvent(String providerKey, String eventKey){
        BundleContext context = FrameworkService.getBundleContext();
        ServiceReference ref = context.getServiceReference(EventAdmin.class.getName());
        if (ref != null)
        {
            EventAdmin eventAdmin = (EventAdmin) context.getService(ref);

            Dictionary properties = new Hashtable();
            properties.put(PROVIDER_KEY, providerKey);

            Event reportGeneratedEvent = new Event(eventKey, properties);
            eventAdmin.postEvent(reportGeneratedEvent);
        }
    }
}
