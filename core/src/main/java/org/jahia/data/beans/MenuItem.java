/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.data.beans;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;


/**
 * Represents a menu item in the Jahia Administration.
 *
 * @author Sergiy Shyrkov
 */
public class MenuItem {

    private boolean enabled;

    private String icon;

    private String iconSmall;

    private String label;

    private String link;

    private String name;

    private boolean selected;
    private final LocalizationContext localizationContext;

    private String tooltip;

    /**
     * Initializes an instance of this class.
     *
     * @param name
     * @param enabled
     * @param label
     * @param link
     * @param icon
     */
    public MenuItem(String name, boolean enabled, String label, String link,
            String icon) {
        this(name, enabled, label, link, icon, null, null, false);
    }

    /**
     * Initializes an instance of this class.
     *
     * @param name
     * @param enabled
     * @param label
     * @param link
     * @param icon
     * @param iconSmall
     * @param tooltip
     * @param selected
     */
    public MenuItem(String name, boolean enabled, String label, String link,
            String icon, String iconSmall, String tooltip, boolean selected) {
        this(name, enabled, label, link, icon, iconSmall, tooltip, selected,null);
    }
    public MenuItem(String name, boolean enabled, String label, String link,
            String icon, String iconSmall, String tooltip, boolean selected, LocalizationContext localizationContext) {
        super();
        this.name = name;
        this.enabled = enabled;
        this.label = label;
        this.link = link;
        this.icon = icon;
        this.iconSmall = iconSmall;
        this.tooltip = tooltip;
        this.selected = selected;
        this.localizationContext = localizationContext;
    }


    public String getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(String iconSmall) {
        this.iconSmall = iconSmall;
    }

    public LocalizationContext getLocalizationContext() {
        return localizationContext;
    }
}
