/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.admin.pwdpolicy;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.util.RequestUtils;
import org.jahia.bin.JahiaAdministration;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicy;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyRule;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.admin.AbstractAdministrationModule;

/**
 * Handles displaying of the password policy management dialog.
 * 
 * @author Sergiy Shyrkov
 */
public class ManagePasswordPolicies extends AbstractAdministrationModule {

	/**
	 * Handles the displaying of password policy management dialog.
	 * 
	 * @param request
	 *            current request
	 * @param response
	 *            current response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
	        throws IOException, ServletException {

		String action = request.getParameter("sub");

		JahiaPasswordPolicyService service = ServicesRegistry.getInstance()
		        .getJahiaPasswordPolicyService();

		JahiaPasswordPolicy pwdPolicy = service.getDefaultPolicy();

		if ("save".equals(action)) {
			for (Iterator iterator = pwdPolicy.getRules().iterator(); iterator.hasNext();) {
	            ((JahiaPasswordPolicyRule) iterator.next()).setActive(false);
            }
			RequestUtils.populate(pwdPolicy, request);
			service.updatePolicy(pwdPolicy);
			request.setAttribute("confirmationMessage",
			        "org.jahia.admin.warningMsg.changSaved.label");
		}

		request.setAttribute("policy", pwdPolicy);

		JahiaAdministration.doRedirect(request, response, request.getSession(),
		        JahiaAdministration.JSP_PATH + "password_policy.jsp");
	}

}
