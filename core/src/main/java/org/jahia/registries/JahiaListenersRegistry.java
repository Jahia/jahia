/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//
//  EV      25.11.2000
//  MJ      23.03.2001  fixed addListener, removeListener, getListener
//

package org.jahia.registries;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListenerInterface;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.jahia.spring.aop.interceptor.SilentJamonPerformanceMonitorInterceptor;
import org.jahia.hibernate.manager.SpringContextSingleton;

public class JahiaListenersRegistry {

    private static Logger logger = Logger.getLogger(JahiaListenersRegistry.class);

    private static final Logger monitorLogger = Logger.getLogger(SilentJamonPerformanceMonitorInterceptor.class);

    private static JahiaListenersRegistry instance = null;

    private Map<String, JahiaEventListenerInterface> classNameToInstance = new HashMap<String, JahiaEventListenerInterface>();
    private boolean initialized = false;

    public List<JahiaEventListenerInterface> listeners = new LinkedList<JahiaEventListenerInterface>();

    /***
     * constructor
     *
     */
    public JahiaListenersRegistry () {
    } // end constructor

    public List<JahiaEventListenerInterface> getListeners() {
        return listeners;
    }

    public void setListeners(List<JahiaEventListenerInterface> listeners) {
        this.listeners = listeners;
        for (JahiaEventListenerInterface listener : listeners) {
            classNameToInstance.put(listener.getClass().getName(), listener);
        }
    }

    /***
     * returns a single instance of the registry
     *
     */
    public static synchronized JahiaListenersRegistry getInstance () {
        if (instance == null) {
            instance = (JahiaListenersRegistry) SpringContextSingleton.getInstance().getContext().getBean("listenersRegistry");
        }
        return instance;
    } // end getInstance

    /***
     * init
     *
     * @param        config              a ServletConfig object, with "homedir" property
     *
     */
    public void init (ServletConfig config) {
        if (!initialized) {
            initialized = true;
        }
    } // end init

    /***
     * gets a listener by its class name
     *
     * @param        listenerClassName        the listener name
     * @return       a JahiaEventListenerInterface object
     *
     */
    public JahiaEventListenerInterface getListenerByClassName (String listenerClassName) {
        return classNameToInstance.get(listenerClassName);
    } // end getListener

    /***
     * hot-plugs a listener instance into the registry
     *
     * @param listener JahiaEventListenerInterface
     * @return boolean
     */
    public synchronized boolean addListener (JahiaEventListenerInterface listener) {
        boolean out = true;
        if (listener != null) {
            String className = listener.getClass().getName();
            classNameToInstance.put(className, listener);
            listeners.add(listener);
        }
        return out;
    } // end addListener

    /***
     * hot-removes a listener from the registry
     *
     * @param        listener          the listener to remove
     * @return       true if everything went okay, false if not
     *
     */
    public synchronized boolean removeListener(JahiaEventListenerInterface listener) {
        classNameToInstance.remove(listener.getClass().getName());
        listeners.remove(listener);
        return true;
    } // end removeListener

    /***
     * hot-removes a listener from the registry
     *
     * @param        className          the class name of listener to remove
     * @return       true if everything went okay, false if not
     *
     */
    public synchronized boolean removeListenerByClassName(String className) {
        JahiaEventListenerInterface listener = classNameToInstance.get(className);
        if (listener != null) {
            listeners.remove(listener);
            classNameToInstance.remove(className);
        }
        return true;
    } // end removeListener

    /***
     * wakes up a specific method of all listeners
     *
     * @param        methodName          method name to ring
     * @param        theEvent            the event to send
     *
     */
    public void wakeupListeners (String methodName, JahiaEvent theEvent) {
        for (JahiaEventListenerInterface theListener : getListeners()) {
            try {
                Class<? extends JahiaEventListenerInterface> theClass = theListener.getClass();
                Class<? extends JahiaEvent> eventClass = theEvent.getClass();
                Method theMethod = theClass.getMethod(methodName, new Class[] { eventClass });
                if (theMethod != null) {
                    Monitor listenerMonitor = null;
                    if (monitorLogger.isDebugEnabled()) {
                        listenerMonitor = MonitorFactory.start(theMethod.toString());
                    }
                    //logger.error("Calling " + theMethod.toString() + " using " + theListener.toString() + " for " + theEvent.toString());
                    theMethod.invoke(theListener, theEvent);
                    if (monitorLogger.isDebugEnabled() && listenerMonitor != null) {
                        listenerMonitor.stop();
                    }
                }
            } catch (NoSuchMethodException nsme) {
                logger.error( "NoSuchMethodException when trying to execute method " + methodName + ". Cause: " + nsme.getMessage(), nsme);
            } catch (InvocationTargetException ite) {
                logger.error("InvocationTargetException when trying to execute method " + methodName + ". Cause: " + ite.getMessage(), ite.getTargetException());
            } catch (IllegalAccessException iae) {
                logger.error("IllegalAccessException when trying to execute method " + methodName + ". Cause: " + iae.getMessage(), iae);
            }
        }
    } // end wakeupListener

} // end JahiaListenersRegistry
