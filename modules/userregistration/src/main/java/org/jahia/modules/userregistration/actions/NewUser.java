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
package org.jahia.modules.userregistration.actions;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Render;
import org.jahia.services.content.*;
import org.jahia.services.notification.CamelNotificationService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.settings.SettingsBean;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 29 juin 2010
 */
public class NewUser implements Action {
    private transient static Logger logger = Logger.getLogger(NewUser.class);
    private String name;
    private JahiaUserManagerService userManagerService;
    private CamelNotificationService camelNotificationService;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setCamelNotificationService(CamelNotificationService camelNotificationService) {
        this.camelNotificationService = camelNotificationService;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String name1 = parameters.get("desired_login").get(0);
        String password = parameters.get("desired_password").get(0);
        Properties properties = new Properties();
        properties.put("j:email",parameters.get("desired_email").get(0));
        properties.put("j:firstName",parameters.get("desired_firstname").get(0));
        properties.put("j:lastName",parameters.get("desired_lastname").get(0));
        final JahiaUser user = userManagerService.createUser(name1, password, properties);
        camelNotificationService.sendMail("seda:users?multipleConsumers=true","A new user has been registered",null,"User "+name1+" has registered on your platform",
                                          SettingsBean.getInstance().getMail_from(),SettingsBean.getInstance().getMail_administrator(),null,null);
        return new ActionResult(HttpServletResponse.SC_ACCEPTED,parameters.get("userprofilepage").get(0), new JSONObject());
    }
}
