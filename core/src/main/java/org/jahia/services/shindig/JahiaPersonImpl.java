package org.jahia.services.shindig;

import org.apache.shindig.social.core.model.PersonImpl;
import org.jahia.services.usermanager.JahiaUser;

/**
 * An extended version of the Shindig PersonImpl class that adds an accessor to the underlying JahiaUser object.
 *
 * @author loom
 *         Date: Aug 18, 2009
 *         Time: 11:38:24 AM
 */
public class JahiaPersonImpl extends PersonImpl {

    private JahiaUser jahiaUser;

    public JahiaPersonImpl(JahiaUser jahiaUser) {
        super();
        this.jahiaUser = jahiaUser;
        populateValues();
    }

    public JahiaUser getJahiaUser() {
        return jahiaUser;
    }

    public void setJahiaUser(JahiaUser jahiaUser) {
        this.jahiaUser = jahiaUser;
    }

    private void populateValues() {
        this.setDisplayName(jahiaUser.getUsername());
    }
}
