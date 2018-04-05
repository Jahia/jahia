/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.api.Constants;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;

/**
 * Listener implementation used to remove the j:published in default workspace if the node in live workspace is removed. 
 * User: wassek 
 * Date: Apr 05, 2018 
 * Time: 10:36:05 AM
 */
public class UnpublishOnLiveDeletionListener extends DefaultEventListener {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(UnpublishOnLiveDeletionListener.class);

	public int getEventTypes() {
		return Event.NODE_REMOVED;
	}

	public void onEvent(final EventIterator eventIterator) {
		try {
			JahiaUser user = ((JCREventIterator) eventIterator).getSession().getUser();

			JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, Constants.EDIT_WORKSPACE, null,
					new JCRCallback<Object>() {
						public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
							try {
								JCRObservationManager.setAllEventListenersDisabled(true);
								while (eventIterator.hasNext()) {
									Event event = eventIterator.nextEvent();

									if (isExternal(event)) {
										continue;
									}

									String path = event.getPath();
									if (event.getType() == Event.NODE_REMOVED
											&& workspace.equals(Constants.LIVE_WORKSPACE)) {
										// check in default if exists
										logger.debug("Node in live removed, check if exists in default");
										if (session.itemExists(path)) {
											JCRNodeWrapper node = session.getNode(path);
											if (node.hasProperty("j:published")
													&& node.getProperty("j:published").getBoolean()) {
												try {
													node.getProperty("j:published").remove();
												} catch (RepositoryException ex) {
													logger.error("cannot remove Property j:published", ex);
												}
											}
										}
									}
								}
							} catch (Exception ex) {
								logger.error(ex.getMessage(), ex);
							} finally {
								session.save();
								JCRObservationManager.setAllEventListenersDisabled(false);
							}
							return null;
						}
					});

		} catch (RepositoryException e) {
			logger.error(e.getMessage(), e);
		}

	}

}
