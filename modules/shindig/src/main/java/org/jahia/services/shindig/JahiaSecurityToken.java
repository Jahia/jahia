package org.jahia.services.shindig;

import org.apache.shindig.auth.SecurityToken;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Jahia Security Token for Shindig's authentification handler.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 11:15:46 AM
 */
public class JahiaSecurityToken implements SecurityToken {

    private JahiaUser jahiaUser;

    public JahiaSecurityToken(JahiaUser jahiaUser) {
        this.jahiaUser = jahiaUser;
    }

    public String getOwnerId() {
        return jahiaUser.getUserKey();
    }

    public String getViewerId() {
        return jahiaUser.getUserKey();
    }

    public String getAppId() {
        return null;
    }

    public String getDomain() {
        return null;
    }

    public String getContainer() {
        return null;
    }

    public String getAppUrl() {
        return null;
    }

    public long getModuleId() {
        return 0;
    }

    public String getUpdatedToken() {
        return null;
    }

    public String getAuthenticationMode() {
        return null;
    }

    public String getTrustedJson() {
        return null;
    }

    public boolean isAnonymous() {
        return false;
    }

    public String getActiveUrl() {
        return null;
    }
}
