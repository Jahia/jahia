/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.newsletter;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.bin.ActionResult;
import org.jahia.bin.BaseAction;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action for subscribing a user to the target node.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscribeAction extends BaseAction {

	private static final Logger logger = LoggerFactory.getLogger(SubscribeAction.class);

	private boolean allowRegistrationWithoutEmail;

	private SubscriptionService subscriptionService;

	public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
	        Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver)
	        throws Exception {

		String email = getParameter(parameters, "email");
		if (email != null) {
			// consider as non-registered user
			if (email.length() == 0 || !MailService.isValidEmailAddress(email, false)) {
				// provided e-mail is empty
				logger.warn("Invalid e-mail address '{}' provided for subscription to {}."
				        + " Ignoring subscription request.", email, resource.getNode().getPath());
				return new ActionResult(SC_OK, null, new JSONObject("{\"status\":\"invalid-email\"}"));
			}
			Map<String, Object> props = new HashMap<String, Object>();
			
			subscriptionService.subscribe(resource.getNode().getIdentifier(), email, props);
		} else {
			JahiaUser user = renderContext.getUser();
			if (JahiaUserManagerService.isGuest(user)) {
				// anonymous users are not allowed (and no email was provided)
				return new ActionResult(SC_UNAUTHORIZED);
			}
			
			if (!allowRegistrationWithoutEmail) {
				// checking if the user has a valid e-mail address
				String userEmail = user.getProperty("j:email");
				if (userEmail == null || !MailService.isValidEmailAddress(userEmail, false)) {
					// no valid e-mail provided -> refuse
					return new ActionResult(SC_OK, null, new JSONObject("{\"status\":\"no-valid-email\"}"));
				}
			}
			
			subscriptionService.subscribe(resource.getNode().getIdentifier(), user.getUserKey());
		}

		return new ActionResult(SC_OK, null, new JSONObject("{\"status\":\"ok\"}"));
	}

	public void setAllowRegistrationWithoutEmail(boolean allowRegistrationWithoutEmail) {
		this.allowRegistrationWithoutEmail = allowRegistrationWithoutEmail;
	}

	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

}