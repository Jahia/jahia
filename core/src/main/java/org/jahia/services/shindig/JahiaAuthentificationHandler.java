package org.jahia.services.shindig;

import org.apache.log4j.Logger;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.SecurityToken;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Shindig authentification handler for Jahia.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 11:15:14 AM
 */
public class JahiaAuthentificationHandler implements AuthenticationHandler {

    private static final transient Logger logger = Logger.getLogger(JahiaAuthentificationHandler.class);


    public static final String AUTHENTICATION_HANDLER_NAME = "Jahia";

    public String getName() {
        return AUTHENTICATION_HANDLER_NAME;
    }

    public SecurityToken getSecurityTokenFromRequest(HttpServletRequest request) throws InvalidAuthenticationException {
        JahiaUser jahiaUser = null;
        if (Jahia.isInitiated()) {
            HttpSession session = request.getSession(false);
            if (session !=null) {
                jahiaUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
            }
            if (jahiaUser != null) {
                jahiaUser =
                        ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(jahiaUser.getUserKey());
            }
        }
        if (jahiaUser != null) {
            return new JahiaSecurityToken(jahiaUser);
        }
        return null;
    }

    public String getWWWAuthenticateHeader(String realm) {
        return null;
    }
}
