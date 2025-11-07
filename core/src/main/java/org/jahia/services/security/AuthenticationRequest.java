package org.jahia.services.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents an authentication request containing user credentials and authentication parameters.
 * <p>
 * This class encapsulates all necessary information to authenticate or validate a user, including:
 * <ul>
 *   <li>Username and password credentials</li>
 *   <li>Optional site context for site-specific user lookup</li>
 *   <li>Global search flag to control user lookup scope</li>
 *   <li>"Remember me" preference for persistent authentication</li>
 * </ul>
 * <p>
 *
 * @see AuthenticationService#authenticate(AuthenticationRequest, AuthenticationOptions, HttpServletRequest, HttpServletResponse)
 * @see AuthenticationService#getUserNodeFromCredentials(AuthenticationRequest)
 */
public class AuthenticationRequest {
    private final String username;
    private final String password;
    private final String site;
    private final boolean globalSearchIncluded;
    private final boolean rememberMe;

    /**
     * Creates a full authentication request with all parameters specified.
     *
     * @param username             the username to authenticate
     * @param password             the user's password
     * @param site                 the site context for user lookup, or {@code null} to search globally
     * @param globalSearchIncluded {@code true} to search global users if site search fails, {@code false} to search only in the specified site
     * @param rememberMe           {@code true} to enable persistent authentication via cookie, {@code false} otherwise
     */
    public AuthenticationRequest(String username, String password, String site, boolean globalSearchIncluded, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.site = site;
        this.globalSearchIncluded = globalSearchIncluded;
        this.rememberMe = rememberMe;
    }

    /**
     * Creates a simple authentication request with username and password only.
     * <p>
     * This constructor uses default values:
     * <ul>
     *   <li>No site context ({@code null}) - searches globally</li>
     *   <li>Global search enabled ({@code true})</li>
     *   <li>"Remember me" enabled ({@code true})</li>
     * </ul>
     *
     * @param username the username to authenticate
     * @param password the user's password
     */
    public AuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.site = null;
        this.globalSearchIncluded = true;
        this.rememberMe = true;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSite() {
        return site;
    }

    public boolean isGlobalSearchIncluded() {
        return globalSearchIncluded;
    }

}
