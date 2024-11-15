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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content.portlet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreatePortletInitBean;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.allen_sauer.gwt.log.client.Log;

import java.util.*;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:06
 */
public class PortletFormCard extends PortletWizardCard {
    private PropertiesEditor pe;

    public PortletFormCard() {
        super(Messages.get("label.parameters","Parameters"), Messages.get("org.jahia.engines.PortletsManager.wizard.parameters.edit.label","Edit parameters"));
    }

    public void createUI() {
        removeAll();
        super.createUI();
        createUIAsync();

    }

    public void next() {
        Log.debug("update properties");

        if (pe != null) {
            getGwtJahiaNewPortletInstance().setProperties(pe.getProperties());
        }
    }

    // laod form asyn
    private void createUIAsync() {
        JahiaContentManagementService.App.getInstance().initializeCreatePortletEngine(getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getPortletType(), getParentNode().getPath(), new BaseAsyncCallback<GWTJahiaCreatePortletInitBean>() {
            public void onSuccess(GWTJahiaCreatePortletInitBean result) {
                List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>(1);
                list.add(result.getNodeType());
                Map<String, GWTJahiaNodeProperty> defaultValues = new HashMap<String, GWTJahiaNodeProperty>();

                GWTJahiaPortletDefinition definition = getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition();
                GWTJahiaNodePropertyValue value = new GWTJahiaNodePropertyValue(definition.getContextName()+"!"+definition.getDefinitionName(), GWTJahiaNodePropertyType.STRING);
                defaultValues.put("j:definition", new GWTJahiaNodeProperty("j:definition", value));
                if (definition.getExpirationTime() != null) {
                    value = new GWTJahiaNodePropertyValue("" + definition.getExpirationTime(), GWTJahiaNodePropertyType.LONG);
                    defaultValues.put("j:expirationTime", new GWTJahiaNodeProperty("j:expirationTime", value));
                }
                if (definition.getCacheScope() != null) {
                    value = new GWTJahiaNodePropertyValue(definition.getCacheScope(), GWTJahiaNodePropertyType.STRING);
                    defaultValues.put("j:cacheScope", new GWTJahiaNodeProperty("j:cacheScope", value));
                }

                pe = new PropertiesEditor(list, defaultValues, Arrays.asList(GWTJahiaItemDefinition.CONTENT));
                pe.setMixin(result.getMixin());
                pe.setExcludedTypes(Arrays.asList("jnt:portlet", "mix:lastModified", "mix:created", "jmix:lastPublished"));
                pe.renderNewFormPanel();
                pe.setChoiceListInitializersValues(result.getChoiceListInitializersValues());
                setFormPanel(pe);
                layout();
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("error", caught);
            }

        });
    }
}
