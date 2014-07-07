package org.jahia.ajax.gwt.client.widget.form.tag;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

import java.util.List;


/**
 * Created by kevan on 02/07/14.
 */
public class AddTagContainer extends HorizontalPanel {
    protected TagComboBox tagComboBox;
    protected Button addTagButton;
    protected TagField tagField;

    public AddTagContainer(TagField _tagField, boolean autoComplete, String separator) {
        super();
        tagField = _tagField;
        tagComboBox = new TagComboBox(autoComplete);
        addTagButton = new Button(Messages.get("label.add"));
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
     * Created by kevan on 02/07/14.
     */
    public class TagComboBox extends ComboBox<GWTJahiaValueDisplayBean> {
        public TagComboBox(boolean autoComplete) {
            if (autoComplete) {
                setDisplayField("display");
                final ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>(new BaseListLoader(
                        new RpcProxy<List<GWTJahiaValueDisplayBean>>() {
                            @Override
                            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaValueDisplayBean>> asyncCallback) {
                                // TODO handle separator to provide better autocomplete
                                GWTJahiaNode site = JahiaGWTParameters.getSiteNode();
                                JahiaContentManagementService.App.getInstance().getTags(getRawValue(),
                                        site != null ? site.getPath() : null, 1L, 10L, 0L, true, asyncCallback);
                            }
                        }));
                setStore(store);
                setTypeAhead(true);
                setTypeAheadDelay(100);
                setHideTrigger(true);
                addKeyListener(new com.extjs.gxt.ui.client.event.KeyListener() {
                    @Override
                    public void componentKeyPress(ComponentEvent event) {
                        if (event.getEvent().getKeyCode() == 13) {
                            addTag();
                        }
                    }
                });
                setTriggerAction(TriggerAction.ALL);
                setMinChars(2);
                setQueryDelay(100);
            } else {
                // create an empty store
                setHideTrigger(true);
                setDisplayField("display");
                final ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
                setStore(store);
            }
        }
    }
}