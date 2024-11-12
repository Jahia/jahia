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
package org.jahia.ajax.gwt.client.widget.form.tag;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

import java.util.List;


/**
 * Provide a container for tags
 *
 * @author kevan
 */
public class AddTagContainer extends HorizontalPanel {
    private static final int DEFAULT_AUTOCOMPLETE_LIMIT = 10;

    protected TagComboBox tagComboBox;
    protected Button addTagButton;
    protected TagField tagField;

    public AddTagContainer(TagField _tagField, String autoComplete) {
        super();
        tagField = _tagField;
        tagComboBox = new TagComboBox(autoComplete);
        addTagButton = new Button(Messages.get("label.add"));
        addTagButton.addStyleName("button-add");
        addTagButton.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        addTagButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                addTag();
            }
        });

        add(tagComboBox);
        add(addTagButton);
    }

    private void addTag() {
        String tag = null;
        if (tagComboBox.getValue() != null) {
            tag = tagComboBox.getValue().getValue();
        } else if (tagComboBox.getRawValue() != null && tagComboBox.getRawValue().trim().length() > 0) {
            tag = tagComboBox.getRawValue().trim();
        }
        if (tag != null) {
            tagField.addTag(tag);
            tagComboBox.setRawValue("");
        }
    }

    /**
     * Provide the input for add tags, autocomplete is provide by this ComboBox
     *
     * @author kevan
     */
    public class TagComboBox extends ComboBox<GWTJahiaValueDisplayBean> {
        public TagComboBox(final String autoComplete) {
            setDisplayField("display");
            if (autoComplete != null) {
                final Long autocompleteLimit = new Integer(Util.parseInt(autoComplete, DEFAULT_AUTOCOMPLETE_LIMIT)).longValue();
                final ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>(new BaseListLoader(
                        new RpcProxy<List<GWTJahiaValueDisplayBean>>() {
                            @Override
                            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaValueDisplayBean>> asyncCallback) {
                                // TODO handle separator to provide better autocomplete
                                GWTJahiaNode site = JahiaGWTParameters.getSiteNode();
                                JahiaContentManagementService.App.getInstance().getTags(getRawValue(),
                                        site != null ? site.getPath() : null, 1L,
                                        autocompleteLimit,
                                        0L, true, asyncCallback);
                            }
                        }));
                setStore(store);
                setTriggerAction(TriggerAction.ALL);
                setMinChars(2);
                setQueryDelay(100);
            } else {
                // create an empty store
                final ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
                setStore(store);
            }
            setHideTrigger(true);
            addKeyListener(new com.extjs.gxt.ui.client.event.KeyListener() {
                @Override
                public void componentKeyPress(ComponentEvent event) {
                    if (event.getEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                        addTag();
                    }
                }

                @Override
                public void componentKeyUp(ComponentEvent event) {
                    if(event instanceof FieldEvent){
                        if (((FieldEvent) event).getField().getRawValue().length() == 0) {
                            collapse();
                        }
                    }
                }
            });
        }
    }
}
