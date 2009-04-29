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
package org.jahia.ajax.gwt.client.widget.definition;

import org.jahia.ajax.gwt.client.widget.tripanel.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:22:43
 */
public class ContentDefinition extends TriPanelBrowserViewport {

    public ContentDefinition() {
        // superclass constructor (define linker)
        super() ;

        // construction of the UI components
        LeftComponent tree = new LeftPanel();
        FormView formViews = new FormView() ;
        BottomRightComponent tabs = new ContentDefinitionDetails() ;
        TopBar toolbar = new ContentDefinitionToolbar(formViews) ;
        BottomBar statusBar = new ContentDefinitionStatusBar() ;

        // setup widgets in layout
        initWidgets(tree.getComponent(),
                    formViews.getComponent(),
                    tabs.getComponent(),
                    toolbar.getComponent(),
                    statusBar.getComponent());

        // linker initializations
        linker.registerComponents(tree, formViews, tabs, toolbar, statusBar) ;
        formViews.initContextMenu();
        linker.handleNewSelection();
    }

}
