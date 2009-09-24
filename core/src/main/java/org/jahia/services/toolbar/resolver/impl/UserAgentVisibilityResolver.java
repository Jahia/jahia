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
package org.jahia.services.toolbar.resolver.impl;

import org.jahia.data.JahiaData;
import org.jahia.gui.GuiBean;
import org.jahia.services.toolbar.resolver.VisibilityResolver;

/**
 * Enables toolbar item based on the user agent.
 *
 * @author Sergiy Shyrkov
 *
 */
public class UserAgentVisibilityResolver implements VisibilityResolver {

	/* (non-Javadoc)
	 * @see org.jahia.services.toolbar.resolver.VisibilityResolver#isVisible(org.jahia.data.JahiaData, java.lang.String)
	 */
	public boolean isVisible(JahiaData jData, String userAgent) {
		boolean matches = false;
		GuiBean gui = jData.getGui();
		if ("ie".equals(userAgent)) {
			matches = gui.isIE();
		} else if ("ie4".equals(userAgent)) {
			matches = gui.isIE4();
		} else if ("ie5".equals(userAgent)) {
			matches = gui.isIE5();
		} else if ("ie6".equals(userAgent)) {
			matches = gui.isIE6();
		} else if ("ie7".equals(userAgent)) {
			matches = gui.isIE7();
		} else if ("ns".equals(userAgent)) {
			matches = gui.isNS();
		} else if ("ns4".equals(userAgent)) {
			matches = gui.isNS4();
		} else if ("ns6".equals(userAgent)) {
			matches = gui.isNS6();
		} else if ("opera".equals(userAgent)) {
			matches = gui.isOpera();
		}
		return matches;
	}

}
