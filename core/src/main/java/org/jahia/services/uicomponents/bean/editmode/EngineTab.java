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

package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:37:29 PM
 */
public class EngineTab implements Serializable, Comparable<EngineTab>, InitializingBean {
    
    private static final long serialVersionUID = -5995531303789738603L;
    
    private String id;
    private String title;
    private String titleKey;
    private Visibility visibility;
    private EditEngineTabItem tabItem;
    private int order;
    private String requiredPermission;

    private Object parent;
    private int position = -1;
    private String positionAfter;
    private String positionBefore;
    
    protected static void removeTab(List<EngineTab> tabs, String tabId) {
        if (tabId != null && tabId.length() > 0) {
            for (Iterator<EngineTab> iterator = tabs.iterator(); iterator.hasNext();) {
                EngineTab tab = iterator.next();
                if (tab.getId() != null && tab.getId().equals(tabId)) {
                    iterator.remove();
                }
            }
        }
    }
    
    public EngineTab() {
        super();
    }
    
    public EngineTab(String id) {
        this();
        setId(id);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public EditEngineTabItem getTabItem() {
        return tabItem;
    }

    public void setTabItem(EditEngineTabItem item) {
        this.tabItem = item;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setParentEditConfiguration(EditConfiguration config) {
        config.getEngineTabs().add(this);
    }

    public void setParentManagerConfiguration(ManagerConfiguration config) {
        config.getEngineTabs().add(this);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public int compareTo(EngineTab o) {
        return getOrder() - o.getOrder();
    }

    public void afterPropertiesSet() throws Exception {
        if (parent == null) {
            return;
        }
        if (parent instanceof String) {
            parent = SpringContextSingleton.getBean((String) parent);
        }
        List<EngineTab> tabs = null;
        if (parent instanceof EditConfiguration) {
            tabs = ((EditConfiguration) parent).getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((EditConfiguration) parent).setEngineTabs(tabs);
            }
        } else if (parent instanceof ManagerConfiguration) {
            tabs = ((ManagerConfiguration) parent).getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((ManagerConfiguration) parent).setEngineTabs(tabs);
            }
        } else if (parent instanceof Engine) {
            tabs = ((Engine) parent).getTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((Engine) parent).setTabs(tabs);
            }
        }

        if (tabs != null) {
            removeTab(tabs, getId());

            int index = -1;
            if (position >= 0) {
                index = position;
            } else if (positionBefore != null) {
                index = tabs.indexOf(new EngineTab(positionBefore));
            } else if (positionAfter != null) {
                index = tabs.indexOf(new EngineTab(positionAfter));
                if (index != -1) {
                    index++;
                }
                if (index >= tabs.size()) {
                    index = -1;
                }
            }
            if (index != -1) {
                tabs.add(index, this);
            } else {
                tabs.add(this);
            }
        } else {
            throw new IllegalArgumentException("Unknown parent type '"
                    + parent.getClass().getName()
                    + "'. Can accept EditConfiguration, ManagerConfiguration, Engine or"
                    + " a String value with a beanId of the those beans");
        }

        // clean the reference
        parent = null;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPositionAfter(String positionAfter) {
        this.positionAfter = positionAfter;
    }

    public void setPositionBefore(String positionBefore) {
        this.positionBefore = positionBefore;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof EngineTab && obj != null) {
            EngineTab other = (EngineTab) obj;
            return getId() != null ? other.getId() != null && getId().equals(other.getId()) : other
                    .getId() == null;
        }

        return false;
    }
}
