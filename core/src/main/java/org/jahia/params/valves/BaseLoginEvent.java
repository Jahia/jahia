package org.jahia.params.valves;

import org.jahia.services.usermanager.JahiaUser;
import org.springframework.context.ApplicationEvent;

/**
 * Base login event
 */
public class BaseLoginEvent extends ApplicationEvent {
    private JahiaUser jahiaUser;
    private AuthValveContext authValveContext;

    public BaseLoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
        super(source);
        this.jahiaUser = jahiaUser;
        this.authValveContext = authValveContext;
    }

    public JahiaUser getJahiaUser() {
        return jahiaUser;
    }

    public AuthValveContext getAuthValveContext() {
        return authValveContext;
    }

}
