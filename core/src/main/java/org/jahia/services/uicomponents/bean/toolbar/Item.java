/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.uicomponents.bean.toolbar;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.uicomponents.bean.Visibility;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:05:20
 */
public class Item implements Serializable, BeanNameAware, InitializingBean, DisposableBean {
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
    private boolean hideWhenDisabled;
    private List<Property> properties = new ArrayList<Property>();
    private ActionItem actionItem;
    private Object parent;
    private int position = -1;
    private String positionAfter;
    private String positionBefore;

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

    public void afterPropertiesSet() throws Exception {
        if (parent != null) {
            if (parent instanceof List) {
                for (Object o : (List) parent) {
                    addToParent(o);
                }
            } else {
                addToParent(parent);
            }

            // clean the reference
            parent = null;
        }
    }

    @Override
    public void destroy() throws Exception {
        // todo remove item
    }

    private void addToParent(Object o) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (o instanceof String) {
            String parentPath = (String) o;
            String beanId = StringUtils.substringBefore(parentPath, ".");
            Object bean = SpringContextSingleton.getBean(beanId);
            String propertyPath = StringUtils.substringAfter(parentPath, ".");
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
            o = bean;
        }
        if (o instanceof Menu) {
            Menu parentMenu = (Menu) o;
            parentMenu.removeItem(getId());
            int index = -1;
            if (position >= 0) {
                index = position;
            } else if (positionBefore != null) {
                index = parentMenu.getItems().indexOf(new Item(positionBefore));
            } else if (positionAfter != null) {
                index = parentMenu.getItems().indexOf(new Item(positionAfter));
                if (index != -1) {
                    index++;
                }
                if (index >= parentMenu.getItems().size()) {
                    index = -1;
                }
            }
            if (index != -1) {
                parentMenu.addItem(index, this);
            } else {
                parentMenu.addItem(this);
            }
        } else if (o instanceof Toolbar) {
            Toolbar parentToolbar = (Toolbar) o;
            parentToolbar.removeItem(getId());
            int index = -1;
            if (position >= 0) {
                index = position;
            } else if (positionBefore != null) {
                index = parentToolbar.getItems().indexOf(new Item(positionBefore));
            } else if (positionAfter != null) {
                index = parentToolbar.getItems().indexOf(new Item(positionAfter));
                if (index != -1) {
                    index++;
                }
                if (index >= parentToolbar.getItems().size()) {
                    index = -1;
                }
            }
            if (index != -1) {
                parentToolbar.addItem(index, this);
            } else {
                parentToolbar.addItem(this);
            }
        } else {
            throw new IllegalArgumentException(
                    "Unknown parent type '"
                            + o.getClass().getName()
                            + "'. Can accept Menu, Toolbar or"
                            + " a String value with a bean-compliant path to the corresponding menu/toobar bean");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof Item && obj != null) {
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
