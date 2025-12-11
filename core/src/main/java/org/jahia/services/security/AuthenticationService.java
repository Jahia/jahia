package org.jahia.services.security;

import org.jahia.services.usermanager.JahiaUser;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service for authenticating users and managing authentication-related operations.
 * <p>
 * It provides methods to fully authenticate users from credentials or from the path of an existing {@link org.jahia.services.content.decorator.JCRUserNode},
 * or to simply retrieve a {@link JahiaUser} from credentials.
 * </p>
 * <p>
 * Depending on the authentication options and HTTP context, this service may update the HTTP session,
 * set properties in the JCR for the user node, set some cookies ("remember me" feature), and trigger login events.
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Standard Web Login Form</h3>
 * <p>Typical authentication for a web login form with full session management:</p>
 * <pre>{@code
 * // Extract form parameters
 * String username = request.getParameter("username");
 * String password = request.getParameter("password");
 * String site = request.getParameter("site");
 * boolean rememberMe = "on".equals(request.getParameter("rememberMe"));
 *
 * // Create authentication request
 * AuthenticationRequest authRequest = new AuthenticationRequest(
 *     username, password, site, true
 * );
 *
 * // Configure authentication options
 * AuthenticationOptions authOptions = AuthenticationOptions.Builder.withDefaults()
 *     .shouldRememberMe(rememberMe)
 *     .build();
 *
 * try {
 *     // Authenticate the user
 *     authService.authenticate(authRequest, authOptions, request, response);
 *
 *     // Success - redirect to home page
 *     response.sendRedirect("/home");
 *
 * } catch (AccountNotFoundException e) {
 *     // Display "unknown user" error
 *     request.setAttribute("error", "UNKNOWN_USER");
 * } catch (FailedLoginException e) {
 *     // Display "invalid password" error
 *     // While technically different errors, it's a good security practice to render `AccountNotFoundException` and `FailedLoginException` errors with the exact same client-facing code: it prevents leaking your user base using brute-force.
 *     request.setAttribute("error", "BAD_PASSWORD");
 * } catch (AccountLockedException e) {
 *     // Display "account locked" error
 *     request.setAttribute("error", "ACCOUNT_LOCKED");
 * } catch (ConcurrentLoggedInUsersLimitExceededLoginException e) {
 *     // Display "too many users" error
 *     request.setAttribute("error", "USER_LIMIT_REACHED");
 * }
 * }</pre>
 *
 * <h3>Example 2: HTTP Basic Authentication (Stateless)</h3>
 * <p>Stateless authentication for HTTP Basic Auth (no session creation):</p>
 * <pre>{@code
 * // Parse HTTP Basic Auth header
 * String authHeader = request.getHeader("Authorization");
 * if (authHeader != null && authHeader.startsWith("Basic ")) {
 *     String base64Credentials = authHeader.substring("Basic ".length());
 *     String credentials = new String(Base64.decodeBase64(base64Credentials));
 *     String[] parts = credentials.split(":", 2);
 *
 *     String username = parts[0];
 *     String password = parts[1];
 *
 *     // Create authentication request
 *     AuthenticationRequest authRequest = new AuthenticationRequest(username, password);
 *
 *     try {
 *         // Authenticate without session
 *         authService.authenticate(authRequest);
 *
 *         // User is now authenticated for this request only
 *         // No session or cookies are created
 *
 *     } catch (Exception e) {
 *         // Send 401 Unauthorized
 *         response.setHeader("WWW-Authenticate", "Basic realm=\"Jahia\"");
 *         response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 *     }
 * }
 * }</pre>
 *
 * <h3>Example 3: Credential Validation Only</h3>
 * <p>Validate credentials without performing full authentication:</p>
 * <pre>{@code
 * String username = request.getParameter("username");
 * String password = request.getParameter("password");
 *
 * AuthenticationRequest authRequest = new AuthenticationRequest(username, password);
 *
 * try {
 *     // Get user without full authentication
 *     JahiaUser user = authService.getUserFromCredentials(authRequest);
 *
 *     // Credentials are valid - perform custom logic
 *     if (user.getProperty("customProperty") != null) {
 *         // Now perform full authentication
 *         AuthenticationOptions authOptions = AuthenticationOptions.Builder.withDefaults()
 *             .build();
 *         authService.authenticate(authRequest, authOptions, request, response);
 *     }
 *
 * } catch (FailedLoginException e) {
 *     // Invalid credentials
 * }
 * }</pre>
 *
 * <h3>Example 4: Custom Login Flow with Selective Options</h3>
 * <p>Authentication with custom configuration for a specific use case:</p>
 * <pre>{@code
 * String username = request.getParameter("username");
 * String password = request.getParameter("password");
 *
 * AuthenticationRequest authRequest = new AuthenticationRequest(username, password);
 *
 * // Custom authentication options:
 * // - Don't trigger login events (will be triggered manually later)
 * // - Don't check session validity (checking done elsewhere)
 * // - Update locales normally
 * AuthenticationOptions authOptions = AuthenticationOptions.Builder.withDefaults()
 *     .triggerLoginEventEnabled(false)
 *     .sessionValidityCheckEnabled(false)
 *     .shouldRememberMe(false)
 *     .build();
 *
 * try {
 *     authService.authenticate(authRequest, authOptions, request, response);
 *
 *     // Perform custom post-login logic
 *     performCustomPostLoginActions(request, response);
 *
 *     // Manually trigger login event if needed
 *     publishCustomLoginEvent(username);
 *
 * } catch (InvalidSessionLoginException e) {
 *     // This won't be thrown because we disabled session validity check
 * } catch (Exception e) {
 *     // Handle other exceptions
 * }
 * }</pre>
 */
