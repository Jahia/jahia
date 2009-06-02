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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.collections.map.UnmodifiableMap;

/**
 * Jahia Administration modules registry, which holds all items, displayed in
 * the server and site sections of the menu.
 * 
 * @author Serge Huber
 * Date: Feb 2, 2009
 * Time: 9:46:21 AM
 */
public class AdministrationModulesRegistry {

    private List<AdministrationModule> serverModules = UnmodifiableList
            .decorate(new LinkedList<AdministrationModule>());

    private Map<String, AdministrationModule> serverModulesByUrlKey = UnmodifiableMap
            .decorate(new HashMap<String, AdministrationModule>());

    private List<AdministrationModule> siteModules = UnmodifiableList
            .decorate(new LinkedList<AdministrationModule>());

    private Map<String, AdministrationModule> siteModulesByUrlKey = UnmodifiableMap
            .decorate(new HashMap<String, AdministrationModule>());

    /**
     * Adds the specified module into the registry.
     * 
     * @param module
     *            the module to be added
     */
    public void add(AdministrationModule module) {
        if (module.isServerModule()) {
            serverModules = new LinkedList<AdministrationModule>(serverModules);
            serverModules.add(module);
            serverModules = UnmodifiableList.decorate(serverModules);
            serverModulesByUrlKey = new HashMap<String, AdministrationModule>(
                    serverModulesByUrlKey);
            serverModulesByUrlKey.put(module.getUrlKey(), module);
            serverModulesByUrlKey = UnmodifiableMap
                    .decorate(serverModulesByUrlKey);
        } else {
            siteModules = new LinkedList<AdministrationModule>(siteModules);
            siteModules.add(module);
            siteModules = UnmodifiableList.decorate(siteModules);
            siteModulesByUrlKey = new HashMap<String, AdministrationModule>(
                    siteModulesByUrlKey);
            siteModulesByUrlKey.put(module.getUrlKey(), module);
            siteModulesByUrlKey = UnmodifiableMap.decorate(siteModulesByUrlKey);
        }
    }

    public AdministrationModule getServerAdministrationModule(String moduleKey) {
        return serverModulesByUrlKey.get(moduleKey);
    }

    public List<AdministrationModule> getServerModules() {
        return serverModules;
    }

    public AdministrationModule getSiteAdministrationModule(String moduleKey) {
        return siteModulesByUrlKey.get(moduleKey);
    }

    public List<AdministrationModule> getSiteModules() {
        return siteModules;
    }

    /**
     * Removes the specified module from the registry.
     * 
     * @param module
     *            the module to be removed
     */
    public void remove(AdministrationModule module) {
        if (module.isServerModule()) {
            serverModules = new LinkedList<AdministrationModule>(serverModules);
            serverModules.remove(module);
            serverModules = UnmodifiableList.decorate(serverModules);
            serverModulesByUrlKey = new HashMap<String, AdministrationModule>(
                    serverModulesByUrlKey);
            serverModulesByUrlKey.remove(module.getUrlKey());
            serverModulesByUrlKey = UnmodifiableMap
                    .decorate(serverModulesByUrlKey);
        } else {
            siteModules = new LinkedList<AdministrationModule>(siteModules);
            siteModules.remove(module);
            siteModules = UnmodifiableList.decorate(siteModules);
            siteModulesByUrlKey = new HashMap<String, AdministrationModule>(
                    siteModulesByUrlKey);
            siteModulesByUrlKey.remove(module.getUrlKey());
            siteModulesByUrlKey = UnmodifiableMap.decorate(siteModulesByUrlKey);
        }
    }

}
