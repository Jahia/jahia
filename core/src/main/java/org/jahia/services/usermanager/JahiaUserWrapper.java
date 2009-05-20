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
 package org.jahia.services.usermanager;

import java.io.Serializable;

/**
 * <p>Title: Jahia user wrapper.</p>
 * <p>Description: Stores users even if they are null to avoid some requests/queries against providers.</p>
 *
 * @author EP
 * @version 1.0
 */
public class JahiaUserWrapper implements Serializable {

	// the internal user, only defined when creating object         	
	private JahiaUser user;
	
	/**
	 * Constructor.
	 *
	 * @param ju JahiaUser, a user from a provider.
	 */	
	public JahiaUserWrapper (JahiaUser ju) {
		user = ju;
	}
	
	/**
	 * Get the internal user.
	 *
	 * @return JahiaUser, the internal user.
	 */
	public JahiaUser getUser() {
		return user;
	}
}