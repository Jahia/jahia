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
