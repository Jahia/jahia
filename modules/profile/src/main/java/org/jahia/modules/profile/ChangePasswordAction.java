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

package org.jahia.modules.profile;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 */
public class ChangePasswordAction extends Action {
    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String passwd = req.getParameter("password").trim();
        JSONObject json = new JSONObject();

        if (!resource.getNode().hasPermission("jcr:write_default") || !resource.getNode().isNodeType("jnt:user")) {
            return new ActionResult(HttpServletResponse.SC_FORBIDDEN, null, null);
        }

        if ("".equals(passwd)) {

            String userMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.specifyPassword.label", renderContext.getUILocale());
            json.put("errorMessage", userMessage);
        } else {
            String passwdConfirm = req.getParameter("passwordconfirm").trim();
            if (!passwdConfirm.equals(passwd)) {
                String userMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.passwdNotMatch.label", renderContext.getUILocale());
                json.put("errorMessage",userMessage);
            } else {
                JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
                JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(resource.getNode().getName());

                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnPasswordChange(user, passwd, true);
                if (!evalResult.isSuccess()) {
                    EngineMessages policyMsgs = evalResult.getEngineMessages();
                    String res = "";
                    for (EngineMessage message : policyMsgs.getMessages()) {
                        res += (message.isResource() ? MessageFormat.format(JahiaResourceBundle.getJahiaInternalResource(message.getKey(), renderContext.getUILocale()), message.getValues()) : message.getKey())+"\n";
                    }
                    json.put("errorMessage", res);
                } else {
                    // change password
                    user.setPassword(passwd);
                    json.put("errorMessage", JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.passwordChanged.label", renderContext.getUILocale()));
                    json.put("result", "success");
                }
            }
        }

        return new ActionResult(HttpServletResponse.SC_OK, null, json);
    }
}
