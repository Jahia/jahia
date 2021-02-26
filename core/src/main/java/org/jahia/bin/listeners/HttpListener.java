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
