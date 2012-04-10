package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Channel switcher action item
 */
public class ChannelDisplayActionItem extends BaseActionItem {
    private static final long serialVersionUID = 9115660301140902069L;
    protected transient HorizontalPanel horizontalPanel;
    protected boolean events = true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
    }

    /**
     * init main component
     */
    private void initMainComponent() {

        CheckBox checkBox = new CheckBox();
        checkBox.setBoxLabel(Messages.get("label.displayChannels", "Display channels"));
        checkBox.addListener(Events.Change, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent fe) {
                if (linker instanceof EditLinker) {
                    ((EditLinker) linker).getMainModule().setChannelDisplay((Boolean)fe.getValue());
                }
            }
        });

        // Channel selector
        horizontalPanel = new HorizontalPanel();
        horizontalPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        horizontalPanel.add(checkBox);

        setEnabled(true);

    }


    @Override
    public Component getCustomItem() {
        return horizontalPanel;
    }


    @Override
    public void setEnabled(boolean enabled) {
        horizontalPanel.setEnabled(enabled);
    }

}
