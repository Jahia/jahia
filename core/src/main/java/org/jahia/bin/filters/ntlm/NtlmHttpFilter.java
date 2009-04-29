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
/* jcifs smb client library in Java
 * Copyright (C) 2002  "Michael B. Allen" <jcifs at samba dot org>
 *                   "Jason Pugsley" <jcifs at samba dot org>
 *                   "skeetz" <jcifs at samba dot org>
 *                   "Eric Glass" <jcifs at samba dot org>
 *                   and Marcel, Thomas, ...
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

 package org.jahia.bin.filters.ntlm;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.smb.NtStatus;
import jcifs.smb.NtlmChallenge;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;
import jcifs.util.LogStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.EnumerationIterator;

import java.io.IOException;
import java.util.Iterator;

/**
 * This servlet Filter can be used to negotiate password hashes with
 * MSIE clients using NTLM SSP. This is similar to <tt>Authentication:
 * BASIC</tt> but weakly encrypted and without requiring the user to re-supply
 * authentication credentials.
 * <p>
 * Read <a href="../../../ntlmhttpauth.html">jCIFS NTLM HTTP Authentication and the Network Explorer Servlet</a> for complete details.
 *
 * This is a slightly modified version from the original to make the
 * skipAuthentification parameter configurable, as it was hardcoded
 * in the original.
 */

public class NtlmHttpFilter implements Filter {

    private static LogStream log = LogStream.getInstance();

    private String defaultDomain;
    private String domainController;
    private boolean loadBalance;
    private boolean enableBasic;
    private boolean useBasic;
    private boolean insecureBasic;
    private String realm;
    private boolean skipAuthentification;

    public void init( FilterConfig filterConfig ) throws ServletException {
        String name;
        int level;

        /* Set jcifs properties we know we want; soTimeout and cachePolicy to 10min.
         */
        Config.setProperty( "jcifs.smb.client.soTimeout", "300000" );
        Config.setProperty( "jcifs.netbios.cachePolicy", "1200" );

        Iterator e = new EnumerationIterator(filterConfig.getInitParameterNames());
        while( e.hasNext() ) {
            name = (String)e.next();
            if( name.startsWith( "jcifs." )) {
                Config.setProperty( name, filterConfig.getInitParameter( name ));
            }
        }
        defaultDomain = Config.getProperty("jcifs.smb.client.domain");
        domainController = Config.getProperty( "jcifs.http.domainController" );
        if( domainController == null ) {
            domainController = defaultDomain;
            loadBalance = Config.getBoolean( "jcifs.http.loadBalance", true );
        }
        enableBasic = Boolean.valueOf(
                Config.getProperty("jcifs.http.enableBasic")).booleanValue();
        useBasic = Boolean.valueOf(
                Config.getProperty("jcifs.http.useBasic")).booleanValue();
        insecureBasic = Boolean.valueOf(
                Config.getProperty("jcifs.http.insecureBasic")).booleanValue();
        realm = Config.getProperty("jcifs.http.basicRealm");
        if (realm == null) realm = "jCIFS";

        skipAuthentification = Boolean.valueOf(
                Config.getProperty("jcifs.http.skipAuthentification")).booleanValue();

        if(( level = Config.getInt( "jcifs.util.loglevel", -1 )) != -1 ) {
            LogStream.setLevel( level );
        }
        if( LogStream.level > 2 ) {
            try {
                Config.store( log, "JCIFS PROPERTIES" );
            } catch( IOException ioe ) {
            }
        }
    }

    public void destroy() {
    }

    /**
     * This method simply calls <tt>negotiate( req, resp, false )</tt>
     * and then <tt>chain.doFilter</tt>. You can override and call
     * negotiate manually to achive a variety of different behavior.
     */
    public void doFilter( ServletRequest request,
                ServletResponse response,
                FilterChain chain ) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;
        NtlmPasswordAuthentication ntlm;

        if ((ntlm = negotiate( req, resp, skipAuthentification )) == null) {
            if (!skipAuthentification) {
                return;
            }
        }

        Boolean isBasicBool = (Boolean) request.getAttribute("isBasic");
        if (isBasicBool == null) {
            isBasicBool = Boolean.FALSE;
        }
        boolean useNtlmRequest = false;
        if (ntlm != null) {
            useNtlmRequest = true;
        }
        if (isBasicBool.booleanValue() && !useBasic) {
            useNtlmRequest = false;
        }

