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

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;

import org.jahia.bin.ActionResult;
import org.jahia.bin.BaseAction;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action and a background task that sends the content of the specified node as a newsletter
 * to its subscribers.
 * 
 * @author Sergiy Shyrkov
 */
public class SendAsNewsletterAction extends BaseAction implements BackgroundAction {

	private static final String J_LAST_SENT = "j:lastSent";

	private static final String J_SCHEDULED = "j:scheduled";

	private static final Logger logger = LoggerFactory.getLogger(SendAsNewsletterAction.class);

	public void executeBackgroundAction(final JCRNodeWrapper node) {
		logger.info("Sending content of the node {} as a newsletter", node.getPath());
		long timer = System.currentTimeMillis();
		
		// TODO do send the newsletter

		try {
			JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
				public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
					session.checkout(node);
					node.setProperty(J_SCHEDULED, (Value) null);
					node.setProperty(J_LAST_SENT, Calendar.getInstance());
					session.save();

					return Boolean.TRUE;
				}
			});
		} catch (RepositoryException e) {
			logger.warn("Unable to update properties for node " + node.getPath(), e);
		}

		logger.info("The content of the node {} was sent as a newsletter in {} ms", node.getPath(),
		        System.currentTimeMillis() - timer);
	}

	public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
            Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver)
            throws Exception {

		// TODO do send the newsletter

	    return ActionResult.OK;
    }

}
