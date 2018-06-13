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
package org.jahia.services.uicomponents.bean.toolbar;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:05:20
 */
public class Item implements Serializable, BeanNameAware, InitializingBean, DisposableBean, ApplicationContextAware {
    private static final long serialVersionUID = -5120594370234680709L;
    private String id;
    private String icon;
    private String title;
    private String titleKey;
    private boolean displayTitle = true;
    private String descriptionKey;
    private Visibility visibility;
    private Selected selected;
    private String layout;
    private String requiredPermission;
    private String requiredModule;
    private boolean hideWhenDisabled;
    private List<Property> properties = new ArrayList<Property>();
    private ActionItem actionItem;
    private Object parent;
    private int position = -1;
    private String positionAfter;
    private String positionBefore;
    private ApplicationContext applicationContext;

    public Item() {
        super();
    }

    private Item(String id) {
        this();
        setId(id);
    }

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

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Selected getSelected() {
        return selected;
    }

    public void setSelected(Selected selected) {
        this.selected = selected;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        this.properties.add(property);
    }

    @Override
    public void setBeanName(String name) {
        this.id = name;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }

    public void setActionItem(ActionItem actionItem) {
        this.actionItem = actionItem;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getRequiredModule() {
        return requiredModule;
    }

    public void setRequiredModule(String requiredModule) {
        this.requiredModule = requiredModule;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public boolean isHideWhenDisabled() {
        return hideWhenDisabled;
    }

    public void setHideWhenDisabled(boolean hideWhenDisabled) {
        this.hideWhenDisabled = hideWhenDisabled;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public void setParentMenu(Menu menu) {
        setParent(menu);
    }

    public void setParentToolbar(Toolbar toolbar) {
        setParent(toolbar);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (parent != null) {
            if (parent instanceof List) {
                for (Object o : (List<?>) parent) {
                    addItem(getItems(o));
                }
            } else {
                addItem(getItems(parent));
            }

            // clean the reference
            parent = null;
        }
    }

    private void addItem(List<List<Item>> items) {
        if (!items.isEmpty()) {
            removeItem(items, getId());
            for (List<Item> it : items) {
                int index = -1;
                if (position >= 0) {
                    index = position;
                } else if (positionBefore != null) {
                    index = it.indexOf(new Item(positionBefore));
                } else if (positionAfter != null) {
                    index = it.indexOf(new Item(positionAfter));
                    if (index != -1) {
                        index++;
                    }
                    if (index >= it.size()) {
                        index = -1;
                    }
                }
                if (index != -1) {
                    it.add(index, this);
                } else {
                    it.add(this);
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        if (parent instanceof List) {
            for (Object o : (List<?>) parent) {
                removeItem(getItems(o), getId());
            }
        } else {
            removeItem(getItems(parent), getId());
        }
    }

    private void removeItem(List<List<Item>> items, String itemId) {
        if (!items.isEmpty() && itemId != null && !itemId.isEmpty()) {
            for (List<Item> it : items) {
                for (Iterator<Item> iterator = it.iterator(); iterator.hasNext();) {
                    Item tab = iterator.next();
                    if (itemId.equals(tab.getId())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private List<List<Item>> getItems(Object parent) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<List<Item>> results = new ArrayList<>();
        if (parent == null) {
            return results;
        }
        if (parent instanceof String) {
            String parentPath = (String) parent;
            String beanId = StringUtils.substringBefore(parentPath, ".");
            Object bean = SpringContextSingleton.getBean(beanId);
            String propertyPath = StringUtils.substringAfter(parentPath, ".");
            if (bean instanceof EditConfiguration || bean instanceof ManagerConfiguration) {
                for (Map.Entry<String, ?> entry : SpringContextSingleton.getBeansOfType(applicationContext, bean.getClass()).entrySet()) {
                    if (entry.getKey().startsWith(beanId + "-")) {
                        results.addAll(getItems(resolveProperty(parentPath, entry.getValue(), propertyPath)));
                    }
                }
            }

            parent = resolveProperty(parentPath, bean, propertyPath);
        }
        if (parent instanceof Menu) {
            Menu parentMenu = (Menu) parent;
            results.add(parentMenu.getItems());
        } else if (parent instanceof Toolbar) {
            Toolbar parentToolbar = (Toolbar) parent;
            results.add(parentToolbar.getItems());
        } else if (parent instanceof List) {
            results.add((List)parent);
        }else {
            throw new IllegalArgumentException(
                    "Unknown parent type '"
                            + parent.getClass().getName()
                            + "'. Can accept Menu, Toolbar or"
                            + " a String value with a bean-compliant path to the corresponding menu/toobar bean");
        }
        return results;
    }

    private Object resolveProperty(String parentPath, Object bean, String propertyPath) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (propertyPath.length() > 0) {
            bean = PropertyUtils.getNestedProperty(bean, propertyPath);
        }
        if (bean == null) {
            throw new IllegalArgumentException("Unable to find target for parent path: "
                    + parentPath);
        }
        if (!(bean instanceof Menu || bean instanceof Toolbar)) {
            throw new IllegalArgumentException("Target bean for path '" + parentPath
                    + "' is not of type Menu or Toolbar. Unable to handle beans of type '"
                    + bean.getClass().getName() + "'");
        }
        return bean;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj != null && this.getClass() == obj.getClass()) {
            Item other = (Item) obj;
            return getId() != null ? other.getId() != null && getId().equals(other.getId()) : other
                    .getId() == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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
}
