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
