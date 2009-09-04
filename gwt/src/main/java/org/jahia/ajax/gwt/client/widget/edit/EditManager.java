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

    private MainModule mainModule;
    private SidePanel sidePanel;
    private EditModeToolBar toolbar;
    private EditLinker editLinker;

    public EditManager(String path, String template, String locale) {
        super();
        setLayout(new BorderLayout());

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        sidePanel = new SidePanel();
        add(sidePanel, data);

        data = new BorderLayoutData(Style.LayoutRegion.NORTH, 26);
        toolbar = new EditModeToolBar();
        add(toolbar, data);

        setScrollMode(Style.Scroll.AUTO);
        add(mainModule = new MainModule(path, template), new BorderLayoutData(Style.LayoutRegion.CENTER));

        editLinker = new EditLinker(mainModule, sidePanel, toolbar);
        editLinker.setLocale(locale);

    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public EditModeToolBar getToolbar() {
        return toolbar;
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

}
