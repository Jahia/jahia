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

package org.jahia.services.content.decorator;

import org.jahia.registries.ServicesRegistry;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 * User: toto
 * Date: Dec 3, 2008
 * Time: 5:55:00 PM
 * 
 */
public class JCRPortletNode extends JCRNodeDecorator {
    public JCRPortletNode(JCRNodeWrapper node) {
        super(node);
    }

    public String getContextName() throws RepositoryException {
        String context;
        try {
            context = getProperty("j:applicationRef").getNode().getProperty("j:context").getString();
        } catch (RepositoryException e) {
            // Is it an old portlet instance ?
            final String[] strings = getProperty("j:application").getString().split("!");
            context = strings[0];
            if (context.startsWith("$context")) {
                context = Jahia.getContextPath() + context.substring("$context".length());
            }
            // Set the applicationReference now
            final String finalContext = context;
            final String uuid = getUUID();
            JCRTemplate.getInstance().doExecuteWithSystemSession(null,this.getSession().getWorkspace().getName(),this.getSession().getLocale(),new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        final ApplicationBean applicationByContext = ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationByContext(
                                finalContext);
                        JCRNodeWrapper nodeByUUID = session.getNodeByUUID(uuid);
                        session.checkout(nodeByUUID);
                        nodeByUUID.setProperty("j:applicationRef", applicationByContext.getID());
                        nodeByUUID.setProperty("j:definition", strings[1]);
                        session.save();
                    } catch (JahiaException e1) {
                        e1.printStackTrace();
                    }
                    return null;
                }
            });
        }
        return context;
    }

    public String getDefinitionName() throws RepositoryException {
        return getProperty("j:definition").getString();
    }

    public void setApplication(String appId,String defName) throws RepositoryException {
        setProperty("j:applicationRef", appId);
        setProperty("j:definition", defName);
        try {
            final ApplicationBean applicationBean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(appId);
            String contextName = applicationBean.getContext();
            String prefix = Jahia.getContextPath();
            if (prefix.equals("/")) {
                prefix = "";
            }
            if (contextName.startsWith(prefix)) {
                contextName = "$context" + contextName.substring(prefix.length());
            }
            String app = contextName + "!" + defName;
            setProperty("j:application", app);
        } catch (JahiaException e) {
            e.printStackTrace();
        }
    }

    public String getCacheScope() throws RepositoryException {
        return getProperty("j:cacheScope").getString();
    }

    public int getExpirationTime() throws RepositoryException {
        return (int) getProperty("j:expirationTime").getLong();
    }

    public EntryPointDefinition getEntryPointDefinition() throws JahiaException, RepositoryException {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationByContext(getContextName()).getEntryPointDefinitionByName(getDefinitionName());
    }

    public Map<String, List<JCRNodeWrapper>> getAvailableRoles() throws RepositoryException {
        Map<String, List<JCRNodeWrapper>> results = new HashMap<String, List<JCRNodeWrapper>>(super.getAvailableRoles());
        try {
            results.putAll(getAvailablePermissions(getContextName(), getDefinitionName()));
        } catch (JahiaException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static Map<String, List<JCRNodeWrapper>> getAvailablePermissions(String contextName, String definitionName) throws JahiaException {
        Map<String, List<JCRNodeWrapper>> results = new HashMap<String, List<JCRNodeWrapper>>();
// todo : portlet roles / permissions ?
//        ApplicationBean bean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationByContext(contextName);
//        WebAppContext appContext = ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationContext(bean);
//        List<String> roles = appContext.getRoles();
//        results.put("roles", roles);
//
//        List<String> modesStr = new ArrayList<String>();
//        EntryPointDefinition epd = bean.getEntryPointDefinitionByName(definitionName);
//        List<PortletMode> modes = epd.getPortletModes();
//        for (PortletMode mode : modes) {
//            // mode view is mandatory and is garted to all users
//            if (mode != PortletMode.VIEW) {
//                modesStr.add(mode.toString());
//            }
//        }
//        results.put("modes", modesStr);
        return results;
    }
}
