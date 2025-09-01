package org.jahia.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.render.RenderContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.function.Supplier;

/**
 * Utility class for hashing session IDs in various contexts.
 * This class provides methods to generate a hashed version of session IDs obtained from different sources, ensuring
 * that session identifiers are securely transformed using SHA-256 hashing before being used mainly in logging messages.
 * <p>
 * All methods return <code>null</code> if the session ID cannot be obtained or is <code>null</code>.
 * The hashing is performed using SHA-256 algorithm via <i>Apache Commons Codec</i>.
 */
public final class SessionIdHashingUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private SessionIdHashingUtils() {
        // Utility class
    }

    /**
     * Gets the hashed session ID from an HTTP request.
     * Does not create a new session if none exists.
     *
     * @param request the HTTP request
     * @return hashed session ID or <code>null</code> if no session exists
     */
    public static String getHashedSessionId(HttpServletRequest request) {
        return getHashedSessionId(request, false);
    }

    /**
     * Gets the hashed session ID from an HTTP request with optional fallback.
     *
     * @param request                      the HTTP request
     * @param fallbackToRequestedSessionId if true, falls back to requested session ID when no active session exists
     * @return hashed session ID or <code>null</code> if no session ID can be obtained
     */
    public static String getHashedSessionId(HttpServletRequest request, boolean fallbackToRequestedSessionId) {
        if (request == null) {
            return null;
        }

        HttpSession httpSession = request.getSession(false);
        Supplier<String> fallbackSupplier = fallbackToRequestedSessionId ? request::getRequestedSessionId : null;
        return getHashedSessionId(httpSession, fallbackSupplier);
    }

    /**
     * Internal method to get hashed session ID with fallback supplier.
     *
     * @param httpSession      the HTTP session (can be <code>null</code>)
     * @param fallbackSupplier supplier for fallback session ID (can be <code>null</code>)
     * @return hashed session ID or <code>null</code>
     */
    private static String getHashedSessionId(HttpSession httpSession, Supplier<String> fallbackSupplier) {
        Supplier<String> sessionIdSupplier = httpSession != null ? httpSession::getId : fallbackSupplier;
        return hashSessionId(sessionIdSupplier);
    }

    /**
     * Gets the hashed session ID directly from an HTTP session.
     *
     * @param httpSession the HTTP session (can be <code>null</code>)
     * @return hashed session ID or <code>null</code> if session is <code>null</code>
     */
    public static String getHashedSessionId(HttpSession httpSession) {
        Supplier<String> sessionIdSupplier = httpSession == null ? null : httpSession::getId;
        return hashSessionId(sessionIdSupplier);
    }

    /**
     * Gets the hashed session ID from a session event.
     *
     * @param event the HTTP session event
     * @return hashed session ID or <code>null</code> if event or session is <code>null</code>
     */
    public static String getHashedSessionId(HttpSessionEvent event) {
        if (event == null || event.getSession() == null) {
            return null;
        }
        return hashSessionId(() -> event.getSession().getId());
    }

    /**
     * Gets the hashed session ID from a Jahia-specific {@link JahiaContextLoaderListener.HttpSessionDestroyedEvent}.
     *
     * @param event the session destroyed event
     * @return hashed session ID or <code>null</code> if event or session is <code>null</code>
     */
    public static String getHashedSessionId(JahiaContextLoaderListener.HttpSessionDestroyedEvent event) {
        if (event == null || event.getSession() == null) {
            return null;
        }
        return hashSessionId(() -> event.getSession().getId());
    }

    /**
     * Gets the hashed session ID from a render context.
     *
     * @param context the render context
     * @return hashed session ID or <code>null</code> if context or request is <code>null</code>
     */
    public static String getHashedSessionId(RenderContext context) {
        if (context == null) {
            return null;
        }
        return getHashedSessionId(context.getRequest());
    }

    /**
     * Hashes a session ID obtained from a supplier.
     *
     * @param sessionIdSupplier supplier that provides the session ID (can be <code>null</code>)
     * @return hashed session ID or <code>null</code> if supplier is <code>null</code> or provides <code>null</code>
     */
    private static String hashSessionId(Supplier<String> sessionIdSupplier) {
        String sessionId = sessionIdSupplier == null ? null : sessionIdSupplier.get();
        return hashSessionId(sessionId);
    }

    /**
     * Hashes a session ID string using SHA-256.
     * This is the core hashing method used by all other methods.
     *
     * @param sessionId the session ID to hash (can be <code>null</code>)
     * @return SHA-256 hex hash of the session ID, or <code>null</code> if input is <code>null</code>
     */
    public static String hashSessionId(String sessionId) {
        return sessionId == null ? null : DigestUtils.sha256Hex(sessionId);
    }
}
