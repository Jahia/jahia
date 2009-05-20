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