        if (useNtlmRequest) {
            req.setAttribute("ntlmPrincipal", ntlm);
            req.setAttribute("ntlmAuthType", "NTLM");
            chain.doFilter( req, response );
        } else {
            chain.doFilter(req, response);
        }
    }

    /**
     * Negotiate password hashes with MSIE clients using NTLM SSP
     * @param req The servlet request
     * @param resp The servlet response
     * @param skipAuthentication If true the negotiation is only done if it is
     * initiated by the client (MSIE post requests after successful NTLM SSP
     * authentication). If false and the user has not been authenticated yet
     * the client will be forced to send an authentication (server sends
     * HttpServletResponse.SC_UNAUTHORIZED).
     * @return True if the negotiation is complete, otherwise false
     */
    protected NtlmPasswordAuthentication negotiate( HttpServletRequest req,
                HttpServletResponse resp,
                boolean skipAuthentication ) throws IOException, ServletException {
        UniAddress dc;
        String msg;
        NtlmPasswordAuthentication ntlm = null;
        msg = req.getHeader( "Authorization" );
        boolean offerBasic = enableBasic && (insecureBasic || req.isSecure());
        boolean isBasic = false;

        if( msg != null && (msg.startsWith( "NTLM " ) ||
                    (offerBasic && msg.startsWith("Basic ")))) {
            if (msg.startsWith("NTLM ")) {
                HttpSession ssn = req.getSession();
                byte[] challenge;

                if( loadBalance ) {
                    NtlmChallenge chal = (NtlmChallenge)ssn.getAttribute( "NtlmHttpChal" );
                    if( chal == null ) {
                        chal = SmbSession.getChallengeForDomain();
                        ssn.setAttribute( "NtlmHttpChal", chal );
                    }
                    dc = chal.dc;
                    challenge = chal.challenge;
                } else {
                    dc = UniAddress.getByName( domainController, true );
                    challenge = SmbSession.getChallenge( dc );
                }

                if(( ntlm = NtlmSsp.authenticate( req, resp, challenge )) == null ) {
                    return null;
                }
                /* negotiation complete, remove the challenge object */
                ssn.removeAttribute( "NtlmHttpChal" );
            } else {
                req.setAttribute("isBasic", Boolean.TRUE);
                isBasic = true;
                String auth = new String(Base64.decode(msg.substring(6)),
                        "US-ASCII");
                int index = auth.indexOf(':');
                String user = (index != -1) ? auth.substring(0, index) : auth;
                String password = (index != -1) ? auth.substring(index + 1) :
                        "";
                index = user.indexOf('\\');
                if (index == -1) index = user.indexOf('/');
                String domain = (index != -1) ? user.substring(0, index) :
                        defaultDomain;
                user = (index != -1) ? user.substring(index + 1) : user;
                ntlm = new NtlmPasswordAuthentication(domain, user, password);
                dc = UniAddress.getByName( domainController, true );
            }
            try {
                if ((isBasic) && (!useBasic)) {
                    return ntlm;
                }
                SmbSession.logon( dc, ntlm );

                if( LogStream.level > 2 ) {
                    log.println( "NtlmHttpFilter: " + ntlm +
                            " successfully authenticated against " + dc );
                }
            } catch( SmbAuthException sae ) {
                if( LogStream.level > 1 ) {
                    log.println( "NtlmHttpFilter: " + ntlm.getName() +
                            ": 0x" + jcifs.util.Hexdump.toHexString( sae.getNtStatus(), 8 ) +
                            ": " + sae );
                }
                if( sae.getNtStatus() == NtStatus.NT_STATUS_ACCESS_VIOLATION ) {
                    /* Server challenge no longer valid for
                     * externally supplied password hashes.
                     */
                    HttpSession ssn = req.getSession(false);
                    if (ssn != null) {
                        ssn.removeAttribute( "NtlmHttpAuth" );
                    }
                }
                resp.setHeader( "WWW-Authenticate", "NTLM" );
                if (offerBasic) {
                    resp.addHeader( "WWW-Authenticate", "Basic realm=\"" +
                            realm + "\"");
                }
                resp.setContentLength(0); /* Marcel Feb-15-2005 */
                resp.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                resp.flushBuffer();
                return null;
            }
            req.getSession().setAttribute( "NtlmHttpAuth", ntlm );
        } else {
            if (!skipAuthentication) {
                HttpSession ssn = req.getSession(false);
                if (ssn == null || (ntlm = (NtlmPasswordAuthentication)
                            ssn.getAttribute("NtlmHttpAuth")) == null) {
                    resp.setHeader( "WWW-Authenticate", "NTLM" );
                    if (offerBasic) {
                        resp.addHeader( "WWW-Authenticate", "Basic realm=\"" +
                                realm + "\"");
                    }
                    resp.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                    resp.flushBuffer();
                    return null;
                }
            }
        }

        return ntlm;
    }

    // Added by cgross to work with weblogic 6.1.
    public void setFilterConfig( FilterConfig f ) {
        try {
            init( f );
        } catch( Exception e ) {
            log.println(e);
        }
    }
    public FilterConfig getFilterConfig() {
        return null;
    }
}