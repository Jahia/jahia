/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.admin;

import javax.portlet.Portlet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Feb 2, 2009
 * Time: 9:46:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class AdministrationModulesRegistry {

    private List<AdministrationModule> serverModules = new ArrayList<AdministrationModule>();
    private List<AdministrationModule> siteModules = new ArrayList<AdministrationModule>();
    private Map<String, AdministrationModule> serverModulesByUrlKey = new HashMap<String, AdministrationModule>();
    private Map<String, AdministrationModule> siteModulesByUrlKey = new HashMap<String, AdministrationModule>();

    public AdministrationModulesRegistry() {

    }

    public List<AdministrationModule> getServerModules() {
        return serverModules;
    }

    public void setServerModules(List<AdministrationModule> serverModules) {
        this.serverModules = serverModules;
        for (AdministrationModule currentModule : serverModules) {
            currentModule.setServerModule(true);
            serverModulesByUrlKey.put(currentModule.getUrlKey(), currentModule);
        }
    }

    public List<AdministrationModule> getSiteModules() {
        return siteModules;
    }

    public void setSiteModules(List<AdministrationModule> siteModules) {
        this.siteModules = siteModules;
        for (AdministrationModule currentModule : siteModules) {
            currentModule.setServerModule(false);
            siteModulesByUrlKey.put(currentModule.getUrlKey(), currentModule);
        }
    }

    public AdministrationModule getServerAdministrationModule(String moduleKey) {
        return serverModulesByUrlKey.get(moduleKey);
    }

    public AdministrationModule getSiteAdministrationModule(String moduleKey) {
        return siteModulesByUrlKey.get(moduleKey);
    }

}
