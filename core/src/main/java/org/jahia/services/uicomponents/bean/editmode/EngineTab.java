/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.*;

/**
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:37:29 PM
 */
public class EngineTab implements Serializable, Comparable<EngineTab>, InitializingBean, DisposableBean, ApplicationContextAware {

    private static final long serialVersionUID = -5995531303789738603L;

    private String id;
    private String title;
    private String titleKey;
    private Visibility visibility;
    private EditEngineTabItem tabItem;
    private int order;
    private String requiredPermission;
    private boolean showInEngine = true;

    private Object parent;
    private Object parentEditConfiguration;
    private Object parentManagerConfiguration;
    private int position = -1;
    private String positionAfter;
    private String positionBefore;

    private ApplicationContext applicationContext;

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

    public boolean showInEngine() {
        return showInEngine;
    }

    public void setShowInEngine(boolean showInEngine) {
        this.showInEngine = showInEngine;
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
        this.parentEditConfiguration = config;
    }

    public void setParentManagerConfiguration(ManagerConfiguration config) {
        this.parentManagerConfiguration = config;
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

    @Override
    public int compareTo(EngineTab o) {
        return getOrder() - o.getOrder();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (parent instanceof List) {
            for (Object o : (List<?>) parent) {
                addTab(getEngineTabs(o));
            }
        } else {
            if (parent != null) {
                addTab(getEngineTabs(parent));
            }
        }
        if (parentManagerConfiguration != null) {
            addTab(getEngineTabs(parentManagerConfiguration));
        }
        if (parentEditConfiguration != null) {
            addTab(getEngineTabs(parentEditConfiguration));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        if (parent instanceof List) {
            for (Object o : (List<?>) parent) {
                removeTab(getEngineTabs(o), getId());
            }
        } else {
            removeTab(getEngineTabs(parent), getId());
        }
        removeTab(getEngineTabs(parent), getId());
        removeTab(getEngineTabs(parentEditConfiguration), getId());
        removeTab(getEngineTabs(parentManagerConfiguration), getId());
    }

    private void addTab(List<List<EngineTab>> tabs) {
        if (!tabs.isEmpty()) {
            removeTab(tabs, getId());
            for (List<EngineTab> t : tabs) {
                int index = -1;
                if (position >= 0) {
                    index = position;
                } else if (positionBefore != null) {
                    index = t.indexOf(new EngineTab(positionBefore));
                } else if (positionAfter != null) {
                    index = t.indexOf(new EngineTab(positionAfter));
                    if (index != -1) {
                        index++;
                    }
                    if (index >= tabs.size()) {
                        index = -1;
                    }
                }
                if (index != -1) {
                    t.add(index, this);
                } else {
                    t.add(this);
                }
            }
        } else if (this.parent != null) {
            throw new IllegalArgumentException("Unknown parent type '"
                    + this.parent.getClass().getName()
                    + "'. Can accept EditConfiguration, ManagerConfiguration, Engine or"
                    + " a String value with a beanId of the those beans");
        }
    }

    protected static void removeTab(List<List<EngineTab>> tabs, String tabId) {
        if (!tabs.isEmpty() && tabId != null && !tabId.isEmpty()) {
            for (List<EngineTab> t : tabs) {
                for (Iterator<EngineTab> iterator = t.iterator(); iterator.hasNext();) {
                    EngineTab tab = iterator.next();
                    if (tabId.equals(tab.getId())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private List<List<EngineTab>> getEngineTabs(Object parent) {
        List<List<EngineTab>> results = new ArrayList<>();
        if (parent == null) {
            return results;
        }
        if (parent instanceof String) {
            parent = SpringContextSingleton.getBean((String) parent);
        }
        List<EngineTab> tabs = null;

        if (parent instanceof EditConfiguration) {
            tabs = ((EditConfiguration) parent).getDefaultEditConfiguration().getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((EditConfiguration) parent).getDefaultEditConfiguration().setEngineTabs(tabs);
            }

            for (Map.Entry<String, ?> entry : SpringContextSingleton.getBeansOfType(applicationContext,EditConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((EditConfiguration) parent).getName() + "-")) {
                    results.addAll(getEngineTabs(entry.getValue()));
                }
            }
        } else if (parent instanceof ManagerConfiguration) {
            tabs = ((ManagerConfiguration) parent).getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((ManagerConfiguration) parent).setEngineTabs(tabs);
            }

            for (Map.Entry<String, ?> entry : SpringContextSingleton.getBeansOfType(applicationContext,ManagerConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((ManagerConfiguration) parent).getName() + "-")) {
                    results.addAll(getEngineTabs(entry.getValue()));
                }
            }
        } else if (parent instanceof EngineConfiguration) {
            tabs = ((EngineConfiguration) parent).getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((EngineConfiguration) parent).setEngineTabs(tabs);
            }
        } else if (parent instanceof Engine) {
            tabs = ((Engine) parent).getTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((Engine) parent).setTabs(tabs);
            }
        }
        results.add(tabs);
        return results;
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
        if (obj != null && this.getClass() == obj.getClass()) {
            EngineTab other = (EngineTab) obj;
            return getId() != null ? other.getId() != null && getId().equals(other.getId()) : other
                    .getId() == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


}
