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

package org.jahia.services.pwdpolicy;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.services.usermanager.JahiaUser;

/**
 * The rule condition evaluation context.
 * 
 * @author Sergiy Shyrkov
 */
class EvaluationContext {

	private String password;

	private JahiaUser user;

	private boolean userInitiated;

	/**
	 * Initializes an instance of this class.
	 * 
	 * @param user
	 *            the target user
	 * @param password
	 *            the new user password
	 * @param isUserInitiated
	 *            set to <code>true</code> if the operation was initiated by
	 *            the user and not via administration interface
	 */
	public EvaluationContext(JahiaUser user, String password,
	        boolean isUserInitiated) {
		super();
		this.user = user;
		this.password = password;
		this.userInitiated = isUserInitiated;
	}

	/**
	 * Returns the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the user.
	 * 
	 * @return the user
	 */
	public JahiaUser getUser() {
		return user;
	}

	/**
	 * Returns <code>true</code> if the operation was initiated by the user
	 * and not via administration interface.
	 * 
	 * @return <code>true</code> if the operation was initiated by the user
	 *         and not via administration interface
	 */
	public boolean isUserInitiated() {
		return userInitiated;
	}

	/**
	 * Sets the value of password.
	 * 
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets the value of user.
	 * 
	 * @param user
	 *            the user to set
	 */
	public void setUser(JahiaUser user) {
		this.user = user;
	}

	/**
	 * Set to <code>true</code> if the operation was initiated by the user and
	 * not via administration interface.
	 * 
	 * @param userInitiated
	 *            set to <code>true</code> if the operation was initiated by
	 *            the user and not via administration interface
	 */
	public void setUserInitiated(boolean userInitiated) {
		this.userInitiated = userInitiated;
	}

	public String toString() {
		return new ToStringBuilder(this).append("user", user).append(
		        "password", password != null ? "<protected>" : "null").append(
		        "userInitiated", userInitiated).toString();
	}

}
