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
package org.jahia.services.content.rules;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.notification.CamelNotificationService;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.script.*;
import java.io.*;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 29 juin 2010
 */
public class RulesNotificationService {
    private transient static Logger logger = Logger.getLogger(RulesNotificationService.class);

    private static RulesNotificationService instance;

    public static synchronized RulesNotificationService getInstance() {
        if (instance == null) {
            instance = new RulesNotificationService();
        }
        return instance;
    }

    private CamelNotificationService notificationService;

    public void setNotificationService(CamelNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyUser(AddedNodeFact node, final String template, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        JCRNodeWrapper userNode = node.getNode();
        if (userNode.hasProperty("j:email") && !userNode.getProperty("j:external").getBoolean()) {
            String userMail = userNode.getProperty("j:email").getString();
            // Resolve template :
            ScriptEngineManager scriptManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = scriptManager.getEngineByExtension(StringUtils.substringAfterLast(template,
                                                                                                          "."));
            ScriptContext scriptContext = scriptEngine.getContext();
            final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("currentUser", userNode);
            InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(
                    template);
            if (scriptInputStream != null) {
                String resourceBundleName = StringUtils.substringBeforeLast(StringUtils.substringAfter(template,"/").replaceAll("/","."),".");
                ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleName,
                                                                         LanguageCodeConverters.languageCodeToLocale(
                                                                                 userNode.getProperty(
                                                                                         "preferredLanguage").getString()));
                bindings.put("bundle",resourceBundle);
                Reader scriptContent = null;
                try {
                    scriptContent = new InputStreamReader(scriptInputStream);
                    scriptContext.setWriter(new StringWriter());
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    Object result = scriptEngine.eval(scriptContent, bindings);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    String body = writer.toString();
                    notificationService.sendMail("seda:newUsers?multipleConsumers=true", "Welcome", body, null,
                                                 SettingsBean.getInstance().getMail_from(), userMail, null, null);
                } finally {
                    if (scriptContent != null) {
                        IOUtils.closeQuietly(scriptContent);
                    }
                }
            }
        }
    }
}
