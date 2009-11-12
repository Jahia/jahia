package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;

/**
 * Edit mode manager widget.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:55:47 PM
 */
public class EditManager extends ContentPanel {

    private MainModule mainModule;
    private SidePanel sidePanel;
    private ActionToolbarLayoutContainer toolbar;
    private EditLinker editLinker;

    public EditManager(String path, String template, String locale) {
        super(new BorderLayout());
        setHeaderVisible(false);

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        sidePanel = new SidePanel();
        sidePanel.setStyleAttribute("z-index", "999");
        add(sidePanel, data);

        toolbar =  new ActionToolbarLayoutContainer("editmode");
        toolbar.init();
        toolbar.setStyleAttribute("z-index", "999");
        setTopComponent(toolbar);

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

    public ActionToolbarLayoutContainer getToolbar() {
        return toolbar;
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }


}
