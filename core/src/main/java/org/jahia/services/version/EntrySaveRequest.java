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
package org.jahia.services.version;

import org.jahia.services.usermanager.JahiaUser;

/**
 * This class should contain every info needed to specify which version (lang) the user
 * wish to save, and what issued the command (maybe it can be usefull for rights check)
 */
public class EntrySaveRequest implements Cloneable {

    private JahiaUser theUser;      // which user is the requester ? null = internal request
    private String languageCode;  // the language in which to save the data, null = not language-specific
    private boolean isNew;

    public EntrySaveRequest(JahiaUser theUser, String languageCode) {
        this.theUser = theUser;
        this.languageCode = languageCode;
    }

    public EntrySaveRequest(JahiaUser theUser, String languageCode, boolean isNew) {
        this.theUser = theUser;
        this.languageCode = languageCode;
        this.isNew = isNew;
    }

    public EntrySaveRequest(JahiaUser theUser) {
        this.theUser = theUser;
        this.languageCode = null;
    }

    public EntrySaveRequest(String theLanguage) {
        this.theUser = null;
        this.languageCode = theLanguage;
    }

    public Object clone() {
        return new EntrySaveRequest(theUser, languageCode, isNew);
    }

    /**
     * Which user issued the request ?
     *
     * @return the user, null if it is an internal request (no user issued it)
     */
    public JahiaUser getUser() {
        return theUser;
    }

    public void setUser(JahiaUser theUser) {
        this.theUser = theUser;
    }

    /**
     * In which language do we want to save
     *
     * @return the language, null if what we want to save is not language specific
     */
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String theLanguage) {
        this.languageCode = theLanguage;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String toString() {
        return new String("[SAVEREQUEST:user=" + theUser + ", language=" + languageCode + "]");
    }
} 
