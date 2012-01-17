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

package org.jahia.ajax.gwt.subscription.server;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionService;
import org.jahia.ajax.gwt.client.widget.subscription.GWTSubscription;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.services.notification.Subscription;
import org.jahia.utils.PaginatedList;
import org.jahia.utils.i18n.JahiaResourceBundle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * GWT subscription service implementation.
 *
 * @author Sergiy Shyrkov
 */
public class SubscriptionServiceImpl extends JahiaRemoteService implements SubscriptionService {

    private static final Map<String, String> FIELD_MAPPING;

    static {
        FIELD_MAPPING = new HashMap<String, String>(4);
        FIELD_MAPPING.put("subscriber", "j:subscriber");
        FIELD_MAPPING.put("provider", "j:provider");
        FIELD_MAPPING.put("confirmed", "j:confirmed");
        FIELD_MAPPING.put("suspended", "j:suspended");
    }

    private org.jahia.services.notification.SubscriptionService subscriptionService;

    public void cancel(List<GWTSubscription> subscriptions) throws GWTJahiaServiceException {
        subscriptionService.cancel(toSubscriptionIds(subscriptions), retrieveCurrentSession("live", getLocale(), true));
    }

    public PagingLoadResult<GWTSubscription> getSubscriptions(String uuid, final PagingLoadConfig pagingConfig)
            throws GWTJahiaServiceException {

        PaginatedList<Subscription> subscriptions = subscriptionService.getSubscriptions(uuid,
                pagingConfig.getSortInfo() != null ? FIELD_MAPPING.get(pagingConfig.getSortInfo().getSortField()) :
                        null,
                pagingConfig.getSortInfo() != null ? pagingConfig.getSortInfo().getSortDir() == SortDir.ASC : false,
                pagingConfig.getOffset(), pagingConfig.getLimit(), retrieveCurrentSession("live", getLocale(), true));

        final List<GWTSubscription> gwtSubscriptions = new LinkedList<GWTSubscription>();
        for (Subscription subscription : subscriptions.getData()) {
            gwtSubscriptions.add(toGWTSubscription(subscription));
        }

        return new BasePagingLoadResult<GWTSubscription>(gwtSubscriptions, pagingConfig.getOffset(),
                subscriptions.getTotalSize());
    }

    public void resume(List<GWTSubscription> subscriptions) throws GWTJahiaServiceException {
        subscriptionService.resume(toSubscriptionIds(subscriptions), retrieveCurrentSession("live", getLocale(), true));
    }

    public void setSubscriptionService(org.jahia.services.notification.SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void subscribe(final String uuid, final List<GWTJahiaUser> users) throws GWTJahiaServiceException {
        List<String> subscribers = new LinkedList<String>();
        for (GWTJahiaUser user : users) {
            subscribers.add(user.getUserKey());
        }
        subscriptionService.subscribe(uuid, subscribers, retrieveCurrentSession("live", getLocale(), true));
    }

    public void subscribe(String uuid, String subscribersFile) throws GWTJahiaServiceException {

        GWTFileManagerUploadServlet.Item fileItem = GWTFileManagerUploadServlet.getItem(subscribersFile);
        if (fileItem == null) {
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.unable.to.locate.uploaded.file",getUILocale()));
        }

        try {
            subscriptionService.importSubscriptions(uuid, fileItem.getFile(), retrieveCurrentSession("live", getLocale(), true));
        } finally {
            fileItem.dispose();
        }
    }

    public void suspend(List<GWTSubscription> subscriptions) throws GWTJahiaServiceException {
        subscriptionService
                .suspend(toSubscriptionIds(subscriptions), retrieveCurrentSession("live", getLocale(), true));
    }

    protected GWTSubscription toGWTSubscription(Subscription subscription) {
        GWTSubscription gwtBean = new GWTSubscription();

        gwtBean.setId(subscription.getId());
        gwtBean.setSubscriber(subscription.getSubscriber());
        gwtBean.setProvider(subscription.getProvider());
        gwtBean.setFirstName(subscription.getFirstName());
        gwtBean.setLastName(subscription.getLastName());
        gwtBean.setEmail(subscription.getEmail());
        gwtBean.setSuspended(subscription.isSuspended());
        gwtBean.setConfirmed(subscription.isConfirmed());
        gwtBean.setConfirmationKey(subscription.getConfirmationKey());

        return gwtBean;
    }

    private List<String> toSubscriptionIds(List<GWTSubscription> subscriptions) {
        List<String> ids = new LinkedList<String>();
        for (GWTSubscription subscription : subscriptions) {
            ids.add(subscription.getId());
        }

        return ids;
    }
}
