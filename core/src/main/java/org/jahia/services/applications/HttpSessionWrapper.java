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
package org.jahia.services.applications;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * This wrapper ensures the fact that Sessions values are not shared between applications
 * running under Jahia. This isn't the best solution because ideally we should reimplement
 * the SessionManager thread in order to make sure that Session objects are never shared.
 * For development time reasons, we have chose to just modify the keys of storage for
 * attribute names to make sure they are unique.
 *
 * @author Serge Huber
 * @version 1.0
 */
public class HttpSessionWrapper implements HttpSession {

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (HttpSessionWrapper.class);

    private String appName;
    private String contextID;
    private HttpSession originalSession;
    private final static String KEY_PREFIX = "org.jahia.";
    private final static String KEY_SEPARATOR = ".";
    private final static boolean debugOutput = false;
    private boolean inheritJahiaSessionAttributes = false;

    /**
     * This constructor sets the wrapping parameters internally. Nothing special done here.
     *
     * @param origSession                   Original session object around which this wrapper wraps :)
     * @param applicationName               Name of the application used to create unique keys. FIXME : This key is
     * @param contextID                     the context ID
     * @param inheritJahiaSessionAttributes not checked to see if it is globally unique
     */
    public HttpSessionWrapper (HttpSession origSession,
                               String applicationName,
                               String contextID,
                               boolean inheritJahiaSessionAttributes) {
        if (debugOutput) {
            logger.debug ("Creating session wrapper for application [" +
                    applicationName + "], contextID [" + contextID +
                    "] from original session object [" + origSession.toString () + "]");
        }
        originalSession = origSession;
        appName = applicationName;
        this.contextID = contextID;
        this.inheritJahiaSessionAttributes = inheritJahiaSessionAttributes;
        if (originalSession.getAttribute (KEY_PREFIX + appName + KEY_SEPARATOR + contextID) == null) {
            originalSession.setAttribute (KEY_PREFIX + appName + KEY_SEPARATOR + contextID,
                    new HashMap());
        }
    }

    public long getCreationTime () {
        return originalSession.getCreationTime ();
    }

    public String getId () {
        return originalSession.getId ();
    }

    public long getLastAccessedTime () {
        return originalSession.getLastAccessedTime ();
    }

    public void setMaxInactiveInterval (int interval) {
        originalSession.setMaxInactiveInterval (interval);
    }

    public int getMaxInactiveInterval () {
        return originalSession.getMaxInactiveInterval ();
    }

    /**
     * @deprecated
     */
    public HttpSessionContext getSessionContext () {
        return originalSession.getSessionContext ();
    }

    public ServletContext getServletContext () {
        //return originalSession.getServletContext();
        // the following is a way to test if the servlet API contains this
        // method. This is the only way to allow support for old versions
        // of the yet unfinalized servlet API
        try {
            Class sessionClass = originalSession.getClass ();
            java.lang.reflect.Method theMethod = sessionClass.getMethod ("getServletContext", (Class[])null);
            if (theMethod == null) {
                return null;
            } else {
                Object returnValue = theMethod.invoke (originalSession, (Object[])null);
                if (returnValue instanceof ServletContext) {
                    return (ServletContext) returnValue;
                } else {
                    return null;
                }
            }
        } catch (Exception t) {
            return null;
        }
    }

    public Object getAttribute (String name) {
        if (debugOutput) {
            logger.debug (
                    "emulatedSession.getAttribute(" + name + ") for app [" + KEY_PREFIX + appName + "]");
        }
        Map appAttributes = (Map) originalSession.getAttribute (
                KEY_PREFIX + appName + KEY_SEPARATOR + contextID);
        if (debugOutput) {
            logger.debug ("...result=[" + appAttributes.get (name) + "]");
        }
        // Try the application attributes, then the global attributes if the
        // flag has been activated.
        Object result = appAttributes.get (name);
        if (inheritJahiaSessionAttributes) {
            if (result == null)
                result = originalSession.getAttribute (name);
        }
        return result;
    }

    /**
     * @deprecated
     */
    public Object getValue (String name) {
        return getAttribute (name);
    }

    public Enumeration getAttributeNames () {
        Map appAttributes = (Map) originalSession.getAttribute (
                KEY_PREFIX + appName + KEY_SEPARATOR + contextID);
        return Collections.enumeration(appAttributes.keySet());
    }

    /**
     * @deprecated
     */
    public String[] getValueNames () {
        Map appAttributes = (Map) originalSession.getAttribute (
                KEY_PREFIX + appName + KEY_SEPARATOR + contextID);
        return (String[]) appAttributes.keySet().toArray(new String[] {});
    }

    public void setAttribute (String name,
                              Object value) {
        Map appAttributes = (Map) originalSession.getAttribute (
                KEY_PREFIX + appName + KEY_SEPARATOR + contextID);
        appAttributes.put (name, value);
    }

    /**
     * @deprecated
     */
    public void putValue (java.lang.String name,
                          java.lang.Object value) {
        setAttribute (name, value);
    }

    public void removeAttribute (String name) {
        Map appAttributes = (Map) originalSession.getAttribute (
                KEY_PREFIX + appName + KEY_SEPARATOR + contextID);
        appAttributes.remove (name);
    }

    /**
     * @deprecated
     */
    public void removeValue (String name) {
        removeAttribute (name);
    }

    public void invalidate () {
        originalSession.removeAttribute (KEY_PREFIX + appName + KEY_SEPARATOR + contextID);
        // originalSession.invalidate();
        if (originalSession.getAttribute (KEY_PREFIX + appName + KEY_SEPARATOR + contextID) == null) {
            originalSession.setAttribute (KEY_PREFIX + appName + KEY_SEPARATOR + contextID,
                    new HashMap());
        }
    }

    public boolean isNew () {
        return originalSession.isNew ();
    }

}