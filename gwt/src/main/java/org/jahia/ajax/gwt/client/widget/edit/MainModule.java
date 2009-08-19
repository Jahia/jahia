package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.core.client.GWT;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:34:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainModule extends LayoutContainer {

    private HTML html;


    public MainModule(final String path, final String template, final EditManager editManager) {
        super(new FlowLayout());

        setScrollMode(Style.Scroll.AUTO);

//        Draggable d = new Draggable(cp);

        JahiaContentManagementService.App.getInstance().getRenderedContent(path, template, true, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                html = new HTML(result);
                add(html);
                ModuleHelper.parse(MainModule.this,html, editManager);
            }

            public void onFailure(Throwable caught) {
                GWT.log("error", caught);
            }
        });

    }

    @Override
    public boolean layout() {
        Log.info("layout--");
        return super.layout();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
