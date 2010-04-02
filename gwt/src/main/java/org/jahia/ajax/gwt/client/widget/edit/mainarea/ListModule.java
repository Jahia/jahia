package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListModule extends SimpleModule {

    public ListModule(String id, String path, String s, String template, String scriptInfo, String nodeTypes, boolean locked, boolean shared, MainModule mainModule) {
        super(id, path, template, scriptInfo, nodeTypes, locked, shared, mainModule);
        head = new Header();
        add(head);

        if (path.contains("/")) {
            setHeaderText(Messages.getResource("em_content") + " : " + path.substring(path.lastIndexOf('/') + 1));
        } else {
            setHeaderText(Messages.getResource("em_content") + " : " + path);
        }
        setBorders(false);
//        setBodyBorder(false);
        head.addStyleName("x-panel-header");
        head.addStyleName("x-panel-header-listmodule");
        if (locked) {
            head.addStyleName("x-panel-header-lockedmodule");
        }
        html = new HTML(s);
        add(html);
    }
}
