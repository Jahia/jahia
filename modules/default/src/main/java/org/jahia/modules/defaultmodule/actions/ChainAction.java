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

package org.jahia.modules.defaultmodule.actions;

import org.jahia.bin.*;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.AccessDeniedException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Action handler that is capable of executing a chain of specified action.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 11 mars 2010
 */
public class ChainAction extends Action implements InitializingBean {

    private JahiaTemplateManagerService templateService;
    private DefaultPostAction defaultPostAction;
    public static final String ACTION_NAME = "chain";
    public static final String CHAIN_OF_ACTION = "chainOfAction";

    public ChainAction() {
        setName(ACTION_NAME);
    }

    public void setTemplateService(JahiaTemplateManagerService templateService) {
        this.templateService = templateService;
    }

    public void setDefaultPostAction(DefaultPostAction defaultPostAction) {
        this.defaultPostAction = defaultPostAction;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
                                  Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver)
            throws Exception {
        List<String> chainOfactions = parameters.get(CHAIN_OF_ACTION);
        if (chainOfactions != null) {
            String[] actions = chainOfactions.get(0).split(",");
            Map<String, Action> actionsMap = templateService.getActions();
            ActionResult result = null;
            for (String actionToDo : actions) {
                if (DefaultPostAction.ACTION_NAME.equals(actionToDo)) {
                    String s = urlResolver.getUrlPathInfo().replace(".chain.do", "/*");
                    URLResolver resolver = new URLResolver(s,req.getServerName(), req);
                    resolver.setSiteKey(urlResolver.getSiteKey());
                    result = defaultPostAction.doExecute(req, renderContext, resource, session, parameters, resolver);
                } else {
                    Action action = actionsMap.get(actionToDo);
                    if (action.getRequiredPermission() == null || resource.getNode().hasPermission(action.getRequiredPermission())) {
                        result = action.doExecute(req, renderContext, resource, session, parameters, urlResolver);
                    } else {
                        throw new AccessDeniedException();
                    }
                }
            }
            return result;
        }
        return ActionResult.BAD_REQUEST;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        Render.getReservedParameters().add(CHAIN_OF_ACTION);
    }
}
