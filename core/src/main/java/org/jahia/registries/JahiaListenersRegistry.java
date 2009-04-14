/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
                Class theClass = theListener.getClass();
                Class eventClass = theEvent.getClass();
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
