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
package org.jahia.exceptions;


/**
 *
 * @author  Fulco Houkes
 * @version 1.0
 * @since   Jahia 4.0
 */
public class JahiaPersistenceException extends JahiaException {

    /** Instanciates a <code>JahiaPersistenceException</code> exception with
     * the specified <code>message</code>. Uses by default the
     * <code>JahiaException.CRITICAL_SEVERITY</code> level and the
     * <code>JahiaException.PERSISTENCE_ERROR</code> error code.
     *
     * @param message   the message associated to the exception
     */
    public JahiaPersistenceException (String message) {
        super ("Persistence error", message, JahiaException.PERSISTENCE_ERROR,
                JahiaException.CRITICAL_SEVERITY, null);
    }

    /** Instanciates a <code>JahiaPersistenceException<code> exception with
     * the specified detail <code>message</code> and the throwable <code>t</code>
     * root cause. Uses by default the <code>JahiaException.CRITICAL_SEVERITY</code> level and
     * the <code>JahiaException.PERSISTENCE_ERROR</code> error code.
     *
     * @param message   the exception message
     * @param t         the root cause exception
     */
    public JahiaPersistenceException (String message, Throwable t) {
        super ("Persistence error", message, JahiaException.PERSISTENCE_ERROR,
                JahiaException.CRITICAL_SEVERITY, t);
    }

    /** Instanciates a <code>JahiaPersistenceException<code> exception with
     * the specified detail <code>message</code> and the throwable <code>t</code>
     * root cause. Uses by default the <code>JahiaException.CRITICAL_SEVERITY</code> level.
     *
     * @param message   the exception message
     * @param errorCode the errore code
     * @param t         the root cause exception
     */
    public JahiaPersistenceException (String message, int errorCode, Throwable t) {
        super ("Persistence error", message, errorCode, JahiaException.CRITICAL_SEVERITY, t);
    }

    /** Instanciates a <code>JahiaPersistenceException<code> exception with
     * the specified detail <code>message</code> and the throwable <code>t</code>
     * root cause. Uses by default the <code>JahiaException.CRITICAL_SEVERITY</code> level.
     *
     * @param message   the exception message
     * @param errorCode the errore code
     */
    public JahiaPersistenceException (String message, int errorCode) {
        super ("Persistence error", message, errorCode, JahiaException.CRITICAL_SEVERITY, null);
    }

    /** Instanciates a <code>JahiaPersistenceException<code> exception with
     * the specified detail <code>message</code> and the throwable <code>t</code>
     * root cause.
     *
     * @param message   the exception message
     * @param errorCode the errore code
     * @param severity  the severity level
     * @param t         the root cause exception
     */
    public JahiaPersistenceException (String message, int errorCode,
                                      int severity, Throwable t)
    {
        super ("Persistence error", message, errorCode, severity, t);
    }

    /** Instanciates a <code>JahiaPersistenceException<code> exception with
     * the specified detail <code>message</code> and the throwable <code>t</code>
     * root cause.
     *
     * @param message   the exception message
     * @param errorCode the errore code
     * @param severity  the severity level
     */
    public JahiaPersistenceException (String message, int errorCode, int severity)
    {
        super ("Persistence error", message, errorCode, severity, null);
    }
}