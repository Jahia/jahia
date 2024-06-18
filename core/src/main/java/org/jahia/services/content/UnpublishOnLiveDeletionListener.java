/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.api.Constants;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;

/**
 * Listener implementation used to remove the j:published in default workspace
 * if the node in live workspace is removed. User: wassek Date: Apr 05, 2018
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
									if (event.getType() == Event.NODE_REMOVED
											&& workspace.equals(Constants.LIVE_WORKSPACE)) {
										// check in default if exists
										if (logger.isDebugEnabled()) {
											logger.debug("Node in live removed, check if exists in default (" + event.getPath() + ")");
										}
										try {
											JCRNodeWrapper node = session.getNodeByUUID(event.getIdentifier());
											if (node != null) {
												if (node.hasProperty("j:published")
														&& node.getProperty("j:published").getBoolean()) {
													try {
														node.getProperty("j:published").remove();
													} catch (RepositoryException ex) {
														logger.error("cannot remove Property j:published", ex);
													}
												}
											}
										} catch (ItemNotFoundException ex) {
											// node doesn't exists in default
											// workspace, do nothing.
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
