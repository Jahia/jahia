package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Text;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

public class NodePathActionItem extends BaseActionItem {
    private transient  Text text;

    @Override
    public Component getCustomItem() {
        text = new Text("");
        return text;
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        text.setText(Messages.get("label.page", "Page") + ": " + node.getPath());
    }
}
