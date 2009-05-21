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
