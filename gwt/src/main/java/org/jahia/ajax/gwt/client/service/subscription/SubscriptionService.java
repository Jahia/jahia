/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.service.subscription;

import java.util.List;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.widget.subscription.GWTSubscription;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Subscription service definition.
 * 
 * @author Sergiy Shyrkov
 */
public interface SubscriptionService extends RemoteService {

	public static class App {
		private static SubscriptionServiceAsync app = null;

		private static String createEntryPointUrl() {
			return JahiaGWTParameters.getServiceEntryPoint() + "subscription.gwt?lang="
			        + JahiaGWTParameters.getLanguage() + "&site="
			        + JahiaGWTParameters.getSiteUUID() + "&workspace="
			        + JahiaGWTParameters.getWorkspace();
		}

		public static synchronized SubscriptionServiceAsync getInstance() {
			if (app == null) {
				app = (SubscriptionServiceAsync) GWT.create(SubscriptionService.class);
				((ServiceDefTarget) app).setServiceEntryPoint(URL
				        .getAbsoluteURL(createEntryPointUrl()));

				JahiaGWTParameters.addUpdater(new JahiaGWTParameters.UrlUpdater() {
					public void updateEntryPointUrl() {
						((ServiceDefTarget) app).setServiceEntryPoint(URL
						        .getAbsoluteURL(createEntryPointUrl()));
					}
				});
			}
			return app;
		}
	}

	/**
	 * Permanently removes the specified subscription entries.
	 * 
	 * @param subscriptions
	 *            the subscriptions to be removed
	 */
	void cancel(List<GWTSubscription> subscriptions) throws GWTJahiaServiceException;

	/**
	 * Returns a list of subscribers for the specified object.
	 * 
	 * @param uuid
	 *            the node identifier to look subscriptions for
	 * @param pagingConfig
	 *            the paging configuration, including limits and sorting
	 * @return a list of subscribers for the specified object and event type
	 */
	PagingLoadResult<GWTSubscription> getSubscriptions(String uuid, PagingLoadConfig pagingConfig) throws GWTJahiaServiceException;

	/**
	 * Resumes the specified subscriptions.
	 * 
	 * @param subscriptions
	 *            the subscriptions to be suspended
	 */
	void resume(List<GWTSubscription> subscriptions) throws GWTJahiaServiceException;

	/**
	 * Creates subscription entries for the provided users and specified object.
	 * 
	 * @param uuid
	 *            the node identifier to subscribe for
	 * @param subscribers
	 *            list of users to create subscription for
	 */
	void subscribe(String uuid, List<GWTJahiaUser> subscribers) throws GWTJahiaServiceException;

	/**
	 * Creates subscription entries for the users, imported from the CSV file.
	 * 
	 * @param uuid
	 *            the identifier of the node to create subscriptions for
	 * @param subscribersFile
	 *            CSV file to import subscribers from
	 * @throws GWTJahiaServiceException
	 *             in case of a processing error
	 */
	void subscribe(String uuid, String subscribersFile) throws GWTJahiaServiceException;

	/**
	 * Suspends the specified subscriptions.
	 * 
	 * @param subscriptions
	 *            the subscriptions to be suspended
	 */
	void suspend(List<GWTSubscription> subscriptions) throws GWTJahiaServiceException;

}
