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
package org.jahia.services.content;

import org.jahia.registries.ServicesRegistry;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.WebAppContext;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;

import javax.jcr.RepositoryException;
import javax.portlet.PortletMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 3, 2008
 * Time: 5:55:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRPortletNode extends JCRNodeDecorator {
    public JCRPortletNode(JCRNodeWrapper node) {
        super(node);
    }

    public String getContextName() throws RepositoryException {
        String s = getProperty("j:application").getString().split("!")[0];
        if (s.startsWith("$context")) {
            String prefix = Jahia.getContextPath();
            if (prefix.equals("/")) {
                prefix = "";
            }
            s = prefix + s.substring("$context".length());
        }
        return s;
    }

    public String getDefinitionName() throws RepositoryException {
        return getProperty("j:application").getString().split("!")[1];
    }

    public void setApplication(String contextName, String defName) throws RepositoryException {
        String prefix = Jahia.getContextPath();
        if (prefix.equals("/")) {
            prefix = "";
        }
        if (contextName.startsWith(prefix)) {
            contextName = "$context" + contextName.substring(prefix.length());
        }
        String app = contextName + "!" + defName;
        setProperty("j:application", app);
    }

    public String getCacheScope() throws RepositoryException {
        return getProperty("j:cacheScope").getString();
    }
    
    public int getExpirationTime() throws RepositoryException {
        return (int) getProperty("j:expirationTime").getLong();
    }

    public EntryPointDefinition getEntryPointDefinition() throws JahiaException, RepositoryException  {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(getContextName()).getEntryPointDefinitionByName(getDefinitionName());
    }

    public Map<String,List<String>> getAvailablePermissions() {
        Map<String,List<String>> results = new HashMap<String,List<String>>(super.getAvailablePermissions());
        try {
            results.putAll(getAvailablePermissions(getContextName(), getDefinitionName()));
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return results;
    }

    public static Map<String,List<String>> getAvailablePermissions(String contextName, String definitionName) throws JahiaException {
        Map<String,List<String>> results = new HashMap<String,List<String>>();
        ApplicationBean bean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(contextName);
        WebAppContext appContext = ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationContext(bean);
        List<String> roles = appContext.getRoles();
        results.put("roles", roles);

        List<String> modesStr = new ArrayList<String>();
        EntryPointDefinition epd = bean.getEntryPointDefinitionByName(definitionName);
        List<PortletMode> modes = epd.getPortletModes();
        for (PortletMode mode : modes) {
            modesStr.add(mode.toString());
        }
        results.put("modes", modesStr);
        return results;
    }
}
