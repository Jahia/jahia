package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Dec 21, 2009
* Time: 3:14:11 PM
* To change this template use File | Settings | File Templates.
*/
class CreateContentTabItem extends SidePanelTabItem {

    private Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> definitions;

    CreateContentTabItem() {
        setText("&nbsp;");
        setIcon(ContentModelIconProvider.CONTENT_ICONS.content());


    }
}
