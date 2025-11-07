package org.jahia.services.security;

import org.jahia.services.content.decorator.JCRUserNode;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service for authenticating users and managing authentication-related operations.
 * <p>
 * It provides methods to fully authenticate users from credentials or from an existing {@link JCRUserNode}, or to simply retrieve a
 * {@link JCRUserNode} from credentials.
 * </p>
 * <p>
 * Depending on the authentication options and HTTP context, this service may update the HTTP session,
 * set properties in the JCR for the user node, set some cookies ("remember me" feature), and trigger login events.
 * </p>
 */
public interface AuthenticationService {

    /**
     * Authenticates a user based on the provided authentication request, options, and HTTP servlet context.
     * <p>
     * If authentication is successful, returns the corresponding {@link JCRUserNode} for the authenticated user.
     * May update the HTTP session, set a property in the JCR for "remember me" functionality, and trigger login events,
     * depending on the {@link AuthenticationOptions} provided.
     * </p>
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @param authenticationOptions the authentication options to process the authentication
     * @param httpServletRequest    the HTTP servlet request, used for session and locale management
     * @param httpServletResponse   the HTTP servlet response, used for setting cookies (e.g., "remember me")
     * @return the authenticated {@link JCRUserNode} object if authentication is successful
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws AccountLockedException                             if the user account is locked
     * @throws FailedLoginException                               if authentication fails due to invalid credentials
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum limit of concurrent logged-in users is exceeded
     * @throws InvalidSessionLoginException                       if there is an issue with the provided session or its validity
     * @throws IllegalArgumentException                           if the authentication request is missing mandatory data, or if the
     *                                                            authentication options are not compatible with the provided HTTP request/response
     */
    JCRUserNode authenticate(AuthenticationRequest authenticationRequest, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, InvalidSessionLoginException, IllegalArgumentException;

    /**
     * Authenticates a user based on the provided authentication request.
     * <p>
     * As no HTTP context is passed, a stateless {@link AuthenticationOptions} is used.
     * </p>
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @return the authenticated {@link JCRUserNode} object if authentication is successful
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws AccountLockedException                             if the user account is locked
     * @throws FailedLoginException                               if authentication fails due to invalid credentials
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum limit of concurrent logged-in users is exceeded
     * @throws IllegalArgumentException                           if the authentication request is missing mandatory data
     */
    JCRUserNode authenticate(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException;

    /**
     * Authenticates an existing user node using the provided authentication options and HTTP servlet context.
     * <p>
     * May update the HTTP session, set a property in the JCR for "remember me" functionality, and trigger login events,
     * depending on the {@link AuthenticationOptions} provided.
     * </p>
     *
     * @param userNode              an already validated user node authentication request
     * @param authenticationOptions the authentication options to process the authentication
     * @param httpServletRequest    the HTTP servlet request, used for session and locale management
     * @param httpServletResponse   the HTTP servlet response, used for setting cookies (e.g., "remember me")
     * @return the authenticated {@link JCRUserNode} object if authentication is successful
     * @throws InvalidSessionLoginException if there is an issue with the provided session or its validity
     * @throws AccountNotFoundException     if the user node does is found in the JCR
     * @throws IllegalArgumentException     if the authentication options are not compatible with the provided
     *                                      HTTP request/response
     *
     */
    JCRUserNode authenticate(JCRUserNode userNode, AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws InvalidSessionLoginException, IllegalArgumentException, AccountNotFoundException;

    /**
     * Retrieves a {@link JCRUserNode} from a {@link AuthenticationRequest}, without completing the authentication process.
     * <p>
     * This method checks for valid username, password, account lock status, and concurrent login limits.<br/>
     * <b>Note:</b> Unlike {@link #authenticate(AuthenticationRequest, AuthenticationOptions, HttpServletRequest, HttpServletResponse)},
     * this method does not set the current user, update the session, set any JCR properties, or trigger login events.
     * </p>
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @return the {@link JCRUserNode} object if validation is successful
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws AccountLockedException                             if the user account is locked
     * @throws FailedLoginException                               if validation fails due to invalid credentials
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum limit of concurrent logged-in users is exceeded
     * @throws IllegalArgumentException                           if the authentication request is missing mandatory data
     */
    JCRUserNode getUserNodeFromCredentials(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException;

}
