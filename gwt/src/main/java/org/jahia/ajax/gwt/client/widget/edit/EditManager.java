package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.Style;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:55:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditManager extends Viewport {

    private LayoutContainer m_component;

    public EditManager(String path, String template) {
        super();
        setLayout(new BorderLayout());

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 340);
        data.setCollapsible(true);
        data.setSplit(true);
        add(new SidePanel(), data);

        final DNDListener dndListener = new DNDListener();
        setScrollMode(Style.Scroll.AUTO);
        add(new MainModule(path, template), new BorderLayoutData(Style.LayoutRegion.CENTER));
    }
}
