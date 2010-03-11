/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.formbuilder.actions;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.DefaultPostActionResult;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 11 mars 2010
 */
public class ChainAction implements Action, InitializingBean {
    private transient static Logger logger = Logger.getLogger(ChainAction.class);
    private JahiaTemplateManagerService templateService;
    public static final String ACTION_NAME = "chain";
    public static final String CHAIN_OF_ACTION = "chainOfAction";

    public String getName() {
        return ACTION_NAME;
    }

    public JCRNodeWrapper getNewNode() {
        return null;
    }

    public void setTemplateService(JahiaTemplateManagerService templateService) {
        this.templateService = templateService;
    }

    public void doExecute(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                          Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver)
            throws Exception {
        List<String> chainOfactions = parameters.get(CHAIN_OF_ACTION);
        if (chainOfactions != null) {
            String[] actions = chainOfactions.get(0).split(",");
            Map<String, Action> actionsMap = templateService.getActions();
            JCRNodeWrapper newNode = null;
            for (String actionToDo : actions) {
                if (DefaultPostActionResult.ACTION_NAME.equals(actionToDo)) {
                    Action defaultPostActionResult = new DefaultPostActionResult();
                    String s = urlResolver.getUrlPathInfo().replace(".chain.do", "/*");
                    URLResolver resolver = new URLResolver(s, urlResolver.getSiteKey());
                    defaultPostActionResult.doExecute(req, resp, renderContext, resource, parameters, resolver);
                    newNode = defaultPostActionResult.getNewNode();
                } else {
                    Action action = actionsMap.get(actionToDo);
                    action.doExecute(req, resp, renderContext, resource, parameters, urlResolver);
                    if (action.getNewNode() != null) {
                        newNode = action.getNewNode();
                    }
                }
            }
            if (newNode != null) {
                String url = newNode.getPath();
                resp.setStatus(HttpServletResponse.SC_CREATED);
                final String requestWith = req.getHeader("x-requested-with");
                if (req.getHeader("accept").contains("application/json") && requestWith != null && requestWith.equals(
                        "XMLHttpRequest")) {
                    try {
                        Render.serializeNodeToJSON(resp, newNode);
                    } catch (JSONException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    Render.performRedirect(url, urlResolver.getPath(), req, resp, parameters);
                }
            }
        }
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
