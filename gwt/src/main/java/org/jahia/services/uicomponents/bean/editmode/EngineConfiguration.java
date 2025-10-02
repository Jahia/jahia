/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.contentengine.ButtonItem;
import org.jahia.ajax.gwt.helper.UIConfigHelper;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
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
 * see {@link UIConfigHelper} for link
 * @deprecated
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
        if (parent instanceof GWTEditConfiguration) {
            results.add(((GWTEditConfiguration) parent).getEngineConfigurations());

            for (Map.Entry<String, ?> entry : SpringContextSingleton.getBeansOfType(applicationContext, GWTEditConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((GWTEditConfiguration) parent).getName() + "-")) {
                    results.addAll(getParentConfigurationMap(entry.getValue()));
                }
            }
        } else if (parent instanceof ManagerConfiguration) {
            results.add(((ManagerConfiguration) parent).getEngineConfigurations());

            for (Map.Entry<String, ?> entry : SpringContextSingleton.getBeansOfType(applicationContext, ManagerConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((ManagerConfiguration) parent).getName() + "-")) {
                    results.addAll(getParentConfigurationMap(entry.getValue()));
                }
            }

        }
        return results;
    }
}
