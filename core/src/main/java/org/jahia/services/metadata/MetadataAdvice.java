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

 package org.jahia.services.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.events.JahiaEventListenerInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.spring.aop.interceptor.SilentJamonPerformanceMonitorInterceptor;
import org.springframework.util.StringUtils;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class MetadataAdvice extends JahiaEventListener implements MethodInterceptor {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(MetadataAdvice.class);

    private static final org.apache.log4j.Logger monitorLogger = org.apache.log4j.Logger.getLogger(SilentJamonPerformanceMonitorInterceptor.class);

    protected Map listenersMap = new HashMap();
    protected boolean useAggregated = true;
    protected List eventsNotSupportingAggregateMode = new ArrayList();

    public Map getListenersMap() {
        checkProxy();
        return this.listenersMap;
    }

    public void setListenersMap(Map listenersMap) {
        this.listenersMap = listenersMap;
    }

    private void checkProxy() {
        if (this.listenersMap == null) {
            this.listenersMap = new HashMap();
        }
    }

    public boolean isUseAggregated() {
        return useAggregated;
    }

    public void setUseAggregated(boolean useAggregated) {
        this.useAggregated = useAggregated;
    }

    public List getEventsNotSupportingAggregateMode() {
        return eventsNotSupportingAggregateMode;
    }

    public void setEventsNotSupportingAggregateMode(List eventsNotSupportingAggregateMode) {
        this.eventsNotSupportingAggregateMode = eventsNotSupportingAggregateMode;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {

        String methodName = invocation.getMethod().getName();
        boolean aggregatedEvent = methodName.startsWith("aggregated");
        if (aggregatedEvent) {
            methodName = methodName.substring("aggregated".length());
            methodName = StringUtils.uncapitalize(methodName);
        }
if (useAggregated) {
            if (aggregatedEvent) {
                if ( this.eventsNotSupportingAggregateMode.contains(methodName) ){
                    // an event method that do not support aggregate so skip it
                    return null;
                }
            } else {
                if ( !this.eventsNotSupportingAggregateMode.contains(methodName) ){
                    // an event method that support aggregate so ignore it now
                    return null;
                }
            }
        }

        logger.debug(" method name :" + methodName);
        List listeners = (List)
                this.listenersMap.get(methodName);
        if (listeners != null) {
            Object[] args = invocation.getArguments();

            JahiaEvent event = (JahiaEvent) args[0];

            // invoke this listener first
            if (aggregatedEvent){
                invokeAggregatedListeners(listeners, methodName, event);
            } else {
                MetadataEventListener eventListener = new MetadataEventListener();
                invokeListener(eventListener, methodName, event);
                if (!eventListener.isMetadata()) {
                    event = (JahiaEvent) args[0];
                    // then invoke registered listener
                    Iterator iterator = listeners.iterator();
                    JahiaEventListenerInterface listener = null;
                    while (iterator.hasNext()) {
                        listener = (JahiaEventListenerInterface) iterator.next();
                        invokeListener(listener, methodName, event);
                    }
                }
            }
        }
        Object rval = invocation.proceed();
        return rval;
    }

    /**
     * wakes up a specific method on a given listener
     *
     * @param methodName method name to ring
     * @param theEvent   the event to send
     */
    private void invokeListener(JahiaEventListenerInterface listener,
                                String methodName, JahiaEvent theEvent)
            throws JahiaException {
        if (listener == null || methodName == null) {
            return;
        }
        try {
            Class theClass = listener.getClass();
            Class eventClass = theEvent.getClass();
            Method theMethod = theClass.getMethod(methodName,
                    new Class[]{eventClass});
            if (theMethod != null) {
                Monitor listenerMonitor = null;
                if (monitorLogger.isDebugEnabled()) listenerMonitor = MonitorFactory.start(theMethod.toString());
                theMethod.invoke(listener,
                        new Object[]{(org.jahia.data.events.
                                JahiaEvent) theEvent});
                if (monitorLogger.isDebugEnabled()) listenerMonitor.stop();
            }
        } catch (NoSuchMethodException nsme) {
            String errorMsg =
                    "NoSuchMethodException when trying to execute method " +
                            methodName + "(" + nsme.getMessage() + ")";
            logger.error(errorMsg, nsme);
            throw new JahiaException("NoSuchMethodException",
                    errorMsg, JahiaException.LISTENER_ERROR,
                    JahiaException.WARNING_SEVERITY, nsme);
        } catch (InvocationTargetException ite) {
            String errorMsg =
                    "InvocationTargetException when trying to execute method " +
                            methodName + "(" + ite.getTargetException().getMessage() + ")";
            logger.error(errorMsg, ite.getTargetException());
            throw new JahiaException("InvocationTargetException",
                    errorMsg, JahiaException.LISTENER_ERROR,
                    JahiaException.WARNING_SEVERITY,
                    ite.getTargetException());
        } catch (IllegalAccessException iae) {
            String errorMsg =
                    "IllegalAccessException when trying to execute method " +
                            methodName + "(" + iae.getMessage() + ")";
            logger.error(errorMsg, iae);
            throw new JahiaException("IllegalAccessException",
                    errorMsg, JahiaException.LISTENER_ERROR,
                    JahiaException.WARNING_SEVERITY, iae);
        }
    }

    /**
     * invoke aggregate listeners
     *
     * @param methodName method name to ring
     * @param theEvent   the event to send
     */
    private void invokeAggregatedListeners(List listeners,
                                           String methodName, JahiaEvent theEvent)
            throws JahiaException {
        if (listeners == null || listeners.size()==0 || methodName == null) {
            return;
        }
        List allEvents = (List) theEvent.getObject();
        //@fixme : this optimization is too basic
        //         i.e. considering ObjectChanged event we have to keep the objectChangedEvent with the nost recent time
        //         not just keeping one by content object.
        /*
        Set viewed = new HashSet();
        for (int i = 0; i < allEvents.size(); ) {
            JahiaEvent je = (JahiaEvent) allEvents.get(i);
            Object k;
            if (je.getObject() instanceof ContentObject) {
                ContentObject object = (ContentObject) je.getObject();
                k = object.getObjectKey().toString();
            } else {
                k = je.getObject();
            }
            if (k == null) {
                allEvents.remove(i);
            } else {
                if (viewed.contains(k)) {
                    allEvents.remove(i);
                } else {
                    i++;
                }
                viewed.add(k);
            }
        }*/
        MetadataEventListener eventListener = null;
        for (Iterator iterator = allEvents.iterator(); iterator.hasNext();) {
            JahiaEvent event = (JahiaEvent) iterator.next();
            eventListener = new MetadataEventListener();
            invokeListener(eventListener,methodName,event);
            if ( !eventListener.isMetadata() ){
                // then invoke registered listener
                Iterator it = listeners.iterator();
                JahiaEventListenerInterface listener = null;
                while (it.hasNext()) {
                    listener = (JahiaEventListenerInterface) it.next();
                    invokeListener(listener, methodName, event);
                }
            }
        }
    }

}
