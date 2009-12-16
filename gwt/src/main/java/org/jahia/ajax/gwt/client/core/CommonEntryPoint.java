package org.jahia.ajax.gwt.client.core;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;

/**
 * Common entry point for all our custom entry points. Use this class to share behavior between all our entry points.
 *
 * @author Serge Huber.
 *         Date: Dec 16, 2009
 *         Time: 10:39:39 AM
 */
public class CommonEntryPoint implements EntryPoint {
    public void onModuleLoad() {
        /* todo The following two lines are a hack to get development mode to work on Mac OS X, should be removed once this
           problem is fixed.
         */
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        /* End of GWT hack */
        
    }
}
