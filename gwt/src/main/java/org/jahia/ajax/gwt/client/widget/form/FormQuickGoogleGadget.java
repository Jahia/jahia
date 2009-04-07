package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 27 févr. 2009
 * Time: 16:43:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class FormQuickGoogleGadget extends FormQuickMashup {

    public FormQuickGoogleGadget(String folderPath) {
        super(folderPath);
        createUI();
    }

    protected void createUI() {
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");


        final TextField nameField = new TextField();
        nameField.setName("name");
        nameField.setFieldLabel(Messages.getNotEmptyResource("name", "Name"));
        nameField.setAllowBlank(false);
        nameField.setMaxLength(200);
        add(nameField);

        final TextField scriptField = new TextArea();
        scriptField.setName("gscript");
        scriptField.setFieldLabel(Messages.getNotEmptyResource("gadget_script", "Gadget script"));
        scriptField.setAllowBlank(false);
        add(scriptField);


        // save properties button
        Button saveButton = new Button(Messages.getResource("fm_save"));
        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent componentEvent) {
                JahiaNodeService.App.getInstance().createGoogleGadgetPortletInstance(getFolderPath(), (String) nameField.getValue(), (String) scriptField.getValue(),new AsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                        onMashupCreated();
                        if (getParent() instanceof Window) {
                            ((Window) getParent()).close();
                        }
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to create rss portlet", throwable);
                        if (getParent() instanceof Window) {
                            ((Window) getParent()).close();
                        }
                    }
                });

            }
        });
        addButton(saveButton);

        // remove all

        layout();
    }

}

