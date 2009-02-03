/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.preferences.layoutmanager;

import org.jahia.services.preferences.JahiaPreferenceKey;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;

import java.security.Principal;
import java.util.Map;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 12:18:38
 */
public class LayoutmanagerJahiaPreferenceKey extends JahiaPreferenceKey {
    public static final String TYPE = "org.jahia.services.preferences.jahiapreferencekey.layoutmanager";
    private String windowId;
    private String pid;


    public LayoutmanagerJahiaPreferenceKey() {
        super(TYPE);
    }

    public LayoutmanagerJahiaPreferenceKey(JahiaPreferenceKey key) throws JahiaPreferenceNotDefinedAttributeException {
        super(TYPE);
        copy(key);
    }

    public LayoutmanagerJahiaPreferenceKey(String type, Principal principal, Map<String, String> extraAttributes) throws JahiaPreferenceNotDefinedAttributeException {
        super(type, principal, extraAttributes);
    }


    public String getWindowId() {
        return windowId;
    }

    public void setWindowId(String windowId) {
        this.windowId = windowId;
    }


    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String toString() {
        return pid + "|" + getPrincipalKey() + "|" + getPrincipalType();
    }
}
