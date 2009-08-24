package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.Style;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:55:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditManager extends Viewport {

    private EditModeDNDListener dndListener;
    private MainModule main;
    private SidePanel sidePanel;

    public EditManager(String path, String template) {
        super();
        setLayout(new BorderLayout());

        dndListener = new EditModeDNDListener(this);

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        sidePanel = new SidePanel(this);
        add(sidePanel, data);

        setScrollMode(Style.Scroll.AUTO);
        add(main = new MainModule(path, template, this), new BorderLayoutData(Style.LayoutRegion.CENTER));
    }

    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    public MainModule getMainModule() {
        return main;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    private ContentPanel selection;

    public void setSelection(ContentPanel selection) {
        if (this.selection != null) {
            this.selection.setBorders(false);
        }
        this.selection = selection;
        this.selection.setBorders(true);
        if(selection instanceof Module) {
            sidePanel.displaySelection(((Module) selection).getNode());
        }
    }
}
