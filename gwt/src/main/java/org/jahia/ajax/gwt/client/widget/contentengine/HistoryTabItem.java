package org.jahia.ajax.gwt.client.widget.contentengine;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 5:41:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoryTabItem extends EditEngineTabItem {

    private HistoryPanel historyPanel;

    public HistoryTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.history", "History"), engine);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabContent());
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() == null) {
            return;
        }

        historyPanel = new HistoryPanel(engine.getNode());
        add(historyPanel);

        layout();
    }
}
