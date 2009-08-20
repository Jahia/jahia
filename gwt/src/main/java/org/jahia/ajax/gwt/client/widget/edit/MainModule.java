package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Element;
import com.google.gwt.core.client.GWT;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:34:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainModule extends LayoutContainer implements Module {

    private HTML html;
    private String path;
    Map<Element, Module> m;

    public MainModule(final String path, final String template, final EditManager editManager) {
        super(new FlowLayout());

        setScrollMode(Style.Scroll.AUTO);

//        Draggable d = new Draggable(cp);

        JahiaContentManagementService.App.getInstance().getRenderedContent(path, template, true, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                html = new HTML(result);
                add(html);
                m = ModuleHelper.parse(MainModule.this,html, editManager);
                layout();
            }

            public void onFailure(Throwable caught) {
                GWT.log("error", caught);
            }
        });

    }

    @Override
    protected void onAfterLayout() {
        super.onAfterLayout();
        if (m != null) {
            ModuleHelper.move(m);
        }
    }

    public Container getContainer() {
        return this;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return null; 
    }


}
