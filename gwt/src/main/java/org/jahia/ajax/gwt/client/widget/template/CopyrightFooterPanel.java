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
package org.jahia.ajax.gwt.client.widget.template;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

/**
 * User: jahia
 * Date: 12 f�vr. 2008
 * Time: 14:42:12
 */
public class CopyrightFooterPanel extends HorizontalPanel {
    public CopyrightFooterPanel() {
        init();
    }

    private void init() {
        Label jahiaLabel = new HTML("&nbsp;");
        jahiaLabel.addStyleName("gwt-copyright");
        Label copyrightLabel = new HTML("&#169;Copyrigth " + JahiaGWTParameters.getCopyright());
        add(jahiaLabel);
        add(copyrightLabel);
        setCellVerticalAlignment(copyrightLabel, VerticalPanel.ALIGN_BOTTOM);
        addStyleName("gwt-enginecomponent");
        addStyleName("gwt-engineversionfooter");
    }
}
