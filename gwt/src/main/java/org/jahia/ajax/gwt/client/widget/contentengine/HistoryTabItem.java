package org.jahia.ajax.gwt.client.widget.contentengine;

import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 5:41:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoryTabItem extends EditEngineTabItem {

    private transient HistoryPanel historyPanel;

    @Override public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        setHandleCreate(false);
        return super.create(engineTab, engine);
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (engine.getNode() == null) {
            return;
        }

        historyPanel = new HistoryPanel(engine.getNode());
        tab.add(historyPanel);

        tab.layout();
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            historyPanel = null;
        }

        super.setProcessed(processed);
    }

}
