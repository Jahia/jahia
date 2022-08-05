/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bundles.securityfilter.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.services.securityfilter.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public class JWTFilter extends AbstractServletFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    private static final String BEARER = "Bearer";
    private static final ThreadLocal<TokenVerificationResult> THREAD_LOCAL = new ThreadLocal<TokenVerificationResult>();
    private JWTConfig jwtConfig;

    public static TokenVerificationResult getJWTTokenVerificationStatus() {
        return THREAD_LOCAL.get();
    }

    private PermissionService permissionService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setJwtConfig(JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Do nothing for now
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String authorization = httpRequest.getHeader("Authorization");

        TokenVerificationResult tvr = new TokenVerificationResult();

        THREAD_LOCAL.set(tvr);

        if (authorization != null && authorization.contains(BEARER)) {
            String token = StringUtils.substringAfter(authorization, BEARER).trim();
            if (!StringUtils.isEmpty(token)) {
                try {
                    DecodedJWT decodedToken = jwtConfig.verifyToken(token);

                    verifyToken(httpRequest, tvr, decodedToken);

                    if (tvr.getVerificationStatusCode() == TokenVerificationResult.VerificationStatus.VERIFIED) {
                        List<String> scopes = decodedToken.getClaim("scopes").asList(String.class);
                        if (scopes != null) {
                            permissionService.addScopes(scopes, httpRequest);
                        }
                    }
                } catch (Exception e) {
                    tvr.setVerificationStatusCode(TokenVerificationResult.VerificationStatus.REJECTED);
                    tvr.setMessage("Failed to verify token");
                    logger.debug("Failed to verify JWT token: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(httpRequest, servletResponse);

        THREAD_LOCAL.set(null);
    }

    private void verifyToken(HttpServletRequest httpRequest, TokenVerificationResult tvr, DecodedJWT decodedToken) {
        String referer = httpRequest.getHeader("referer");
        List<String> claimReferers = decodedToken.getClaim("referer").asList(String.class);
        String ip = httpRequest.getHeader("X-FORWARDED-FOR") != null
                ? httpRequest.getHeader("X-FORWARDED-FOR") : httpRequest.getRemoteAddr();
        List<String> ips = decodedToken.getClaim("ips").asList(String.class);

        if (claimReferers != null && !claimReferers.isEmpty() && !checkReferer(claimReferers, referer)) {
            //Check referers
            tvr.setVerificationStatusCode(TokenVerificationResult.VerificationStatus.REJECTED);
            tvr.setMessage("Incorrect referer in token");
        } else if (ips != null && !ips.isEmpty() && !ips.contains(ip)) {
            //Check IP
            tvr.setVerificationStatusCode(TokenVerificationResult.VerificationStatus.REJECTED);
            tvr.setMessage("Your IP did not match any of the permitted IPs");
        } else {
            tvr.setToken(decodedToken);
            tvr.setVerificationStatusCode(TokenVerificationResult.VerificationStatus.VERIFIED);
            tvr.setMessage("Token verified");
        }
    }

    private boolean checkReferer(List<String> claimReferers, String referer) {
        for (String claimReferer : claimReferers) {
            if (referer.startsWith(claimReferer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        //Do nothing for now
    }
}
