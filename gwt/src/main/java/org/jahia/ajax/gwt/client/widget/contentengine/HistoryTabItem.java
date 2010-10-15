package org.jahia.ajax.gwt.client.widget.contentengine;

import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 5:41:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoryTabItem extends EditEngineTabItem {

    private transient HistoryPanel historyPanel;

    @Override
    public void init(String locale) {
        if (engine.getNode() == null) {
            return;
        }

        historyPanel = new HistoryPanel(engine.getNode());
        tab.add(historyPanel);

        tab.layout();
    }
}
