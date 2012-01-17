/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.params.valves;

import java.io.Serializable;

/**
 * Cookie authentication valve configuration.
 * 
 * @author Sergiy Shyrkov
 */
public class CookieAuthConfig implements Serializable {

    private static final long serialVersionUID = 1575999842721969622L;

    private boolean activated;

    private String cookieName = "jid";

    private int idLength = 40;

    private int maxAgeInSeconds = 2592000;

    private boolean renewalActivated;

    private String userPropertyName = "org.jahia.user.cookieauth.id";

    public String getCookieName() {
        return cookieName;
    }

    public int getIdLength() {
        return idLength;
    }

    public int getMaxAgeInSeconds() {
        return maxAgeInSeconds;
    }

    public String getUserPropertyName() {
        return userPropertyName;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isRenewalActivated() {
        return renewalActivated;
    }

    public void setActivated(boolean activted) {
        this.activated = activted;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setIdLength(int idLength) {
        this.idLength = idLength;
    }

    public void setMaxAgeInSeconds(int maxAgeInSeconds) {
        this.maxAgeInSeconds = maxAgeInSeconds;
    }

    public void setRenewalActivated(boolean renewalActivated) {
        this.renewalActivated = renewalActivated;
    }

    public void setUserPropertyName(String userPropertyName) {
        this.userPropertyName = userPropertyName;
    }

}
