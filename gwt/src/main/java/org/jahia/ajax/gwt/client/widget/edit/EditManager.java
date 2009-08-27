package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:55:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditManager extends Viewport {

    private EditModeDNDListener dndListener;
    private MainModule mainModule;
    private SidePanel sidePanel;
    private EditLinker editLinker;
    private Module selection;

    public EditManager(String path, String template) {
        super();
        setLayout(new BorderLayout());
        editLinker = new EditLinker(this);
        dndListener = new EditModeDNDListener(this);

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        sidePanel = new SidePanel(this);
        add(sidePanel, data);

        setScrollMode(Style.Scroll.AUTO);
        add(mainModule = new MainModule(path, template, this), new BorderLayoutData(Style.LayoutRegion.CENTER));
    }

    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public void setSelection(Module selection) {
        if (this.selection != null) {
            this.selection.setSelected(false);
        }
        this.selection = selection;
        this.selection.setSelected(true);
        String template = selection.getTemplate();
        editLinker.onModuleSelection(selection.getNode(),"".equals(template)?null:template);
    }
}
