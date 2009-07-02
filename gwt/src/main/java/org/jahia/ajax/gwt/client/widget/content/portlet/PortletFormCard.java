/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content.portlet;

import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;

import java.util.*;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:06
 */
public class PortletFormCard extends MashupWizardCard {
    private PropertiesEditor pe;

    public PortletFormCard() {
        super(Messages.getNotEmptyResource("mw_params","Parametes"));
        setHtmlText(getText());
    }

    public String getText() {
        return Messages.getNotEmptyResource("mw_edit_params","Edit parameters");
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
        ContentDefinitionServiceAsync service = ContentDefinitionService.App.getInstance();
        service.getNodeType(getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getPortletType(), new AsyncCallback<GWTJahiaNodeType>() {
            public void onSuccess(GWTJahiaNodeType result) {
                List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
                list.add(result);
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

                pe = new PropertiesEditor(list, defaultValues, false, true, null, Arrays.asList("jnt:portlet", "mix:createdBy", "mix:lastModified", "mix:created"));
                if (pe != null) {
                    setFormPanel(pe);
                    layout();
                } else {
                    add(new Label(Messages.getNotEmptyResource("mw_prop_load_error","Unable to load properties panel")));
                }
            }

            public void onFailure(Throwable caught) {
                Log.error("error", caught);
            }

        });
    }
}
