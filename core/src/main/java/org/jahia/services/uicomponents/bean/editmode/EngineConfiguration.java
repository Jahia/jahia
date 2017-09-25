/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.ajax.gwt.client.widget.contentengine.ButtonItem;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core part of engine configuration
 * see GWTEngineConfiguration for gwt part
 * see {@link org.jahia.ajax.gwt.helper.UIConfigHelper} for link
 */

public class EngineConfiguration implements Serializable, InitializingBean, DisposableBean, ApplicationContextAware {

    private static final long serialVersionUID = -5991528610464460659L;

    private String key;

    private List<EngineTab> engineTabs;

    private List<ButtonItem> creationButtons = new ArrayList<ButtonItem>();
    private List<ButtonItem> editionButtons = new ArrayList<ButtonItem>();
    private List<ButtonItem> commonButtons = new ArrayList<ButtonItem>();

    private Object parent;

    private ApplicationContext applicationContext;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /***
     * @return list of engine tabs
     */
    public List<EngineTab> getEngineTabs() {
        return engineTabs;
    }

    public void setEngineTabs(List<EngineTab> engineTabs) {
        this.engineTabs = engineTabs;
    }

    /**
     * @return List of button to display for create engine
     */
    public List<ButtonItem> getCreationButtons() {
        return creationButtons;
    }

    public void setCreationButtons(List<ButtonItem> creationButtons) {
        this.creationButtons = creationButtons;
    }

    /**
     * @return List of ButtonItem for edit engine
     */
    public List<ButtonItem> getEditionButtons() {
        return editionButtons;
    }

    public void setEditionButtons(List<ButtonItem> editionButtons) {
        this.editionButtons = editionButtons;
    }

    /**
     * @return List of button for both create and edit (like cancel)
     */
    public List<ButtonItem> getCommonButtons() {
        return commonButtons;
    }

    public void setCommonButtons(List<ButtonItem> commonButtons) {
        this.commonButtons = commonButtons;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (parent instanceof List) {
            for (Object o : (List<?>) parent) {
                addToParent(o);
            }
        } else {
            addToParent(parent);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        if (parent instanceof List) {
            for (Object o : (List<?>) parent) {
                removeFromParent(o);
            }
        } else {
            removeFromParent(parent);
        }
    }

    private void removeFromParent(Object o) {
        List<Map<String, EngineConfiguration>> configs = getParentConfigurationMap(o);
        for (Map<String, EngineConfiguration> config : configs) {
            config.remove(getKey());
        }
    }

    private void addToParent(Object o) {
        List<Map<String, EngineConfiguration>> configs = getParentConfigurationMap(o);
        if (!configs.isEmpty()) {
            for (Map<String, EngineConfiguration> config : configs) {
                config.put(getKey(), this);
            }
        } else if (o != null) {
            throw new IllegalArgumentException("Unknown parent type '"
                    + o.getClass().getName()
                    + "'. Can accept EditConfiguration, ManagerConfiguration, Engine or"
                    + " a String value with a beanId of the those beans");
        }
    }

    private List<Map<String, EngineConfiguration>> getParentConfigurationMap(Object parent) {
        List<Map<String, EngineConfiguration>> results = new ArrayList<>();
        if (parent instanceof EditConfiguration) {
            results.add(((EditConfiguration) parent).getEngineConfigurations());

            for (Map.Entry<String, ?> entry : applicationContext.getBeansOfType(EditConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((EditConfiguration) parent).getName() + "-")) {
                    results.addAll(getParentConfigurationMap(entry.getValue()));
                }
            }
        } else if (parent instanceof ManagerConfiguration) {
            results.add(((ManagerConfiguration) parent).getEngineConfigurations());

            for (Map.Entry<String, ?> entry : applicationContext.getBeansOfType(ManagerConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((ManagerConfiguration) parent).getName() + "-")) {
                    results.addAll(getParentConfigurationMap(entry.getValue()));
                }
            }

        }
        return results;
    }
}
