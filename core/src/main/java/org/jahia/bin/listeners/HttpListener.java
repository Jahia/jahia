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
package org.jahia.bin.listeners;

import javax.servlet.*;
import javax.servlet.http.*;

public interface HttpListener extends ServletContextListener,
        ServletRequestListener,
        ServletRequestAttributeListener,
        HttpSessionListener,
        HttpSessionActivationListener,
        HttpSessionAttributeListener,
        HttpSessionBindingListener,
        ServletContextAttributeListener {

    @Override
    default void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    default void contextDestroyed(ServletContextEvent sce) {

    }

    @Override
    default void attributeAdded(ServletContextAttributeEvent event) {

    }

    @Override
    default void attributeRemoved(ServletContextAttributeEvent event) {

    }

    @Override
    default void attributeReplaced(ServletContextAttributeEvent event) {

    }

    @Override
    default void attributeAdded(ServletRequestAttributeEvent srae) {

    }

    @Override
    default void attributeRemoved(ServletRequestAttributeEvent srae) {

    }

    @Override
    default void attributeReplaced(ServletRequestAttributeEvent srae) {

    }

    @Override
    default void requestDestroyed(ServletRequestEvent sre) {

    }

    @Override
    default void requestInitialized(ServletRequestEvent sre) {

    }

    @Override
    default void sessionWillPassivate(HttpSessionEvent se) {

    }

    @Override
    default void sessionDidActivate(HttpSessionEvent se) {

    }

    @Override
    default void attributeAdded(HttpSessionBindingEvent event) {

    }

    @Override
    default void attributeRemoved(HttpSessionBindingEvent event) {

    }

    @Override
    default void attributeReplaced(HttpSessionBindingEvent event) {

    }

    @Override
    default void valueBound(HttpSessionBindingEvent event) {

    }

    @Override
    default void valueUnbound(HttpSessionBindingEvent event) {

    }

    @Override
    default void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    default void sessionDestroyed(HttpSessionEvent se) {

    }
}
