package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
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

    public EditManager(String html, String path, String template, String locale, GWTEditConfiguration config) {
        long start = System.currentTimeMillis();
        borderLayout =  new BorderLayout();
        setLayout(borderLayout);
        setHeaderVisible(false);
        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        data.setFloatable(true);
        sidePanel = new SidePanel(config);
        sidePanel.setStyleAttribute("z-index", "999");
        add(sidePanel, data);

        toolbar =  new ActionToolbarLayoutContainer(config.getTopToolbar());
        toolbar.setStyleAttribute("z-index", "999");
        toolbar.setStyleAttribute("position", "relative");
        setTopComponent(toolbar);

        setScrollMode(Style.Scroll.NONE);
        mainModule = new MainModule(html, path, template, config);
        add(mainModule, new BorderLayoutData(Style.LayoutRegion.CENTER));

        editLinker = new EditLinker(mainModule, sidePanel, toolbar, config);
        editLinker.setLocale(locale);

        Log.info("Edit manager inited : " + (System.currentTimeMillis() - start));




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
