/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.bin.ActionResult;
import org.jahia.bin.Action;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Newsletter confirmation action.
 * User: toto
 * Date: Dec 7, 2010
 * Time: 11:07:04 AM
 */
public class ConfirmAction extends Action {
    private SubscriptionService subscriptionService;
    private String subscriptionConfirmationPagePath;
    private String unsubscriptionConfirmationPagePath;

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void setUnsubscriptionConfirmationPagePath(String unsubscriptionConfirmationPagePath) {
        this.unsubscriptionConfirmationPagePath = unsubscriptionConfirmationPagePath;
    }

    public void setSubscriptionConfirmationPagePath(String subscriptionConfirmationPagePath) {
        this.subscriptionConfirmationPagePath = subscriptionConfirmationPagePath;
    }

    public ActionResult doExecute(final HttpServletRequest req, final RenderContext renderContext, final Resource resource,
                                  JCRSessionWrapper session, final Map<String, List<String>> parameters, final URLResolver urlResolver) throws Exception {

        return JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<ActionResult>() {
            public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String key = req.getParameter("key");
                String action = req.getParameter("exec");
                JCRNodeWrapper sub = subscriptionService.getSubscriptionFromKey(key, session);
                if (sub != null) {
                    if ("add".equals(action)) {
                        sub.setProperty(SubscriptionService.J_CONFIRMED, true);
                        sub.getProperty(SubscriptionService.J_CONFIRMATION_KEY).remove();
                        session.save();
                        req.setAttribute("subscribed", Arrays.asList(resource.getNode()));
                        return new ActionResult(SC_OK, resource.getNode().getResolveSite().getPath() +
                                subscriptionConfirmationPagePath);
                    } else if ("rem".equals(action)) {
                        sub.remove();
                        session.save();
                        req.setAttribute("unsubscribed", Arrays.asList(resource.getNode()));
                        return new ActionResult(SC_OK, resource.getNode().getResolveSite().getPath() + unsubscriptionConfirmationPagePath);
                    }
                }

                 req.setAttribute("subscribed", Arrays.asList(resource.getNode()));
                        return new ActionResult(SC_OK, resource.getNode().getResolveSite().getPath() +
                                subscriptionConfirmationPagePath);
            }
        });
    }
}
