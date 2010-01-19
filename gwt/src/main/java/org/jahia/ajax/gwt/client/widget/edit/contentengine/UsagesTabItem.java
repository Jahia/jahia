package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.GWTLanguageSwitcherLocaleBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.content.NodeUsagesGrid;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsagesTabItem extends EditEngineTabItem {

    public UsagesTabItem(EditContentEngine engine) {
        super(Messages.get("fm_usages", "Usages"), engine);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabContent());
    }

    @Override
    public void create(GWTLanguageSwitcherLocaleBean locale) {
        setLayout(new FitLayout());
        if (engine.getNode() != null) {
            Grid<GWTJahiaNodeUsage> grid = NodeUsagesGrid.createUsageGrid(Arrays.asList(engine.getNode()));
            add(grid);
            setProcessed(true);
        }
        layout();
    }
}