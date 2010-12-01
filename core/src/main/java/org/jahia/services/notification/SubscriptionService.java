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

package org.jahia.services.notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.PaginatedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Jahia subscription service implementation.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionService {

	private static final String J_ALLOW_UNREGISTERED_USERS = "j:allowUnregisteredUsers";

	private static final String J_CONFIRMATION_KEY = "j:confirmationKey";

	private static final String J_CONFIRMED = "j:confirmed";

	private static final String J_EMAIL = "j:email";

	private static final String J_FIRST_NAME = "j:firstName";

	private static final String J_LAST_NAME = "j:lastName";

	private static final String J_PROVIDER = "j:provider";

	private static final String J_SUBSCRIBER = "j:subscriber";

	private static final String J_SUBSCRIPTIONS = "j:subscriptions";

	private static final String J_SUSPENDED = "j:suspended";

	private static final String JMIX_SUBSCRIBABLE = "jmix:subscribable";

	private static final String JNT_SUBSCRIPTION = "jnt:subscription";

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	private JahiaUserManagerService userManagerService;

	/**
	 * Cancels the specified subscriptions.
	 * 
	 * @param subscriptionIds
	 *            the list of IDs for subscriptions being canceled
	 */
	public void cancel(final List<String> subscriptionIds) {
		try {
			JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
				public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
					int count = 0;
					for (String id : subscriptionIds) {
						try {
							JCRNodeWrapper target = session.getNodeByIdentifier(id);
							count++;
							session.checkout(target.getParent());
							target.remove();
						} catch (ItemNotFoundException e) {
							logger.warn(
							        "Unable to find subscription node for identifier {}. Skip cancelling subscription.",
							        id);
						}
					}
					if (count > 0) {
						session.save();
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Successfully cancelled {} subscriptions.", count);
					}
					return Boolean.TRUE;
				}
			});
		} catch (RepositoryException e) {
			logger.error("Error cancelling subscriptions " + subscriptionIds, e);
		}
	}

	/**
	 * Cancels the specified subscription.
	 * 
	 * @param subscriptionId
	 *            the IDs of a subscription being canceled
	 */
	public void cancel(String subscriptionId) {
		List<String> ids = new LinkedList<String>();
		ids.add(subscriptionId);

		cancel(ids);
	}

	/**
	 * Changes the suspended status of the specified subscriptions.
	 * 
	 * @param subscriptionIds
	 *            the list of subscription IDs to be changed
	 */
	private void changeSuspendedStatus(final List<String> subscriptionIds, final boolean doSuspend) {
		try {
			JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
				public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

					int count = 0;
					for (String subscriptionId : subscriptionIds) {
						try {
							JCRNodeWrapper subscriptionNode = session
							        .getNodeByIdentifier(subscriptionId);
							JCRPropertyWrapper property = subscriptionNode.getProperty(J_SUSPENDED);
							if (doSuspend && !property.getBoolean() || !doSuspend
							        && property.getBoolean()) {
								count++;
								session.checkout(subscriptionNode);
								subscriptionNode.setProperty(J_SUSPENDED,
								        Boolean.valueOf(doSuspend));
							}
						} catch (ItemNotFoundException nfe) {
							logger.warn("Unable to find subscription node for identifier {}",
							        subscriptionId);
						} catch (RepositoryException e) {
							logger.error(
							        "Error changing suspended status of the subscription with ID "
							                + subscriptionId, e);
						}
						if (count > 0) {
							session.save();
						}
					}

					return true;
				}
			});
		} catch (RepositoryException e) {
			logger.error("Error changing suspended status of subscriptions " + subscriptionIds, e);
		}
	}

	/**
	 * Retrieves subscription for the specified node.
	 * 
	 * @param subscribableIdentifier
	 *            the UUID of the subscribable node
	 * @param orderBy
	 *            order by property; <code>null</code> if no sorting should be
	 *            done
	 * @param orderAscending
	 *            do we sort in ascending direction?
	 * @param offset
	 *            the index of the first result to start with; <code>0</code> to
	 *            start from the beginning
	 * @param limit
	 *            the maximum number of results to return; <code>0</code> to
	 *            return all available
	 * @return paginated list list of {@link Subscription} objects
	 */
	public PaginatedList<Subscription> getSubscriptions(final String subscribableIdentifier,
	        final String orderBy, final boolean orderAscending, final int offset, final int limit) {

		long timer = System.currentTimeMillis();

		final List<Subscription> subscriptions = new LinkedList<Subscription>();
		int total = 0;
		try {
			total = JCRTemplate.getInstance().doExecuteWithSystemSession(
			        new JCRCallback<Integer>() {

				        public Integer doInJCR(JCRSessionWrapper session)
				                throws RepositoryException {
					        JCRNodeWrapper target = session
					                .getNodeByIdentifier(subscribableIdentifier);
					        if (!target.isNodeType(JMIX_SUBSCRIBABLE)) {
						        logger.warn("The target node {} does not have the "
						                + JMIX_SUBSCRIBABLE + " mixin."
						                + " No subscriptions can be found.", target.getPath());
						        return 0;
					        }
					        QueryManager queryManager = session.getWorkspace().getQueryManager();
					        if (queryManager == null) {
						        logger.error("Unable to obtain QueryManager instance");
						        return 0;
					        }

					        StringBuilder q = new StringBuilder();
					        q.append(
					                "select * from [" + JNT_SUBSCRIPTION
					                        + "] where isdescendantnode([")
					                .append(target.getPath()).append("/" + J_SUBSCRIPTIONS + "])");
					        if (orderBy != null) {
						        q.append(" order by [").append(orderBy).append("]")
						                .append(orderAscending ? "asc" : "desc");
					        }
					        Query query = queryManager.createQuery(q.toString(), Query.JCR_SQL2);

					        long totalCount = JCRContentUtils.size(query.execute().getNodes());

					        query.setLimit(limit);
					        query.setOffset(offset);

					        for (NodeIterator nodes = query.execute().getNodes(); nodes.hasNext();) {
						        JCRNodeWrapper subscriptionNode = (JCRNodeWrapper) nodes.next();
						        subscriptions.add(toSubscription(subscriptionNode, session));
					        }

					        return (int) totalCount;
				        }
			        });
		} catch (RepositoryException e) {
			logger.error("Error retrieving subscriptions for node " + subscribableIdentifier, e);
		}

		if (logger.isDebugEnabled()) {
			logger.info("Subscriber search took " + (System.currentTimeMillis() - timer)
			        + " ms. Returning " + subscriptions.size() + " subscriber(s)");
		}

		return new PaginatedList<Subscription>(subscriptions, limit > 0 ? total
		        : subscriptions.size());
	}

	/**
	 * Resumes the specified subscriptions.
	 * 
	 * @param subscriptionIds
	 *            the list of subscription IDs to be resumed
	 */
	public void resume(List<String> subscriptionIds) {
		changeSuspendedStatus(subscriptionIds, false);
	}

	/**
	 * Resumes the specified subscription.
	 * 
	 * @param subscriptionId
	 *            the subscription ID to be resumed
	 */
	public void resume(final String subscriptionId) {
		List<String> subscriptions = new LinkedList<String>();
		subscriptions.add(subscriptionId);
		changeSuspendedStatus(subscriptions, false);
	}

	
	public void setUserManagerService(JahiaUserManagerService userManagerService) {
		this.userManagerService = userManagerService;
	}

	protected void storeProperties(JCRNodeWrapper newSubscriptionNode,
	        Map<String, Object> properties, JCRSessionWrapper session) {
		if (properties == null || properties.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> property : properties.entrySet()) {
			if (!property.getValue().getClass().isArray()) {
				try {
					newSubscriptionNode.setProperty(
					        property.getKey(),
					        JCRContentUtils.createValue(property.getValue(),
					                session.getValueFactory()));
				} catch (RepositoryException e) {
					logger.warn("Unable to set property " + property.getKey(), e);
				}
			} else {
				logger.warn("Cannot handle nultivalue properties. Skipping property {}",
				        property.getKey());
			}
		}
	}

	/**
	 * Import the subscriber data from the specified CSV file and creates
	 * subscriptions for the provided subscribable node.
	 * 
	 * @param subscribableIdentifier
	 *            the UUID of the target subscribable node
	 * @param subscribersCSVFile
	 *            the subscribers file in CSV format
	 */
	public void subscribe(String subscribableIdentifier, File subscribersCSVFile) {
		long timer = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			logger.debug("Start importing subscriptions for source node {}", subscribableIdentifier);
		}

		int importedCount = 0;

		InputStream is = null;
		CSVReader reader = null;
		try {
			is = new FileInputStream(subscribersCSVFile);
			char separator = ',';
			reader = new CSVReader(new InputStreamReader(is, "UTF-8"), separator);
			String[] columns = reader.readNext();
			if (columns == null) {
				logger.warn("No data for importing subscriptions is found"
				        + " or the file is not well-formed");
				return;
			}
			if (columns.length == 1 && columns[0].contains(";")) {
				// semicolon is used as a separator
				reader.close();
				IOUtils.closeQuietly(is);
				is = new FileInputStream(subscribersCSVFile);
				separator = ';';
				reader = new CSVReader(new InputStreamReader(is, "UTF-8"), separator);
				columns = reader.readNext();
			}
			int usernamePosition = ArrayUtils.indexOf(columns, "j:nodename");
			int emailPosition = ArrayUtils.indexOf(columns, J_EMAIL);
			if (usernamePosition == -1 && emailPosition == -1) {
				logger.warn("No data for importing subscriptions is found"
				        + " or the file is not well-formed");
				return;
			}
			Map<String, Map<String, Object>> subscribers = new HashMap<String, Map<String, Object>>();
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				String username = usernamePosition != -1 ? nextLine[usernamePosition] : null;
				String email = emailPosition != -1 ? nextLine[emailPosition] : null;
				boolean registered = true;
				if (StringUtils.isNotEmpty(username)) {
					// registered Jahia user is provided
					JahiaUser user = username.charAt(0) == '{' ? userManagerService
					        .lookupUserByKey(username) : userManagerService.lookupUser(username);
					if (user != null) {
						if (username.charAt(0) != '{') {
							username = "{" + user.getProviderName() + "}" + username;
						}
					} else {
						logger.warn("No user can be found for the specified username '" + username
						        + "'. Skipping subscription: "
						        + StringUtils.join(nextLine, separator));
						continue;
					}
				} else if (StringUtils.isNotEmpty(email)) {
					username = email;
					registered = false;
				} else {
					logger.warn("Neither a j:nodename nor j:email is provided."
					        + "Skipping subscription: " + StringUtils.join(nextLine, separator));
					continue;
				}
				Map<String, Object> props = new HashMap<String, Object>(columns.length);
				for (int i = 0; i < columns.length; i++) {
					String column = columns[i];
					if ("j:nodename".equals(column) || !registered && J_EMAIL.equals(column)) {
						continue;
					}
					props.put(column, nextLine[i]);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Subscribing '" + username + "' with properties: " + props);
				}

				subscribers.put(username, props);
				if (subscribers.size() > 1000) {
					// flush
					subscribe(subscribableIdentifier, subscribers);
					importedCount += subscribers.size();
					subscribers = new HashMap<String, Map<String, Object>>();
				}
			}
			if (!subscribers.isEmpty()) {
				// subscribe the rest
				importedCount += subscribers.size();
				subscribe(subscribableIdentifier, subscribers);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					// ignore
				}
			}
			IOUtils.closeQuietly(is);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Importing {} subscriptions for source node {} took {} ms", new Object[] {
			        importedCount, subscribableIdentifier, System.currentTimeMillis() - timer });
		}
	}

	/**
	 * Creates subscription for the specified users and subscribable node.
	 * 
	 * @param subscribableIdentifier
	 *            the UUID of the target subscribable node
	 * @param userKeys
	 *            the list of values for registered Jahia user keys (the one,
	 *            returned by {@link JahiaUser#getUserKey()})
	 */
	public void subscribe(final String subscribableIdentifier, List<String> userKeys) {
		Map<String, Map<String, Object>> subscribers = new HashMap<String, Map<String, Object>>(
		        userKeys.size());
		for (String user : userKeys) {
			subscribers.put(user, null);
		}
		subscribe(subscribableIdentifier, subscribers);
	}

	/**
	 * Creates subscription for the specified users and subscribable node
	 * 
	 * @param subscribableIdentifier
	 *            the UUID of the target subscribable node
	 * @param subscribers
	 *            a map with subscriber information. The key is a subscriber ID,
	 *            the value is a map with additional properties that will be
	 *            stored for the subscription object. The subscriber ID is a a
	 *            user key in case of a registered Jahia user (the one, returned
	 *            by {@link JahiaUser#getUserKey()}). In case of a
	 *            non-registered user this is an e-mail address of the
	 *            subscriber.
	 */
	public void subscribe(final String subscribableIdentifier,
	        final Map<String, Map<String, Object>> subscribers) {
		try {
			JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {

				public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
					JCRNodeWrapper target = session.getNodeByIdentifier(subscribableIdentifier);
					if (!target.isNodeType(JMIX_SUBSCRIBABLE)) {
						logger.warn("The target node {} does not have the " + JMIX_SUBSCRIBABLE
						        + " mixin." + " No subscriptions can be created.", target.getPath());
						return Boolean.FALSE;
					}
					JCRNodeWrapper subscriptionsNode = target.getNode(J_SUBSCRIPTIONS);
					String targetPath = subscriptionsNode.getPath();
					if (target.isLocked() || subscriptionsNode.isLocked()) {
						logger.info(
						        "The target {} is locked and no subscriptions can be created. Skipping {} subscribers.",
						        targetPath, subscribers.size());
					}
					
					boolean allowUnregisteredUsers = target.hasProperty(J_ALLOW_UNREGISTERED_USERS) ? target.getProperty(J_ALLOW_UNREGISTERED_USERS).getBoolean() : true;
					boolean checkoutDone = false;
					QueryManager queryManager = session.getWorkspace().getQueryManager();
					if (queryManager == null) {
						logger.error("Unable to obtain QueryManager instance");
						return Boolean.FALSE;
					}
					for (Map.Entry<String, Map<String, Object>> subscriber : subscribers.entrySet()) {
						String username = subscriber.getKey();
						String provider = null;
						if (username.charAt(0) == '{') {
							// we deal with a registered user
							username = StringUtils.substringAfter(subscriber.getKey(), "}");
							provider = StringUtils.substringBetween(subscriber.getKey(), "{", "}");
						} else if (!allowUnregisteredUsers) {
							logger.info(
							        "The target {} does not allow unregistered users to be subscribed. Skipping subscription for {}.",
							        targetPath, subscriber.getKey());
							continue;
						}

						StringBuilder q = new StringBuilder(64);
						q.append(
						        "select * from [" + JNT_SUBSCRIPTION + "] where [" + J_SUBSCRIBER
						                + "]='").append(username).append("'");
						if (provider != null) {
							q.append(" and [" + J_PROVIDER + "]='").append(provider).append("'");
						}
						q.append(" and").append(" isdescendantnode([").append(targetPath)
						        .append("])");
						Query query = queryManager.createQuery(q.toString(), Query.JCR_SQL2);
						query.setLimit(1);
						if (!query.execute().getNodes().hasNext()) {
							if (logger.isDebugEnabled()) {
								logger.debug("Creating subscription to the {} for {}.", targetPath,
								        subscriber.getKey());
							}
							if (!checkoutDone) {
								session.checkout(subscriptionsNode);
								checkoutDone = true;
							}
							JCRNodeWrapper newSubscriptionNode = subscriptionsNode.addNode(
							        JCRContentUtils.findAvailableNodeName(subscriptionsNode,
							                "subscription"), JNT_SUBSCRIPTION);
							newSubscriptionNode.setProperty(J_SUBSCRIBER, username);
							if (provider != null) {
								newSubscriptionNode.setProperty(J_PROVIDER, provider);
							}
							storeProperties(newSubscriptionNode, subscriber.getValue(), session);
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug(
								        "Subscription for the {} and {} is already present. Skipping ceraring new one.",
								        targetPath, subscriber.getKey());
							}
						}
					}
					if (checkoutDone) {
						session.save();
					}
					return Boolean.TRUE;
				}
			});
		} catch (RepositoryException e) {
			logger.error("Error creating subscriptions for node " + subscribableIdentifier, e);
		}
	}

	/**
	 * Creates subscription for the specified user and subscribable node
	 * 
	 * @param subscribableIdentifier
	 *            the UUID of the target subscribable node
	 * @param userKey
	 *            the key of a registered Jahia user (the one, returned by
	 *            {@link JahiaUser#getUserKey()})
	 */
	public void subscribe(final String subscribableIdentifier, String userKey) {
		Map<String, Map<String, Object>> subscribers = new HashMap<String, Map<String, Object>>(1);
		subscribers.put(userKey, null);
		subscribe(subscribableIdentifier, subscribers);
	}

	/**
	 * Creates subscription for the specified user and subscribable node
	 * 
	 * @param subscribableIdentifier
	 *            the UUID of the target subscribable node
	 * @param subscriberEmail
	 *            an e-mail for the non-registered user to be subscribed
	 * @param properties
	 *            additional properties to be stored for the subscription (e.g.
	 *            first and last name, etc.)
	 */
	public void subscribe(final String subscribableIdentifier, String subscriberEmail,
	        Map<String, Object> properties) {
		Map<String, Map<String, Object>> subscribers = new HashMap<String, Map<String, Object>>(1);
		subscribers.put(subscriberEmail, properties);
		subscribe(subscribableIdentifier, subscribers);
	}

	/**
	 * Suspends corresponding subscriptions.
	 * 
	 * @param subscriptionId
	 *            the list of subscription IDs to suspend
	 */
	public void suspend(List<String> subscriptionIds) {
		changeSuspendedStatus(subscriptionIds, true);
	}

	/**
	 * Suspends the specified subscription.
	 * 
	 * @param subscriptionId
	 *            the subscription ID to be suspended
	 */
	public void suspend(final String subscriptionId) {
		List<String> subscriptions = new LinkedList<String>();
		subscriptions.add(subscriptionId);
		changeSuspendedStatus(subscriptions, true);
	}

	protected Subscription toSubscription(JCRNodeWrapper subscriptionNode, JCRSessionWrapper session)
	        throws ValueFormatException, PathNotFoundException, RepositoryException {
		Subscription subscriber = new Subscription();

		subscriber.setId(subscriptionNode.getIdentifier());
		subscriber.setSubscriber(subscriptionNode.getProperty(J_SUBSCRIBER).getString());

		String provider = null;
		try {
			provider = subscriptionNode.getProperty(J_PROVIDER).getString();
			subscriber.setProvider(provider);
		} catch (PathNotFoundException e) {
			// non-registered subscriber
		}

		if (provider != null) {
			// registered user
			String key = "{" + provider + "}" + subscriber.getSubscriber();
			JahiaUser user = userManagerService.lookupUserByKey(key);
			if (user != null) {
				subscriber.setFirstName(user.getProperty(J_FIRST_NAME));
				subscriber.setLastName(user.getProperty(J_LAST_NAME));
				subscriber.setEmail(user.getProperty(J_EMAIL));
			} else {
				logger.warn("Unable to find user for key {}", key);
			}
		} else {
			subscriber.setEmail(subscriber.getSubscriber());
			try {
				subscriber.setFirstName(subscriptionNode.getProperty(J_FIRST_NAME).getString());
			} catch (PathNotFoundException e) {
				// no property set
			}
			try {
				subscriber.setLastName(subscriptionNode.getProperty(J_LAST_NAME).getString());
			} catch (PathNotFoundException e) {
				// no property set
			}
		}

		try {
			subscriber.setConfirmationKey(subscriptionNode.getProperty(J_CONFIRMATION_KEY)
			        .getString());
		} catch (PathNotFoundException e) {
			// no confirmation key set
		}
		subscriber.setConfirmed(subscriptionNode.getProperty(J_CONFIRMED).getBoolean());
		subscriber.setSuspended(subscriptionNode.getProperty(J_SUSPENDED).getBoolean());

		return subscriber;
	}

}
