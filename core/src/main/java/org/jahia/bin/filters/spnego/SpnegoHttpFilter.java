/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
/* Copyright 2006 Taglab Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.jahia.bin.filters.spnego;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jahia.services.security.spnego.SpnegoAuthenticator;

/**
 * 
 */
public class SpnegoHttpFilter implements Filter {

    /**
     * Logger.
     * NOTE Some logs about error will be shown only in debug mode. Just one
     * reason for this, if client is out of domain it can be authenticate in some
     * other way (not NTLM or SPNEGO).
     */
    private static final Logger logger = Logger.getLogger("org.jahia.bin.filters.spnego.SpnegoHttpFilter");

    public static final String SSOAUTHENTICATOR_KEY = "SSOAuthenticator";
    
    private boolean skipAuthentication;
    private boolean useBasic;    
    
    private boolean enabled;
    
    /**
     * {@inheritDoc}
     */
    public void destroy() {
      // nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        if (!enabled) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse resp = (HttpServletResponse) response;
            HttpServletRequest req = (HttpServletRequest) request;
            Principal principal = null;
            if ((principal = authenticate(req, resp)) == null) {
                if (!skipAuthentication || resp.isCommitted()) {
                    if (!resp.isCommitted()) {
                        if (useBasic) {
                            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        } else {
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "SPNEGO could not authenticate and authentication is configured to not be skipped");
                        }
                    }
                    return;
                }
            }
    
            Boolean isBasicBool = (Boolean) request.getAttribute("isBasic");
            if (isBasicBool == null) {
                isBasicBool = Boolean.FALSE;
            }
            boolean useSpnegoRequest = principal != null
                    && !(isBasicBool.booleanValue() && !useBasic) ? true : false;
    
            chain.doFilter(useSpnegoRequest ? new SpnegoHttpServletRequest(req,
                principal) : req, response);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    /**
     * Do all work about authentication.
     * @param httpRequest the request object to check for headers.
     * @param httpResponse the response object to set headers and <code>sendError(401)</code>.
     * @return Principal id authentication complete and success and null  otherwise.
     * @throws IOException if i/o error occurs.
     */
    public final Principal authenticate(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws IOException {
      // Try get authenticator from session first.
      SpnegoAuthenticator auth = null;
      HttpSession session = httpRequest.getSession(false);
      if (session != null)
        auth = (SpnegoAuthenticator) session.getAttribute(SSOAUTHENTICATOR_KEY);

      // Authenticator found in session
      if (auth != null) {
        if (logger.isDebugEnabled()) {
          logger.debug("Get authenticator from HTTP session." + " principal : " + auth.getPrincipal()
              + " authentication complete: " + auth.isComplete()
              + " authentication success: " + auth.isSuccess());
        }

        // Authentication complete with success.
        if (auth.isComplete() && auth.isSuccess())
          return auth.getPrincipal();

        // Authentication complete with error.
        if (auth.isComplete() && !auth.isSuccess()) {
          return null;
        }

      }
      // Continue if authentication not finished yet (or not started).
      String authHeader = httpRequest.getHeader("Authorization");

      if (authHeader == null) {
        // Authentication process is not started yet.
        if (logger.isDebugEnabled()) {
          logger.debug("No authorization headers, send WWW-Authenticate header.");
        }

        /*
         * Few WWW-Authenticate headers. WWW-Authenticate: Negotiate
         * WWW-Authenticate: NTLM NTLM nut suported yet
         */
//        for (String mech : Config.getSupportedAuthenticationMechanisms())
          httpResponse.addHeader("WWW-Authenticate", "Negotiate");
//          httpResponse.addHeader("WWW-Authenticate", "NTLM");          

        // return HTTP status 401
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
      }

      // authentication header presents, so process started
      int endSignature = authHeader.indexOf(' ');
      // We are waiting for NTLM or Negotiate.
      String authMechanism = authHeader.substring(0, endSignature);
      byte[] token = Base64.decodeBase64(authHeader.substring(endSignature + 1).getBytes());

      // If authenticator not initialized yet.
      if ("Negotiate".equalsIgnoreCase(authMechanism))
        auth = new SpnegoAuthenticator();
      else if ("NTLM".equalsIgnoreCase(authMechanism)) {
          logger.warn("NTLM fallback in SPNEGO is not supported yet!!!");
      }

      // Do authentication here.
      if (auth != null) {
        try {
          auth.doAuthenticate(token);
        } catch (Exception e) {
          return null;
        }
      } else {
        /*
         * Authenticator is null. Appropriated authenticator can't be created 
         */
        return null;
      }

      /*
       * Authentication (or one step of it) successful. Save authenticator in HTTP
       * session. Session can be null. Above we tried to get is as
       * httpRequest.getSession(false).
       */
      session = httpRequest.getSession();
      session.setAttribute(SSOAUTHENTICATOR_KEY, auth);

      byte[] backToken = auth.getSendBackToken();
      /*
       * If one step of authentication successful but authentication not finished
       * yet. NTLM works in two steps. Return HTTP status 401.
       */
      if (backToken != null && !auth.isComplete()) {
        httpResponse.setHeader("WWW-Authenticate", authMechanism + " " + new String(Base64.encodeBase64(backToken)));
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
      }

      return auth.getPrincipal();

    }
    
    /**
     * @see javax.servlet.http.HttpServletResponseWrapper .
     */
    final class SpnegoHttpServletRequest extends HttpServletRequestWrapper {

      /**
       * User principal.
       */
      private Principal principal;
      
      /**
       * @param request the original request.
       * @param principal the user principal.
       */
      public SpnegoHttpServletRequest(final HttpServletRequest request, final Principal principal) {
        super(request);
        this.principal = principal;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getRemoteUser() {
        return getUserPrincipal().getName();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Principal getUserPrincipal() {
        return this.principal;
      }
      
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSkipAuthentication(boolean skipAuthentication) {
        this.skipAuthentication = skipAuthentication;
    }

    public void setUseBasic(boolean useBasic) {
        this.useBasic = useBasic;
    }
    
  }
