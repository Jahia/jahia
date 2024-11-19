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
