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
 * <p>Title: Jahia group wrapper.</p>
 * <p>Description: Stores groups even if they are null to avoid some requests/queries against providers.</p>
 *
 * @author EP
 * @version 1.0
 */
public class JahiaGroupWrapper implements Serializable {

	// the internal group, only defined when creating object
	private JahiaGroup group;
	
	/**
	 * Constructor.
	 *
	 * @param jg JahiaGroup, a group from a provider.
	 */
	public JahiaGroupWrapper (JahiaGroup jg) {
		group = jg;
	}
	
	/**
	 * Get the internal group.
	 *
	 * @return JahiaGroup, the internal group.
	 */
	public JahiaGroup getGroup() {
		return group;
	}
}