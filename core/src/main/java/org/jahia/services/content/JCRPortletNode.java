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
