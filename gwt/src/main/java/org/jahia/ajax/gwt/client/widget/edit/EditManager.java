package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionMenu;
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
    private BorderLayout borderLayout ;

    public EditManager(String html, String path, String template, String locale) {
        borderLayout =  new BorderLayout();
        setLayout(borderLayout);
        setHeaderVisible(false);

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        data.setFloatable(true);
        sidePanel = new SidePanel();
        sidePanel.setStyleAttribute("z-index", "999");
        add(sidePanel, data);

        toolbar =  new ActionToolbarLayoutContainer("editmode");
        toolbar.init();
        toolbar.setStyleAttribute("z-index", "999");
        toolbar.setStyleAttribute("position", "relative");
        setTopComponent(toolbar);

        setScrollMode(Style.Scroll.NONE);
        add(mainModule = new MainModule(html, path, template), new BorderLayoutData(Style.LayoutRegion.CENTER));

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
