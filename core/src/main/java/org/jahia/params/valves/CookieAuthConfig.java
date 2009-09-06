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
package org.jahia.params.valves;

import java.io.Serializable;

/**
 * Cookie authentication valve configuration.
 * 
 * @author Sergiy Shyrkov
 * 
 */
public class CookieAuthConfig implements Serializable {

	private boolean activated;

	private int idLength = 40;

	private String userPropertyName = "org.jahia.user.cookieauth.id";

	private String cookieName = "jid";

	private int maxAgeInSeconds = 2592000;

	private boolean renewalActivated;

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activted) {
		this.activated = activted;
	}

	public int getIdLength() {
		return idLength;
	}

	public void setIdLength(int idLength) {
		this.idLength = idLength;
	}

	public String getUserPropertyName() {
		return userPropertyName;
	}

	public void setUserPropertyName(String userPropertyName) {
		this.userPropertyName = userPropertyName;
	}

	public String getCookieName() {
		return cookieName;
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public int getMaxAgeInSeconds() {
		return maxAgeInSeconds;
	}

	public void setMaxAgeInSeconds(int maxAgeInSeconds) {
		this.maxAgeInSeconds = maxAgeInSeconds;
	}

	public boolean isRenewalActivated() {
		return renewalActivated;
	}

	public void setRenewalActivated(boolean renewalActivated) {
		this.renewalActivated = renewalActivated;
	}

}
