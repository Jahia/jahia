/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data.toolbar;

import com.extjs.gxt.ui.client.widget.Component;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.security.PermissionsResolver;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;

import java.io.Serializable;
import java.util.*;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:41:31
 */
public class GWTJahiaToolbarItem implements Serializable {

    private String id;
    private String icon;
    private String title;
    private boolean displayTitle;
    private String description;
    private boolean selected;
    private int layout = 0;
    private List<String> processes;
    private Map<String, GWTJahiaProperty> properties = new HashMap<String, GWTJahiaProperty>();
    private List<String> requiredPermissions = Collections.emptyList();
    private PermissionsResolver requiredPermissionsResolver = PermissionsResolver.MATCH_ALL;
    private String requiredModule;
    private boolean hideWhenDisabled = false;

    private ActionItem actionItem;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, GWTJahiaProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, GWTJahiaProperty> properties) {
        this.properties = properties;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void addProperty(GWTJahiaProperty gwtProperty) {
        if (gwtProperty != null) {
            properties.put(gwtProperty.getName(), gwtProperty);
        }
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }

    public void setActionItem(ActionItem actionItem) {
        this.actionItem = actionItem;
    }

    public List<String> getProcesses() {
        return processes;
    }

    public void setProcesses(List<String> processes) {
        this.processes = processes;
    }

    /**
     * Returns the required permission for enabling this item.
     *
     * @return the required permission if any, {@code null} otherwise
     * @deprecated Please consider using {@link #getRequiredPermissions()} instead of this method.
     *      If multiple permissions were provided (using {@link #setRequiredPermissions(List)}
     *      then this method will only return the first one.
     */
    @Deprecated
    public String getRequiredPermission() {
        return requiredPermissions.isEmpty() ? null : requiredPermissions.get(0);
    }

    public void setRequiredPermission(String requiredPermission) {
        if (requiredPermission == null) {
            this.requiredPermissions = Collections.emptyList();
        } else {
            this.requiredPermissions = Collections.singletonList(requiredPermission);
        }
    }

    /**
     * Sets a list of required permissions to enable this item.
     * <p>
     * {@link #setRequiredPermissionsResolver(PermissionsResolver)}
     * allows to specify how this list of permissions should be
     * interpreted.
     *
     * @param requiredPermissions a list of permissions
     */
    public final void setRequiredPermissions(List<String> requiredPermissions) {
        this.requiredPermissions = new ArrayList<String>(requiredPermissions);
    }

    /**
     * Returns the list of required permissions to enable this item.
     * <p>
     * {@link #getRequiredPermissionsResolver()} provides a way to
     * consume those permissions as expected.
     *
     * @return a list of required permissions
     */
    public final List<String> getRequiredPermissions() {
        return new ArrayList<String>(requiredPermissions);
    }

    /**
     * Sets the {@link PermissionsResolver} to use to interpret
     * required permissions.
     *
     * @param resolver the permission resolver to use
     */
    public final void setRequiredPermissionsResolver(PermissionsResolver resolver) {
        this.requiredPermissionsResolver = resolver;
    }

    /**
     * Returns the {@link PermissionsResolver} to use to interpret
     * required permissions.
     *
     * @return the permission resolver to use
     */
    public final PermissionsResolver getRequiredPermissionsResolver() {
        return requiredPermissionsResolver;
    }

    public String getRequiredModule() {
        return requiredModule;
    }

    public void setRequiredModule(String requiredModule) {
        this.requiredModule = requiredModule;
    }

    public boolean isHideWhenDisabled() {
        return hideWhenDisabled;
    }

    public void setHideWhenDisabled(boolean hideWhenDisabled) {
        this.hideWhenDisabled = hideWhenDisabled;
    }

    public String getClassName() {
        if (getId() == null) {
            // We are not sure each toolbar item has an ID.
            return "";
        }
        String className = getId().toLowerCase().replace('.', '-');
        return className.contains("$") ? className.substring(0, className.indexOf("$")) : className;
    }

    public void addClasses(Component component) {
        component.addStyleName(getClassName());
        GWTJahiaProperty p = getProperties().get("additional-classes");
        if (p != null) {
            for (String s : p.getValue().split(" ")) {
                component.addStyleName(s);
            }
        }
    }

}
