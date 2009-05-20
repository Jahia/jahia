/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.opensearch;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 26 oct. 2008
 * Time: 08:53:28
 * To change this template use File | Settings | File Templates.
 */
public class StandAloneOpenSearchWindow extends Window {

    private OpenSearchPanel openSearchPanel;

    public StandAloneOpenSearchWindow(OpenSearchPanel openSearchPanel) {
        this.openSearchPanel = openSearchPanel;
    }

    @Override
    protected void onRender(Element element, int i) {
        this.initWidget();
        super.onRender(element, i);
    }

    private void initWidget(){
        setPlain(true);
        setSize(800, 600);
        //setHeading(this.openSearchPanel.getOpenSearchWidgetItem().getName());
        setLayout(new FitLayout());
        add(this.openSearchPanel);
    }
    
}
