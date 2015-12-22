/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper bean to allow auto-registration of the workflow types.
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 22/12/10
 */
public class WorklowTypeRegistration implements JahiaModuleAware{
    private String type;
    private String definition;
    private String provider;
    private Map<String, String> permissions;
    private Map<String, String> forms;
    private Map<String, String> coordinates;
    private JahiaTemplatesPackage module;

    private boolean canBeUsedForDefault;
    private int defaultPriority;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getForms() {
        return forms;
    }

    public void setForms(Map<String, String> forms) {
        this.forms = forms;
    }

    public Map<String, String[]> getCoordinates() {
        if (coordinates == null) {
            return null;
        }
        Map<String, String[]> splitCoords = new HashMap<String, String[]>();
        for (Map.Entry<String,String> entry : coordinates.entrySet()) {
            splitCoords.put(entry.getKey(), entry.getValue().split(","));
        }
        return splitCoords;
    }

    public void setCoordinates(Map<String, String> coordinates) {
        this.coordinates = coordinates;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public boolean isCanBeUsedForDefault() {
        return canBeUsedForDefault;
    }

    public void setCanBeUsedForDefault(boolean canBeUsedForDefault) {
        this.canBeUsedForDefault = canBeUsedForDefault;
    }

    public JahiaTemplatesPackage getModule() {
        return module;
    }

    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }
}
