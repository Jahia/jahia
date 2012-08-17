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

package org.jahia.services.content.rules;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.utils.Patterns;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.mail.MailService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.script.*;
import java.io.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Notification service that is used in the right-hand-side (consequences) of
 * the business rules.
 * 
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 29 juin 2010
 */
public class RulesNotificationService {

    private static RulesNotificationService instance;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RulesNotificationService.class);

    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }

    private ScriptEngineUtils scriptEngineUtils;

    public static RulesNotificationService getInstance() {
        if (instance == null) {
            synchronized (RulesNotificationService.class) {
                if (instance == null) {
                    instance = new RulesNotificationService();
                }
            }
        }
        return instance;
    }

    private MailService notificationService;

    public void setNotificationService(MailService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyNewUser(AddedNodeFact node, final String template, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        if (!notificationService.isEnabled()) {
            return;
        }
        JCRNodeWrapper userNode = node.getNode();
        if (userNode.hasProperty("j:email") && !userNode.getProperty("j:external").getBoolean()) {
            String toMail = userNode.getProperty("j:email").getString();
            String ccList = null;
            String bcclist = null;
            Locale locale;
            try {
                locale = LanguageCodeConverters.languageCodeToLocale(userNode.getProperty(
                        "preferredLanguage").getString());
            } catch (RepositoryException e) {
                locale = SettingsBean.getInstance().getDefaultLocale();
            }
            sendMail(template, userNode, toMail, null, ccList, bcclist, locale, drools);

        }
    }

    public void notifyCurrentUser(User user, final String template, final String fromMail, final String ccList,
                                  final String bccList, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        if (!notificationService.isEnabled()) {
            return;
        }
        JahiaUser userNode = user.getJahiaUser();
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            Locale locale = getLocale(userNode);

            sendMail(template, userNode, toMail, fromMail, ccList, bccList, locale, drools);
        }
    }

    public void notifyCurrentUser(User user, final String template, final String fromMail, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        if (!notificationService.isEnabled()) {
            return;
        }
        JahiaUser userNode = user.getJahiaUser();
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            Locale locale = getLocale(userNode);
            sendMail(template, userNode, toMail, fromMail, null, null, locale, drools);
        }
    }

    private Locale getLocale(JahiaUser userNode) {
        Locale locale;
        String property = userNode.getProperty("preferredLanguage");
        if (property != null) {
            locale = LanguageCodeConverters.languageCodeToLocale(property);
        } else {
            locale = SettingsBean.getInstance().getDefaultLocale();
        }
        return locale;
    }

    public void notifyUser(String user, final String template, final String fromMail, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        if (!notificationService.isEnabled()) {
            return;
        }
        JahiaUser userNode = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
        if (userNode!=null && userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            sendMail(template, userNode, toMail, fromMail, null, null, getLocale(userNode), drools);
        }
    }

    public void notifyUser(String user, final String template, final ChangedPropertyFact fromMail, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        if (!notificationService.isEnabled()) {
            return;
        }
        JahiaUser userNode = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
        if (userNode!=null && userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            sendMail(template, userNode, toMail, fromMail.getStringValue(), null, null, getLocale(userNode), drools);
        }
    }

    public void notifyUser(String user, final String template, final String fromMail, final String ccList,
                           final String bccList, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        if (!notificationService.isEnabled()) {
            return;
        }
        JahiaUser userNode = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            sendMail(template, userNode, toMail, fromMail, ccList, bccList, getLocale(userNode), drools);
        }
    }

    private void sendMail(String template, Object user, String toMail, String fromMail, String ccList, String bcclist,
                          Locale locale, KnowledgeHelper drools) throws RepositoryException, ScriptException {
        if (!notificationService.isEnabled()) {
            return;
        }
        if (StringUtils.isEmpty(fromMail)) {
            logger.warn("A mail couldn't be sent because from: has no recipient");
            return;
        }
        if (StringUtils.isEmpty(toMail)) {
            logger.warn("A mail couldn't be sent because to: has no recipient");
            return;
        }

        // Resolve template :
        String extension = StringUtils.substringAfterLast(template, ".");
        ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(extension);
        ScriptContext scriptContext = new SimpleScriptContext();
        final Bindings bindings = new SimpleBindings();
        bindings.put("currentUser", user);
        bindings.put("contextPath", Jahia.getContextPath());
        final Object object = drools.getActivation().getTuple().getFactHandles()[0].getObject();
        JCRNodeWrapper node = null;
        if (object instanceof AbstractNodeFact) {
            node = ((AbstractNodeFact) object).getNode();
            bindings.put("currentNode", node);
            final int siteURLPortOverride = SettingsBean.getInstance().getSiteURLPortOverride();
            bindings.put("servername",
                    "http" + (siteURLPortOverride == 443 ? "s" : "") + "://" + node.getResolveSite().getServerName() +
                    ((siteURLPortOverride != 0 && siteURLPortOverride != 80 && siteURLPortOverride != 443) ?
                     ":" + siteURLPortOverride : ""));
        }
        InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(template);
        if (scriptInputStream != null) {
            String resourceBundleName = StringUtils.substringBeforeLast(
                    Patterns.SLASH.matcher(
                            StringUtils.substringAfter(Patterns.WEB_INF.matcher(template)
                                    .replaceAll(""), "/")).replaceAll("."), ".");
            String subject = "";
            try {
                ResourceBundle resourceBundle = JahiaResourceBundle.lookupBundle(resourceBundleName, locale);
                bindings.put("bundle", resourceBundle);
                subject = resourceBundle.getString("subject");
            } catch (MissingResourceException e) {
                if(node!=null){
                    final Value[] values = node.getResolveSite().getProperty("j:installedModules").getValues();
                    for (Value value : values) {
                        try {
                            ResourceBundle resourceBundle = new JahiaResourceBundle(null,locale,ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(value.getString()).getName());
                            subject = resourceBundle.getString(drools.getRule().getName().toLowerCase().replaceAll(" ",".")+".subject");
                            bindings.put("bundle", resourceBundle);
                        } catch (MissingResourceException ee) {
                            // Do nothing
                        }
                    }
                }
            }
            Reader scriptContent = null;
            try {
                scriptContent = new InputStreamReader(scriptInputStream);
                scriptContext.setWriter(new StringWriter());
                // The following binding is necessary for Javascript, which doesn't offer a console by default.
                bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                scriptContext.setBindings(scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
                scriptEngine.eval(scriptContent, scriptContext);
                StringWriter writer = (StringWriter) scriptContext.getWriter();
                String body = writer.toString();
                notificationService.sendMessage(fromMail, toMail, ccList, bcclist, subject, null, body);
            } finally {
                if (scriptContent != null) {
                    IOUtils.closeQuietly(scriptContent);
                }
            }
        }
    }
}
