package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.SwallowEvent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends Module {
    private HorizontalPanel panel;

    public PlaceholderModule(String id, String path, String nodeTypes, String referenceType, MainModule mainModule) {
        super(id, path, null, null, nodeTypes, referenceType, mainModule, new FlowLayout());
        this.id = id;
        this.path = path;
        this.mainModule = mainModule;
        this.nodeTypes = nodeTypes;

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }

        setHeight(20);

        panel = new HorizontalPanel();
        panel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        panel.addStyleName("x-small-editor");
        panel.addStyleName("x-panel-header");
        panel.addStyleName("x-panel-placeholder");

//        html = new HTML("<img src=\""+JahiaGWTParameters.getContextPath() + "/modules/default/images/add.png"+"\" /> Add new content here");
        html = new HTML("Add : &nbsp;");
        panel.add(html);
        add(panel);
    }

    @Override
    public void onParsed() {
        DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.PLACEHOLDER_TYPE);
        target.setOperation(DND.Operation.COPY);
        target.setFeedback(DND.Feedback.INSERT);

        target.addDNDListener(mainModule.getEditLinker().getDndListener());
        if (getParentModule() != null && getParentModule().getNodeTypes() != null) {
        String[] nodeTypesArray = getParentModule().getNodeTypes().split(" ");
//        HorizontalPanel buttonsPanel = new HorizontalPanel();
//        buttonsPanel.setStyleName("listEditToolbar");
        for (String s : nodeTypesArray) {
            Button button = new Button(s);
            button.setStyleName("button-placeholder");
            button.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    ContentActions.showContentWizard(mainModule.getEditLinker(), parentModule.getNodeTypes(), getParentModule().getNode());
                }
            });
            panel.add(button);
        }
        }
//        add(buttonsPanel);
//        html.setHTML("Drop here : " + getParentModule().getNodeTypes());
//        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
//            @Override
//            public void componentSelected(ButtonEvent ce) {
//                ContentActions.showContentWizard(mainModule.getEditLinker(), parentModule.getNodeTypes());
//            }
//        });
    }

    public boolean isDraggable() {
        return false;
    }
}
