package org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.picker;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.Style;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 23 janv. 2009
 * Time: 17:13:04
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortletLeftComponent extends LayoutContainer {
    private JahiaFolderPortletTree sharedPortletFolderView = new JahiaFolderPortletTree( "/content/mashups");
    private JahiaFolderPortletTree sitePortletFolderView = new JahiaFolderPortletTree( "/content/mashups");
    private JahiaFolderPortletTree myPortletFolderView = new JahiaFolderPortletTree( "/content/mashups");


    public JahiaPortletLeftComponent() {
        setLayout(new FlowLayout(10));

        ContentPanel panel = new ContentPanel();
        panel.setHeading("AccordionLayout");
        panel.setBodyBorder(false);

        panel.setLayout(new AccordionLayout());
        panel.setIconStyle("icon-accordion");

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Shared");
        cp.setScrollMode(Style.Scroll.AUTO);
        cp.add(sharedPortletFolderView);
        panel.add(cp);
        

        cp = new ContentPanel();
        cp.setHeading("Site");
        cp.add(sitePortletFolderView);
        panel.add(cp);

        cp = new ContentPanel();
        cp.setHeading("Private");
        cp.add(myPortletFolderView);
        panel.add(cp);

        panel.setSize(200, 325);
        add(panel);
    }

}
