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
package org.jahia.data.beans;


/**
 * Represents a menu item in the Jahia Administration.
 * 
 * @author Sergiy Shyrkov
 */
public class MenuItem {

    private boolean enabled;

    private String icon;

    private String label;

    private String link;

    private String name;

    private boolean selected;

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
        this(name, enabled, label, link, icon, null, false);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name
     * @param enabled
     * @param label
     * @param link
     * @param icon
     * @param tooltip
     * @param selected
     */
    public MenuItem(String name, boolean enabled, String label, String link,
            String icon, String tooltip, boolean selected) {
        super();
        this.name = name;
        this.enabled = enabled;
        this.label = label;
        this.link = link;
        this.icon = icon;
        this.tooltip = tooltip;
        this.selected = selected;
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

}