public interface AuthenticationService {

    /**
     * Authenticates a user based on the provided authentication request, options, and HTTP servlet context.
     * If the user can't be authenticated, an exception is thrown.
     * <p>
     * May update the HTTP session, set a property in the JCR for "remember me" functionality, and trigger login events,
     * depending on the {@link AuthenticationOptions} provided.
     * </p>
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @param authenticationOptions the authentication options to process the authentication
     * @param httpServletRequest    the HTTP servlet request, used for session and locale management
     * @param httpServletResponse   the HTTP servlet response, used for setting cookies (e.g., "remember me")
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws AccountLockedException                             if the user account is locked
     * @throws FailedLoginException                               if authentication fails due to invalid credentials
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum limit of concurrent logged-in users is exceeded
     * @throws InvalidSessionLoginException                       if there is an issue with the provided session or its validity
     * @throws IllegalArgumentException                           if the authentication request is missing mandatory data, or if the
     *                                                            authentication options are not compatible with the provided HTTP request/response
     */
    void authenticate(AuthenticationRequest authenticationRequest, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, InvalidSessionLoginException, IllegalArgumentException;

    /**
     * Authenticates a user based on the provided authentication request.
     * If the user can't be authenticated, an exception is thrown.
     * <p>
     * As no HTTP context is passed, a stateless {@link AuthenticationOptions} is used.
     * </p>
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws AccountLockedException                             if the user account is locked
     * @throws FailedLoginException                               if authentication fails due to invalid credentials
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum limit of concurrent logged-in users is exceeded
     * @throws IllegalArgumentException                           if the authentication request is missing mandatory data
     */
    void authenticate(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException;

    /**
     * Authenticates a user from their path using the provided authentication options and HTTP servlet context.
     * If the user can't be authenticated, an exception is thrown.
     * <p>
     * May update the HTTP session, set a property in the JCR for "remember me" functionality, and trigger login events,
     * depending on the {@link AuthenticationOptions} provided.
     * </p>
     *
     * @param userNodePath          the path of the user node to authenticate, typically retrieved with
     *                              {@link org.jahia.services.content.decorator.JCRUserNode#getPath()}
     * @param authenticationOptions the authentication options to process the authentication
     * @param httpServletRequest    the HTTP servlet request, used for session and locale management
     * @param httpServletResponse   the HTTP servlet response, used for setting cookies (e.g., "remember me")
     * @throws InvalidSessionLoginException if there is an issue with the provided session or its validity
     * @throws AccountNotFoundException     if the user node does is found in the JCR
     * @throws IllegalArgumentException     if the authentication options are not compatible with the provided
     *                                      HTTP request/response
     *
     */
    void authenticate(String userNodePath, AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws InvalidSessionLoginException, IllegalArgumentException, AccountNotFoundException;

    /**
     * Retrieves a {@link JahiaUser} from a {@link AuthenticationRequest}, without completing the authentication process.
     * <p>
     * This method checks for valid username, password, account lock status, and concurrent login limits.<br/>
     * <b>Note:</b> Unlike {@link #authenticate(AuthenticationRequest, AuthenticationOptions, HttpServletRequest, HttpServletResponse)},
     * this method does not set the current user, update the session, set any JCR properties, or trigger login events.
     * </p>
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @return the {@link JahiaUser} object if validation is successful
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws AccountLockedException                             if the user account is locked
     * @throws FailedLoginException                               if validation fails due to invalid credentials
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum limit of concurrent logged-in users is exceeded
     * @throws IllegalArgumentException                           if the authentication request is missing mandatory data
     */
    JahiaUser getUserFromCredentials(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException;

    /**
     * Validates a user from their path in the JCR to ensure their account is not locked, and the maximum
     * number of concurrent logged-in users has not been reached (based on the license).
     *
     * @param userNodePath the path of the user node to validate, typically retrieved with
     *                     {@link org.jahia.services.content.decorator.JCRUserNode#getPath()}
     * @throws AccountLockedException                             if the user's account is locked
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the user has exceeded the allowed limit of concurrent logins
     */
    void validateUserNode(String userNodePath) throws AccountLockedException, ConcurrentLoggedInUsersLimitExceededLoginException;
}
